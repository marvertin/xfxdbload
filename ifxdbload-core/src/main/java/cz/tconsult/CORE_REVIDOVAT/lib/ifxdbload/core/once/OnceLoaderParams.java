/**
 * 
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Parametry chování zavaděče.  
 * @author veverka
 *
 */
public class OnceLoaderParams {

 
 /**
  * DB connection, na které se bude zavádět, nesmí mít nastaven autoCommit (aktivně testováno).
  */
  private Connection connection;
 
 /**
  * Zda opakované zavést, i když už zavedeno bylo.
  */
  private boolean forceReload = false;
 
 /**
  * Zda se má nějak zabývat transakcemi. Pokud true, tak operace dle rollbackWhenSuccess.
  * Při neúspěchu je volán ROLLBACK, ale pouze pokud je manageTransactions true.
  */
  private boolean manageTransactions = true;
 
 /**
  * Zda při úspěchu provést ROLLBACK, jinak COMMIT.
  * Nastavení je ignorováno, pokud došlo k neúspěchu, anebo když je manageTransactions false.
  */
  private boolean rollbackWhenSuccess;
  
  /**
   * Jméno databázového prostoru pro Indexy
   */
  private String indexesDbSpace;
  
  /**
   * Zatím nevíme, co toznamená, pokud někdo víá, nechť dokumentaci doplní.
   */
  private boolean strictMode;

  /**
   * @return the indexesDbSpace
   */
  public String getIndexesDbSpace() {
    return indexesDbSpace;
  }

  /**
   * @param aIndexesDbSpace the indexesDbSpace to set
   */
  public void setIndexesDbSpace(String aIndexesDbSpace) {
    indexesDbSpace = aIndexesDbSpace;
  }

  /**
   * @return the strictMode
   */
  public boolean isStrictMode() {
    return strictMode;
  }

  /**
   * @param aStrictMode the strictMode to set
   */
  public void setStrictMode(boolean aStrictMode) {
    strictMode = aStrictMode;
  }

  public Connection getConnection() {
    return connection;
  }
  
  public void setConnection(Connection aConnection) {
    connection = aConnection;
    
    //Může být null - např. pro kontroly once scriptu není DB vůbec potřeba
    if (connection != null) {
      try {
        if (connection.getAutoCommit()) {
          
          //FIXME: dořešit, jak u ORACLE? [polakm;2011-05-27 09:44:37]
          //throw new IllegalStateException("AutoCommit nesmí být nastaveno!");
        }
      } catch (SQLException e) {
  
        throw new RuntimeException("Zjišťovaní AutoCommit flagu.", e);
      }
    }
  }
  
  public boolean isForceReload() {
    return forceReload;
  }
  
  public void setForceReload(boolean aForceReload) {
    forceReload = aForceReload;
  }
  
  public boolean isManageTransactions() {
    return manageTransactions;
  }
  
  public void setManageTransactions(boolean aManageTransactions) {
    manageTransactions = aManageTransactions;
  }
  
  public boolean isRollbackWhenSuccess() {
    return rollbackWhenSuccess;
  }
  
  public void setRollbackWhenSuccess(boolean aRollbackWhenSuccess) {
    rollbackWhenSuccess = aRollbackWhenSuccess;
  }
   
   
}
