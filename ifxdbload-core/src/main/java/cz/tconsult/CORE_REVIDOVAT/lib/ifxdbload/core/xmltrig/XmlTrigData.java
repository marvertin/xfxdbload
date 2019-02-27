/**
 * 
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.xmltrig;

import java.util.Arrays;


/**
 * @author veverka
 *
 */
public class XmlTrigData  {
  /** Data onceskriptu (načtený soubor) */
  private final byte[] data;

  /** Cesta k onescriptu. Je používáno pro logování a konec cesty 
   */
  private final String path;

  /**
   * @return the data
   */
  public byte[] getData() {
    return data;
  }

  /**
   * @param aData
   * @param aPath
   */
  public XmlTrigData(byte[] aData, String aPath) {
    data = aData;
    path = aPath;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "XmlTrigData [path=" + path + ", data=" + data + "]";
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
    result = prime * result + Arrays.hashCode(data);
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
    XmlTrigData other = (XmlTrigData) obj;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (!Arrays.equals(data, other.data))
      return false;
    return true;
  }




}
