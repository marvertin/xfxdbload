/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.dbloader.itf.EMessageCategory;
import cz.tconsult.dbloader.itf.UniversalResultMessage;
import cz.tconsult.dbutil.core.FDb;
import cz.tconsult.tw.lang.FString;

/**
 * @author veverka
 *
 */
class SqlCommand {


  private static final Logger log = LoggerFactory.getLogger(SqlCommand.class);


  private final Zavadenec zavadenec;
  private final String prikaz;
  private final int firstLine;
  private final int iPoradoveCislo;


  /**
   * @param aZavadenec
   * @param aPrikaz
   * @param aFirstLine
   */
  public SqlCommand(final Zavadenec aZavadenec, final String aPrikaz, final int aFirstLine, final int poradoveCislo) {
    super();
    zavadenec = aZavadenec;
    prikaz = aPrikaz;
    firstLine = aFirstLine;
    iPoradoveCislo = poradoveCislo;
  }

  /**
   *
   * @param aCon
   * @return Message pokud došlo k chybě, jinak vrací null
   */
  public UniversalResultMessage execute(final Connection aCon) {

    UniversalResultMessage result = null;
    //    prikaz = prikaz.trim();
    //    prikaz = StringUtils.removeEnd(prikaz, "/");
    //    prikaz = prikaz.trim();
    //    prikaz = StringUtils.removeEnd(prikaz, ";");
    final String upravenyPrikaz = FString.wrapLines(prikaz, "     ::", "");
    log.debug("=====================================================================================" +
        "%d. : %d (%s) \"%s\"%n%s", iPoradoveCislo, firstLine, zavadenec.getSchema(), zavadenec.getLocator(), upravenyPrikaz);
    try {
      aCon.setAutoCommit(false);
      try (final Statement stm = aCon.createStatement()) {
        stm.setEscapeProcessing(false);
        //            Arrays.toString(prikaz.toCharArray());
        //prikaz = prikaz.replace('\r', ' ');
        //            for (char c : prikaz.toCharArray()) {
        //              System.out.print((int)c + " ");
        //            }
        //            System.out.println();
        final boolean jeToResultSet = stm.execute(prikaz);
        if (jeToResultSet) {
          log.info("%d. : %d (%s) \"%s\"%n%s", iPoradoveCislo, firstLine, zavadenec.getSchema(), zavadenec.getLocator(), upravenyPrikaz);
          vypisResultSet(stm);
        } else {
          final int updateCount = stm.getUpdateCount();
          log.debug("   ... updated rows: %d", updateCount);

        }
      }
      aCon.commit();
      //jt.execute(prikaz);
    } catch (final SQLException e) {
      //log.error("%s", e.getMessage());
      final String ptikazProLogupravenyPrikaz = FString.wrapLines(prikaz, "     !!! ", "");
      final String descr = FDb.locateAndFormatSqlException(e);
      //      FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Zavádění");
      log.error("%n*************************************************************************************************" +
          "%n%s%n%d. : %d (%s) \"%s\"%n%s%n",
          descr,
          iPoradoveCislo, firstLine, zavadenec.getSchema(), zavadenec.getLocator(), ptikazProLogupravenyPrikaz);

      result = UniversalResultMessage.create(EMessageCategory.ERROR, descr
          , null/*aBeginFilePos*/, null/*aEndFilePos*/
          , prikaz, null/*aBeginSqlPos*/, null/*aEndSqlPos*/, e);
    }

    return result;
  }

  private void vypisResultSet(final Statement aStm) throws SQLException {
    try (final ResultSet rs = aStm.getResultSet()) {
      while (rs.next()) {
        for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
          String data = rs.getString(i + 1);
          if (data != null) {
            data = data.trim();
          }
          System.out.print(data + "   ");
        }
        System.out.println();
      }
      System.out.println();
    }
  }

}
