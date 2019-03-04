package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.Iterator;

/**
 * Iterátor, který se dívá vždy dopředu a je tedy možné získat aktuální hodnotu bez posunu.
 * Konec streamu je identifikován speciální hodnotou, která je vracena vždy, když je iterátor vyčerpán.
 * @author veverka
 *
 * @param <T>
 */
public class LookAheadIterator<T> {

  private final T endItem;

  private final Iterator<T> iterator;

  private T currentItem;


  /**
   * Zřídí iterátor, tak, že aktuální hdonota bude ta první. Pokud však byl iterátor prázdný, bude to ta koncová.
   * @param source
   * @param endItem
   */
  public LookAheadIterator(final Iterable<T> source, final T endItem) {
    this.endItem = endItem;
    iterator = source.iterator();
    goNext(); // už nazačátk use musíme iterátorem posunout, abychom měli aktuální token, proto také nepřijímáme iterátor, ale raději iterable
  }


  /**
   * Zřídí iterátor, tak, že aktuální hdonota bude ta první. Pokud však byl iterátor prázdný, vypadne výjimka.
   * Iterátor nesmí být vyčerpán, jinak skončí s výjimkou.
   * @param source
   * @param endItem
   */
  public LookAheadIterator(final Iterable<T> source) {
    this.endItem = null;
    iterator = source.iterator();
    goNext(); // už nazačátk use musíme iterátorem posunout, abychom měli aktuální token, proto také nepřijímáme iterátor, ale raději iterable
  }

  /**
   * Aktuální hodnota. Může to být i koncová hodnota, pokud byl iterátor vyčerpán.
   * @return
   */
  public T current() {
    return currentItem;
  }

  /**
   * Posune akuální hodnotu na následující. Pokdu není kam pospouvat, tak už navždy bude current() vracet tu koncovou hodnotu.
   */
  public void goNext() {
    if (iterator.hasNext()) {
      currentItem = iterator.next();
    } else {
      if (endItem == null) {
        throw new IllegalStateException("Iterator je vycerpan, neni definovan koncovy item a presto je snaha jit dal");
      } else {
        currentItem = endItem;
      }
    }
  }
}
