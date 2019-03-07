package cz.tconsult.lib.ifxdbload.core.loaders;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Loader0 {

  private final LoadContext ctx;
  private final ASchema schema;


  protected DbContext dc() {
    return ctx.dc(schema);
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

  /** Schéma, do kterého se zavádí. Musí být dodáno schéma, do kterého zavádí template.
   * Schéma se použije proto, aby se daly vyfilrovat ty správné procedury ze systémového katalogu.
   * Zavádět lze JsbcTempatem bez schématu */
  protected ASchema schema() {
    return schema;
  }

  protected LoadContext ctx() {
    return ctx;
  }


}
