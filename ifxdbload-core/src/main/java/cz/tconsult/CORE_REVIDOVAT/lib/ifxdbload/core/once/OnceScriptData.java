/**
 * 
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;


/**
 * @author veverka
 *
 */
public class OnceScriptData  {
  /** Data onceskriptu (načtený soubor) */
  private final String data;
  
  /** Cesta k onescriptu. Je používáno pro logování a konec cesty 
   * je použit pro zjišťování dalších informacích jako je:
   *    prc ... je to oncová procedura
   *    VCzech, VSlovak ... je tovarianta
   *    o kterou se jedná fázi
   * Cesta může být oddělena lomítky nebo beksleši..   
   */
  private final String path;
  
  /**
   * @return the data
   */
  public String getData() {
    return data;
  }

  /**
   * @param aData
   * @param aPath
   */
  public OnceScriptData(String aData, String aPath) {
    data = aData;
    path = aPath;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "OnceScriptData [path=" + path + ", data=" + data + "]";
  }


  /**
   * @return
   */
  public String getPath() {
    return path;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((data == null) ? 0 : data.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OnceScriptData other = (OnceScriptData) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    } else if (!data.equals(other.data))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    return true;
  }

  
}
