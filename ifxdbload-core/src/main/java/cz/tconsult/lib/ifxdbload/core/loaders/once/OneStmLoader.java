package cz.tconsult.lib.ifxdbload.core.loaders.once;

import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.RequiredArgsConstructor;

/**
 * Zavaděč jednoho příkazu.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class OneStmLoader {
  private final SplStatement stm;
  private final OnceDirectivesLocal directives;
  private final LoadContext ctx;

  /**
   * Provede vlastní load, bude zakládat vlákna pro zjišťování stavu.
   * Musí ho však správně uzavřít.
   */
  public void load() {
    final String sql = stm.getText();

    try {
      ctx.dc().getJt().execute(sql);
    } catch (final DataAccessException e) {
      throw new XOnceCommand(e);
    }
  }

}
