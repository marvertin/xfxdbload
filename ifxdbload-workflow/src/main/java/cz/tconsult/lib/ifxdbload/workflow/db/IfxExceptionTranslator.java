package cz.tconsult.lib.ifxdbload.workflow.db;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class IfxExceptionTranslator implements SQLExceptionTranslator {

  @Override
  public DataAccessException translate(final String task, final String sql, final SQLException ex) {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:13:32]
    return new UncategorizedSQLException("TRANSLACOVNA: " + task, sql, ex);
  }

}
