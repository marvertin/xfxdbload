/**
 *
 */
package cz.tconsult.lib.ifxdbload.tool;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core.UniversalDbLoader;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core.UniversalDbLoaderParams;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core.UniversalDbLoaderResult;
import cz.tconsult.REVIDOVAT.SeeAndWriteDBLoadLogTable;
import cz.tconsult.REVIDOVAT.TwConnectionFactoryyy;
import cz.tconsult.REVIDOVAT.ZabudovaneObjekty;
import cz.tconsult.dbloader.itf.EFileCategory;
import cz.tconsult.dbloader.itf.EMessageCategory;
import cz.tconsult.dbloader.itf.UniversalResultMessage;
import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.FUtils;
import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.LoData;
import cz.tconsult.lib.ifxdbload.workflow.data.LoDbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.Processor;
import cz.tconsult.tcbase.clib.bdb.mdbpgmbase.DbPgmBase;
import cz.tconsult.tcbase.clib.bdb.mdbpgmbase.DbPgmBaseConnectorProperties;
import cz.tconsult.tcbase.clib.mpgmbase.OptManager;
import cz.tconsult.tw.lang.FEnvVar;
import cz.tconsult.tw.util.CounterMap;
import cz.tconsult.tw.util.logging.Logf;

/**
 * @author veverka
 *
 */
public class IfxDbLoad extends DbPgmBase {

  // TODO [veverka] co znamená tkové defaultn íschema? -- 26. 2. 2019 9:23:18 veverka
  private static final ASchema DEFAULT_SCHEMA = ASchema.of("<defalut>");

  private static final Logf log = Logf.wrap(LogFactory.getLog(IfxDbLoad.class));

  private final Instant toolStartTime;

  public IfxDbLoad() {

    toolStartTime = Instant.now();
  }
  @Override
  protected void modifyDbPgmBaseConnectorProperties(final DbPgmBaseConnectorProperties aProperties) {

    super.modifyDbPgmBaseConnectorProperties(aProperties);
    //nechceme omezovat spuštění na jedinou instanci v jednom čase
    aProperties.setCheckDuplicitExecution(false);
    // u simulace connection nepotřebujeme, takže ať se vytvoří až bude potřeba a nikoliv na začátku
    aProperties.setEarlyCreateDbConnection(false);
    // Kvůli http://jira:8080/browse/PRUBINA-1141
    aProperties.setCallApInitDbConnectionProc(false);

  }

  @Override
  protected void modifyAditionalDbPgmBaseConnectorProperties(final DbPgmBaseConnectorProperties aProperties) {

    super.modifyAditionalDbPgmBaseConnectorProperties(aProperties);
    //nechceme omezovat spuštění na jedinou instanci v jednom čase
    aProperties.setCheckDuplicitExecution(false);
    // u simulace connection nepotřebujeme, takže ať se vytvoří až bude potřeba a nikoliv na začátku
    aProperties.setEarlyCreateDbConnection(false);
    // Kvůli http://jira:8080/browse/PRUBINA-1179
    aProperties.setCallApInitDbConnectionProc(false);
  }

  @Override
  protected void defineOptions(final OptManager aOm) {

    super.defineOptions(aOm);
    aOm.registerBean(new OptBeanTcDbLoadBase());
  }


  public static void main (final String[] args) {
    final IfxDbLoad m = new IfxDbLoad();
    m.runApp(args);
  };

  @Override
  public void execute() throws Exception {

    final OptBeanTcDbLoadBase optBean = getRegisteredOptBean(OptBeanTcDbLoadBase.class);

    boolean failOnError;
    {
      failOnError = optBean.isFailOneError();
      final Boolean failonerrorBool = FEnvVar.isOnceLoaderFailOnError();
      if (failonerrorBool != null) {
        failOnError = failonerrorBool;
      }
    }
    final List<Path> dirs = optBean.getDir();
    final Set<EFazeZavedeni> processedFazes = optBean.getProcessedFazes();

    final Processor processor = new Processor();

    final LoData data = processor.readFiles(dirs, processedFazes);
    processor.executeFazes(optBean.getProcessedFazes(), data);

    if (true) {
      return;
    }

    logCounters(data.getFilesForRoots(), "Files for root");
    logCounters(data.getFilesForDbkinds(), "Files for dbkinds");

    //    if (true) throw new RuntimeException("Bum na vyjimku", new RuntimeException("Bum bum bum"));
    //    if (true) return;

    logDbkindCounters(data);

    SeeAndWriteDBLoadLogTable seeAndWriteDBLoadLogTable = null;

    final boolean simulace = optBean.isSimulation();
    if (!simulace) {
      final TwConnectionFactoryyy fa;
      final TwConnectionFactoryyy factory = new SessionSettingsTwConnectionFactory(new TwConnectionFactoryyyImpl(getFactoryForAdditionalConnections()), optBean.getNlsLengthSemantics());

      ZabudovaneObjekty.zavestOracleTcDbLoadATcDbTools(getPrimaryConnection(), factory);

      int pocitadloSouboru = 0; // Kolik souborů dohromady ve všech fázích bylo zavedeno.
      hlavnicyklus: for (final LoDbkind loDbkind : data.getLoDbkinds()) {
        faze: for (final LoFaze loFaze : loDbkind.getFazesDeprecated()) {

          // RN00279139 Spustit f020clean jen v případě parametru --clean
          if (loFaze.getNameFazeZavedeni() == EFazeZavedeni.f020clean && !optBean.isClean()) {
            log.info("Fáze 020clean přeskočena, protože nebyl specifikován parametr --clean.");
            continue faze;
          }

          // RN00406845 Pokud bylo před fází 260 zavedeno 0 souborů, přeskočíme ji.
          if (loFaze.getNameFazeZavedeni() == EFazeZavedeni.f260afterobj
              && pocitadloSouboru == 0) {
            log.info("Fáze 260afterobj přeskočena, protože dosud nebyly zavedeny žádné soubory.");
            continue faze;
          }

          // zacatecny cas spracovani
          final Instant startTime = Instant.now();
          final String elapsedTime = FUtils.prettyDuration(Duration.between(toolStartTime, startTime)
              , true /*showLeftZeros*/, false /*showMilliseconds*/);
          log.info("%n##########################################%n"
              + "~faze: %s, doposud uplynulo: %s%n##########################################%n"
              , loFaze, elapsedTime);

          // vypojeny vypis pripojovani do DB
          //            log.info("Connectioning to database: url='%s' user='%s'", dbConnectionParams.getJdbcUrl(), dbConnectionParams.getUserName());
          final Connection conn = factory.createConnection();
          //            log.info("...connected");

          if (loFaze.getNameFazeZavedeni() == EFazeZavedeni.f260afterobj ||
              loFaze.getNameFazeZavedeni() == EFazeZavedeni.f700finish){

            if (seeAndWriteDBLoadLogTable == null) {

              final Connection connNew = factory.createConnection();
              seeAndWriteDBLoadLogTable = new SeeAndWriteDBLoadLogTable(connNew, toolStartTime);
              seeAndWriteDBLoadLogTable.init();

            }
          }

          try {
            // zacni nacitavat table v novom vlakne
            if (seeAndWriteDBLoadLogTable != null) {
              seeAndWriteDBLoadLogTable.beginSeeAndWrite();
              //                seeAndWriteDBLoadLogTable.setStartBeginTime(Instant.now());
            }

            final UniversalDbLoaderParams params = new UniversalDbLoaderParams();
            params.setConnection(conn);
            //params.setOnceForceReload(true);
            final UniversalDbLoader dbLoader = UniversalDbLoader.getInstance(params);

            dbLoader.reinit();
            for (final LoSoubor loSoubor : loFaze.getSoubors()) {
              pocitadloSouboru++;
              log.info("LOADING %s", loSoubor.getLocator());
              final UniversalDbLoaderResult result = dbLoader.load(loSoubor.getZavadenec());
              if (ukoncitOkamzineZpracovani(loSoubor, result, failOnError)) {
                log.info("Posledni zpracovavany soubor: \"%s\"", loSoubor);
                log.info("Preruseno: %n - dbkind=%s%n - faze=%s%n - soubor=%s", loDbkind.getName(), loSoubor.getFaze(), loSoubor.getLocator());
                log.info("Zpracovani bylo automaticky ukonceno, protoze doslo k chybe, pokud nechces koncit pri prvni chybe, nastav promennou prostredi \"set %s=false\".", FEnvVar.ONCELOADER_FAILONERROR);

                // konecny cas spracovani pri chybe
                final Instant finishTime = Instant.now();
                final Duration duration = Duration.between(startTime, finishTime);
                final String dur = FUtils.prettyDuration(duration, false/*showLeftZeros*/, false /*showMilliseconds*/);
                log.info("%n~faze finish with ERROR: %s, duration: %s, start time: %s, finish time: %s%n"
                    , loFaze, dur, startTime, finishTime);

                break hlavnicyklus;
              }
            }

            // ukonci nacitavanie z table a ukonci vlakno
            if (seeAndWriteDBLoadLogTable != null) {
              seeAndWriteDBLoadLogTable.endSeeAndWrite();
              //                seeAndWriteDBLoadLogTable.setEndBeginTime(Instant.now());
            }

            // konecny cas spracovani
            final Instant finishTime = Instant.now();
            //              final Long runTime = finishTime.diff(startTime);

            final Duration duration = Duration.between(startTime, finishTime);
            final String dur = FUtils.prettyDuration(duration, true/*showLeftZeros*/, false /*showMilliseconds*/);
            log.info("%n~faze finish OK: %s, duration: %s, start time: %s, finish time: %s%n"
                , loFaze.getNameFazeZavedeni(), dur, startTime, finishTime);

          } finally  {
            try {
              conn.close();
            } catch (final Exception e) {
              // Musíme ignorovat, protože nechceme shodit celé zavádění třeba kvůli nevalidním triggerům.
            }
          }
        }
      }
    }
  }


  private void logDbkindCounters(final LoData data) {
    for (final LoDbkind loDbkind : data.getLoDbkinds()) {
      log.info("Counters for dbkind=%s", loDbkind.getName());
      if (loDbkind.getFilesForSchemaCounter().count(DEFAULT_SCHEMA) > 0) {
        log.warn("There are dbpacks which have no 'dbpack.properties' then using default schema!");
      }
      logCounters(loDbkind.getFilesForSchemaCounter(), "Files for schema");
      logCounters(loDbkind.getFilesForPhases(), "Files for phase");
      logCounters(loDbkind.getFilesForDbpacksCounter(), "Files for dbpack");
      logCounters(loDbkind.getFilesForSchemasAndPhases(), "Files for schema and phase");
    }
  }

  /**
   * @param aLoSoubor
   * @param aResult
   * @return
   */
  private boolean ukoncitOkamzineZpracovani(final LoSoubor aLoSoubor, final UniversalDbLoaderResult aResult, final boolean aFailOnError) {
    if (! aFailOnError) {
      return false; // vypnuto fajlovani
    }
    if (aLoSoubor.getZavadenec().getFileCategory() != EFileCategory.ONCE) {
      return false; // jinde nez ve once nefajlovat
    }
    if (FEnvVar.isOnceLoaderFailOnError() != null && ! FEnvVar.isOnceLoaderFailOnError()) {
      return false;
    }
    for (final UniversalResultMessage msg : aResult.getMessages()) {
      if (msg.getCategory() == EMessageCategory.ERROR) {
        return true; //neco sfajlvoalo
      }
    }
    return false; // nic nefajluje
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.app.AppBase#initLogging()
   */
  @Override
  protected void initLogging() throws Exception {
    //    getGlobalLogger().setLoggers(getClass().getPackage().getName() + ";cz.tconsult.tw;");
    super.initLogging();
  }

  private void logCounters(final CounterMap<?> cm, final String header) {
    log.info("%s:%n%s", header, cm);
  }

  class SessionSettingsTwConnectionFactory implements TwConnectionFactoryyy {

    private final TwConnectionFactoryyy connectionFactory;
    private final String nlsLengthSemantics;

    public SessionSettingsTwConnectionFactory(final TwConnectionFactoryyy connectionFactory, final String nlsLengthSemantics) {
      super();
      this.connectionFactory = connectionFactory;
      this.nlsLengthSemantics = nlsLengthSemantics;
      if (nlsLengthSemantics != null && !"CHAR".equals(nlsLengthSemantics) && "BYTE".equals(nlsLengthSemantics)){
        throw new RuntimeException("chybné nastavení nlsLengthSemantics");
      }
    }

    @Override
    public Connection createConnection() {
      final Connection connection = connectionFactory.createConnection();
      if (nlsLengthSemantics != null){
        try (Statement stm = connection.createStatement()){
          stm.execute("ALTER SESSION SET NLS_LENGTH_SEMANTICS=" + nlsLengthSemantics);
        } catch (final SQLException e) {
          throw new RuntimeException(e);
        }
      }
      return connection;
    }
  };


}
