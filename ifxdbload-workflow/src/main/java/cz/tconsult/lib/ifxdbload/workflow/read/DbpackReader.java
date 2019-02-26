/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.read;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.core.FFaze;
import cz.tconsult.lib.ifxdbload.core.core.FazeAnalyzeResult;
import cz.tconsult.lib.ifxdbload.workflow.FResList;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.Builder;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import cz.tconsult.lib.ifxdbload.workflow.scan.DbpackScaner;
import cz.tconsult.lib.ifxdbload.workflow.scan.FileContentReceiver;
import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;

/**
 * Objekt zodpovědný za načtení dbpacků.
 * @author veverka
 *
 */
public class DbpackReader implements FileContentReceiver {


  private static final Logger log = LoggerFactory.getLogger(DbpackReader.class);

  private static final Path INTERNAL_ROOT = Paths.get("<InternalTcDbLoad>");

  private final Builder builder;

  private static final Set<String> ignoredEntries = new HashSet<String>();
  private final CounterMap<String> filesForSuppressedFazes = new CCounterMap<String>();

  private Set<EFazeZavedeni> processedFazes;

  /**
   * @param aBuilder
   */
  public DbpackReader(final Builder aBuilder) {
    super();
    builder = aBuilder;
  }


  public void readInternalToolDbpack(final ADbkind aDbKind) throws IOException {
    // FIXME Nějak parametrizovat, ale jak? Možná to ani nebude potřeba
    // TODO [veverka] žádné schema "tc" není -- 26. 2. 2019 9:22:32 veverka
    final DbpackProperties dbprops = new DbpackProperties(INTERNAL_ROOT, aDbKind, ASchema.of("tc"));
    for (final FResList.Entry entry : FResList.getEntries()) {
      // TODO [veverka] řešit, pokud to bude nutné --  žádné schema "tc" není -- 26. 2. 2019 9:22:32 veverka
      // readAndCallBuilder(dbprops, entry.istm, entry.entryName);
    }
  }



  /**
   * @param istm
   * @param aEntryName
   * @return
   * @throws IOException
   */
  @Override
  public void add(final DbpackProperties dbprops, final Supplier<byte[]> contentSupplier, final String aEntryName) {
    if (! isScriptForLoad(aEntryName)) {
      //      log.info(aEntryName);
      return;
    }
    //log.info("   found .... " + name);
    final FazeAnalyzeResult analyzeResult = FFaze.analyzeEntryName(aEntryName);
    if (analyzeResult.getFileCategory() != null) {
      byte[] data = null;
      builder.makeLoDbKind(dbprops);
      for (final EFazeZavedeni faze : analyzeResult.getFazes()) {
        if (zavadetTutoFazi(faze)) {
          if (data == null) {
            data = contentSupplier.get();
          }
          builder.addLoSoubor(aEntryName, faze, dbprops, data);
        } else {
          if (dbprops.getRoot() != INTERNAL_ROOT) {
            filesForSuppressedFazes.inc("!!! SUPPRESSED: " + faze);
          }
        }
      }
    } else {
      log.error("Nevalidni faze v \"%s\"", aEntryName);
    }

  }

  /**
   * @param aFaze
   * @return
   */
  private boolean zavadetTutoFazi(final EFazeZavedeni aFaze) {
    return processedFazes == null || processedFazes.contains(aFaze); // všechny fáze zavádíme
  }

  /**
   * @return
   */
  private boolean isScriptForLoad(final String aEntryName) {
    if (ignoredEntries.contains(aEntryName)) {
      return false;
    }
    if (aEntryName.toLowerCase().endsWith(".osql")) {
      return true;
    }
    if (aEntryName.toLowerCase().endsWith(".isql")) {
      return true;
    }
    if (aEntryName.toLowerCase().endsWith(".mysql")) {
      return true;
    }
    if (aEntryName.toLowerCase().endsWith(".auttrigs.xml")) {
      return true;
    }
    return false;
  }


  {
    ignoredEntries.add("dbpack.properties"); // ignoruje se jen částerčně, ve fázi načítání dbpacku, byl však načten předem
    ignoredEntries.add("once/once.txt"); // ignoruje se jen dokonale
  }


  /**
   * @return the processedFazes
   */
  public Set<EFazeZavedeni> getProcessedFazes() {
    return processedFazes;
  }


  /**
   * @param aProcessedFazes the processedFazes to set
   */
  public void setProcessedFazes(final Set<EFazeZavedeni> aProcessedFazes) {
    processedFazes = aProcessedFazes;
  }


  /**
   * @return the filesForSuppressedFazes
   */
  public CounterMap<String> getFilesForSuppressedFazes() {
    return filesForSuppressedFazes;
  }


  /**
   * Hlavní načítací metoda, která zbuduje výsledek do builderu.
   * @param defaultDbpackProperties
   * @throws IOException
   */
  public void readDirWithDbpacks(final DbpackProperties defaultDbpackProperties) throws IOException {
    new DbpackScaner(this).read(defaultDbpackProperties.getRoot());

  }
}
