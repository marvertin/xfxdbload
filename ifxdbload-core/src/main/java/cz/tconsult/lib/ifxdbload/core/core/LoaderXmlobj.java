/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;

import cz.tconsult.dbloader.itf.EMessageCategory;
import cz.tconsult.dbloader.itf.UniversalResultMessage;
import cz.tconsult.dbutil.core.FDb;
import cz.tconsult.lib.ifxdbload.core.xmltrig.XmlTrigData;
import cz.tconsult.lib.ifxdbload.core.xmltrig.XmlTrigLoader;
import cz.tconsult.lib.ifxdbload.core.xmltrig.XmlTrigLoaderResult;
import cz.tconsult.tw.lang.FThrowable;
import cz.tconsult.tw.util.logging.Logf;

/**
 * @author veverka
 *
 */
class LoaderXmlobj extends Loader0 {

  private static final Logf log = Logf.wrap(LogFactory.getLog(LoaderXmlobj.class));

  private XmlTrigLoader xmlTrigLoader;


  /**
   * @param aLoSoubor
   */
  @Override
  public UniversalDbLoaderResult load(final Connection conn, final Zavadenec aLoSoubor) {
    final XmlTrigData xsd = new XmlTrigData(aLoSoubor.getDataAsBytes(), aLoSoubor.getLocator());
    log.info("Working on XmlTrig "+ xsd.getPath());
    final XmlTrigLoaderResult result = xmlTrigLoader.load(xsd);

    final boolean bylaChyba = result.exc != null;
    if (bylaChyba) {
      final StringBuilder message = new StringBuilder();
      message.append(xsd.getPath() + ": ");
      message.append(IOUtils.LINE_SEPARATOR);
      message.append(FDb.locateAndFormatSqlException(result.exc));
      message.append(IOUtils.LINE_SEPARATOR);
      message.append(FThrowable.getStackTrace(result.exc, "xmltrig"));
      log.error("%s\n", message.toString());
    }

    final List<UniversalResultMessage> msgs = new ArrayList<UniversalResultMessage>();

    EMessageCategory category;
    String description;
    if (bylaChyba) {

      category = EMessageCategory.ERROR;
      description = FDb.locateAndFormatSqlException(result.exc) + " Path: " + xsd.getPath();
    }
    else {

      category = EMessageCategory.NOTICE;
      description = "XmlTrig OK: " + xsd.getPath();
    }
    final UniversalResultMessage m = UniversalResultMessage.create(
        category, description
        , null /*aBeginFilePos*/, null/*aEndFilePos*/
        , null/*aSqlCommand*/, null/*aBeginSqlPos*/, null/*aEndSqlPos*/
        , result.exc);
    msgs.add(m);

    final UniversalDbLoaderResult uniresult = UniversalDbLoaderResult.create(msgs);
    return  uniresult;
  }


  /**
   * @param aXmlTrigLoader the xmlTrigLoader to set
   */
  public void setXmlTrigLoader(final XmlTrigLoader aXmlTrigLoader) {
    xmlTrigLoader = aXmlTrigLoader;
  }


  /**
   * @return the xmlTrigLoader
   */
  public XmlTrigLoader getXmlTrigLoader() {
    return xmlTrigLoader;
  }


}
