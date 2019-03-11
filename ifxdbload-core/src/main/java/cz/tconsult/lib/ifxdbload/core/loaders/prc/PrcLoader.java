package cz.tconsult.lib.ifxdbload.core.loaders.prc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.lang.Nullable;

import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrcLoader {

  private static final Logger log = LoggerFactory.getLogger(PrcLoader.class);

  private static final String PROCEDURES_FROM_CATALOG = "select procid, owner, procname, isproc from sysprocedures where  mode!='o' and mode!='r' and mode!='d'order by procid";

  private static final String PROCEDURES_BODY_FROM_CATALOG = "select data from sysprocbody where procid=? and datakey='T' order by seqno";

  private final Map<String, String> proceduresInDb = new HashMap<>();

  private int skipped;
  private int loaded;
  private int reloaded;

  /**
   * Template pro zavedení, musí být už spojen s tím správným schématem pomocí SET
   * SESSION AUTHORIZATION TO.
   */
  private final JdbcTemplate jt;


  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu
   * do mapy, aby bylo možno zkontrolovat, zda nedošlo ke změně. Je volána jako
   * první.
   */
  public void readFromCatalog() {

    final List<Map<String, Object>> procedures = jt.queryForList(PROCEDURES_FROM_CATALOG);

    //procedures.stream().filter(p -> StringUtils.equals(StringUtils.trim((String) p.get("owner")), schema.toString())).forEach(p -> readProcedureBody(p));
    procedures.stream().forEach(p -> readProcedureBody(p));

  }

  private void readProcedureBody(final Map<String, Object> x) {

    final Integer procid = (Integer) x.get("procid");
    final String procname = ((String) x.get("procname")).toLowerCase();

    final StringBuilder procbody = new StringBuilder();

    jt.queryForList(PROCEDURES_BODY_FROM_CATALOG, procid).stream().forEach(b -> procbody.append(b.get("data")));

    proceduresInDb.put(procname, procbody.toString().trim());

  }

  /**
   * Zavedení všech procedur, které se nezměnily. Teoreticky může být volána
   * vícekrát.
   *
   * @param stms Seznam procedur a funkcí k zavedení. Implementace může
   *             předpokládat, že zde nejsou žádné další objekty.
   */
  public void load(final List<SplStatement> stms) {

    stms.stream().forEach(p -> loadProcedure(p));

    log.info("Zavedeno: {}", loaded);
    log.info("Přezavedeno: {}", reloaded);
    log.info("Přeskočeno: {}", skipped);

  }

  private void loadProcedure(final SplStatement procedure) {

    final String procname = procedure.getName().toLowerCase();

    if (proceduresInDb.containsKey(procname)) {

      final String procbodyInDb = proceduresInDb.get(procname);
      final String procbodyInSource = procedure.getText();


      if (StringUtils.equalsAnyIgnoreCase(procbodyInDb, procbodyInSource)) {
        skipped++;
        log.debug("Přeskakuji (nezměněno) {}", procname);
      } else {
        reloaded++;
        dropProcedure(procedure);
        createProcedure(procedure);
      }

    } else {
      loaded++;
      createProcedure(procedure);
    }

  }

  private void dropProcedure(final SplStatement procedure) {

    final String procname = procedure.getName();
    final EStmType type = procedure.getStmType();

    log.debug("Dropuji {}", procname);
    jt.execute("DROP " + type + " " + procname);

  }

  private void createProcedure(final SplStatement procedure) {

    log.debug("Vytvářím {}", procedure.getName());

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

    jt.execute(new ExecuteStatementCallback());

  }

}
