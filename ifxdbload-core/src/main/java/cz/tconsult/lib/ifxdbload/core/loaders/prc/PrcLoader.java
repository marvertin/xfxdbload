package cz.tconsult.lib.ifxdbload.core.loaders.prc;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrcLoader {

  /**
   * Template pro zavedení, musí být už spojen s tím správným schématem pomocí
   *   SET SESSION AUTHORIZATION TO.
   */
  private final JdbcTemplate jt;

  /** Schéma, do kterého se zavádí. Musí být dodáno schéma, do kterého zavádí template.
   * Schéma se použije proto, aby se daly vyfilrovat ty správné procedury ze systémového katalogu.
   * Zavádět lze JsbcTempatem bez schématu */
  private final ASchema schema;


  /**
   * Zjistí informace z katalogu. to znamená těla všech procedur daného schématu do mapy,
   * aby bylo možno zkontrolovat, zda nedošlo ke změně.
   * Je volána jako první.
   */
  public void readFromCatalog() {
    final List<Map<String, Object>> xxx = jt.queryForList("SELECT USER,* FROM ap_status");
    System.out.println(xxx);
    System.out.println("louduji z katalogu: " + schema);
  }

  /**
   * Zavedení všech procedur, které se nezměnily.
   * Teoreticky může být volána vícekrát.
   *
   * @param stms Seznam procedur a funkcí k zavedení. Implementace může předpokládat, že zde nejsou žádné další objekty.
   */
  public void load(final List<SplStatement> stms) {
    System.out.println("měl bych loudnout: " + stms.size());
  }
}
