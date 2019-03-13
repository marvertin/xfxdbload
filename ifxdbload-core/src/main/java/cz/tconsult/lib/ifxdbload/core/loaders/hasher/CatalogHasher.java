package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class CatalogHasher {

  /** Seznam typů, které podporujeme, například PRROCEDUE a FUNCTION.
   * Typy se předají jako parametry do vybíracích selektů.
   */
  private final EnumSet<EStmType> stmTypes;
  //private final EStmType stmType;
  private final JdbcTemplate jt;

  /** Obsahuje heše zdrojáků dříve zavedených objektů, ale jen takových, které jsou stále v systémovém katalogu
   * a aktuální heš obsahu systémového katalo odpovídá evidovanému hešei. To znamené, že objekty nebyly ručně přezavedeny.
   *
   * Objekty této mapy jsou kandidáti na objekty, které nebude nutné zavádět, pokud bude sedět i zdrojový heš.
   */
  private final Map<String, String> hešeDříveZavedených = Collections.synchronizedMap(new HashMap<>());

  //45b5431a56cf58cf676c5b67c0a404b5b64b63b8
  private static final Logger log = LoggerFactory.getLogger(CatalogHasher.class);


  /**
   * Načte z katalogu, spočítá heše a načte pomocné heše, aby byly k dispozici pro kontroly zavedenosti.
   * Metoda bude volána za zavaděčů jen v případě hromadného zavádění.
   * Při zavádění z eclipsu nejspíš volána nebude, což znamená, že mapa zůstane prázdná
   */
  public void readFromCatalog() {
    final Map<String, String> mapCatalog = hashCatalogAll(); // mapa názvů objektů na heše
    //System.err.println(mapCatalog.get);

    hešeDříveZavedených.clear();
    for (final IfxdbloaderObjhash rec : readIfxDbloaderObjhash()) {
      final String catalogHash = mapCatalog.get(rec.getObjname()); // může být null, pokud objekt není v databázi, třeba byl zrušen mimo zavaděč
      if (rec.getCathash().equals(catalogHash)) { // objekt máme a heše katalogu se rovnají
        hešeDříveZavedených.put(rec.getObjname(), rec.getSrchash()); // už nás budou zajímat jen heše zdrojáků
      }

    }
  }


  /**
   * Získá z naší pomocné evidence seznam objektů určitého typu
   * @param stmType
   * @return
   */
  private List<IfxdbloaderObjhash> readIfxDbloaderObjhash() {
    final Object[] params = stmTypes.stream()
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray();

    final String otazniky = stmTypes.stream()
        .map(__-> "?")
        .collect(Collectors.joining(","));

    return jt.query("SELECT objtype, objname, srchash, cathash FROM ifxdbloader_objhash WHERE objtype IN (" + otazniky + ")",
        params,
        new BeanPropertyRowMapper<>(IfxdbloaderObjhash.class, true));
  }


  /**
   * Vytvoří tabulku s hashama, pokud neexistuje.
   * @param jt
   */
  public void createDbTableWithHashesIfNotExists() {
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
  private Map<String, String> hashCatalogAll() {
    final String sql = sql(stmTypesAsParamsAsPartOfResourceName() + "_hashCatalogAll.sql");
    final Map<String, String> map = FSql.selectBodies(jt, sql);
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
  private String hashCatalogOne(final String objName, final EStmType stmType) {
    final String body = jt.queryForList(sql(stmType+ "_hashCatalogOne.sql"),
        new Object[] {objName}, String.class).stream()
        .collect(Collectors.joining());
    return DigestUtils.sha1Hex(body);
  }

  /**
   * Aktualizuje hashe v tabulce hešů podle zdrojáků i systémového katalogu. Očekává se, že objekt byl právě zaveden ve stejné transakci.
   * Buď záznam aktualizuje nebo ho přidá.
   * @param stm
   */
  public void updateHashes(final SplStatement stm) {
    checkStmType(stm);
    final Object[] params = new Object[] {
        stm.getSha1Hash(),
        hashCatalogOne(stm.getNameLower(), stm.getStmType()),
        stm.getStmType().toString(),
        stm.getNameLower().toString(),
    };
    // nejdříve odstranit heše z evidovaných dříve zavedených. Je to proto, že nevíme, zda transakce nebude rolbacknuta a tak by došlo k nekonzistenci
    /// evidence a skutečného stavu. Menší zlo je zavádět vícekrát než nezavést vůbec.
    hešeDříveZavedených.remove(stm.getNameLower());

    if (jt.update(sql("hashesUpdate.sql"), params) == 0) { // zkusit updatnout
      jt.update(sql("hashesInsert.sql"), params); // a když to tam nebylo, tak vložit
    }
  }

  private void checkStmType(final SplStatement stm) {
    if (! stmTypes.contains(stm.getStmType())) {
      throw new RuntimeException("Očekáván jeden z typů " + stmTypes + ", ale přišel typ " + stm.getStmType() + " objektu " + stm.getName() + " | " + stm);
    }
  }

  //  private String sha1(final SplStatement stm) {
  //    return DigestUtils.sha1Hex(stm.getText());
  //  }



  private String stmTypesAsParamsAsPartOfResourceName() {
    return stmTypes.stream()
        .map(Object::toString)
        .collect(Collectors.joining("_"));
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
  public Set<String> notChangedObjNames(final Collection<SplStatement> stms) {
    return stms.stream()
        .filter(stm -> stm.getSha1Hash().equals(hešeDříveZavedených.get(stm.getNameLower())))
        .map(SplStatement::getNameLower)
        .collect(Collectors.toSet());
  }



  @SneakyThrows
  private String sql(final String sqlResoruceName) {
    return Resources.toString(Resources.getResource("sql/" + sqlResoruceName), StandardCharsets.UTF_8);
  }


}
