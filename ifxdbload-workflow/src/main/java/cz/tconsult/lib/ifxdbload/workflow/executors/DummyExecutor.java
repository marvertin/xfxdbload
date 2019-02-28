package cz.tconsult.lib.ifxdbload.workflow.executors;

import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.process.ExecutionContext;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;

public class DummyExecutor implements FazeExecutor {

  private LoFaze lofaze;

  @Override
  public void init(final LoFaze lofaze, final InterFazeBoard board) {
    this.lofaze = lofaze;
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]

  }

  @Override
  public boolean isLoadIfEmty() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    return false;
  }

  @Override
  public boolean shouldLoad() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    return true;
  }

  @Override
  public void prepare() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]

  }

  @Override
  public String execute(final ExecutionContext ctx) {
    System.out.println(lofaze.getSoubors().size());
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("jedu");
    return null;
  }

  @Override
  public void skip(final ExecutionContext ctx) {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("skipuju");

  }

}
