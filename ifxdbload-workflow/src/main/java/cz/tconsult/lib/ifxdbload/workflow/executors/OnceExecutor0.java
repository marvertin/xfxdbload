package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.once.OnceLoader;
import cz.tconsult.lib.ifxdbload.core.loaders.once.OnceLoaderImpl;
import cz.tconsult.lib.ifxdbload.core.loaders.once.XOnceScript;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplParser;
import cz.tconsult.lib.ifxdbload.core.tw.F8;
import cz.tconsult.lib.ifxdbload.core.tw.Partitioned;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;

public abstract class OnceExecutor0 extends FazeExecutor0 {


  private static final Logger log = LoggerFactory.getLogger(OnceExecutor0.class);


  @Override
  public String execute(final LoadContext ctx) {
    final SplParser splParser = new SplParser();

    final OnceLoader  loader = new OnceLoaderImpl(ctx);


    final Partitioned<ParseredSource> parseredSources = F8.postPartition(
        lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .collect(Collectors.groupingBy(ParseredSource::hasParseError)));

    for (final ParseredSource ps : parseredSources.getAno()) {
      ctx.errorReporter().parsing(ps.getBadToken(), ps.getSource());
    }

    // TODO [veverka] shodit, když bude chyba parser  -- 13. 3. 2019 8:32:25 veverka


    loader.readAllFromCatalog();
    try {
      loader.load(parseredSources.getNe());
    } catch (final XOnceScript e) {
      final String err = FThrowable.formatter(e).withoutStackTraceInMoreLines().toText();
      log.error(err);
    }

    return null;


  }

}
