package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.trg.TrgLoader;
import cz.tconsult.lib.ifxdbload.core.splparser.SplParser;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;

public class F250trgExecutor implements FazeExecutor {

  private LoFaze lofaze;

  @Override
  public void init(final LoFaze lofaze, final InterFazeBoard board) {
    this.lofaze = lofaze;

    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]

  }

  @Override
  public boolean isLoadIfEmty() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]
    return false;
  }

  @Override
  public boolean shouldLoad() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]
    return true;
  }

  @Override
  public void prepare() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]

  }

  @Override
  public String execute(final LoadContext ctx) {
    final SplParser splParser = new SplParser();

    final Instant str = Instant.now();

    final List<SplStatement> stms = lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.toList());

    // TODO [veverka] řešit schema -- 7. 3. 2019 10:34:16 veverka
    final TrgLoader prcLoader = new TrgLoader(ctx);
    prcLoader.readAllFromCatalog();
    prcLoader.load(stms);

    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]
    return null;


  }

  @Override
  public void skip() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]

    System.err.println("TRIGRY SE NEZAVADI");
  }

}
