/**
 * 
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;

/**
 * @author veverka
 *
 */
public class UniversalDbLoaderParams {

  /**
   * DB connection, na které se bude zavádět, nesmí mít nastaven autoCommit (aktivně testováno).
   */
   private Connection connection;
   
   /**
   * Zda opakované zavést Once script, i když už zaveden byl.
   */
   private boolean onceLoaderForceReload = false;

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

  public boolean isOnceLoaderForceReload() {
    return onceLoaderForceReload;
  }

  public void setOnceLoaderForceReload(boolean aOnceLoaderForceReload) {
    onceLoaderForceReload = aOnceLoaderForceReload;
  }


  

}
