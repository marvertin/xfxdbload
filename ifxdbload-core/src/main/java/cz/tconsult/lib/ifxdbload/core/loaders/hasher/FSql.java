package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.Data;

public class FSql {

  /**
   * Metoda, která vybere těla na základě zadaného SQL příkazu
   * SQL musí býát typu:
   *    SELECT neco1 as nazev, neco2 as data FROM .....
   *
   * kde první vracená hodnota je název objektu
   * a druhá obdsahuje texty ve více řádcích. Musí být řazeno podle názvu a pak tak, aby texty šly ve správném pořadí.
   *
   * @param sql
   * @return
   */
  public static Map<String, String> selectBodies( final JdbcTemplate jt, final String sql, final Object ... params) {
    final Map<String, String> map =
        jt.query(sql,
            params,
            new BeanPropertyRowMapper<>(Record.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(Record::getNazev, // podle názvu seskupit do seznamu stringů
                mapping(Record::getData, joining()))); // a všechny stringy spojit
    return map;
  }

  @Data
  public static class Record {
    private String nazev;
    private String data;
  }

}
