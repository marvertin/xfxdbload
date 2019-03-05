package cz.tconsult.parser.lexer;

import cz.tconsult.tw.util.Token;

/**
 * Speciální token vracený lexerem. Veškerá volání {@link QuickSimpleLexer0#read} budou vracejí instanci tohoto objektu.
* Volající si může vrácený token přetypovat, aby se dostal k údaji, který není na standarním tokenu k dispozici.
* Tímto údajem je původní text tokenu, vracený metodou {@link #getText}.
 * @author Martin Veverka
 * @version 1.0
 */

public interface LexerToken extends Token {

  /**
   * Vrátí typ tokenu, je to hodnota definovaná metodu {@link QuickSimpleLexer#defineToken}
   * @return Typ tokenu.
   */
  public Object getType();

  /**
   * Vrátí hodnotu tokenu, hodnota může být přímo zadaný řetězec, nebo se jedná o konvertovanou hodnotu.
   * @return Hodnota tokenu.
   */
  public Object getValue();


  /**
   * Vrátí infomraci o pozici. Vrací totéž co {@link #getLocator}, ale beztypově.
   * @return Vrácená hodnota je typu {@link LexerTokenLocator}, nikdy nevrací null.
   */
  public Object getSourcePositionInfo();

  /**
   * Vrátí token locator. Vracet totéž co {@link #getSourcePositionInfo}, ale typově.
   * @return Vrácená hodnota je typu {@link LexerTokenLocator}, nikdy nevrací null.
   */
  public LexerTokenLocator getLocator();

  /**
   * Vrátí vždy původní nekonvertovaný text token, jenž byl přečten ze vstupního proudu.
   * @return Text tokenu.
   */
  public String getText();


}
