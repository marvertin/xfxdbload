package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.EnumSet;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplParser;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.F8;
import cz.tconsult.lib.ifxdbload.core.tw.Partitioned;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;

public abstract class DbObjExecutor0 extends FazeExecutor0 {


  public abstract DbObjLoader createDbObjLoader(LoadContext ctx);

  @Override
  public String execute(final LoadContext ctx) {
    final SplParser splParser = new SplParser();

    final DbObjLoader  loader = createDbObjLoader(ctx);

    final EnumSet<EStmType> supportedTypes = loader.getSupportedTypes();

    final Partitioned<SplStatement> objekty = F8.postPartition(lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .filter(pas -> filterAndReportErrors(pas, ctx))
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.groupingBy(spl -> supportedTypes.contains(spl.getStmType()))));

    for (final SplStatement stm : objekty.getNe()) {
      // TODO [veverka] pozicovat chybovou hlášku -- 12. 3. 2019 14:06:06 veverka
      ctx.errorReporter().reportSpravnyObjektNaNespravnemMiste(stm);
    }
    loader.readAllFromCatalog();
    loader.load(objekty.getAno());

    return null;


  }

  boolean filterAndReportErrors(final ParseredSource parsedSource, final LoadContext ctx) {
    if (parsedSource.hasParseError()) {
      ctx.errorReporter().reportError(parsedSource.getBadToken(), parsedSource.getSource());
      return false;
    } else {
      return true;
    }
  }


}
