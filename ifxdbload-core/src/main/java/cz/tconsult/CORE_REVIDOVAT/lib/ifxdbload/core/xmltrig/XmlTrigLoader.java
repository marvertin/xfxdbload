/**
 *
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.xmltrig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.jdom.JDOMException;

import cz.tconsult.CORE_REVIDOVAT.dbutil.informix.IfxAutomaticTriggersCreator;



/**
 * @author veverka
 *
 */
public class XmlTrigLoader {

  private final XmlTrigLoaderParams cfg;
  private IfxAutomaticTriggersCreator __iAutCreator;

  private XmlTrigLoader(final XmlTrigLoaderParams aXmlTrigLoaderParams) {

    cfg = aXmlTrigLoaderParams;
  }

  private IfxAutomaticTriggersCreator _getIfxAutomaticTriggersCreator() {

    if (__iAutCreator == null) {

      //    String className="com.informix.jdbc.IfxDriver";
      //    Class.forName(className);
      //    String url = "jdbc:informix-sqli://kosatka:1526/ah1fdrsts03i:;INFORMIXSERVER=kosatka1";
      //    Connection c = DriverManager.getConnection(url, "aris", "heslo");
      //    System.out.println(c.getMetaData().getDatabaseProductName());
      //vypíše: Informix Dynamic Server

      //    String className="oracle.jdbc.driver.OracleDriver";
      //    Class.forName(className);
      //    String url = "jdbc:oracle:thin:@localhost:1521:XE";
      //    Connection c = DriverManager.getConnection(url, "aris", "heslo");
      //    System.out.println(c.getMetaData().getDatabaseProductName());
      //vypíše: Oracle

      final Connection c = cfg.getConnection();
      boolean oracleDialect;
      try {
        final String dbProductName = c.getMetaData().getDatabaseProductName();
        if (dbProductName.startsWith("Informix")) {

          oracleDialect = false;
        }
        else if (dbProductName.startsWith("Oracle")) {

          oracleDialect = true;
        }
        else if (dbProductName.startsWith("MySQL")) {

          oracleDialect = true;
        }

        else {

          throw new UnsupportedOperationException("Unrecognized DB product name: " + dbProductName);
        }
      } catch (final SQLException e) {

        throw new RuntimeException(e);
      }

      __iAutCreator = new IfxAutomaticTriggersCreator();
      __iAutCreator.init(c, oracleDialect, false);
    }
    return __iAutCreator;
  }
  public static XmlTrigLoader getInstance(final XmlTrigLoaderParams aOnceLoaderParams) {
    return new XmlTrigLoader(aOnceLoaderParams);
  }

  public XmlTrigLoaderResult load(final XmlTrigData xtd)  {
    final XmlTrigLoaderResult result = new XmlTrigLoaderResult();
    try {
      final XmlAutoTiggerParser autTrgParser=new XmlAutoTiggerParser();
      final IfxAutomaticTriggersCreator creator = _getIfxAutomaticTriggersCreator();
      autTrgParser.loadAutTriggers2db(xtd, creator);
    }
    catch(JDOMException | SQLException | IOException | RuntimeException e){
      result.exc = e;
    }

    return result;
  }


  /**
   *
   */
  public void reinit() {

    _getIfxAutomaticTriggersCreator().reinit();
  }

}
