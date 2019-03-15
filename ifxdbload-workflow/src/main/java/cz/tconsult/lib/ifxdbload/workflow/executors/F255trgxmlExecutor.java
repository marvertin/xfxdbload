package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.prc.CatalogLoader;
import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.AutomaticTriggersSplStatementGenerator;
import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.TrgXmlLoader;
import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.XmlAutoTiggerParser;
import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.XmlTrigData;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
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

    final CatalogLoader cat = new CatalogLoader(ctx.dc().getJt());

    final AutomaticTriggersSplStatementGenerator generator = new AutomaticTriggersSplStatementGenerator(cat.readTableColumnsFromCatalog());

    final XmlAutoTiggerParser parser = new XmlAutoTiggerParser();


    final List<SplStatement> stms = lofaze.getSoubors().stream()
        .map(lo -> { return new XmlTrigData(lo.getDataAsBytes().getBytes(), lo.getLocator());
        })
        .map(parser::parse) //parsujeme XML definice
        .flatMap(Collection::stream)
        .map(generator::generate) //henerujeme SplStatementy pro jednotlivé triggery
        .collect(Collectors.toList());


    final TrgXmlLoader prcLoader = new TrgXmlLoader(ctx);
    prcLoader.readAllFromCatalog();
    prcLoader.load(stms);

    return null;


  }

  @Override
  public void skip() {
  }

}
