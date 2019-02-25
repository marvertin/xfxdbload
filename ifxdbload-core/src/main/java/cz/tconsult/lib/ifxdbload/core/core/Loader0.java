/**
 * 
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;


/**
 * @author veverka
 *
 */
abstract class Loader0 {

  public abstract UniversalDbLoaderResult load(Connection conn, Zavadenec aLoSoubor);
  
}
