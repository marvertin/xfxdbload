/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;

/**
 * @author veverka
 *
 */
public class LoData {

  SortedMap<String, LoDbkind> lodbkinds = new TreeMap<String, LoDbkind>();

  final CounterMap<DbpackProperties> filesForRoots = new CCounterMap<DbpackProperties>();
  final CounterMap<String> filesForDbkinds = new CCounterMap<String>();

  public LoDbkind makeLoDbkind(final String aDbkindName) {
    LoDbkind loDbkind = lodbkinds.get(aDbkindName);
    if (loDbkind == null) {
      loDbkind = new LoDbkind(this, aDbkindName);
      lodbkinds.put(aDbkindName, loDbkind);
    }
    return loDbkind;
  }

  public List<LoDbkind> getLoDbkinds() {
    return new ArrayList<LoDbkind>(lodbkinds.values());

  }

  /**
   * @return the filesForRoots
   */
  public CounterMap<DbpackProperties> getFilesForRoots() {
    return filesForRoots;
  }

  /**
   * @return the filesForDbkinds
   */
  public CounterMap<String> getFilesForDbkinds() {
    return filesForDbkinds;
  }

}
