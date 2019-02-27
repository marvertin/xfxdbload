/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;

/**
 * @author veverka
 *
 */
public class LoFaze implements Comparable<LoFaze> {


  private static final Logger log = LoggerFactory.getLogger(LoFaze.class);

  private final EFazeZavedeni name;


  /** Druh databáze, do kterého fáze patří */
  private final LoDbkind loDbkind;

  private final SortedMap<String, LoSoubor> losoubors = new TreeMap<String, LoSoubor>();


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final String result = String.format("%s [id=%s]{DbKind=%s}"
        , name.titleToLoad(), name.toString(), loDbkind.getName()
        );
    return result;
  }

  /**
   * @param aName
   * @param aLoDbkind
   */
  public LoFaze(final LoDbkind aLoDbkind, final EFazeZavedeni aName) {
    loDbkind = aLoDbkind;
    name = aName;
  }

  public void add(final LoSoubor loSoubor) {
    final LoSoubor staryLoSoubor = losoubors.put(loSoubor.getNameForSort().toLowerCase(), loSoubor);
    if (staryLoSoubor != null) {

      final boolean dataMatched = Arrays.equals(loSoubor.getData(), staryLoSoubor.getData());
      String msg = dataMatched ? "Lehka duplicita (kod stejny)" : "Tezka duplicita (kod ROZDILNY)";
      msg += " v souborech: " + IOUtils.LINE_SEPARATOR + "    " + loSoubor.getLocator()
      + IOUtils.LINE_SEPARATOR  + "    " + staryLoSoubor.getLocator();
      log.warn(msg);
      if (!dataMatched) {

        throw new RuntimeException(msg);
      }
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final LoFaze x) {
    return name.name().compareTo(x.name.name());
  }

  public List<LoSoubor> getSoubors() {
    return new ArrayList<LoSoubor>(losoubors.values());
  }

  public EFazeZavedeni getNameFazeZavedeni(){
    return name;
  }

}
