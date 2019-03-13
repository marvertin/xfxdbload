package cz.tconsult.lib.ifxdbload.core.loaders.once;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import lombok.RequiredArgsConstructor;

/**
 * Implementace onceloaderu společná pro všechny zaváděcí fáze.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public abstract class OnceLoaderImpl implements OnceLoader {

  private final LoadContext ctx;


  protected DbContext dc() {
    return ctx.dc();
  }

  /**
   * Template pro zavedení, musí být už spojen s tím správným schématem pomocí
   *   SET SESSION AUTHORIZATION TO.
   */
  protected JdbcTemplate jt() {
    return dc().getJt();
  }

  protected TransactionTemplate tranik() {
    return dc().getTt();
  }


  protected LoadContext ctx() {
    return ctx;
  }



}
