package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

/**
 * Kešuje získanou hodnotu ze supliera.
 * @author veverka
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class CachingSupplierWrapper<T> implements Supplier<T> {

  private final Supplier<T> wraped;

  private T cachedContent;
  private boolean jizByloZiskano = false;

  /**
   * Získá hodnotu, ale zakešuje ji pro případné získání znovu.
   */
  @Override
  public synchronized T get() {
    if (! jizByloZiskano) {
      cachedContent = wraped.get();
      jizByloZiskano = true;
    }
    return cachedContent;
  }

}
