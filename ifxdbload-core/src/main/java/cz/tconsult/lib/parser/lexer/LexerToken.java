package cz.tconsult.lib.parser.lexer;


/**
 * Speciální token vracený lexerem. Veškerá volání {@link QuickSimpleLexer0#read} budou vracejí instanci tohoto objektu.
 * Volající si může vrácený token přetypovat, aby se dostal k údaji, který není na standarním tokenu k dispozici.
 * Tímto údajem je původní text tokenu, vracený metodou {@link #getText}.
 * @author Martin Veverka
 * @version 1.0
 */

public interface LexerToken {

  /**
   * Vrátí typ tokenu, je to hodnota definovaná metodu {@link QuickSimpleLexer#defineToken}
   * Vrací typ tokenu. Může to být objekt libovolného typu. Typicky to však bude  řetězec, číslo, či výčet.
   * Vracený typ musí správně implementovat toString, equals a hashCode.
   * @return Objekt reprezentující typ tokenu. Nemělo by to být nikdy null.
   */
  public Object getType();

  /**
   * Vrátí hodnotu tokenu, hodnota může být přímo zadaný řetězec, nebo se jedná o konvertovanou hodnotu.
   * Vrací hodnotu tokenu. Hodnotou může být libovolný typ. Typické jsou však řetězce, čísla,
   * datumy BigDecimaly a podobné "elementární typy". Další požadavky nejsou
   * @return Hodnota tokenu, může vrátit i null, to je také hodnota.
   */
  public Object getValue();

  /**
   * Vrátí infomraci o pozici. Vrací totéž co {@link #getLocator}, ale beztypově.
   * Vrací objekt nesoucí informace o pozici tokenu ve zdroji tokenů. Může to být třeba dvojice řádek sloupec,
   * pořadové číslo bytu v tokenu ze sítí atd. Objekt by měl mít metodu toString
   * implementovánu tak, aby vrátil něco smysluplného, co půjde zobrazit člověku a člověk bude schopen token nalézt.
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
