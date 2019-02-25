package cz.tconsult.lib.ifxdbload.workflow;

import java.sql.Connection;

public interface TwConnectionFactoryyy {

  /**
   * Vytvoří connection. Očekává se, že vytvoří nové connection.
   * @return
   */
  public Connection createConnection();
}
