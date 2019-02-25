package cz.tconsult.lib.ifxdbload.core.once.clientsupport;

/**
 * Třída podporující jednu stranu rozhraní OnceLoader-volaný Java program, a to stranu volanou
 */
public class FOnceLoaderClient {

  /**
   * Vrátí prostředí pro OnceLoader klienta.
   * @param aArgs Parametry příkazového řádku, přijaté např. v metodě main
   * @return
   * @throws RuntimeException Pokud se nepodaří připojit k databázi.
   */
  public static OnceLoaderClientEnvironment getEnvironment(final String[] aArgs) {
    return OnceLoaderClientEnvironment.create(aArgs);
  }
}

