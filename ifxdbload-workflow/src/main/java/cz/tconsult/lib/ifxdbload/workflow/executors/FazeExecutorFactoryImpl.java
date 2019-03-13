package cz.tconsult.lib.ifxdbload.workflow.executors;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutorFactory;

/**
 * TOvárna na fáze.
 * Nechceme mít továrnu přímo ve výčtu, aby se nepropojoval výčet s implementacdí zavádění.
 * @author veverka
 *
 */
public class FazeExecutorFactoryImpl implements FazeExecutorFactory {

  @Override
  public FazeExecutor createExecutor(final EFazeZavedeni faze) {
    switch (faze) {

    case f020clean: return new ____________DummyExecutor_____________();
    case f050before: return new ____________DummyExecutor_____________();
    case f100alter: return new F100alterExecutor();
    case f205seq: return new ____________DummyExecutor_____________();
    case f210tmp: return new ____________DummyExecutor_____________();
    case f230prc: return new F230prcExecutor();
    case f240vue: return new F240vueExecutor();
    case f250trg: return new F250trgExecutor();
    case f255trgxml: return new ____________DummyExecutor_____________();
    case f260afterobj: return new ____________DummyExecutor_____________();
    case f270reg: return new ____________DummyExecutor_____________();
    case f300settings: return new F300settingsExecutor();
    case f400migration: return new F400migrationExecutor();
    case f500alter: return new F500alterExecutor();
    case f555trgxml: return new ____________DummyExecutor_____________();
    case f600tidy:  return new F600tidyExecutor();
    case f700finish: return new ____________DummyExecutor_____________();

    default: throw new RuntimeException("Nepodporovana faze: " + faze);
    }

  }

}

//