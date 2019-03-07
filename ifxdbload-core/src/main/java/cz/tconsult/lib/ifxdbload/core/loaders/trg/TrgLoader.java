package cz.tconsult.lib.ifxdbload.core.loaders.trg;

import java.util.List;
import java.util.Set;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.lib.ifxdbload.core.loaders.hasher.CatalogHasher;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrgLoader {

  private final TransactionTemplate tranik;
  /**
   * Template pro zavedení, musí být už spojen s tím správným schématem pomocí
   *   SET SESSION AUTHORIZATION TO.
   */
  private final JdbcTemplate jt;

  /** Schéma, do kterého se zavádí. Musí být dodáno schéma, do kterého zavádí template.
   * Schéma se použije proto, aby se daly vyfilrovat ty správné procedury ze systémového katalogu.
   * Zavádět lze JsbcTempatem bez schématu */
  private final ASchema schema;
  private CatalogHasher catalogHasher;

  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu do mapy,
   * aby bylo možno zkontrolovat, zda nedošlo ke změně.
   * Je volána jako první.
   */
  public void readAllFromCatalog() {
    catalogHasher = new CatalogHasher(jt, schema);
    catalogHasher.createDbTableWithHashesIfNotExists(jt);
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
      tranik.execute(status -> {
        if (! notChangedObjNames.contains(trg.getName())) { // jen změněné triggery
          jt.update("DROP TRIGGER IF EXISTS " + trg.getName());
          try {
            jt.execute(trg.getText());  // Vlastní zavedení triggeru
          } catch (final BadSqlGrammarException e) {
            System.out.println("CHYBA: " + e);
            System.out.println(trg.getText());
            status.setRollbackOnly();
          }
          catalogHasher.updateHashes(trg);
        }
        //      }
        return null;
      });

    }
  }
}
