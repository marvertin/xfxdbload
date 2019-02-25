package cz.tconsult.dbutil.informix;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import cz.tconsult.dbutil.core.TcSqlException;

/**
 * Výjmka Informixí Sql Chyby nahrazující SQLException.
 * Výjimka je vytvářena proto, že Informixí drivery velmi nedostatečně
 * prezentuje chybové informace, jenž jsou k dispozici.
 * Výjimka je potomkem SQLException, takže po přehození
 * s ní lze pracovat zcela normálně jako s každou jinou SQLException
 * s tím rozdílem, že jsou k dispozici podrobné údaje.
 * <p>Typické použití je zde. Tučně jsou vyznačeny korespondující hodnoty,
 * máli být správně vyhodnocována pozice chyby.
 *
 * <pre>
 *     Connection conn = ...
 *     String command = "UPDATE SYSPROCBODYX\r\n SET \r\nnesloupec =  neexistujicisloupec";
 *     try {
 *         conn = connect("veve1@orcela");
 *         <b>conn</b>.createStatement().execute(<b>command</b>);
 *     } catch (SQLException ex) {
 *         throw new IfxSqlException(ex, <b>conn</b>, <b>command</b>, 420, 38); // ta čísla jsou pozice začátku příkazu ve vstupním souboru
 *     }
 *</pre>
 * @author Martin Veverka
 * @version 1.0
 */

public class IfxSqlException extends TcSqlException {

  static final long serialVersionUID = 2872295406582246454L;

  private Connection iConnection;
  private int isam;
  /**
   * Vytvoří výjimku umožňující poskytnou pořádné informace o Informixí chybě.
   * @param ex SQL výjimka vyhozená nejlépe z databázového stroje Informix. Výjimka není
   * přehozena přes tuto výjimku, ale ifnormace z této výjimky jsou vycucány a vloženy do této výjimky,
   * čímž se zabrání zbytečnému natahování výjimkového řetězce.
   * @param aConn Připojení k databázi. Pokud je null, nebude možné určit pozici chyby.
   * @param aErroringSqlCommand Chybující SQL příkaz, pokud není nastaven, nebude možné
   * určit číslo řádku a sloupce s chybou.
   * @param aRowBase Číslo řádku v souboru, na němž začíná SQL přkaz. Počítáno od nuly.
   * @param aColBase Pozice na řádku v souboru, na němž začíná SQL příkaz.
   */
  public IfxSqlException(final SQLException ex, final Connection aConn, final String aErroringSqlCommand, final int aRowBase, final int aColBase) {
    this(ex, aConn, aErroringSqlCommand);
    setRowColBase(aRowBase, aColBase);
  }

  /**
   * Vytvoří výjimku umožňující poskytnou pořádné informace o Informixí chybě.
   * @param ex SQL výjimka vyhozená nejlépe z databázového stroje Informix. Výjimka není
   * přehozena přes tuto výjimku, ale ifnormace z této výjimky jsou vycucány a vloženy do této výjimky,
   * čímž se zabrání zbytečnému natahování výjimkového řetězce.
   * @param aConn Připojení k databázi. Pokud je null, nebude možné určit pozici chyby.
   * @param aErroringSqlCommand Chybující SQL příkaz, pokud není nastaven, nebude možné
   * určit číslo řádku a sloupce s chybou.
   */
  public IfxSqlException(final SQLException ex, final Connection aConn, final String aErroringSqlCommand) {
    this(ex, aConn);
    setSqlCommand(aErroringSqlCommand);
  }


  /**
   * Vytvoří výjimku umožňující poskytnou pořádné informace o Informixí chybě.
   * @param ex SQL výjimka vyhozená nejlépe z databázového stroje Informix. Výjimka není
   * přehozena přes tuto výjimku, ale ifnormace z této výjimky jsou vycucány a vloženy do této výjimky,
   * čímž se zabrání zbytečnému natahování výjimkového řetězce.
   * @param aConn Připojení k databázi. Pokud je null, nebude možné určit pozici chyby.
   */
  public IfxSqlException(final SQLException ex, final Connection aConn) {
    this(ex);
    setConnection(aConn);
  }


  /*
  private static String mesiga(SQLException ex) {
    if (ex instanceof IfxSqlException) {
       return  ((IfxSqlException)ex).iOriginalMessage;  //su "XXXXXXX AAAAAAAA";
    } else return ex.getMessage();
  }
   */
  /**
   * Vytvoří výjimku umožňující poskytnou pořádné informace o Informixí chybě.
   * @param ex SQL výjimka vyhozená nejlépe z databázového stroje Informix. Výjimka není
   * přehozena přes tuto výjimku, ale ifnormace z této výjimky jsou vycucány a vloženy do této výjimky,
   * čímž se zabrání zbytečnému natahování výjimkového řetězce.
   */
  public IfxSqlException(final SQLException ex) {
    super(ex instanceof IfxSqlException ? ((IfxSqlException)ex).iOriginalMessage
        : ex.getMessage(),
        ex.getSQLState(), ex.getErrorCode());
    iOriginalMessage = ex.getMessage(); // originální zprávu zachovat pro případ přehození ještě jendné výjimky
    // řetězece výjimek zkrátit od přehazovanou výjimku,
    // aby nebylo poznat, že je přehozena další výjima
    super.initCause(ex);
    super.setNextException(ex.getNextException());
    if (ex instanceof IfxSqlException) {
      natahniHodnotySemZJineVyjimky((IfxSqlException)ex);
    }
  }

  /**
   * Vrátí zprávu.
   * @return Zpráva.
   */
  @Override
  protected String getVnitřekZprávy() {
    final StringBuffer sb = new StringBuffer();
    int poradi = 0;
    for (SQLException ex = this; ex != null; ex = ex.getNextException()) {
      final String messsage = ex == this ? super.getSuperMessage() : ex.getMessage(); // zabránit nekonečné rekurzi
      sb.append(ex.getErrorCode() + ": " + messsage);
      //       if (ex.getNextException() != null) sb.append(System.getProperty("line.separator"));
      if (ex.getNextException() != null) {
        sb.append(' ');
      }
      if (poradi == 1) {
        isam = ex.getErrorCode(); // schovat ISAM kód
      }
      poradi++;
    }

    //final String connstr = IfxConnectionFactory.findConnectString(iConnection);
    // TODO získat skutečný connection String nebo aspoň něco
    final String connstr = null;
    if (connstr != null) { // pokud se podaří naléze conenct string, přidáme ho
      sb.append(" \"");
      sb.append(connstr);
      sb.append("\"");
    }
    return sb.toString();
  }

  /**
   * Nastaví připojení k databázi. Připojení je potřeba proto, že Informixové
   * nesprávně navrhli vracení informací o chybách. Pozici v SQL příkazu
   * u poslední vhyby mají na objektu připojení a nikoli v oběktu výjimky.
   * Pokud připojení nebude výjimce sděleno, nemůže výjimka vypisovat přesné určení pozice chyby.
   * Metoda nastaví připojení jen jednou a již v okamžiku vyvolání spočte pozcie.
   * Je to proto, že druhé a další vyvolání může klient udělat pozdě, kdy již byl
   * proveden jiný příkaz a pozice chyby není k dispizici.
   *
   * @param aConn Připojení k databázi. Pokud je null, nebude možné určit pozici chyby.
   */
  public void setConnection(final Connection aConn) {
    iConnection = aConn;

    if (iErrorOffset >= 0) {
      return;
    } // offset už byl nstaven
    if (aConn == null) {return;}
    final Class<?> clz = aConn.getClass();

    // špatná isntace ?
    if (!clz.getName().equals("com.informix.jdbc.IfxConnection")) {return;}

    try {
      final Method m = clz.getMethod("getSQLStatementOffset");
      final Object methodResult = m.invoke(aConn);

      // nastavit offfset
      iErrorOffset = Math.max(0, (Integer)methodResult - 1); // vypadá to, že je tam od jedničky
    }
    catch (final Exception e) {

      //never?
      throw new RuntimeException(e);
    }
    computeRelativeRowAndCol();
  }

  /**
   * Vrátí nastavené připojení k databázi.
   * @return Připojení k databázi nastavené metodou {@link setConnection} nebo konstruktorem.
   */
  public Connection getConnection() { return iConnection; }

  /**
   * Vrátí ISAM kód informuxi nebo 0, pokud pro danou chybu není ISAM.
   * @return ISAM kód informuxi nebo 0, pokud pro danou chybu není ISAM
   */
  public int getIsam() {
    return isam;
  }

  // pro přehazování přes sebe pořád dokola
  private void natahniHodnotySemZJineVyjimky(final IfxSqlException ex) {
    iConnection = ex.iConnection;
    iErrorOffset = ex.iErrorOffset;
    iSqlCommand = ex.iSqlCommand;
    iRowOffset = ex.iRowOffset;
    iColOffset = ex.iColOffset;
    iRowBase = ex.iRowBase;
    iColBase = ex.iColBase;
    iBazeNastavena = ex.iBazeNastavena;
    iOriginalMessage = ex.iOriginalMessage;
    isam = ex.isam;
  }

}
