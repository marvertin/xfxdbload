package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import lombok.RequiredArgsConstructor;

/**
 * Implementace onceloaderu společná pro všechny zaváděcí fáze.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class OnceLoaderImpl implements OnceLoader {

  private final LoadContext ctx;
  private Map<String, Long> checksums;


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

  @Override
  public void readAllFromCatalog() {
    final XOnceScriptDao dao = new XOnceScriptDao(jt());
    checksums = dao.readChecksums();
  }

  @Override
  public void load(final List<ParseredSource> pss) {
    final String sss = checksums.entrySet().stream().map(Object::toString).collect(Collectors.joining("\n"));
    System.out.println(sss);

    System.out.println("---------------------------------------------------");
    pss.sort((x,y) -> x.getFileNameOnly().compareTo(y.getFileNameOnly()));

    for (final ParseredSource ps : pss) {
      System.out.println(ps.getFileNameOnly());
    }
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 8:33:37]

  }



}
