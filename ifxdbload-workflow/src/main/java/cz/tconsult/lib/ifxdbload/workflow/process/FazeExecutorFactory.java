package cz.tconsult.lib.ifxdbload.workflow.process;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;

public interface FazeExecutorFactory {

  public FazeExecutor createExecutor(final EFazeZavedeni faze);
}
