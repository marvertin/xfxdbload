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
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:08:09]
    switch (faze) {
    case f230prc: return new F230prcExecutor();
    case f250trg: return new F250trgExecutor();
    default: return new DummyExecutor();
    }

  }

}
