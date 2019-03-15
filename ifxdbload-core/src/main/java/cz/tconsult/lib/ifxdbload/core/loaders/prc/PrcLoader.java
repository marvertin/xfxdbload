package cz.tconsult.lib.ifxdbload.core.loaders.prc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.lang.Nullable;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader0;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;

public class PrcLoader extends DbObjLoader0 {

  private static final Logger log = LoggerFactory.getLogger(PrcLoader.class);

  private final CatalogLoader catalogLoader;

  public PrcLoader(final LoadContext ctx) {
    super(ctx);
    catalogLoader = new CatalogLoader(jt());
  }

  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu
   * do mapy, aby bylo možno zkontrolovat, zda nedošlo ke změně. Je volána jako
   * první.
   */
  @Override
  public void readAllFromCatalog() {
    // FIXME [jaksik] upravit logování -- 11. 3. 2019 14:18:02 jaksik
    log.info("louduji z katalogu");
    catalogLoader.readProceduresFromCatalog();
  }

  /**
   * Zavedení všech procedur, které se nezměnily. Teoreticky může být volána
   * vícekrát.
   *
   * @param stms Seznam procedur a funkcí k zavedení. Implementace může
   *             předpokládat, že zde nejsou žádné další objekty.
   */
  @Override
  public void load(final List<SplStatement> stms) {
    checkSupportedTypes(stms);

    //nalezení procedur, které se mají být zavedeny - nové, změněné
    final List<SplStatement> proceduryKZavedeni = catalogLoader.diff(stms);

    log.info("PROCEDURES: changed {} + same {} = total {}",  proceduryKZavedeni.size(),  stms.size() - proceduryKZavedeni.size(), stms.size());
    // Procedury zavádíme paralelně.
    final AtomicInteger pocetChyb = new AtomicInteger(0);
    proceduryKZavedeni
    //.stream()
    .parallelStream()
    .forEach(prc -> {
      log.debug("PROCEDURE --> \"{}\"", prc.getName());
      tranik().execute(status -> {
        dropProcedure(prc);
        try {
          createProcedure(prc);
        } catch (final BadSqlGrammarException e) {
          ctx().errorReporter().sql(e, prc);
          status.setRollbackOnly(); // rolbackujeme
          pocetChyb.incrementAndGet();
        }
        return null; // není co vracet
      });
      log.debug("PROCEDURE <-- \"{}\"", prc.getName());
    });
    log.info("PROCEDURES: loaded {} + error {} = total {}",  proceduryKZavedeni.size() - pocetChyb.get(),  pocetChyb.get(), proceduryKZavedeni.size());

  }


  private void dropProcedure(final SplStatement procedure) {

    final String procname = procedure.getName();
    final EStmType type = procedure.getStmType();

    // FIXME [jaksik] upravit logování -- 11. 3. 2019 14:18:15 jaksik
    log.info("Dropuji {} THREAD[{}]", procname,Thread.currentThread().getName());
    jt().execute("DROP " + type  + " IF EXISTS " + procname);

  }

  private void createProcedure(final SplStatement procedure) {

    // FIXME [jaksik] upravit logování -- 11. 3. 2019 14:18:30 jaksik
    log.info("Vytvářím {} THREAD[{}]", procedure.getName(), Thread.currentThread().getName());

    final String sql = procedure.getText();

    // vypnutí sql escapingu, řeší se tím komentře ve složených závorkách
    class ExecuteStatementCallback implements StatementCallback<Object>, SqlProvider {
      @Override
      @Nullable
      public Object doInStatement(final Statement stmt) throws SQLException {
        stmt.setEscapeProcessing(false);
        stmt.execute(sql);
        return null;
      }
      @Override
      public String getSql() {
        return sql;
      }
    }


    jt().execute(new ExecuteStatementCallback());
    log.info("HOTOVO. {}", procedure.getName());


  }

  @Override
  public EnumSet<EStmType> getSupportedTypes() {
    return EnumSet.of(EStmType.PROCEDURE, EStmType.FUNCTION);
  }



}
