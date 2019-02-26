/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.nio.file.Path;
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

  private  final LoData lodata;
  private final ADbkind name;

  private final SortedMap<EFazeZavedeni, LoFaze> lofazes = new TreeMap<EFazeZavedeni, LoFaze>();

  private final CounterMap<ASchema> filesForSchemaCounter = new CCounterMap<ASchema>(); // TODO [veverka] byl zde case insentsitive -- 26. 2. 2019 9:26:46 veverka
  private final CounterMap<Path> filesForDbpacksCounter = new CCounterMap<Path>(FILE_Comparator);
  private final CounterMap<String> filesForSchemasAndPhases = new CCounterMap<String>(String.CASE_INSENSITIVE_ORDER);
  private final CounterMap<EFazeZavedeni> filesForPhases = new CCounterMap<EFazeZavedeni>(NAME_Comparator);


  private static final Comparator<EFazeZavedeni> NAME_Comparator = new Comparator<EFazeZavedeni>() {

    @Override
    public int compare(final EFazeZavedeni o1, final EFazeZavedeni o2) {

      return o1.name().compareTo(o2.name());
    }

  };

  private static final Comparator<Path> FILE_Comparator = new Comparator<Path>() {

    @Override
    public int compare(final Path o1, final Path o2) {

      int result;
      if (o1 == null) {

        result = o2 == null ? 0 : 1;
      }
      else if (o2 == null) {

        result = -1;
      }
      else {

        final String a = o1.toAbsolutePath().toString();
        final String b = o2.toAbsolutePath().toString();
        result = String.CASE_INSENSITIVE_ORDER.compare(a, b);
      }
      return result;
    }

  };

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
  public ADbkind getName() {
    return name;
  }



  /**
   * @param aLodata
   * @param aName
   */
  public LoDbkind(final LoData aLodata, final ADbkind aName) {
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
  public CounterMap<ASchema> getFilesForSchemaCounter() {
    return filesForSchemaCounter;
  }

  /**
   * @return the filesForDbpacksCounter
   */
  public CounterMap<Path> getFilesForDbpacksCounter() {
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
