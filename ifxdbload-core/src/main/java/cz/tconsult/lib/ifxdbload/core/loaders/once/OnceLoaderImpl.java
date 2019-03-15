package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EStatementStatus;
import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.once.OnceScriptDao.Record;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.RequiredArgsConstructor;

/**
 * Implementace onceloaderu společná pro všechny zaváděcí fáze.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class OnceLoaderImpl implements OnceLoader {


  private static final Logger log = LoggerFactory.getLogger(OnceLoaderImpl.class);


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
    final OnceScriptDao dao = new OnceScriptDao(jt());
    checksums = dao.readChecksums();
  }

  @Override
  public void load(final List<ParseredSource> pss) {
    _load(pss.stream()
        .map(OnceScript::new)
        .filter(this::filterGoodSyntaxOnce)
        .filter(this::filterShouldLoad)
        .collect(Collectors.toList())
        );
  }

  private void _load(final List<OnceScript> pss) {

    System.out.println("---------------------------------------------------");
    Collections.sort(pss);

    for (final OnceScript once : pss) {
      loadOnce(once);
    }
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 8:33:37]

  }

  private void reportOnceError(final OnceScript once, final String description) {
    log.error("Once " + once.getPs().getSource().getName() + "  " + description);
  }

  private void reportOnceErrors(final OnceScript once, final Set<String> errors) {
    ;
    log.error("Once " +  once.getPs().getSource().getName() + "  " +
        errors.stream().collect(Collectors.joining("\n    ", "\n    ", "")));
  }

  private boolean filterGoodSyntaxOnce(final OnceScript once) {
    final Set<String> syntaxErrors = once.checkSyntaxErrors();
    if (! syntaxErrors.isEmpty()) {
      reportOnceErrors(once, syntaxErrors);
      return false;
    } else {
      return true;
    }
  }

  private boolean filterShouldLoad(final OnceScript once) {
    final boolean b
    = once.getGlobalDirectives().getLoadIfLoaded().map(id -> isAllreadyLoaded(id)).orElse(true)   // jen pokud byl loudnut určitý skript
    && once.getGlobalDirectives().getLoadUnlessLoaded().map(id -> ! isAllreadyLoaded(id)).orElse(true) // jen pokud nebyl loadnut určitý skript
    && ! isAllreadyLoaded(once.getScriptIdFromDirective());
    return b;
  }

  private boolean isAllreadyLoaded(final String id) {
    return checksums.containsKey(id);
  }

  private void _loadStmsOfOneOnce(final OnceScript once) {
    final OnceScriptDao dao = new OnceScriptDao(jt());
    final Record record = new OnceScriptDao.Record(once.computeChecksum(), once.getGlobalDirectives().getDescription(), "", once.getScriptIdFromDirective());

    // už jsem možná v transakci, nastavujeme, že běžíme
    dao.updateEvidence(record, EStatementStatus.EXECUTING);
    for (final SplStatement stm : once.getStatementsToRun()) {
      new OneStmLoader(stm, once.localDirectives(stm), ctx).load();
    }
    // nasavujeme, že jsme skončili
    dao.updateEvidence(record, EStatementStatus.DONE);
  }

  /**
   * Zajistí provolání statementů buď v transakci nebo mimo ni
   * @param once
   */
  private void loadStmsOfOneOnce(final OnceScript once) {
    if (once.isNoTransactionControl()) {
      // provoláme mimo transakci
      _loadStmsOfOneOnce(once);
    } else {
      tranik().execute(status -> {
        // provoláme v transakci
        _loadStmsOfOneOnce(once);
        return null;
      });
    }
  }


  private void loadOnce(final OnceScript once) {
    final String scriptid = once.getScriptIdFromDirective();

    //      final boolean ignorovatChecksum =  once.getStatements().stream().flatMap(stm -> stm.getDirectives().stream()).filter(dir -> dir.getKey().equals("IGNORE_CHECKSUM")).findFirst().isPresent();
    //      ps.getStatements().stream().forEach(stm -> {
    //      });
    //      ps.getStatements().stream().flatMap(stm -> stm.getDirectives().stream()).forEach(dir -> {
    //        // System.out.println("DIRENKA: " + dir.getKey());
    //
    //      });

    //    final Long checksum = checksums.get(scriptid);
    //    if (checksum == null) {
    //      System.out.println("NEZAVEDEN:        " + scriptid);
    //    } else {
    //      if (once.getGlobalDirectives().isIgnoreChecksum()) {
    //        System.out.println("IGNOROVAT:       " + scriptid);
    //      } else if (once.verify(checksum) ) {
    //        System.out.println("OK:              " + scriptid);
    //      } else {
    //        System.out.println("!!!!BAD!!!       " + scriptid);
    //      }
    //    }
    try {
      loadStmsOfOneOnce(once);
    } catch (final XOnceScript e) {
      throw e;
    } catch (final XErrors e) {
      throw new XOnceScript(once, e, e.getErrors());
    } catch (final XOnceCommand e) {
      // výme, že příčina této výjimky je vždy databázová a že ve výjimce není žádná zpráva
      throw new XOnceScript(once, e.getCause(), Collections.emptySet());
    }
  }



}
