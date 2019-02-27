/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.read;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.Builder;
import cz.tconsult.lib.ifxdbload.workflow.scan.DbpackScaner;
import cz.tconsult.tw.util.CounterMap;

/**
 * Objekt zodpovědný za načtení dbpacků.
 * @author veverka
 *
 */

public class DbpackReader {



  private final Builder builder;
  private final Set<EFazeZavedeni> processedFazes;

  private LoFileContentReceiver loFileContentReceiver;

  // private static final Path INTERNAL_ROOT = Paths.get("<InternalTcDbLoad>");

  public DbpackReader(final Builder builder, final Set<EFazeZavedeni> processedFazes) {
    this.builder = builder;
    this.processedFazes = processedFazes;
  }
  /**
   * Načte a zabuduje interní věci do builderu.
   * @param aDbKind
   * @throws IOException
   */
  public void readInternalToolDbpack(final ADbkind aDbKind) throws IOException {
    //    // FIXME Nějak parametrizovat, ale jak? Možná to ani nebude potřeba
    //    // TODO [veverka] žádné schema "tc" není -- 26. 2. 2019 9:22:32 veverka
    //    final DbpackProperties dbprops = new DbpackProperties(INTERNAL_ROOT, aDbKind, ASchema.of("tc"));
    //    for (final FResList.Entry entry : FResList.getEntries()) {
    //      // TODO [veverka] řešit, pokud to bude nutné --  žádné schema "tc" není -- 26. 2. 2019 9:22:32 veverka
    //      // readAndCallBuilder(dbprops, entry.istm, entry.entryName);
    //    }
  }
  //

  /**
   * @return the filesForSuppressedFazes
   */
  public CounterMap<String> getFilesForSuppressedFazes() {
    return loFileContentReceiver.getFilesForSuppressedFazes();
  }


  /**
   * Hlavní načítací metoda, která zbuduje výsledek do builderu.
   * @param defaultDbpackProperties
   * @throws IOException
   */
  public void readDirWithDbpacks(final Path path) throws IOException {
    loFileContentReceiver = new LoFileContentReceiver(builder, processedFazes);
    new DbpackScaner(loFileContentReceiver).read(path);
  }

}
