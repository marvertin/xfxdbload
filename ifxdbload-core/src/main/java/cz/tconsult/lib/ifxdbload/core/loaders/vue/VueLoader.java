package cz.tconsult.lib.ifxdbload.core.loaders.vue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader0;
import cz.tconsult.lib.ifxdbload.core.loaders.hasher.CatalogHasher;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.Data;

/**
 * Zavaděč view
 * @author veverka
 *
 */
public class VueLoader extends DbObjLoader0 implements DbObjLoader {


  private static final Logger log = LoggerFactory.getLogger(VueLoader.class);

  private final CatalogHasher catalogHasher;
  private final ViewBodiesSaver viewBodiesSaver;


  public VueLoader(final LoadContext ctx) {
    super(ctx);
    catalogHasher = new CatalogHasher(getSupportedTypes(), jt());
    catalogHasher.createDbTableWithHashesIfNotExists();
    viewBodiesSaver = new ViewBodiesSaver(jt());
  }

  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu do mapy,
   * aby bylo možno zkontrolovat, zda nedošlo ke změně.
   * Je volána jako první. Ale jen při zavádění z loaderu. Při adhok zavádění z eclipsu se nevolá.
   */
  @Override
  public void readAllFromCatalog() {
    catalogHasher.readFromCatalog();
  }

  /**
   * Zavedení všech procedur, které se nezměnily.
   * Teoreticky může být volána vícekrát.
   *
   * @param stms Seznam view k zavedení. Implementace může předpokládat, že zde nejsou žádné další objekty.
   */
  @Override
  public void load(final List<SplStatement> stms) {
    checkSupportedTypes(stms);

    final Set<String> notChangedObjNames = catalogHasher.notChangedObjNames(stms);
    final List<SplStatement> vueZeZdrojuKZavedeni = stms.stream().
        filter(vue -> ! notChangedObjNames.contains(vue.getNameLower()))
        .collect(Collectors.toList());
    log.info("VIEWS: changed {} + same {} = total {}",  vueZeZdrojuKZavedeni.size(),  stms.size() - vueZeZdrojuKZavedeni.size(), stms.size());

    final List<Error> errors = loadAllViewsInTrans(vueZeZdrojuKZavedeni);

    // Rozdělíme chyby na chyby view ze zdrojůl a na chyby view z katalogu
    final Map<Boolean, List<Error>> parta = errors.stream().collect(Collectors.partitioningBy(err -> ViewBodiesSaver.viewIsFromCatalog(err.view)));
    final List<Error> errorsFromCatalog = parta.get(true);
    final List<Error> errorsFromSources = parta.get(true);

    if (errorsFromSources.size() > 0) { // pokud máme nějaké chyby ze zdrojů
      // tak můžeme ignorovat chyby z katalogu, ty mohou být indukované například tím, že view ze zdrojů bylo dropnuto, ale nezavedeno,
      // pak se nepodařilo zavést view z katalogu

      final Set<String> jménaViewZeZdrojuKtraChybovala = errorsFromSources.stream()
          .map(err -> err.view.getNameLower())
          .collect(Collectors.toSet());

      // zredukujeme zaváděná voiew o ta, která jsou chybá
      final List<SplStatement> vueZeZdrojuKZavedeniRedukovane =
          vueZeZdrojuKZavedeni.stream()
          .filter(vue -> errorsFromSources.contains(vue.getNameLower()))
          .collect(Collectors.toList());

      final List<Error> errors2 = loadAllViewsInTrans(vueZeZdrojuKZavedeniRedukovane);
      if (errors2.size() > 0) {
        log.error("Žádné view nebylo zavedeno ani napodruhé, neboť jsou chyby ve view, jenž nemáme ve zdrojácéch a při zavedení by byly dropnuty. Zřejmě se změnila definice některého z view tak, že existující view toto view používající již nebude možno zavést.");
        reportErrors(errorsFromSources); // reportujeme puvodni chyby view, ktere se nepodarilo zavost
        reportErrors(errors2);
      } else {
        log.info("VIEWS ZAVEDENA NAPODRUHE, ALE NE VSECHNA:  changed {} + same {} = total {}",  vueZeZdrojuKZavedeni.size(),  stms.size() - vueZeZdrojuKZavedeni.size(), stms.size());
        reportErrors(errorsFromSources); // reportujeme puvodni chyby view, ktere se nepodarilo zavost
      }
    } else if (errorsFromCatalog.size() > 0) { // pokud máme chyby je z katalogu a ne ze zdrojů
      log.error("Žádné view nebylo zavedeno, neboť jsou chyby ve view, jenž nemáme ve zdrojácéch a při zavedení by byly dropnuty. Zřejmě se změnila definice některého z view tak, že existující view toto view používající již nebude možno zavést.");
      reportErrors(errors); // reportujeme všechny původní chyby
    } else { // view se podařilo napoprvé zavést
      log.info("VIEWS ZAVEDENA NAPOPRVE:  changed {} + same {} = total {}",  vueZeZdrojuKZavedeni.size(),  stms.size() - vueZeZdrojuKZavedeni.size(), stms.size());
    }
  }

  private void reportErrors(final List<Error> errors) {
    errors.forEach(res -> {
      ctx().errorReporter().sql(res.exc, res.view); // už víme, že všechno co zbylo, je s chybama
    });
  }

  /**
   * Kompletní zavedení seznamu view.
   *  1. Nejdříve si schová katalog všech view v temtable.
   *  2. Pak se všechna zuaváděná view podropují.
   *  3. Zjistí se, která view se dropla, aniž by byla ve zdrojácích a do zdrojáků se přidají.
   *  4. Vše se iteračně zavádí, dokud klesá počet chyb.
   *  5. Provede se commit, pokud vše zavedeno nebo ROLBACK, pokud je nějaká chyna.
   *  6. Vrac se seznam chyb.
   *
   * @param vueZeZdrojuKZavedeni View, která se mají skutečně zavést. Už se nebdue ovařovat, zda se view změnilo nebo ne, prostě se zavádí.
   * @return View, která se nepovedlo zavést. Může osahovat i view, která nbyla ve vstupu, pokud je bylo nutné přezavést.
   */
  private List<Error> loadAllViewsInTrans(final List<SplStatement> vueZeZdrojuKZavedeni) {
    return tranik().execute(status -> {
      // schovat view, ještě dřív než začneme
      viewBodiesSaver.copyViewBodiesToTempTable();

      // dropnout všechna view, která se budou zavádět
      for (final SplStatement vue : vueZeZdrojuKZavedeni) {
        jt().update("DROP VIEW IF EXISTS " + vue.getName());
      }

      // view, které jsme dropli sami, ale i taková, která se dropla navíc
      final List<SplStatement> droppedViews = new ArrayList<>(viewBodiesSaver.queryDroppedViews().values());

      // a necháme tam jen ta z view, která nezavádíme, což jsou ty, které se droply samy
      final Set<String> nazvyZavadenychView = vueZeZdrojuKZavedeni.stream()
          .map(SplStatement::getNameLower)
          .collect(Collectors.toSet());
      droppedViews.removeIf( vue -> nazvyZavadenychView.contains(vue.getNameLower()));

      for (final SplStatement stm : droppedViews) {
        System.out.println("DROPNUT TGRIGER NECHTENE: " + stm.getName());
      }

      /** Doplněné o view, která zmizela a ze systémového katalogu byla nbejdříve zachráněna tělíčka */
      final List<SplStatement> vueKZavedeni = ListUtils.union(vueZeZdrojuKZavedeni, droppedViews);
      //Lists.asList(first, rest)

      final List<Error> results = tryLoadAllViews(vueKZavedeni);
      final int pocetChyb = results.size();
      log.info("VIEWS: loaded {} + error {} = total {}",  vueKZavedeni.size() - pocetChyb,  pocetChyb, vueKZavedeni.size());
      if (pocetChyb > 0) { // při chybách rolbackujeme
        status.setRollbackOnly();
      }
      return results;
    });

  }

  /**
   * Pokusí se iteračnězavést všecna view, dokud se daří něco zavádět bez chyb. Pak vrátí pouze seznam chybujícíc view i s chybou.
   * @param stms
   * @return
   */
  private List<Error> tryLoadAllViews(final List<SplStatement> stms) {
    return _tryLoadAllViews(stms, 0);
  }

  private List<Error> _tryLoadAllViews(final List<SplStatement> stms, final int početChybPřiMinulémPokusu) {
    log.info("VIEWS -> iteration, resulting {} views.", stms.size());
    final List<Error> viewSChybama = stms.stream()  // žádný paralelismus, potřebujeme hezky všechno v jedné transakci.
        .map(this::loadOneVue)              // tady se zavedou view
        .filter(Optional::isPresent) // jen, ty, kteří skočili špatně sem půjdou
        .map(Optional::get)
        .collect(Collectors.toList());
    if (početChybPřiMinulémPokusu == viewSChybama.size()) {
      return viewSChybama; // už se počet chyb nezměnil, tak vracíme
    } else {
      return _tryLoadAllViews(viewSChybama.stream()
          .map(Error::getView)
          .collect(Collectors.toList()),
          viewSChybama.size());
    }
  }


  /**
   * Zavede jedno jediné view a vrací empty, pokud se to povedlo, jinak zabalenou chybu.
   * Metoda nedropuje view a neřídí transakce.
   * @param vue
   * @return
   */
  private Optional<Error> loadOneVue(final SplStatement vue) {
    try {
      log.debug("VIEWS --> \"{}\"", vue.getName());

      if (vue.getNameLower().equals("sec_rolepermissions_v") || vue.getNameLower().equals("sec_roleroles_v")) {
        System.out.println("MOJE");
      }
      try {
        jt().execute(vue.getText());  // Vlastní zavedení view
        catalogHasher.updateHashes(vue); // ve stejné tgransakci updatneme heše
        log.debug("VIEWS <-- \"{}\" OK", vue.getName());
        return Optional.empty();
      } catch (final DataAccessException e) {
        log.debug("VIEWS <-- \"{}\" !!!ERRRO!!", vue.getName());
        return Optional.of(new Error(vue, e));
      }
    } finally {
      //log.info("VIEWS <-- \"{}\"", vue.getName());
    }
  }

  @Data
  private static class Error {
    private final SplStatement view;
    private final DataAccessException exc;
  }

  @Override
  public EnumSet<EStmType> getSupportedTypes() {
    return EnumSet.of(EStmType.VIEW);
  }
}
