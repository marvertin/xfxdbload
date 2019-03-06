package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.List;
import java.util.Map;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
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
  public String execute(final LoadContext ctx) {
    System.out.println(lofaze.getSoubors().size());
    final List<Map<String, Object>> xxx = ctx.dc(ASchema.of("aris")).getJt().queryForList("SELECT USER,* FROM  ap_status ");
    System.out.println(xxx);
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("jedu");
    return null;
  }

  @Override
  public void skip() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("skipuju");

  }

}
