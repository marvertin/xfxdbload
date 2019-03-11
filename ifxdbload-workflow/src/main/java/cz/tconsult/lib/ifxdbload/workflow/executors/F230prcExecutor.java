package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.prc.PrcLoader;
import cz.tconsult.lib.ifxdbload.core.splparser.SplParser;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;

public class F230prcExecutor implements FazeExecutor {

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
    //    lofaze.getSoubors()
    //    .spliterator()
    final SplParser splParser = new SplParser();

    final Instant str = Instant.now();

    final List<SplStatement> stms = lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.toList());

    final PrcLoader prcLoader = new PrcLoader(ctx.dc().getJt());
    prcLoader.readFromCatalog();
    prcLoader.load(stms);

    return null;
  }

  @Override
  public void skip() {

    System.err.println("PROCEDURY SE NEZAVADI");

  }

}
