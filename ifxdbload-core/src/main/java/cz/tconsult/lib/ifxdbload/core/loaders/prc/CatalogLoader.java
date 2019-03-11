package cz.tconsult.lib.ifxdbload.core.loaders.prc;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.io.Resources;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author jaksik
 *
 */
/**
 * @author jaksik
 *
 */
@RequiredArgsConstructor
public class CatalogLoader {

  private final JdbcTemplate jt;

  //45b5431a56cf58cf676c5b67c0a404b5b64b63b8
  private static final Logger log = LoggerFactory.getLogger(CatalogLoader.class);

  private Map<String, String> proceduresInDb;

  /**
   * Načte z katalogu procedury a jejich těla
   */
  public void readFromCatalog() {

    proceduresInDb =
        jt.query(sql("ProceduresCatalogAll.sql"),
            new Object[] {},  // podle schématu se vybírá
            new BeanPropertyRowMapper<>(Record.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(Record::getNazev, // podle názvu seskupit do seznamu stringů
                mapping(Record::getData, joining()))); // a všechny stringy spojit
    log.debug("Mapa názvů na data: {}" , proceduresInDb);

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


  public List<SplStatement> getProcedures(final List<SplStatement> stms) {
    return stms.stream().filter(prc -> shouldLoad(prc)).collect(Collectors.toList());
  }

  private boolean shouldLoad(final SplStatement prc) {

    final String procname = prc.getName().toLowerCase();
    final String procbody = prc.getText().trim();

    return proceduresInDb.containsKey(procname) ? StringUtils.equalsIgnoreCase(procbody, proceduresInDb.get(procname)) : true;

  }


}
