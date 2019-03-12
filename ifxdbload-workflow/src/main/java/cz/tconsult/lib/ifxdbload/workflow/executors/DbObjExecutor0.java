package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.datasource.init.UncategorizedScriptException;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplParser;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;

public abstract class DbObjExecutor0 extends FazeExecutor0 {


  public abstract DbObjLoader createDbObjLoader(LoadContext ctx);

  @Override
  public String execute(final LoadContext ctx) {
    final SplParser splParser = new SplParser();

    final DbObjLoader  loader = createDbObjLoader(ctx);

    final EnumSet<EStmType> supportedTypes = loader.getSupportedTypes();

    final Map<Boolean, List<SplStatement>> spls = lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.groupingBy(spl -> supportedTypes.contains(spl.getStmType())));

    for (final SplStatement stm : spls.get(false)) {
      // TODO [veverka] pozicovat chybovou hlášku -- 12. 3. 2019 14:06:06 veverka
      ctx.reportError(new  UncategorizedScriptException("Nezaveditelná potvora"), stm);
    }
    loader.readAllFromCatalog();
    loader.load(spls.get(true));

    return null;


  }


}
