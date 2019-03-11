/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;
import cz.tconsult.tw.util.CCounterMap;
import cz.tconsult.tw.util.CounterMap;
import lombok.RequiredArgsConstructor;

/**
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class LoDbkind {

  private final ADbkind name;
  private final LoData lodata;

  private final EnumMap<EFazeZavedeni, LoFaze> lofazes
  = new EnumMap<>(Arrays.stream(EFazeZavedeni.values())
      .collect(Collectors.toMap(Function.identity() , faze -> new LoFaze(this, faze))));

  // TODO [veverka] povyhazovat country odtud -- 28. 2. 2019 13:37:29 veverka
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
   * @return the filesForDbpacksCounter
   */
  public CounterMap<Path> getFilesForDbpacksCounter() {
    return filesForDbpacksCounter;
  }

  /**
   * Vrátí seřazený seznam fází, které máme
   * @return
   */
  public SortedSet<LoFaze> getFazesDeprecated() {
    return new TreeSet<LoFaze>(lofazes.values());
  }
  public LoFaze getFaze(final EFazeZavedeni faze) {
    return lofazes.get(faze);
  }

  public LoData getLodata() {
    return lodata;
  }

}
