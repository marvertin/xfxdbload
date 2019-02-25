/**
 *
 */
package cz.tconsult.lib.ifxdbload.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.core.FFaze;
import cz.tconsult.lib.ifxdbload.core.core.FazeAnalyzeResult;
import cz.tconsult.lib.ifxdbload.tool.data.Builder;
import cz.tconsult.lib.ifxdbload.tool.data.DbpackProperties;
import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;

/**
 * Objekt zodpovědný za načtení dbpacků.
 * @author veverka
 *
 */
public class DbpackReader {


  private static final Logger log = LoggerFactory.getLogger(DbpackReader.class);

  private static final File INTERNAL_ROOT = new File("<InternalTcDbLoad>");

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


  public void readDirWithDbpacks(final DbpackProperties aDefaultDbpackProperties) throws IOException {
    log.info("Reading dbpacks form dir \"%s\".", aDefaultDbpackProperties.root);
    final DbpackReaderHelper dbpackReaderHelper = new DbpackReaderHelper(this, aDefaultDbpackProperties);
    dbpackReaderHelper.readDir();
  }

  public void readInternalToolDbpack(final String aDbKind) throws IOException {
    // FIXME Nějak parametrizovat, ale jak? Možná to ani nebude potřeba
    final DbpackProperties dbprops = new DbpackProperties();
    dbprops.dbkind = aDbKind;
    dbprops.dbschema = "tc";
    dbprops.root = INTERNAL_ROOT;
    for (final FResList.Entry entry : FResList.getEntries()) {
      readAndCallBuilder(dbprops, entry.istm, entry.entryName);
    }
  }



  /**
   * @param istm
   * @param aEntryName
   * @return
   * @throws IOException
   */
  void readAndCallBuilder(final DbpackProperties dbprops, final InputStream istm, final String aEntryName) throws IOException {
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
            data = readCurrentFile(istm);
          }
          builder.addLoSoubor(aEntryName, faze, dbprops, data);
        } else {
          if (dbprops.root != INTERNAL_ROOT) {
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




  //private EFileCategory urciKategori()

  /**
   * @param zis
   * @return
   * @throws IOException
   */
  private byte[] readCurrentFile(final InputStream zis) throws IOException {
    int size;
    final byte[] buffer = new byte[2048];
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while ((size = zis.read(buffer)) != -1) {
      baos.write(buffer, 0, size);
    }
    final byte[] result = baos.toByteArray();
    return result;
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
}
