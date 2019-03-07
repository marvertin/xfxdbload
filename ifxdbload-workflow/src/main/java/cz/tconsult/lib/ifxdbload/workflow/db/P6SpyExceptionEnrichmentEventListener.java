package cz.tconsult.lib.ifxdbload.workflow.db;

import java.sql.SQLException;

import com.informix.jdbc.IfxConnection;
import com.p6spy.engine.common.CallableStatementInformation;
import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.Loggable;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.ResultSetInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.zaxxer.hikari.pool.ProxyConnection;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.tw.RowCol;
import cz.tconsult.lib.ifxdbload.workflow.process.FSqlExcFormatter;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * Listener, který do SQL výjimky přidá také celý sestavený SQL příkaz včetně svých parametrů.
 * Implementuje všechny metody, které dostávají výjimku.
 *
 * @author veverka
 *
 */
public class P6SpyExceptionEnrichmentEventListener extends JdbcEventListener {

  @Override
  public void onAfterGetConnection(final ConnectionInformation connectionInformation, final SQLException e) {
    setInformationToException(connectionInformation, e);
  }

  @Override
  public void onAfterAddBatch(final PreparedStatementInformation statementInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterAddBatch(final StatementInformation statementInformation, final long timeElapsedNanos, final String sql, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecute(final PreparedStatementInformation statementInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecute(final StatementInformation statementInformation, final long timeElapsedNanos, final String sql, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecuteBatch(final StatementInformation statementInformation, final long timeElapsedNanos, final int[] updateCounts, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecuteUpdate(final PreparedStatementInformation statementInformation, final long timeElapsedNanos, final int rowCount, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecuteUpdate(final StatementInformation statementInformation, final long timeElapsedNanos, final String sql, final int rowCount, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecuteQuery(final PreparedStatementInformation statementInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterExecuteQuery(final StatementInformation statementInformation, final long timeElapsedNanos, final String sql, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterPreparedStatementSet(final PreparedStatementInformation statementInformation, final int parameterIndex, final Object value, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterCallableStatementSet(final CallableStatementInformation statementInformation, final String parameterName, final Object value, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterGetResultSet(final StatementInformation statementInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  @Override
  public void onAfterResultSetNext(final ResultSetInformation resultSetInformation, final long timeElapsedNanos, final boolean hasNext, final SQLException e) {
    setInformationToException(resultSetInformation, e);
  }

  @Override
  public void onAfterResultSetClose(final ResultSetInformation resultSetInformation, final SQLException e) {
    setInformationToException(resultSetInformation, e);
  }

  @Override
  public void onAfterResultSetGet(final ResultSetInformation resultSetInformation, final String columnLabel, final Object value, final SQLException e) {
    setInformationToException(resultSetInformation, e);
  }

  @Override
  public void onAfterResultSetGet(final ResultSetInformation resultSetInformation, final int columnIndex, final Object value, final SQLException e) {
    setInformationToException(resultSetInformation, e);
  }

  @Override
  public void onAfterCommit(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(connectionInformation, e);
  }

  @Override
  public void onAfterConnectionClose(final ConnectionInformation connectionInformation, final SQLException e) {
    setInformationToException(connectionInformation, e);
  }

  @Override
  public void onAfterRollback(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
    setInformationToException(connectionInformation, e);
  }

  @Override
  public void onAfterStatementClose(final StatementInformation statementInformation, final SQLException e) {
    setInformationToException(statementInformation, e);
  }

  private void setInformationToException(final Loggable loggable, final SQLException e) {
    if (e != null) {

      final ProxyConnection hikariConnection =  (ProxyConnection) loggable.getConnectionInformation().getConnection();
      IfxConnection ifxconn;
      ifxconn = unwrap(hikariConnection, IfxConnection.class);
      final int errorOffset = ifxconn.getSQLStatementOffset() - 1; // chceme počítat od nuly
      // Do výjimky přidáme informace o SQL příkazu a jeho parametrech.
      // Nedokážeme to jinak, než přidáním do existující výjimky na konec řetězce.
      // Je totiž proti fylozofii P6Spy modifikovat chování JDBC driveru, proto k tomu nedvá žádné prostředky.
      // Obohacení výjimky o informace o prováděném příkazu za tak velku "modifikaci" nepovažuji.
      final int connectionId = loggable.getConnectionInformation().getConnectionId();
      final String text = "connId=" + connectionId + ", " + loggable.getSqlWithValues();
      final ErrorOffset errorOfsetException = new ErrorOffset(errorOffset);
      e.setNextException(errorOfsetException);
      final String s = FSqlExcFormatter.format(e, "", new RowCol(0,0), loggable.getSqlWithValues(), errorOffset);
      errorOfsetException.setNextException(new SQLException(s));
    }
  }

  /**
   * Vrátí offset chyby zababušený ve výjimce nebo 0, když nebyl offset nalezen
   * Offset je počítán od nuly.
   * @param e
   * @return
   */
  public static int pickErrorOffset(final Exception e) {
    return FThrowable.findThrowableType(e, ErrorOffset.class)
        .map(ErrorOffset::getOffset)
        .orElse(1);
  }

  /**
   * Obálka unwrapování, aby to neobtěžovalo s exceptionou.
   * @param proxyConnection
   * @param iface
   * @return
   */
  @SneakyThrows
  private final <T> T unwrap(final ProxyConnection proxyConnection, final Class<T> iface) {
    return proxyConnection.unwrap(iface);
  }

  @Data
  private static class ErrorOffset extends SQLException {
    private final int offset;
  }
}



//