package cz.tconsult.tw.util;

import cz.tconsult.parser.lexer.LexerToken;

/**Rozhraní reprezentuje vstupní tok tokenů. Umožnuje jednou přečtený token vrátit zpět.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface TokenInputStream
{
  /**
   * Vrátí následující token v toku tokenů. Pokud byl vracen token metodou
   * unread musí nejdříve vrátit tyto tokeny v odpovídajícím pořadí.
   * @return Následující token. Pokud již ve streamu není žádný token vrací null. Pokud
   * jednou vrátí null, již nemůže vráti nic jiného než null. Na token čeká.
   */
  public LexerToken read();

  /**
   * Vrátí token zpět do streamu. Následující volání read() musí opět vrátit tyto tokeny.
   * @param aToken Vracený token. Musí to být token získaný metodou read(), ve všech ostatních případech
   *  není činnost definována. Pokud je null, neprovede se žádná akce.
   */
  public void unread(LexerToken aToken);

  /**
   * Zjistí, zda je stream reedy pro dodávání tokenů. Metoda se použije v případech,
   * kdy čteme tokeny z nějakého asynchronního zdroje (Message queue atd.) a nechceme
   * blokovat vlákno, než bude token k dispozici.
   * @return True, pokud následné volání read() nebude blokováno komunikací mezi procesy. False,
   * pokud by k blokobání došlo. Pokud se již ví, že je stream vyčerpán, vrací true, aby se mohlo
   * použít read a přečíst null. Tato metoda se nesmí používat pro zjištění, že je konec streamu.
   */
  public boolean isReady();
}
