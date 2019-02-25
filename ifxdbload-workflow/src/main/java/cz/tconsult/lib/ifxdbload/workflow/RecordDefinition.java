/**
 * 
 */
package cz.tconsult.lib.ifxdbload.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author veverka
 *
 */
public class RecordDefinition {

  private List<ItemDef> list = new ArrayList<ItemDef>();
  
  public void def(int aSloupec, String  aFormat, String aSqlColumnExpression) {
    ItemDef itemdef = new ItemDef();
    itemdef.sloupec = aSloupec;
    itemdef.format = aFormat;
    itemdef.sqlColumnExpression = aSqlColumnExpression;
    list.add(itemdef);
    
  }
  
  class ItemDef {
    int sloupec;
    String format;
    String sqlColumnExpression;
  }
  
  public String getColumnNamesList() {
    StringBuilder sb = new StringBuilder();
    for (ItemDef itemDef : list) {
      if (sb.length() > 0) sb.append(',');
      sb.append(itemDef.sqlColumnExpression);
    }
    return sb.toString();
  }
  
  public List<ItemDef> getDefinitions() {
    return list;
  }
}
