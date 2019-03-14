package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.Collections;
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
    _load(pss.stream().map(OnceScript::new).collect(Collectors.toList()));
  }

  private void _load(final List<OnceScript> pss) {
    final String sss = checksums.entrySet().stream().map(Object::toString).collect(Collectors.joining("\n"));
    //System.out.println(sss);

    System.out.println("---------------------------------------------------");
    Collections.sort(pss);

    for (final OnceScript once : pss) {
      if (! once.getDirectiveErrors().isEmpty()) {
        ctx.errorReporter().badOnceDirective(once.getDirectiveErrors(), once.getPs().getSource());
      }
      final String scriptid = once.getScriptId();

      //      final boolean ignorovatChecksum =  once.getStatements().stream().flatMap(stm -> stm.getDirectives().stream()).filter(dir -> dir.getKey().equals("IGNORE_CHECKSUM")).findFirst().isPresent();
      //      ps.getStatements().stream().forEach(stm -> {
      //      });
      //      ps.getStatements().stream().flatMap(stm -> stm.getDirectives().stream()).forEach(dir -> {
      //        // System.out.println("DIRENKA: " + dir.getKey());
      //
      //      });

      final Long checksum = checksums.get(scriptid);
      if (checksum == null) {
        //System.out.println("NEZAVEDEN:        " + scriptid);
      } else {
        if (once.getGlobalDirectives().isIgnoreChecksum()) {
          System.out.println("IGNOROVAT:       " + scriptid);
        } else if (once.verify(checksum) ) {
          //System.out.println("OK:              " + scriptid);
        } else {
          //System.out.println("!!!!BAD!!!       " + scriptid);
        }
      }
    }
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 8:33:37]

  }



}
