package cz.tconsult.lib.ifxdbload.workflow.executors;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutorFactory;

public class FazeExecutorFactoryImpl implements FazeExecutorFactory {

  @Override
  public FazeExecutor createExecutor(final EFazeZavedeni faze) {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:08:09]
    return new DummyExecutor();

  }

}
