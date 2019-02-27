/**
 *
 */
package cz.tconsult.CORE_REVIDOVAT.dbutil.core;

import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.exception.picker.ThrowableChainItem;

/**
 * @author veverka,polakm
 *
 */
public final class FDb {

  //LATER [veverka]: Předělat na hodnoty lépe vystihující, že se jedná o DB, třeba prefixem "db-" ?[polakm;2012-08-01 15:06:48]
  public static final String DB_CONNECTION_PARAMNAME = "connection";
  public static final String DB_USER_PARAMNAME = "user";
  public static final String DB_PASSWORD_PARAMNAME = "password";

  private FDb() {}

  private static final Logger log = LoggerFactory.getLogger(FDb.class);



  /**
   * Najde SQL výjimku a naformátuje ji. Pokud žádná SQL výjimka nebyla nalezena,
   * vrací prázdný řetězec.
   * @param thr
   * @return Naformátovaná výjimka nebo prázdný řetězec. Nevrací null.
   */
  public static String locateAndFormatSqlException(final Throwable thr) {
    if (thr == null) {
      return "";
    }
    final SQLException sqle = locateSqlException(thr);
    if (sqle == null) {
      return "";
    }
    return formatSqlException(sqle);
  }


  /**
   * Najde SQL výjimku v zadaném řetězci výjimek.
   * @param thr
   * @return Nalezená SQL výjimka (první v řtězci),
   * pokud se ji nepodaří najít, vrací null
   */
  public static SQLException locateSqlException(final Throwable thr) {
    for(final ThrowableChainItem tasm :  FThrowable.getThrowableChain(thr)) {
      // TODO [veverka] Revidovat výjimkování -- 25. 2. 2019 12:59:46 veverka
      if (tasm.getThrowable() instanceof SQLException) {
        return (SQLException) tasm.getThrowable();
      }
    }
    return null;
  }

  /**
   * Naformátuje SQL chybu, tak aby v ní byl celý řetězec SQL chyb a také
   * errorCode a sqlState, ale jen jednou. Pokud jsou již tyto údaje ve zprávě,
   * tak je znovu do chyby nedává.
   * @param sqle an exception that provides information on a database access
   * error or other errors
   * @return naformátovana SQL chyba
   */
  public static String formatSqlException(final SQLException sqle) {
    //if (true) return sqle.toString();
    final StringBuilder sb = new StringBuilder();
    for (SQLException e = sqle; e != null; e = e.getNextException()) {
      if (sb.length() > 0) {
        sb.append("; ");
      }
      final String message = e.getMessage();
      final String errorCode = Integer.toString(e.getErrorCode());
      final String sqlState = e.getSQLState();
      if (errorCode != null && message.indexOf(errorCode) < 0) {
        sb.append("errorCode=");
        sb.append(errorCode);
        sb.append(", ");
      }
      if (sqlState != null && message.indexOf(sqlState) < 0) {
        sb.append("sqlState=");
        sb.append(sqlState);
        sb.append(", ");
      }
      sb.append(message);
      if (e instanceof TcSqlException) {
        final TcSqlException tse = (TcSqlException) e;
        sb.append(IOUtils.LINE_SEPARATOR);
        sb.append(tse.getSqlCommandWithMarkedErrors());
        sb.append(IOUtils.LINE_SEPARATOR);
      }
    }
    return sb.toString();
  }

}
