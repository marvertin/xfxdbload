package cz.tconsult.lib.ifxdbload.workflow.process;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.tw.ErrorReporter;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.LoData;
import cz.tconsult.lib.ifxdbload.workflow.data.LoDbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Manažer manažuje jednotlivé fáze a řídí jejich zavádění
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class FazeManager {


  private static final Logger log = LoggerFactory.getLogger(FazeManager.class);

  private final LoData loData;
  private final FazeExecutorFactory fazeExecutorFactory;
  private final DbContextFactory dbContextFactory;

  private final ErrorReporter errorReporter = new ErrorReporterImpl();

  /**
   * Provede zavdení dané množiny fází v daném exekučním kontextu.
   *
   * @param fazes Fáze, které si přejme zavést (ostatní budou skipnuty)
   * @param ctx Kontext zavádění, zde získáme databázi a sem reportujeme chyby
   */
  public void executeAll(final Set<EFazeZavedeni> fazes) {
    // vytvoření a inicializace exekutoru fází
    final List<ExecutorHolder> executorHolders = init();

    // všechny fáze, které vznikly teď provedeme
    for (final ExecutorHolder holder : executorHolders) {
      final FazeExecutor executor = holder.executor;
      if (fazes.contains(holder.lofaze.getNameFazeZavedeni())) {
        final boolean prazdna = holder.lofaze.getSoubors().isEmpty();
        final ADbkind dbkind = holder.lofaze.getLoDbkind().getName();
        final boolean zavadet = (! prazdna || executor.isLoadIfEmty()) // jen neprázdné nebo ty, které se chtějí zavádět i prázdné
            && executor.shouldLoad()  // a jen ty, které přesto mají být zavedeny
            && dbContextFactory.canCreate(dbkind);
        if (zavadet) {
          // fázi nezavádíme a patřičně logujeme
          // TODO [veverka] Lépe logovat zavádění v samostatných metodách -- 28. 2. 2019 12:14:18 veverka
          log.info("Zahajuji fázi {}", holder.lofaze);

          final String resultMessage = executor.execute(new LoadContextImpl(dbkind)); // toto je vlastní provedení fáze

          log.info("Ukončuji fázi {} s výsledkem: {}", holder.lofaze, resultMessage);
        } else {
          // nezavádíme protože je prázdná nebo fáze sama rozhodla o nezavedení
          executor.skip();
        }
      } else {
        // nezavádíme, protože uživatel nechtěl fázi zavádět
        executor.skip();
      }
    }
  }




  private List<ExecutorHolder> init() {
    // vytvoříme board pro komunikaci mezi fázemi, bude přžívat v celém zpracování
    final InterFazeBoard board = new InterFazeBoard();
    return Arrays.stream(EFazeZavedeni.values())
        .flatMap(faze -> createAndInitExecutors(faze, board))
        .collect(Collectors.toList());
  }

  private Stream<ExecutorHolder> createAndInitExecutors(final EFazeZavedeni faze, final InterFazeBoard board) {
    return loData.getLoDbkinds().stream()
        .map(lokind -> createAndInitExecutor(faze, lokind,board));
  }


  private ExecutorHolder createAndInitExecutor(final EFazeZavedeni faze, final LoDbkind lokind, final InterFazeBoard board) {
    final FazeExecutor executor = fazeExecutorFactory.createExecutor(faze);
    final LoFaze loFaze = lokind.getFaze(faze);
    executor.init(loFaze, board);
    return new ExecutorHolder(executor, loFaze);
  }

  @RequiredArgsConstructor
  private class LoadContextImpl implements LoadContext {

    private final ADbkind dbkind;

    @Override
    public DbContext dc() {
      return dbContextFactory.dc(dbkind); // když se to dostane až sem, tak template existuje
    }

    @Override
    public ErrorReporter errorReporter() {
      return errorReporter;
    }


  }

  @Data
  private class ExecutorHolder {
    private final FazeExecutor executor;
    private final LoFaze lofaze; // nemusí být vůbec součástí
  }
}
