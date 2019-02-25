package cz.tconsult.lib.ifxdbload.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZabudovaneObjekty {

  private static final String TCDBLOAD_LOG_RESOURCE = "/procedury/tcdbload_log.osql";
  private static final String TCDBTOOLS_RESOURCE = "/procedury/tcdbtools.osql";
  private static final String TCDBLOAD_RESOURCE = "/procedury/tcdbload.osql";
  private static final String STATEMENT_SEPARATOR = "(?m)^/\\s{0,}$";


  private static final Logger log = LoggerFactory.getLogger(ZabudovaneObjekty.class);

  private ZabudovaneObjekty() { }

  /**
   * Zjistí, zda je připojení Oracle a pokud ano zavede
   * procedury/tcdbload.osql a procedury/tcdbtools.osql.
   * Volá se ještě před všemi fázemi.
   * @param aConnectionFactory
   * @throws SQLException
   */
  public static void zavestOracleTcDbLoadATcDbTools(
      final Connection aPrimaryConnection,
      final TwConnectionFactory aConnectionFactory) throws SQLException {
    final DatabaseMetaData databaseMetadata = aPrimaryConnection.getMetaData();
    if (!isOracle(databaseMetadata)) {
      // Neuděláme nic, databáze není Oracle
      return;
    }

    log.info("Oracle detected, now loading tcdbload and tcdbtools...");
    final String tcdbloadLogSql = loadStringFromResource(TCDBLOAD_LOG_RESOURCE);
    final String tcdbtoolsSql = loadStringFromResource(TCDBTOOLS_RESOURCE);
    final String tcdbloadSql = loadStringFromResource(TCDBLOAD_RESOURCE);

    try (Connection connection = aConnectionFactory.createConnection()) {
      loadStatements(connection, splitSql(tcdbloadLogSql));
      loadStatements(connection, splitSql(tcdbloadSql));
      loadStatements(connection, splitSql(tcdbtoolsSql));
    }
    log.info("tcdbload and tcdbtools loaded successfully.");
  }

  private static void loadStatements(final Connection aConnection, final List<String> aSqlList) throws SQLException {
    for (final String sql : aSqlList) {
      log.debug("Executing SQL: %s", sql);
      try (Statement statement = aConnection.createStatement()) {
        statement.executeUpdate(sql);
      }
      log.debug("SQL executed successfully.");
    }
  }

  /**
   * Načte soubor z resource. Kódování souboru je UTF-8.
   * @param aResource Např. "procedury/tcdbload.osql"
   * @return Obsah souboru
   */
  private static String loadStringFromResource(final String aResource) {
    try {
      return IOUtils.toString(new BOMInputStream(ZabudovaneObjekty.class.getClass().getResourceAsStream(aResource)),
          StandardCharsets.UTF_8);
    } catch (final IOException e) {
      // Nemělo by se stát, pokud je správně umístěna procedura.
      throw new RuntimeException("Error loading resource: " + aResource, e);
    }
  }

  private static boolean isOracle(final DatabaseMetaData aDatabaseMetaData) throws SQLException {
    return aDatabaseMetaData != null && aDatabaseMetaData.getDatabaseProductName().startsWith("Oracle");
  }

  private static List<String> splitSql(final String combinedSql) {
    final List<String> result = new LinkedList<>();
    if (combinedSql != null) {
      final String[] tokens = combinedSql.split(STATEMENT_SEPARATOR);
      for (final String token : tokens) {
        if (StringUtils.isNotBlank(token)) {
          result.add(token.trim());
        }
      }
    }
    return result;
  }

}
