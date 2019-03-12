package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class F8 {

  /**
   * Zpracuje výsledek stream.groupingBy( predicate ), kde vznikne nesmyslně mapa s klíčem boolean
   * a s neexiostujícíma hodnotama, místo prázdných seznamů.
   *
   * @param map
   * @return
   */
  public static <T> Partitioned<T> postPartition(final Map<Boolean, List<T>> map) {
    return new Partitioned<>(
        map.getOrDefault(true, Collections.emptyList()),
        map.getOrDefault(false, Collections.emptyList()));
  }
}
