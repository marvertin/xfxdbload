package cz.tconsult.tw.util;

/**Rozhraní reprezentuje obecný token. Toto rrozhraní bylo vytvořeno proto, aby bylo použito v TokenInputStream.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface Token
{
  /**
   * Vrací typ tokenu. Může to být objekt libovolného typu. Typicky to však bude  řetězec, číslo, či výčet.
   * Vracený typ musí správně implementovat toString, equals a hashCode.
   * @return Objekt reprezentující typ tokenu. Nemělo by to být nikdy null.
   */
  public Object getType();

  /**
   * Vrací hodnotu tokenu. Hodnotou může být libovolný typ. Typické jsou však řetězce, čísla,
   * datumy BigDecimaly a podobné "elementární typy". Další požadavky nejsou
   * @return Hodnota tokenu, může vrátit i null, to je také hodnota.
   */
  public Object getValue();

  /**
   * Vrací objekt nesoucí informace o pozici tokenu ve zdroji tokenů. Může to být třeba dvojice řádek sloupec,
   * pořadové číslo bytu v tokenu ze sítí atd. Objekt by měl mít metodu toString
   * implementovánu tak, aby vrátil něco smysluplného, co půjde zobrazit člověku a člověk bude schopen token nalézt.
   * @return
   */
  public Object getSourcePositionInfo();
}
