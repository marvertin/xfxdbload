package cz.tconsult.lib.ifxdbload.tool;

import java.sql.Connection;

public interface TwConnectionFactory {

  /**
   * Vytvoří connection. Očekává se, že vytvoří nové connection.
   * @return
   */
  public Connection createConnection();
}
