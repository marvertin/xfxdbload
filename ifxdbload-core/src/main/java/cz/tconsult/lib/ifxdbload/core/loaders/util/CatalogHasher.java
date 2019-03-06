package cz.tconsult.lib.ifxdbload.core.loaders.util;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.Data;

public class CatalogHasher {


  private static final Logger log = LoggerFactory.getLogger(CatalogHasher.class);

  /**
   *
   * @param sql Musí to výt selec vracející dva loupce "nazev" a "data"
   * @param schema Schéma, ze kterého vybírat
   * @param jt template, který použít
   * @return mapa názvů na data
   */
  public Map<String, String> hashCatalog(final String sql, final ASchema schema, final JdbcTemplate jt) {
    final Map<String, String> map =
        jt.query(sql,
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

  @Data
  public static class Record {
    private String nazev;
    private String data;
  }

}
