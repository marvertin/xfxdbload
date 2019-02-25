/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import cz.tconsult.dbloader.itf.EFileCategory;
import cz.tconsult.lib.ifxdbload.core.once.OnceLoader;
import cz.tconsult.lib.ifxdbload.core.once.OnceLoaderParams;
import cz.tconsult.lib.ifxdbload.core.xmltrig.XmlTrigLoader;
import cz.tconsult.lib.ifxdbload.core.xmltrig.XmlTrigLoaderParams;

/**
 * @author veverka
 *
 */
public class UniversalDbLoader {

  //private static final Logf log = Logf.wrap(LogFactory.getLog(UniversalDbLoader.class));


  private final LoaderOnce loaderOnce = new LoaderOnce();
  private final  LoaderDbobj loaderDbObj = new LoaderDbobj();
  private final  LoaderXmlobj loaderXmlobj = new LoaderXmlobj();

  private final UniversalDbLoaderParams iUniversalDbLoaderParams;

  public static UniversalDbLoader getInstance(final UniversalDbLoaderParams params) {
    return new UniversalDbLoader(params);
  }

  private static OnceLoaderParams _toOnceLoaderParams(final UniversalDbLoaderParams aUniversalDbLoaderParams)  {

    final OnceLoaderParams result = new OnceLoaderParams();
    result.setConnection(aUniversalDbLoaderParams.getConnection());
    result.setForceReload(aUniversalDbLoaderParams.isOnceLoaderForceReload());
    return result;
  }

  private static XmlTrigLoaderParams _toXmlTrigLoaderParams(final UniversalDbLoaderParams aUniversalDbLoaderParams)  {

    final XmlTrigLoaderParams result = new XmlTrigLoaderParams();
    result.setConnection(aUniversalDbLoaderParams.getConnection());
    return result;
  }

  protected UniversalDbLoader(final UniversalDbLoaderParams aUniversalDbLoaderParams) {
    iUniversalDbLoaderParams = aUniversalDbLoaderParams;

    final OnceLoaderParams onceLodaerParams = _toOnceLoaderParams(aUniversalDbLoaderParams);
    final OnceLoader onceLoader = OnceLoader.getInstance(onceLodaerParams);
    loaderOnce.setOnceLoader(onceLoader);


    final XmlTrigLoaderParams xmlTrigLoaderParams = _toXmlTrigLoaderParams(aUniversalDbLoaderParams);
    final XmlTrigLoader xmlTrigLoader = XmlTrigLoader.getInstance(xmlTrigLoaderParams);
    loaderXmlobj.setXmlTrigLoader(xmlTrigLoader);

  }


  public UniversalDbLoaderResult load(final Zavadenec aZavadenec)  {
    Loader0 loader;

    EFileCategory cat = aZavadenec.getFileCategory();

    //Null musíme speciálně ošetřit, blbě ho zkousne příkaz switch (vyhazuje NPE)
    //cat přijde jako null pokud např. někdo zavádí z Eclipse z nestandardní lokality[polakm;2011-11-18 15:02:53]
    if (cat == null) {

      cat = EFileCategory.DBOBJ;
    }

    switch (cat) {
    case DBOBJ:
      loader = loaderDbObj;
      break;
    case ALWAYS:
      loader = loaderDbObj;
      break;
    case ONCE:
      loader = loaderOnce;
      break;
    case XMLTRIGER:
      loader = loaderXmlobj;
      break;
    default:
      throw new RuntimeException("Neznama kategorie: " + aZavadenec.getFileCategory());

    }
    final UniversalDbLoaderResult result = loader.load(iUniversalDbLoaderParams.getConnection(), aZavadenec);
    return result;
  }

  /**
   *
   */
  public void reinit() {
    loaderXmlobj.getXmlTrigLoader().reinit(); // stačilo by pouze před triggerovými fázemi, ale je to zbytečné takto testovat
  }

  public UniversalDbLoaderResult checkByEclipse(final Zavadenec aZavadenec) {

    final UniversalDbLoaderResult result = loaderOnce.checkByEclipse(aZavadenec);
    return result;
  }
}
