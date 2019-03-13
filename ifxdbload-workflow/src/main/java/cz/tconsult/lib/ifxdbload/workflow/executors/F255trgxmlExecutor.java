package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.time.Instant;
import java.util.List;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;

public class F255trgxmlExecutor implements FazeExecutor {

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




    final Instant str = Instant.now();

    final List<LoSoubor> loSoubor = lofaze.getSoubors();


    /*
    final List<SplStatement> stms =
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.toList());

    // TODO [veverka] řešit schema -- 7. 3. 2019 10:34:16 veverka
    final XmlTrgLoader prcLoader = new XmlTrgLoader(ctx);
    prcLoader.readAllFromCatalog();
    prcLoader.load(stms);

     */

    // TODO [jaksik]  -- 12. 3. 2019 14:34:12 jaksik
    return null;


  }

  @Override
  public void skip() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 10:47:26]

    System.err.println("XML TRIGRY SE NEZAVADI");
  }

}
