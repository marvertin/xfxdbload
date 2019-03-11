package cz.tconsult.lib.ifxdbload.core.loaders.vue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.Loader0;
import cz.tconsult.lib.ifxdbload.core.loaders.hasher.CatalogHasher;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.Data;

/**
 * Zavaděč view
 * @author veverka
 *
 */
public class VueLoader extends Loader0 {


  private static final Logger log = LoggerFactory.getLogger(VueLoader.class);

  private final CatalogHasher catalogHasher;


  public VueLoader(final LoadContext ctx) {
    super(ctx);
    catalogHasher = new CatalogHasher(EnumSet.of(EStmType.VIEW), jt());
    catalogHasher.createDbTableWithHashesIfNotExists();
  }

  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu do mapy,
   * aby bylo možno zkontrolovat, zda nedošlo ke změně.
   * Je volána jako první. Ale jen při zavádění z loaderu. Při adhok zavádění z eclipsu se nevolá.
   */
  public void readAllFromCatalog() {
    catalogHasher.readFromCatalog();
  }

  /**
   * Zavedení všech procedur, které se nezměnily.
   * Teoreticky může být volána vícekrát.
   *
   * @param stms Seznam view k zavedení. Implementace může předpokládat, že zde nejsou žádné další objekty.
   */
  public void load(final List<SplStatement> stms) {
    final Set<String> notChangedObjNames = catalogHasher.notChangedObjNames(stms);
    final List<SplStatement> vueKZavedeni = stms.stream().
        filter(vue -> ! notChangedObjNames.contains(vue.getNameLower()))
        .collect(Collectors.toList());
    log.info("VIEWS: changed {} + same {} = total {}",  vueKZavedeni.size(),  stms.size() - vueKZavedeni.size(), stms.size());
    // Triggery zavádíme paralelně.

    final List<Result> results = tryLoadAllViews(vueKZavedeni, 0);
    results.forEach(res -> {
      ctx().reportError(res.exc.get(), res.view); // už víme, že všechno co zbylo, je s chybama
    });

    final int pocetChyb = results.size();
    log.info("VIEWS: loaded {} + error {} = total {}",  vueKZavedeni.size() - pocetChyb,  pocetChyb, vueKZavedeni.size());
  }

  private List<Result> tryLoadAllViews(final List<SplStatement> stms, final int početChybPřiMinulémPokusu) {
    log.info("VIEWS -> iteration, resulting {} views.", stms.size());
    final List<Result> viewSChybama = stms.stream()
        .map(this::loadOneVue)              // tady se zavedou view
        .filter(res -> res.exc.isPresent()) // jen, ty, kteří skočili špatně sem půjdou
        .collect(Collectors.toList());
    if (početChybPřiMinulémPokusu == viewSChybama.size()) {
      return viewSChybama; // už se počet chyb nezměnil, tak vracíme
    } else {
      return tryLoadAllViews(viewSChybama.stream()
          .map(Result::getView)
          .collect(Collectors.toList()),
          viewSChybama.size());
    }
  }


  private Result loadOneVue(final SplStatement vue) {
    try {
      log.info("VIEWS --> \"{}\"", vue.getName());
      final Result t = tranik().execute(status -> {
        if (vue.getNameLower().equals("sec_rolepermissions_v") || vue.getNameLower().equals("sec_roleroles_v")) {
          System.out.println("MOJE");
        }
        jt().update("DROP VIEW IF EXISTS " + vue.getName());
        try {
          jt().execute(vue.getText());  // Vlastní zavedení view
          catalogHasher.updateHashes(vue); // ve stejné tgransakci updatneme heše
          log.info("VIEWS <-- \"{}\" OK", vue.getName());
          return new Result(vue, Optional.empty());
        } catch (final DataAccessException e) {
          log.info("VIEWS <-- \"{}\" !!!ERRRO!!", vue.getName());
          status.setRollbackOnly(); // rolbackujeme
          return new Result(vue, Optional.of(e));
        }
      });
      return t;
    } finally {
      //log.info("VIEWS <-- \"{}\"", vue.getName());
    }
  }

  @Data
  private static class Result {
    private final SplStatement view;
    private final Optional<DataAccessException> exc;
  }
}
