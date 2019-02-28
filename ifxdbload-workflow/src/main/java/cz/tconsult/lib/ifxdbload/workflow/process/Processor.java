/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.Builder;
import cz.tconsult.lib.ifxdbload.workflow.data.LoData;
import cz.tconsult.lib.ifxdbload.workflow.data.LoDbkind;
import cz.tconsult.lib.ifxdbload.workflow.executors.FazeExecutorFactoryImpl;
import cz.tconsult.lib.ifxdbload.workflow.read.DbpackReader;

/**
 * @author veverka
 *
 */
public class Processor  {
  private static final ADbkind DBKIND_MAIN = ADbkind.of("main");


  private static final Logger log = LoggerFactory.getLogger(Processor.class);


  /**
   * Načte z daného seznamu adresářů všechny soubory pro požadované fáze.
   * Načte také interní soubory, jenž jsou v nástroji, pokud takové budou potřeba.
   * @param dirs
   * @param processedFazes
   * @return Objekt s načtenými soubory.
   * @throws IOException
   */
  public LoData readFiles(final List<Path> dirs, final Set<EFazeZavedeni> processedFazes) throws IOException {
    final Builder builder = new Builder();
    final DbpackReader dbpackReader = new DbpackReader(builder, processedFazes);

    if (dirs != null) {
      for (final Path sfile : dirs) {
        dbpackReader.readDirWithDbpacks(sfile.toAbsolutePath());
      }

      for (final LoDbkind loDbkind : builder.getData().getLoDbkinds()) {
        // interní věci se zavedou pro každý dbkind.
        dbpackReader.readInternalToolDbpack(loDbkind.getName());
      }

    } else {
      // Není žádný dbpack, budou jen interní soubory
      dbpackReader.readInternalToolDbpack(DBKIND_MAIN);
    }

    final LoData data = builder.getData();

    if (dbpackReader.getFilesForSuppressedFazes().count() > 0) {
      log.warn("There are files in suppressed fazes: %d:%n%s", dbpackReader.getFilesForSuppressedFazes().count(), dbpackReader.getFilesForSuppressedFazes());
    }
    return data;
  }


  /**
   * Vykonání všech zavedených fází.
   * @param fazes
   * @param lodata
   */
  public void executeFazes(final Set<EFazeZavedeni> fazes, final LoData lodata) {
    final Set<EFazeZavedeni> fazes2 = fazes == null ?  EnumSet.allOf(EFazeZavedeni.class) : fazes;

    final FazeManager fazeManager = new FazeManager(lodata, new FazeExecutorFactoryImpl(), new JdbcTemplateFactory() {

      // TODO [veverka] Databázovat správně přes datasorurce a nějaký hikari -- 28. 2. 2019 15:44:21 veverka
      @Override
      public JdbcTemplate jt(final ADbkind dbkind, final ASchema schema) {
        //TODO [veverka] implementuj - vygenerovana metoda [veverka 15:44:02]
        return null;
      }
    });
    fazeManager.executeAll(fazes2);
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 12:24:14]

  }

}
