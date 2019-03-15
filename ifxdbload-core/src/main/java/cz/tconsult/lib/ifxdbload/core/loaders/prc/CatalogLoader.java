package cz.tconsult.lib.ifxdbload.core.loaders.prc;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.io.Resources;

import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.AColumnName;
import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.ATableName;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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
  private Map<ATableName, Set<AColumnName>> tableColumnsInDb;


  private int skipped;
  private int reloaded;
  private int loaded;

  /**
   * Načte z katalogu procedury a jejich těla
   */
  public void readProceduresFromCatalog() {

    proceduresInDb =
        jt.query(sql("ProceduresBodyAll.sql"),
            new BeanPropertyRowMapper<>(ProcedureBody.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(ProcedureBody::getProcname, // podle názvu seskupit do seznamu stringů
                mapping(ProcedureBody::getData, joining()))); // a všechny stringy spojit
    log.debug("Mapa názvů na data: {}" , proceduresInDb);

  }

  @Data
  public static class ProcedureBody {
    private String procname;
    private String data;
  }

  /**
   * Načte z katalogu tabulky a jejich sloupce
   */
  public Map<ATableName, Set<AColumnName>> readTableColumnsFromCatalog() {
    tableColumnsInDb =
        jt.query(sql("TablesColumnsAll.sql"),
            new BeanPropertyRowMapper<>(TableColumn.class, true)) // a to je jen pomocný objekt
        .stream().collect(
            groupingBy(TableColumn::getTabname, // podle názvu seskupit do seznamu stringů
                mapping(TableColumn::getColname, Collectors.toSet()))); // a všechny do setu
    log.debug("Mapa názvů na sloupce: {}" , tableColumnsInDb);

    return tableColumnsInDb;
  }

  @Data
  public static class TableColumn {
    private ATableName tabname;
    private AColumnName colname;
  }

  /**
   * Načte z katalogu názvy tabulek, která mají "bekíé serno" a v triggeru je pro ně zípis do tw_serno
   */
  public List<ATableName> readBigsernoTablesFromCatalog() {
    final List<ATableName> tables = jt.queryForList(sql("BigsernoTables.sql"),ATableName.class);
    log.debug("Sezname tabulek s bigsernem: {}" , tables);

    return tables;
  }



  @SneakyThrows
  public String sql(final String sqlResoruceName) {
    return Resources.toString(Resources.getResource("sql/" + sqlResoruceName), StandardCharsets.UTF_8);
  }


  public List<SplStatement> diff(final List<SplStatement> stms) {
    return stms.stream().filter(this::shouldLoad).collect(Collectors.toList());
  }

  private boolean shouldLoad(final SplStatement prc) {

    final String procname = prc.getName().toLowerCase();
    final String procbody = prc.getText().trim();

    if (proceduresInDb.containsKey(procname)) {
      final String procBodyDb = proceduresInDb.get(procname).trim();
      if (StringUtils.equalsIgnoreCase(procbody, procBodyDb)) {
        skipped++;
        return false;
      }
      reloaded++;
      return true;
    } else {
      loaded++;
      return true;
    }
  }


  public int getSkipped() {
    return skipped;
  }


  public void setSkipped(final int skipped) {
    this.skipped = skipped;
  }


  public int getReloaded() {
    return reloaded;
  }


  public void setReloaded(final int reloaded) {
    this.reloaded = reloaded;
  }


  public int getLoaded() {
    return loaded;
  }


  public void setLoaded(final int loaded) {
    this.loaded = loaded;
  }




}
