package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.io.Resources;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class CatalogHasher {

  private final JdbcTemplate jt;
  private final ASchema schema;

  //45b5431a56cf58cf676c5b67c0a404b5b64b63b8
  private static final Logger log = LoggerFactory.getLogger(CatalogHasher.class);


  /**
   * Vytvoří tabulku s hashama, pokud neexistuje.
   * @param jt
   */
  public void createDbTableWithHashesIfNotExists(final JdbcTemplate jt) {
    try {
      jt.execute(sql("createDbTableWithHashesIfNotExists.sql"));
      log.info("Created table ifxdbloader_objhash");
      jt.execute("GRANT select,update,insert,delete ON ifxdbloader_objhash to PUBLIC");
    } catch (final DataAccessException e) {
      final int errorCode = FThrowable.findThrowableType(e, SQLException.class)
          .map(SQLException::getErrorCode)
          .orElse(0);
      if (errorCode != -310) {
        throw e;
      }
    }

  }
  /**
   *
   * @param sql Musí to výt selec vracející dva loupce "nazev" a "data"
   * @param schema Schéma, ze kterého vybírat
   * @param jt template, který použít
   * @return mapa názvů na data
   */
  public Map<String, String> hashCatalog(final EStmType stmType) {
    final Map<String, String> map =
        jt.query(sql(stmType + "_hashCatalog.sql"),
            new Object[] {schema.toString()},  // podle schématu se vybírá
            new BeanPropertyRowMapper<>(Record.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(Record::getNazev, // podle názvu seskupit do seznamu stringů
                mapping(Record::getData, joining()))); // a všechny stringy spojit
    log.debug("Mapa názvů na data: {}" , map);
    map.replaceAll((__, data) -> DigestUtils.sha1Hex(data)); // spočítat heše
    log.debug("Mapa názvů na heše: {}" , map);
    return map;
  }

  /**
   * Aktualizuje hashe v tabulce hešů podle zdrojáků i systémového katalogu. Očekává se, že objekt byl právě zaveden.
   * @param stm
   */
  public void updateHashes(final SplStatement stm) {

  }


  @Data
  public static class Record {
    private String nazev;
    private String data;
  }


  @SneakyThrows
  public String sql(final String sqlResoruceName) {
    return Resources.toString(Resources.getResource("sql/" + sqlResoruceName), StandardCharsets.UTF_8);
  }

}
