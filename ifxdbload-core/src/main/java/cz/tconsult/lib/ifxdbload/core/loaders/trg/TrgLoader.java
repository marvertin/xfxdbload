package cz.tconsult.lib.ifxdbload.core.loaders.trg;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.Loader0;
import cz.tconsult.lib.ifxdbload.core.loaders.hasher.CatalogHasher;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;

/**
 * Zavaděč triggerů
 * @author veverka
 *
 */
public class TrgLoader extends Loader0 {


  private static final Logger log = LoggerFactory.getLogger(TrgLoader.class);

  private final CatalogHasher catalogHasher;


  public TrgLoader(final LoadContext ctx) {
    super(ctx);
    catalogHasher = new CatalogHasher(EnumSet.of(EStmType.TRIGGER), jt());
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
   * @param stms Seznam triggerů k zavedení. Implementace může předpokládat, že zde nejsou žádné další objekty.
   */
  public void load(final List<SplStatement> stms) {
    final Set<String> notChangedObjNames = catalogHasher.notChangedObjNames(stms);
    final List<SplStatement> triggeryKZavedeni = stms.stream().
        filter(trg -> ! notChangedObjNames.contains(trg.getNameLower()))
        .collect(Collectors.toList());
    log.info("TRIGGERS: changed {} + same {} = total {}",  triggeryKZavedeni.size(),  stms.size() - triggeryKZavedeni.size(), stms.size());
    // Triggery zavádíme paralelně.
    final AtomicInteger pocetChyb = new AtomicInteger(0);
    triggeryKZavedeni.parallelStream()
    .forEach(trg -> {
      log.debug("TRIGGER --> \"{}\"", trg.getName());
      tranik().execute(status -> {
        jt().update("DROP TRIGGER IF EXISTS " + trg.getName());
        try {
          jt().execute(trg.getText());  // Vlastní zavedení triggeru
          catalogHasher.updateHashes(trg); // ve stejné tgransakci updatneme heše
        } catch (final BadSqlGrammarException e) {
          ctx().reportError(e, trg);
          status.setRollbackOnly(); // rolbackujeme
          pocetChyb.incrementAndGet();
        }
        return null; // není co vracet
      });
      log.debug("TRIGGER <-- \"{}\"", trg.getName());
    });
    log.info("TRIGGERS: loaded {} + error {} = total {}",  triggeryKZavedeni.size() - pocetChyb.get(),  pocetChyb.get(), triggeryKZavedeni.size());
  }
}
