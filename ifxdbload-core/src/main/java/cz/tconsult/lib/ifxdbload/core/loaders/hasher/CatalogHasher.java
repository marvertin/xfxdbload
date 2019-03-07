package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
   * Načte z katalogu, spočítá heše a načte pomocné heše, aby byly k dispozici pro kontroly zavedenosti.
   */
  public void readFromCatalog() {
  }

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
  private Map<String, String> hashCatalogAll(final EStmType stmType) {
    final Map<String, String> map =
        jt.query(sql(stmType + "_hashCatalogAll.sql"),
            new Object[] {schema.toString()},  // podle schématu se vybírá
            new BeanPropertyRowMapper<>(Record.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(Record::getNazev, // podle názvu seskupit do seznamu stringů
                mapping(Record::getData, joining()))); // a všechny stringy spojit
    log.debug("Mapa názvů na data: {}" , map);
    map.replaceAll((__, body) -> DigestUtils.sha1Hex(body)); // spočítat heše
    log.debug("Mapa názvů na heše: {}" , map);
    return map;
  }

  /**
   * Hešne jeden objekt z katalogu
   * @param objName
   * @param stmType
   * @return
   */
  public String hashCatalogOne(final String objName, final EStmType stmType) {
    final String body = jt.queryForList(sql(stmType+ "_hashCatalogOne.sql"),
        new Object[] {objName, schema.toString()}, String.class).stream()
        .collect(Collectors.joining());
    return DigestUtils.sha1Hex(body);
  }

  /**
   * Aktualizuje hashe v tabulce hešů podle zdrojáků i systémového katalogu. Očekává se, že objekt byl právě zaveden ve stejné transakci.
   * Buď záznam aktualizuje nebo ho přidá.
   * @param stm
   */
  public void updateHashes(final SplStatement stm) {
    final Object[] params = new Object[] {
        DigestUtils.sha1Hex(stm.getText()),
        hashCatalogOne(stm.getName(), stm.getStmType()),
        stm.getStmType().toString(),
        stm.getName().toString(),
        schema.toString(),
    };
    if (jt.update(sql("hashesUpdate.sql"), params) == 0) { // zkusit updatnout
      jt.update(sql("hashesInsert.sql"), params); // a když to tam nebylo, tak vložit
    }
  }


  /**
   * Seznam jmen objektů, které není nutné zavádět, protože:
   *  - je typu, který bereme v úvahu
   *  - má záznam v tabulce ifxdbloader_objhash
   *  - heš zdrojáku souhlasí s hešem v tabulce
   *  - heš katalogu souhlasí s hešem v tabulce
   * @param stms Kolekce k prověření
   * @param stmType Typ příkazu
   * @return
   */
  public Set<String> notChangedObjNames(final Collection<SplStatement> stms, final EStmType stmType) {
    final Map<String, String> hešeZKatalogu = hashCatalogAll(stmType);
    // TODO [veverka]  -- 7. 3. 2019 9:50:08 veverka
    return Collections.emptySet();
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
