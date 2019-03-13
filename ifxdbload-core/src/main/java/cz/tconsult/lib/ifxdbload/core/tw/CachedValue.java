package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

/**
 * Drží hodnotu určitého v keši. Dodá ji při první potřebě. Nemůže držet null;
 * @author veverka
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class CachedValue<T> {

  private final Supplier<T> supplier;

  private T value;

  /**
   * Získá hodnotu.
   * Nesynchronizuejeme, přinejhorším se spočítá vícekrát, ale protože je to keš, musí být funcke taková, aby se spočítala stejná hodntoa.
   * @return
   */
  public T get() {
    if (value == null) {
      value = supplier.get();
    }
    return value;
  }
}
