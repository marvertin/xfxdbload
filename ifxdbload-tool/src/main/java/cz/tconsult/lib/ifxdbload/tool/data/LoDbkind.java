/**
 *
 */
package cz.tconsult.lib.ifxdbload.tool.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;

/**
 * @author veverka
 *
 */
public class LoDbkind {

  final LoData lodata;
  final String name;

  private static final Comparator<EFazeZavedeni> NAME_Comparator = new Comparator<EFazeZavedeni>() {

    @Override
    public int compare(final EFazeZavedeni o1, final EFazeZavedeni o2) {

      return o1.name().compareTo(o2.name());
    }

  };

  private static final Comparator<File> FILE_Comparator = new Comparator<File>() {

    @Override
    public int compare(final File o1, final File o2) {

      int result;
      if (o1 == null) {

        result = o2 == null ? 0 : 1;
      }
      else if (o2 == null) {

        result = -1;
      }
      else {

        final String a = o1.getAbsolutePath();
        final String b = o2.getAbsolutePath();
        result = String.CASE_INSENSITIVE_ORDER.compare(a, b);
      }
      return result;
    }

  };

  SortedMap<EFazeZavedeni, LoFaze> lofazes = new TreeMap<EFazeZavedeni, LoFaze>();

  final CounterMap<String> filesForSchemaCounter = new CCounterMap<String>(String.CASE_INSENSITIVE_ORDER);
  final CounterMap<File> filesForDbpacksCounter = new CCounterMap<File>(FILE_Comparator);
  final CounterMap<String> filesForSchemasAndPhases = new CCounterMap<String>(String.CASE_INSENSITIVE_ORDER);
  final CounterMap<EFazeZavedeni> filesForPhases = new CCounterMap<EFazeZavedeni>(NAME_Comparator);

  /**
   * @return the filesForPhases
   */
  public CounterMap<EFazeZavedeni> getFilesForPhases() {
    return filesForPhases;
  }

  /**
   * @return the filesForSchemasAndPhases
   */
  public CounterMap<String> getFilesForSchemasAndPhases() {
    return filesForSchemasAndPhases;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }



  /**
   * @param aLodata
   * @param aName
   */
  public LoDbkind(final LoData aLodata, final String aName) {
    super();
    lodata = aLodata;
    name = aName;

  }

  /**
   * @param aFazeName
   * @return
   */
  public LoFaze makeLoFaze(final EFazeZavedeni aFazeName) {
    LoFaze loFaze = lofazes.get(aFazeName);
    if (loFaze == null) {
      loFaze = new LoFaze(this, aFazeName);
      lofazes.put(aFazeName, loFaze);
    }
    return loFaze;
  }

  public List<LoSoubor> getSoubors() {
    final List<LoSoubor> soubors = new ArrayList<LoSoubor>();
    for (final LoFaze loFaze : lofazes.values()) {
      soubors.addAll(loFaze.getSoubors());
    }
    Collections.sort(soubors);
    return soubors;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "LoDbkind [name=" + name + "]";
  }

  /**
   * @return the filesForSchemaCounter
   */
  public CounterMap<String> getFilesForSchemaCounter() {
    return filesForSchemaCounter;
  }

  /**
   * @return the filesForDbpacksCounter
   */
  public CounterMap<File> getFilesForDbpacksCounter() {
    return filesForDbpacksCounter;
  }

  /**
   * Vrátí seřazený seznam fází, které máme
   * @return
   */
  public SortedSet<LoFaze> getFazes() {
    return new TreeSet<LoFaze>(lofazes.values());
  }

}
