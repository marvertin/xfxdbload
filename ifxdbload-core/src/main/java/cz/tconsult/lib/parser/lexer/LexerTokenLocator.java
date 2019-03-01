package cz.tconsult.lib.parser.lexer;


/**
 * Třída reprezentující pozici tokenu ve vstupním proudu. Instance vznbiká
 * zvlášt pro každý token, pokud není na lexeru nastaveno jinak. Token si nese
 * infomraci s sebou a lze ji zjistit pomocí  {@link cz.tconsult.tw.util.Token#getSourcePositionInfo}
 * @author Martin Veverka
 * @version 1.0
 */

public interface LexerTokenLocator  {

  /**
   * Vrací číslo řádku, kde začíná token.
   * @return Číslo řádku počítané od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků než přečteno.
   */
  public int getBegLineNumber();

  /**
   * Vrátí číslo sloupec, kde začíná token.
   * @return Číslo sloupce počítané od nuly v aktuálním řádku. Nikdy nevrací zápornou hodnotu.
   */
  public int getBegColumnNumber();

  /**
   * Vrátí pozici proudu, kde začíná token.
   * @return Pozice v proudu, počítána od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků, než bylo přečteno.
   */
  public int getBegPosition();

  /**
   * Vrací číslo řádku, kde končí token.
   * @return Číslo řádku počítané od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků než přečteno.
   */
  public int getEndLineNumber();

  /**
   * Vrátí číslo sloupec, kde končí token.
   * @return Číslo sloupce počítané od nuly v aktuálním řádku. Nikdy nevrací zápornou hodnotu.
   */
  public int getEndColumnNumber();

  /**
   * Vrátí pozici proudu, kde končí token.
   * @return Pozice v proudu, počítána od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků, než bylo přečteno.
   */
  public int getEndPosition();

  /**
   * Vrátí jméno zdroje proudu. To je jméno zadané při tvorbě objektu.
   * @return Jméno zdrojového souboru, z nějž jsou tokeny čteny, pokud jsou čteny ze souboru.
   * Nebo to může být celé URL, pokud jsou čteny z URL.
   */
  public String getInputSourceName();

}
