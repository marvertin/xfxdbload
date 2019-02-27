/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.read;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.faze.AEntryName;
import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.faze.FFaze;
import cz.tconsult.lib.ifxdbload.core.faze.FazeAnalyzeResult;
import cz.tconsult.lib.ifxdbload.core.tw.CachingSupplierWrapper;
import cz.tconsult.lib.ifxdbload.workflow.data.Builder;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import cz.tconsult.lib.ifxdbload.workflow.scan.FileContentReceiver;
import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;
import lombok.RequiredArgsConstructor;

/**
 * Objekt zodpovědný za načtení dbpacků.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class LoFileContentReceiver implements FileContentReceiver {


  private static final Logger log = LoggerFactory.getLogger(LoFileContentReceiver.class);
  private static final Set<String> ignoredEntries = new HashSet<String>();

  private final Builder builder;

  private final Set<EFazeZavedeni> processedFazes;

  // private static final Path INTERNAL_ROOT = Paths.get("<InternalTcDbLoad>");


  private final CounterMap<String> filesForSuppressedFazes = new CCounterMap<String>();


  /**
   * @param istm
   * @param aEntryName
   * @return
   * @throws IOException
   */
  @Override
  public void add(final DbpackProperties dbprops, final Supplier<byte[]> contentSupplier, final AEntryName aEntryName) {
    if (! isScriptForLoad(aEntryName)) {
      //      log.info(aEntryName);
      return;
    }
    final Supplier<byte[]> supp = new CachingSupplierWrapper<>(contentSupplier);
    //log.info("   found .... " + name);
    final FazeAnalyzeResult analyzeResult = FFaze.analyzeEntryName(aEntryName);
    builder.makeLoDbKind(dbprops);
    for (final EFazeZavedeni faze : analyzeResult.getFazes()) {
      if (zavadetTutoFazi(faze)) {
        builder.addLoSoubor(aEntryName, faze, dbprops, supp.get());
      } else {
        filesForSuppressedFazes.inc("!!! SUPPRESSED: " + faze);
      }
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
  private boolean isScriptForLoad(final AEntryName entryName) {
    final String en = entryName.toString().toLowerCase();
    if (ignoredEntries.contains(en)) {
      return false;
    }
    if (en.endsWith(".isql")) {
      return true;
    }
    if (en.endsWith(".auttrigs.xml")) {
      return true;
    }
    return false;
  }


  {
    ignoredEntries.add("dbpack.properties"); // ignoruje se jen částerčně, ve fázi načítání dbpacku, byl však načten předem
    ignoredEntries.add("once/once.txt"); // ignoruje se jen dokonale
  }


  /**
   * @return the filesForSuppressedFazes
   */
  public CounterMap<String> getFilesForSuppressedFazes() {
    return filesForSuppressedFazes;
  }


}
