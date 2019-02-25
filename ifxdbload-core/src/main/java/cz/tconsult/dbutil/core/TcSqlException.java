/**
 * 
 */
package cz.tconsult.dbutil.core;

import java.sql.SQLException;

import cz.tconsult.tw.lang.FString;


/**
 * @author veverka
 *
 */
public abstract class TcSqlException extends SQLException {

  private static final long serialVersionUID = -4920186646922568592L;
  protected int iErrorOffset = -1;

  protected abstract String getVnitřekZprávy();

  protected String iSqlCommand;
  protected int iRowOffset;
  protected int iColOffset;
  protected int iRowBase;
  protected int iColBase;
  protected boolean iBazeNastavena;
  protected String iOriginalMessage;

  /**
   * @param aString a description of the exception
   * @param aSqlState XOPEN or SQL:2003 code identifying the exception
   * @param aErrorCode a database vendor-specific exception code
   */
  public TcSqlException(String aString, String aSqlState, int aErrorCode) {
    super(aString, aSqlState, aErrorCode);
  }

  /**
   * Vrátí zprávu.
   * @return Zpráva.
   */
  public String getMessage() {
    StringBuffer sb = new StringBuffer();
    sb.append("TCSQLERROR: ");
    sb.append(getVnitřekZprávy());
    sb.append(getDisplayErrorPosition());
    return sb.toString();
  }

  /**
   * Vrátí offset k místu v příkazu, který chybu způsobil.
   * @return Offset k příkazu, který chybu způsobil. Nula, pokud není znám, čili ukazuje na začátek příkazu. Pozice je počítána od nuly.
   */
  public int getErrorOffset() {
    return Math.max(iErrorOffset,0);
  }

  /**
   * Nastaví SQL příkaz, jehož provedení vedlo k této výjimce.
   * účelem je umožnit vypsat řádek a sloupec, kde doělo k chybě, protože informic
   * vrací jen relativní pozici od začátku příkazu a bez příkazu není tedy možné řádek a sloupec dopočítat.
   * Nastavením příkazu se okamžitě přepočítají hodnoty vracené metodami {@link getRow}, {@link getCol}, {@link getRowOffset} , {@link getColOffset}.
   * @param aSqlCommand Příkaz jenž byl zpracováván. Pokud je null, nestane se žádná akce.
   */
  public void setSqlCommand(String aSqlCommand) {
    iSqlCommand = aSqlCommand;
    computeRelativeRowAndCol();
  }

  /**
   * Vrátí nastavený SQL příkaz.
   * @return Příkaz nastavený metodou {@link setSqlCommand} nebbo v kosntoru. Pokud žádný příkaz nebyl nastaven, vrací null.
   */
  public String getSqlCommand() {
    return iSqlCommand;
  }

  /**
   * Vrátí nastavený SQL příkaz s vyznačením místa vzniklé chyby a s číslováním řádků odvozených od případné báze.
   * Protože je řetězec určen k vypsání, jsou řádky číáslovány od jedničky.
   * @return Příkaz nastavený metodou {@link setSqlCommand} nebbo v kosntoru. Pokud žádný příkaz nebyl nastaven, vrací null.
   */
  public String getSqlCommandWithMarkedErrors() {
    if (iSqlCommand == null) return null;
    StringBuffer sb = new StringBuffer();
    int radek = iRowBase;
    boolean novyradek = true;
    boolean usecr = false;
    int pozice = 0;
    int vzdalenostchyby = -1;
    String sql = iSqlCommand.endsWith("\n") ? iSqlCommand : iSqlCommand + "\n";
    for (int i = 0; i < sql.length(); i++) {
      if (i == iErrorOffset)  vzdalenostchyby = pozice; // uchovat zdálenost chyby, zjištuje se jediná
      pozice++;
      char c = sql.charAt(i);  // aktuální znak
      if (novyradek) {
        sb.append(FString.alignLeft((radek+1) + "", 5, ' ') + ": ");
        radek++;
        novyradek = false;
      }
      sb.append(c);  // ten znak vždy přidávám
      if (c == '\r') usecr = true;
      if (c == '\n') { // a pokud jsem ukončil řádek
        if (vzdalenostchyby >= 0) {
          sb.append("ERROR: ");
          for (int j = 0; j < vzdalenostchyby; j++) sb.append('.');
          sb.append("^ " );
          sb.append(getVnitřekZprávy());
          sb.append(" !!!!!!!!!!!!!!!!!!!!");
          if (usecr) sb.append('\r');
          sb.append('\n');
          vzdalenostchyby = -1; // nevypisovat vícekrát
        }
        novyradek = true;
        pozice = 0;
      }
    }
    return sb.toString();
  }

  /**
   * Vrátí číslo řádku, na kterém byla nalezena chyba. Řádky jsou počítány od nuly.
   * Pokud výjimka nemá k dispozici command, vrátí pořátek zpracovávaného příkazu,
   * pokud nebyla nastaven ani počáteční řádek příkazu, vrátí 0.
   * @return Číslo řádku, na němž byla nalezena chyba. Počítáno od nuly, nemůže vrátit záproné číslo.
   */
  public int getRow() {
    return Math.max(iRowBase + iRowOffset, 0);
  }

  /**
   * Vrátí číslo sloupce, na kterém byla nalezena chyba. Sloupce jsou počítány od nuly.
   * Pokud výjimka nemá k dispozici command, vrátí pořátek zpracovávaného příkazu,
   * pokud nebyla nastaven ani počáteční řádek příkazu, vrátí 0.
   * @return Číslo sloupce, na němž byla nalezena chyba. Počítáno od nuly, nemůže vrátit zápornéé číslo.
   */
  public int getCol() {
    return iRowOffset == 0 ? Math.max(iColBase + iColOffset, 0) : iColOffset;
  }

  /**
   * Vrátí číslo řádku v SQL příkazu, na níž byla nalezena chyba relativně k začátku příkazu.
   * Funguje vždy, bez ohledu na to, za má výjimka k dispozici command, či nikoli.
   * @return Pozice na řádku s chybou. Počítáno od nuly.
   */
  public int getRowOffset() {
    return iRowOffset;
  }

  /**
   * Vrátí pozici na řádku v SQL příkazu, na níž byla nalezena chyba relativně k začátku příkazu.
   * Funguje vždy, bez ohledu na to, za má výjimka k dispozici command, či nikoli.
   * @return Pozice na řádku s chybou. Počítáno od nuly.
   */
  public int getColOffset() {
    return iColOffset;
  }

  /**
   * Nastaví bázový řádek a pozici na tomto řádku, na němž se vyskytl zpracovávaný příkaz ve vstupním souboru.
   * Slouží pro účely výpisu čísla řádku s chybou. Pokud toto nastavení není provedeno ani nejsou zadány
   * hodnoty v kosntruktoru, nebude moci být ve výpisu chyby pozice správně uvedena.
   * @param aRowBase Číslo řádku počítáno od nuly.
   * @param aColBase Pozice na řádku počítaná od od nuly. Báze může mít i zápornou hodnotu, pokud klient požaduje příslušnou korekci.
   * Například v případě, kdy příkaz byl nějak upravován před předáním sem. Typický příklad je, kdy klient konstrukci "#PROCEDURE ahoj"
   * převádí na "CREATE PROCEDURE ahoj", parametrem pak bude -6.
   */
  public void setRowColBase(int aRowBase, int aColBase) {
    this.iRowBase = aRowBase;
    this.iColBase = aColBase;
    iBazeNastavena = true;
  }

  /**
   * Vrátí nastavené bázové číslo řádku.
   * @return Nastavené bázové číslo řádku. Pokud žádná báze nebyla nstavena, vrací nulu.
   */
  public int getRowBase() {
    return iRowBase;
  }

  /**
   * Vrátí nastavenou bázovou pozici na řádku.
   * @return Nastavená bázová pozice na řádku. Pokud žádná báze nebyla nstavena, vrací nulu.
   */
  public int getColBase() {
    return iColBase;
  }

  /**
   * Zjistí, zda byla explictině nastavena báze pozice chyby voláním {@link setRowColBase} nebo
   * kosntruktorem.
   * @return True, pokud báze byla nastavena. Bázi již nelze odnastavit.
   */
  public boolean isRowColBaseEntered() {return iBazeNastavena; }

  /**
   * Vrátí textovou reprezentaci pozice chyby.
   * @return Textová reprezentace pozice chyby, její tvar
   * závisí na informacích, jenž jsou k dispozici. Může vrátit prázdný
   * řetězec, pokud jsou informace nedostatečné. Veškeré pozice jsou od jedničky,
   * protože uživatelé i editory s tím takto počítají.
   * na rozdíl od funkcí, jenž vracejí pozice od nuly pro jedodušší programové zpracování.
   * <p>Předpokládejme, že ve zdrojovém souboru
   * na pozici (2000,10) začíná SQL příkaz v němž se vyskytla chyba
   * na řádku 15, pozice 7, offset od začátku příkazu je 356.
   * Pak výsledky budu následující.
   * </p>
   * <pre>
   *   Baze   Offset Prikaz     Výsledek
   *   ne     ne     ne         ""
   *   ne     ne     ano        ""
   *   ne     ano    ne         "offset 356"
   *   ne     ano    ano        "(15,7 relative)"
   *   ano    ne     ne         "(2000,10)"
   *   ano    ne     ano        "(2000,10)"
   *   ano    ano    ne         "(2000,10) offset 356"
   *   ano    ano    ano        "(2015,17)"
   * </pre>
   * V žádném případě se nelze spoléhat na výsledky této funkce, nějak je rozebárat
   * a podobně. Poskytnuté výslydky slouží pro zobrazení. Pokud potřebujete
   * vlastní zobrazení, volejte jednotlivé funkce vracející příslušné hodnoty:
   * {@link getRow}, {@link getCol}, {@link getErrorOffset}, {@link getSqlCommand},  {@link isRowColBaseEntered},
   * jak to dělá tato funkce.
   */
  public String getDisplayErrorPosition() {
    StringBuffer sb = new StringBuffer();
    if (isRowColBaseEntered()) { // pokud máme bázi nastavenu, určujeme poměrně přesně.
      sb.append(" [" + (getRow()+1) + "," + (getCol()+1) + "]");
      // pokud nemáme SQL příkaz, neznáme číslo řádku a sloucpe, tak pošleme aspon offset
      if (getErrorOffset() > 0 && getSqlCommand() == null) sb.append(" offset=" + (getErrorOffset()+1));
    } else { // ele pokud báze nastavena není, nemůžeme s přesnotí určit nic
      if (getErrorOffset() > 0) { // máme aspoň foffset, tak něco můžeme nastavit
        if (getSqlCommand() != null) { // máme SQL příkaz, můžeme určit relativně řádek a sloupec
          sb.append(" [" + (getRow()+1) + "," + (getCol()+1) + " relative]");
        } else {
          sb.append(" offset=" + (getErrorOffset()+1));
        }
      }
    }
    return sb.toString();
  }

  protected void computeRelativeRowAndCol() {
    iRowOffset = 0;
    iColOffset = 0;
    if (iSqlCommand == null) return;
    for (int i = 0; i < Math.min(iErrorOffset, iSqlCommand.length()); i++) {
      iColOffset ++;
      if (iSqlCommand.charAt(i) == '\n') { // jen pro Windows a Unix, žádné mekintoše
        iColOffset = 0;
        iRowOffset ++;
      }
    }
  }

  protected final String getSuperMessage() {
    return super.getMessage();
  }
}
