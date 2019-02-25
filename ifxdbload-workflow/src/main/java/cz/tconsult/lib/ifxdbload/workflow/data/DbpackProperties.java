/**
 * 
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.io.File;

public class DbpackProperties {
  public File root;  // kořen, kde byly property nalezeny
  public String dbkind;
  public String dbschema;
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DbpackProperties [root=" + root + ", dbkind=" + dbkind + ", dbschema=" + dbschema + "]";
  }
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dbkind == null) ? 0 : dbkind.hashCode());
    result = prime * result + ((dbschema == null) ? 0 : dbschema.hashCode());
    result = prime * result + ((root == null) ? 0 : root.hashCode());
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
    DbpackProperties other = (DbpackProperties) obj;
    if (dbkind == null) {
      if (other.dbkind != null)
        return false;
    } else if (!dbkind.equals(other.dbkind))
      return false;
    if (dbschema == null) {
      if (other.dbschema != null)
        return false;
    } else if (!dbschema.equals(other.dbschema))
      return false;
    if (root == null) {
      if (other.root != null)
        return false;
    } else if (!root.equals(other.root))
      return false;
    return true;
  }
  
  
  
}
