package cz.tconsult.lib.ifxdbload.core.loaders.vue;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Maps;

import cz.tconsult.lib.ifxdbload.core.loaders.hasher.FSql;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.lexer.LexerTokenFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ViewBodiesSaver {

  private final JdbcTemplate jt;

  /**
   * Schovat veškeré view body do dočasné tabulky
   */
  void copyViewBodiesToTempTable() {
    jt.execute("DROP TABLE IF EXISTS ifxdbloader_views_tmp");
    jt.execute("SELECT v.tabid, tabname, seqno, viewtext  FROM sysviews v JOIN systables t ON v.tabid = t.tabid INTO TEMP ifxdbloader_views_tmp");
  }



  /**
   * Vrátí z dočasné tabulky view, které byly dropnuty.
   * @return Mapa jména view na tělo view. Tělo by mělo být připrveno k zavádění.
   */
  Map<String, SplStatement> queryDroppedViews() {
    final Map<String, String> views = FSql.selectBodies(jt, "SELECT tabname as nazev, viewtext as data  FROM ifxdbloader_views_tmp v WHERE tabid not in (SELECT tabid FROM systables t)  ORDER by tabname, seqno");
    final LexerTokenFactory locatorFactory = LexerTokenFactory.getInstance();
    locatorFactory.createLocator("xxx", 0, 0, 0, 0, 0, 0);

    return Maps.transformEntries(views, (nazev, data) ->  new SplStatement(null, EStmType.VIEW, nazev, data,
        locatorFactory.createLocator("<informix/catalog/sysviews>/" + nazev, 0, 0, 0, 0, 0, 0)));
  }


  @Data
  public static class Record {
    private String nazev;
    private String data;
  }

}
