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
 * Kořen veškerých dat k zavádění. Pro každý nalezený dbkind, obsahuje LoDbKind, což jsou veškerá data.
 * @author veverka
 *
 */
public class LoData {

  private final SortedMap<ADbkind, LoDbkind> lodbkinds = new TreeMap<>();

  final CounterMap<DbpackProperties> filesForRoots = new CCounterMap<DbpackProperties>();
  final CounterMap<ADbkind> filesForDbkinds = new CCounterMap<>();

  public LoDbkind makeLoDbkind(final ADbkind aDbkindName) {
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
  public CounterMap<ADbkind> getFilesForDbkinds() {
    return filesForDbkinds;
  }

}
