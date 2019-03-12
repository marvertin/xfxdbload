package cz.tconsult.lib.ifxdbload.workflow.executors;

import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;

public abstract class FazeExecutor0 implements FazeExecutor {


  protected InterFazeBoard board;
  protected LoFaze lofaze;

  @Override
  public void init(final LoFaze lofaze, final InterFazeBoard board) {
    this.lofaze = lofaze;
    this.board = board;
  }

  @Override
  public boolean isLoadIfEmty() {
    return false;
  }

  @Override
  public boolean shouldLoad() {
    return true;
  }

  @Override
  public void prepare() {
  }

  @Override
  public void skip() {
  }


}
