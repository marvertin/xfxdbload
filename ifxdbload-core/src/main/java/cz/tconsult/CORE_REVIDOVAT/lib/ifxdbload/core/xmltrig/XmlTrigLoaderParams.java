/**
 * 
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.xmltrig;

import java.sql.Connection;

/**
 * @author veverka
 *
 */
public class XmlTrigLoaderParams {
  /**
   * DB connection, na které se bude zavádět, nesmí mít nastaven autoCommit (aktivně testováno).
   */
   private Connection connection;

  /**
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * @param aConnection the connection to set
   */
  public void setConnection(Connection aConnection) {
    connection = aConnection;
  }
  

}
