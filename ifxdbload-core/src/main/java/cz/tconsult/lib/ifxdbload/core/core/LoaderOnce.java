/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.dbloader.itf.EMessageCategory;
import cz.tconsult.dbloader.itf.Position;
import cz.tconsult.dbloader.itf.UniversalResultMessage;
import cz.tconsult.lib.ifxdbload.core.once.OnceLoader;
import cz.tconsult.lib.ifxdbload.core.once.OnceLoaderResult;
import cz.tconsult.lib.ifxdbload.core.once.OnceScriptData;
import cz.tconsult.lib.ifxdbload.core.once.datatypes.OnceError;

/**
 * @author veverka
 *
 */
class LoaderOnce extends Loader0 {


  private static final Logger log = LoggerFactory.getLogger(LoaderOnce.class);

  private OnceLoader onceLoader;


  private static UniversalResultMessage _toMessage(final OnceError aErr) {

    final String message= aErr.getMessage();

    EMessageCategory category;
    if (aErr.getErrorType().isOnlyWarning()) {

      category = EMessageCategory.WARNING;
    } else {
      category = EMessageCategory.ERROR;
    }
    final UniversalResultMessage m = UniversalResultMessage.create(category, message
        , Position.create(aErr.getBeginPosition(), aErr.getBeginRow(), aErr.getBeginCol())
        , Position.create(aErr.getEndPosition(), aErr.getEndRow(), aErr.getEndCol())
        , null /*aSqlCommand*/, null/*aBeginSqlPos*/, null/*aEndSqlPos*/
        , aErr.getCause());

    return m;
  }
  /**
   * @param aLoSoubor
   */
  @Override
  public UniversalDbLoaderResult load(final Connection conn, final Zavadenec aLoSoubor) {
    final OnceScriptData osd = new OnceScriptData(aLoSoubor.getDataAsString(), aLoSoubor.getLocator());
    log.info("Working on once "+ osd.getPath());
    final Instant start = Instant.now();
    final OnceLoaderResult result = onceLoader.load(osd);
    boolean bylaChyba = false;
    boolean byloVarovani = false;
    final List<UniversalResultMessage> msgs = new ArrayList<UniversalResultMessage>();
    for(final OnceError err : result.getErrs()){

      final UniversalResultMessage m = _toMessage(err);

      final String message = err.getMessage();
      if (err.getErrorType().isOnlyWarning()) {
        byloVarovani = true;
        log.warn(message);
      } else {
        bylaChyba = true;
        log.error(message);
      }
      msgs.add(m);
    }
    if (result.getResult() != null) {
      log.info("  .....  " + result.getResult().toString() + " time=" + Duration.between(start, Instant.now()));
    }

    if (! (bylaChyba || byloVarovani)) {
      // Michalovina eklipsovina.
      final UniversalResultMessage m = UniversalResultMessage.create(
          EMessageCategory.NOTICE, ""+result.getResult()
          , null /*aBeginFilePos*/, null/*aEndFilePos*/
          , null/*aSqlCommand*/, null/*aBeginSqlPos*/, null/*aEndSqlPos*/
          , null/*aThr*/);
      msgs.add(m);
    }

    final UniversalDbLoaderResult uniresult = UniversalDbLoaderResult.create(msgs);
    return  uniresult;

  }

  public UniversalDbLoaderResult checkByEclipse(final Zavadenec aLoSoubor) {

    final OnceScriptData osd = new OnceScriptData(aLoSoubor.getDataAsString(), aLoSoubor.getLocator());
    final List<OnceError> errs = onceLoader.checkByEclipse(osd);

    final List<UniversalResultMessage> msgs = new ArrayList<UniversalResultMessage>(errs.size());
    for (final OnceError e : errs) {

      final UniversalResultMessage m = _toMessage(e);
      msgs.add(m);
    }
    final UniversalDbLoaderResult result = UniversalDbLoaderResult.create(msgs);
    return result;
  }


  /**
   * @param aOnceLoader the onceLoader to set
   */
  public void setOnceLoader(final OnceLoader aOnceLoader) {
    onceLoader = aOnceLoader;
  }


}
