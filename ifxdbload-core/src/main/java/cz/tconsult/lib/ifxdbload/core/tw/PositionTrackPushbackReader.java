package cz.tconsult.lib.ifxdbload.core.tw;

import java.io.IOException;
import java.io.PushbackReader;


/**
 * Následník PushBack readeru, který počítá sloupce a řádky a pozici.
 * <p>Za oddělovače řádků se považují samostatné znaky CR (ASCII 13) a LF (ASCII 10) a dvojice znaků CR LF.
 * Řádky a sloupce jsou počítány od nuly. Vracená hodnota čísla řádku i sloupce se vztahuje k následujícímu
 * znaku. Možná je však lépe si představit, že se vztahuje k mezeře mezi znaky s tím,
 * že pozice před prvním znakem je nulová.
 * V okamžiku, kdy je přečten znak CR nebo LF, přechází se na další řádek a na sloupec nula. Následné čtení
 * LF bězprostředně po R nemění ani číslo řádku ani číslo sloupce, ale mění pozici, do ní se započítávají
 * všechny znaky jednou.
 * <p>Operace unread zajistí, že jakmile se znaky vracené zpšět do bufferu přečtou, bude číslo řádku, sloupce i pozice
 * shodné před vyvoláním unread a to bez ohledu na počet vracených znaků. To se podaří vždy.
 * <p>Dálese sznaží chovat tak, aby se při operaci unread číslo řádku a sloupce dostalo do stavu před čtením.
 * To je však možné pouze v omezeném rozsahu, protože jinak by se musely pamatovat délky všech řádků a oddělovačů řádků.
 * Už vůbec to nelze zajistit, pokud budou dávány zpět jiné znaky, než byly čteny.
 * S číslem řádku je problém pouze při zpětném přechodu přes dvouznakový oddělovač řádků CRLF. Při čtení read() se číslo zvyšuje
 * ihned při přečtení CR, při vracení zpět se naopak číslo řádku snižuje již v okamžiku zpětného zápisu LF, čímž vzniká nekonzistence právě jen v mezi
 * znaky CR a LF oddělující řádek. Po zpětném zapsání druhého znaku se situace srovná.
 * <p>Horší je to však se číslem sloupce. Aby se zajistilo správné vracení sloupce, musely by se uchovávat délky všech řádků.
 * To je však zbytečné výkonostní omezění a v praxi to není potřeba. Uchovává se tedy délka bezprostředně předcházejícího řádku. Při zpětném
 * přechodu přes tento konec řádku se správně nastaví číslo řádku na konec a vkládáním dalších znaků se snižuje až k nule.
 * Při pokusu vracet zpět další znaky zůstává na nule. Pokud se však pokusím přejít zpět ještě o jeden řádek, bude sloupec vždy nula.
 * <p>Číslo řádku a sloupce je vhodné především k lokalizaci chyby ve vstupním textu. Pro automatizovanou editaci nebo jiné
 * účely je vhodnější používat pozici.
 * <p>Musíme si uvědomit, že ani číslo řádku a sloupce ani pozice nebudou mít vypovídající hodnotu, pokud zpět budou vraceny
 * jiné znaky než byly čteny. V tom případě musí doladit patřičné hodnoty sama apliakce pomocí set metod.
 * <p>Poznámka k výkonnosti: Veškeré metody pracující hromadně s poli museli být předělány tak, aby provolávali metody pracující s jedním znakem read a unread, čímž
 * můžebýt částečně snížena jejich výkonnost. Vadit by to ale nemělo, protože tato třída se používá nejčastěji v lexikálních analyzátorech, kde toi ení problém.

 * @author martin Veverka
 * @version 1.0
 */

public class PositionTrackPushbackReader extends PushbackReader {

  private static final int CR = 13;
  private static final int LF = 10;

  private final PushbackReader iReader;
  private final String iInputSourceName;

  private boolean iIgnorujDalsiLf;  // příznak, že jsem možná uvnitř CRLF.
  private boolean iIgnorujPredchoziCr;  // příznak, že jsem možná uvnitř CRLF.

  private int iRadek;
  private int iSloupec;
  private int iPozice;   // pozice znaku od počátku čtení z readeru (počítáno od 0)

  private int iDelkaMinulehoRadku;

  public PositionTrackPushbackReader(final PushbackReader aReader, final String aInputSourceName) {
    super(aReader); // musí být, ale nepoužije se, všechny metody jsou přepsány
    iReader =  aReader;
    iInputSourceName = aInputSourceName;
  }


  @Override
  public void unread(final int c) throws java.io.IOException {
    iReader.unread( c);
    iPozice --;
    iIgnorujDalsiLf = false;
    if (c < 0) {   // je to konec
    } else if (c == LF) {
      iRadek --;
      iSloupec = iDelkaMinulehoRadku;
      iDelkaMinulehoRadku = 0;
      iIgnorujPredchoziCr = true;
    } else if (c == CR) {
      if (! iIgnorujPredchoziCr) {
        iRadek --;
        iSloupec = iDelkaMinulehoRadku;
        iDelkaMinulehoRadku = 0;
      } else {
        iIgnorujPredchoziCr = false;
      }
    } else {  // běžný znak
      if (iSloupec > 0) {
        iSloupec --;
      }
      iIgnorujPredchoziCr = false;
    }
  }

  @Override
  public int read() throws java.io.IOException {
    final int c = iReader.read();
    iPozice ++;
    iIgnorujPredchoziCr = false;
    if (c < 0) {   // je to konec
    } else if (c == CR) {
      iRadek ++;
      iDelkaMinulehoRadku = iSloupec;
      iSloupec = 0;
      iIgnorujDalsiLf = true;
    } else if (c == LF) {
      if (! iIgnorujDalsiLf) {
        iRadek ++;
        iDelkaMinulehoRadku = iSloupec;
        iSloupec = 0;
      } else {
        iIgnorujDalsiLf = false;
      }
    } else { // běžný znak
      iSloupec++;
      iIgnorujDalsiLf = false;
    }
    return c;  // a vždy vrátíme předchozí znak
  }

  ////////// Metody, jenž bylo nutné předefinovat, aby pracovali s jednoduchou metodu
  @Override
  public int read(final char[] aZnaky, final int aOdkud, final int aDelka) throws java.io.IOException {
    int c = read();
    if (c < 0)
    {
      return -1; // nepodařilo se načíst
    }
    int pocet = 0;
    for (int i = aOdkud; i < aOdkud + aDelka; i++) {
      pocet ++;
      aZnaky[i] = (char)c;
      c = read();
      if (c < 0)
      {
        break;  // konec souboru, ale něco jsme načetli
      }
    }
    return pocet;

  }

  @Override
  public void unread(final char[] aZnaky, final int aOdkud, final int aDelka) throws java.io.IOException {
    for (int i = aOdkud + aDelka - 1; i>=aOdkud; i--) {
      unread(aZnaky[i]);
    }
  }


  @Override
  public int read(final char[] aZnaky) throws java.io.IOException {
    return read(aZnaky, 0, aZnaky.length);
  }

  @Override
  public void unread(final char[] aZnaky) throws java.io.IOException {
    unread(aZnaky,0, aZnaky.length);
  }

  @Override
  public long skip(final long n) throws java.io.IOException {
    for (int i=0; i<n; i++)
    {
      if (read() < 0)
      {
        return i;  // pokud jsme na konci, vratme co jsme přečetli
      }
    }
    return n;

  }

  //////////// Nové metody určené pro práci s pozicí

  /**
   * Nastaví na nulu číslo, řádku, číslo sloupce a pozici.
   * Přitom nijak nemanipuluje s daty v proudu, pouze nastaví, jakoby
   * proud šel odtud.
   */
  public void clearAlPositions() {
    iRadek = 0;
    iSloupec = 0;
    iPozice = 0;
  }

  /**
   * Nastaví číslo řádku.
   * @param line Číslo řádku počítané od nuly.
   */
  public void setLineNumber(final int line) {
    this.iRadek = line;
  }


  /**
   * Vrací číslo řádku.
   * @return Číslo řádku počítané od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků než přečteno.
   */
  public int getLineNumber() {
    return iRadek;
  }

  /**
   * Nastaví číslo sloupce.
   * @param column Číslo sloupce počítané od nuly. Pokud je menší než nula, nastaví se nula.
   */
  public void setColumnNumber(final int column) {
    this.iSloupec = column < 0 ? 0 : column;
  }

  /**
   * Vrátí číslo sloupec.
   * @return Číslo sloupce počítané od nuly v aktuálním řádku. Nikdy nevrací zápornou hodnotu.
   */
  public int getColumnNumber() {
    return iSloupec;
  }

  /**
   * Nastaví pozici v proudu. Metoda nemění skutečnou pozici, pouze nastaví,
   * co bude vracet metoda getPosition.
   * @param position Poczice v proudu.
   */
  public void setPosition(final int position) {
    this.iPozice = position;
  }

  /**
   * Vrátí pozici proudu.
   * @return Pozice v proudu, počítána od nuly. Může vrátit zápornou hodnotu, pokud bylo vráceno zpět více znaků, než bylo přečteno.
   */
  public int getPosition() {
    return iPozice;
  }


  /**
   * Vrátí jméno zdroje proudu. To je jméno zadané při tvorbě objektu.
   * @return
   */
  public String getInputSourceName() {
    return iInputSourceName;
  }

  /**
   * Vrátí aktuální pozici v proudu jako text. V textu je jméno zdroje, číslo řádku a číslo sloupce,
   * není tam fyzická pozice v souboru. Text slozuží pro zobrazení pozice, například při detekci chyby.
   * Text se může kdykoli změnit, v žádném případě nesmí být parserován a nějak interpretován.
   * @return Informaci o pozici jako text, včetně zdroje.
   */
  public String getPositionAsText() {
    return '"' + iInputSourceName + "\" [" + getLineNumber() + "," + getColumnNumber() + "]";
  }


  /// Jednoduše přehozené metody
  @Override
  public void close() throws java.io.IOException {
    iReader.close();
  }

  @Override
  public boolean ready() throws java.io.IOException {
    return iReader.ready();
  }

  @Override
  public void mark(final int readAheadLimit) throws java.io.IOException {
    iReader.mark( readAheadLimit); // vyhodí výjimku
  }

  @Override
  public boolean markSupported() {
    return iReader.markSupported();  // vrátí false
  }

  @Override
  public void reset() throws java.io.IOException {
    iReader.reset();  // vyhodí výjimku
  }


  @Override
  public boolean equals(final Object parm1) {
    return iReader.equals( parm1);
  }

  @Override
  public String toString() {
    return iReader.toString();
  }

  @Override
  public int hashCode() {
    return iReader.hashCode();
  }

  public String readLine() throws IOException {

    final StringBuffer result = new StringBuffer();
    for (;;) {

      int c = read();
      if (c < 0) {

        if (result.length() > 0) {
          return result.toString();
        }
        else {

          return null;
        }
      }

      boolean eor = false;
      if (c == CR) {

        eor = true;
        c = read();
        if (c != LF) {unread(c);}
      }
      else if (c == LF) {

        eor = true;
      }

      if (eor) {return result.toString();}
      result.append(Character.valueOf((char)c));
    }//end of for (;;) {
  }

}
