package cz.tconsult.tw.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Vůbec se v nástrojích neřešilo například kódování souborů zdrojových kódů což byla chyba, tak v tichosti se předpokládalo win-1250 (respektive defaultní charset).
 * Od jistého bodu v budoucnu to bude výhradně UTF-8. Po přechodnou dobu ale je nutno umět podporovat obojí. Abychom se vyhnuli mohutným reimplementacím, tak
 * si vypomůžeme trochu humusem - zavedeme si kontext ve formě statické proměnné, kam bude moct každý jednotlivý nástroj specifikovat chtěný charset.
 * Představuju si to tak, že implicitně to bude UTF-8. A nástroj, když mu operátor zadá "--backward-compatibility", tak do property nastaví win-1250.[polakm;2015-05-15 17:51:23]
 *
 * S přechodem posledního projektu do UTF-8 (do GITu) tuto třídu zase zahubme.
 *
 * @author polakm
 *
 */
public class TcSourceCodeInfo {
  // TODO [veverka] Prověřit, co je to za zhůvěřilost -- 25. 2. 2019 13:17:21 veverka

  private static Charset tc_sourcecode_charset = StandardCharsets.UTF_8;

  private static String tc_sourcecode_charsetName_backwardCompatibility = "windows-1250";
  private static Charset tc_sourcecode_charset_backwardCompatibility = Charset.forName(tc_sourcecode_charsetName_backwardCompatibility);

  /**
   * Pokud nástroj potřebuje podporovat zpětnou kompatibilitu zdrojových kódů - např. proto, že pracuje s dosud nepřevedenou repozitoří zdrojových kódů,
   * stačí mu zavolat tuto metodu - bude nastaveno windows-1250 jako kódování zsdrojových kódů naprosto globálně
   */
  public static void TC_SOURCECODE_BACKWARDCOMPATIBILITY() {

    TC_SOURCECODE_CHARSET(tc_sourcecode_charsetName_backwardCompatibility);
  }

  /**
   * Metoda, která nastaví kódování souborů zdrojových kódů naprosto globálně.
   *
   * @param aCharset
   */
  public static void TC_SOURCECODE_CHARSET(final String aCharset) {

    tc_sourcecode_charset = Charset.forName(aCharset);
  }

  public static String getDefaultCharsetName() {

    final String result = getDefaultCharset().name();
    return result;
  }

  public static Charset getDefaultCharset() {

    Charset result = tc_sourcecode_charset;
    if (result == null) {

      result = Charset.defaultCharset();
    }
    return result;
  }

  public static String getCharsetNameBackwardCompatibility() {

    return tc_sourcecode_charsetName_backwardCompatibility;
  }

  public static Charset getCharsetBackwardCompatibility() {

    return tc_sourcecode_charset_backwardCompatibility;
  }
}
