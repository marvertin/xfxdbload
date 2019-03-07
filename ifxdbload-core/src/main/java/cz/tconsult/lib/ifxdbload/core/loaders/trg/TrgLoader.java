package cz.tconsult.lib.ifxdbload.core.loaders.trg;

import java.util.List;
import java.util.Set;

import org.springframework.jdbc.BadSqlGrammarException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.Loader0;
import cz.tconsult.lib.ifxdbload.core.loaders.hasher.CatalogHasher;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;

public class TrgLoader extends Loader0 {

  private final CatalogHasher catalogHasher;


  public TrgLoader(final LoadContext ctx, final ASchema schema) {
    super(ctx, schema);
    catalogHasher = new CatalogHasher(jt(), schema());
    catalogHasher.createDbTableWithHashesIfNotExists(jt());
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
   * @param stms Seznam procedur a funkcí k zavedení. Implementace může předpokládat, že zde nejsou žádné další objekty.
   */
  public void load(final List<SplStatement> stms) {
    System.out.println("měl bych loudnout: " + stms.size());
    final Set<String> notChangedObjNames = catalogHasher.notChangedObjNames(stms, EStmType.TRIGGER);


    for (final SplStatement trg: stms) {
      System.out.println("-**------------------------------------- " + trg.getName());
      tranik().execute(status -> {
        if (! notChangedObjNames.contains(trg.getName())) { // jen změněné triggery
          jt().update("DROP TRIGGER IF EXISTS " + trg.getName());
          try {
            jt().execute(trg.getText());  // Vlastní zavedení triggeru
          } catch (final BadSqlGrammarException e) {
            ctx().reportError(e, trg);
            status.setRollbackOnly();
          }
          catalogHasher.updateHashes(trg);
        }
        return null;
      });

    }
  }
}
