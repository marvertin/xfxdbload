package cz.tconsult.lib.ifxdbload.core.loaders;

import java.util.Collection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DbObjLoader0 implements DbObjLoader {

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

  protected void checkSupportedTypes(final Collection<SplStatement> stms) {
    stms
    .stream()
    .filter(stm -> ! getSupportedTypes().contains(stm.getStmType()))
    .forEach(stm -> { // hned na prvním t oshodím, nemá tam co dělat
      throw new IllegalArgumentException("Do zpracování vstoupil nepodporovaný objekt, to si musí odfiltrovat a patřičně ošetřit volající: " + stm + " podporujeme jen " + getSupportedTypes());
    });

  }

}
