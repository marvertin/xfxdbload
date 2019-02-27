package cz.tconsult.REVIDOVAT;

import java.sql.Connection;

public interface TwConnectionFactoryyy {

  /**
   * Vytvoří connection. Očekává se, že vytvoří nové connection.
   * @return
   */
  public Connection createConnection();
}
