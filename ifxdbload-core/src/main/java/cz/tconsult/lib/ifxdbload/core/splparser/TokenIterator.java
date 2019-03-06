package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.List;

import lombok.ToString;

/**
 * Iterátor, který se dívá vždy dopředu a je tedy možné získat aktuální hodnotu bez posunu.
 * Dokáže udělat svoji kopii, která bude iterovat také.
 * Nemůže tedy pracovat nad prázdným seznamem.
 * @author veverka
 *
 * @param <T>
 */
@ToString
class TokenIterator<T> {

  private int nextIndex = 0;

  private final List<T> source;

  /**
   * Zřídí iterátor, tak, že aktuální hdonota bude ta první. Pokud však byl iterátor prázdný, bude to ta koncová.
   * @param source
   * @param endItem
   */
  public TokenIterator(final List<T> source) {
    this.source = source;
  }

  /**
   * Aktuální hodnota. Může to být i koncová hodnota, pokud byl iterátor vyčerpán.
   * @return
   */
  public T get() {
    assert ! isAtEnd();
    return source.get(nextIndex);
  }

  /**
   * Posune akuální hodnotu na následující. Pokdu není kam pospouvat, tak už navždy bude current() vracet tu koncovou hodnotu.
   * @return Token, kerý by vrátil getNext(), před voláním next()
   */
  public T shift() {
    final T next = get();
    nextIndex ++;
    return next;
  }

  public TokenIterator<T> copy() {
    final TokenIterator<T> kopie = new TokenIterator<>(source);
    kopie.nextIndex = nextIndex;
    return kopie;
  }

  /**
   * Zjistit, zda je na konci
   * @return
   */
  public boolean isAtEnd() {
    return nextIndex >= source.size(); // nemůže být větší, ale raději
  }


}
