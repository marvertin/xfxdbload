package cz.tconsult.parser.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cz.tconsult.lib.ifxdbload.core.tw.PositionTrackPushbackReader;
import cz.tconsult.parser.lexer.automaton.Automaton;
import cz.tconsult.parser.lexer.automaton.RegExp;
import cz.tconsult.parser.lexer.automaton.RunAutomaton;
import cz.tconsult.parser.lexer.automaton.State;

/**
 * Rychlý jednoduchý lexikální analyzátor postavený na definici tokenů pomocí regulárních výrazů.
 * Zatím veškeré lexikální analyzátory, které jsem našel jsou založeny na externích definicích
 * a generátorech lexerů. To může být dost pracné a v mnoha případech zbytečné. Některé z nich
 * jako například JFlex a JLex si myslí, že jsou páni světa a kradou znaky. (Je to zajímavé s ohledem
 * na stařičký unixový lex, dle něhož se prý inspirují - ten nekradl.
 * <p>K definice lexeru není potřeba žádný externí textový soubor ani předkompilátor, vše se definuje
 * přímo v Javě. Je možné udělat následníka této třídy a v něm konkrétní lexer nadefinovat, nebo je možné
 * použít předpřipravenéhop potomka {@link QuickSimpleLexer} a nadefinovat lexer bez nutnosti tvořit třídu.
 * <p>Objekt lexeru se může nacházet v jedné ze dvou fází. Ve fázi definice, v níž je od svého vzniku
 * a ve fázi lexování, do níž se dostane voláním compile. V každé fázi lze používat jiné metody.
 * Příklad: Nadefinujme jednoduchý lexer tak, že ho vytvoříme v kosntruktoru.
 *
 * <p><pre>
 * import cz.tconsult.parser.lexer.*
 *
 * public class TestLexerJakoPotomek extends QuickSimpleLexer0 {
 *   public TestLexerJakoPotomek() {
 *      defineToken("BEGIN",                "KWD_BEGIN");
 *      defineToken("END",                  "KWD_END");
 *      defineToken("[Ll][Ee][Tt]",         "KWD_LET");
 *      defineToken("0x[0-9a-fA-F]+",       "HEXACISLO");
 *      defineToken("[a-zA-Z][a-zA-Z0-9]*", "IDENTIFIKATOR");
 *      defineToken("[0-9]+",           "INTEGER");
 *      defineToken("[0-9]+\\.[0-9]+(e([\\+\\-])?[0-9])?", "REAL");
 *      defineToken("+|-|\\*|/",        "OPERATOR");
 *      defineIgnoredToken("[\t\r\n ]+");  // bílé znaky se ignortují
 *      setErrorToken("ERROR");
 *      compile();
 *  }
 *}

</pre>
<p>Lexer pak použijeme v jiném kódu takto:
<pre>
 *    import cz.tconsult.tw.util.PositionTrackPushbackReader;
 *    import cz.tconsult.tw.util.TokenInputStream
 *    import cz.tconsult.tw.util.Token;
 *    import java.io.*
 *
 *     // stvoření lexeru
 *    QuickSimpleLexer0 lex = new TestLexerJakoPotomek();
 *
 *    // nastavení vstupu
 *    String jmeno = "X:\\Tools\\Source\\ParserJavaLib\\test\\testlexerinput.txt";
 *    lex.setInput(
 *     new PositionTrackPushbackReader(
 *         new PushbackReader(
 *           new BufferedReader(
 *             new FileReader(jmeno)))
 *       , jmeno));
 *
 *    // čtení tokenů
 *    Token token;
 *    while ((token = lex.read()) != null) {
 *      LexerTokenLocator locator = (LexerTokenLocator)token.getSourcePositionInfo();
 *      System.out.println(
 *          "Token:  " +  token.getType() + ": "
 *                     + token.getValue()
 *         + "           ["
 *         + "begline="   + locator.getBegLineNumber() +  ", "
 *         + "endline="   + locator.getEndLineNumber() + ", "
 *         + "begcolun="  + locator.getBegColumnNumber() + ", "
 *         + "endcolumn=" + locator.getEndColumnNumber() + ", "
 *         + "begpos="    + locator.getBegPosition() + ", "
 *         + ",endpos="   + locator.getEndPosition() + "]"
 *         + "     " + locator.getInputSourceName()  );
 *    }
 *   }
 * </pre>
 * <p>Používající kód se skládá ze tří sekcí.
 * <p>V první sekci jednoduše vytvoříme instaci lexeru, čímž se v jeho konstruktoru vytvoří definicee a provede kompilace, čímž vznikne příslušný automat. Lexer
 * je již ve fázi lexování, fáze definice proběhla uvnitř konstruktoru.
 * <p>Ve druhé sekci se určí vstup voláním metody {@link #setInput}. Metoda přijímá speciální objekt PositionTrackPushbackReader. Tento objekt
 * je vytvořen podle návrhového vzoru "decorator" jako všechny readery v balíku java.io, to znamená, že veškerá volání přehazuje
 * na agregovaný objekt typu PushBackReader. Třída PositionTrackPushbackReader sama také implementuje rozhraní PushBackReader (i když je využita dědičnost, díky nevhodném
 * návrhu java.io). Navíc však počítá pozici, řádek a sloupec. tyto informace lexer v sobě nedrží.
 * <p>Až bude lexer číst tokeny, tak vždy čte znaky z předaného Readeru a znak, který musel přečíst navíc vrátí zpět do readeru (proto PushBackReader).
 * Toto uspořádání má tu výhodu, že na základě výskytu jistého tokenu mohu přestat číst pomocí daného lexeru, číst data z readeru ručně a poté se vrátit ke čtení
 * z lexeru, přičemž stále bude souhlasit číslo řádku a sloupce. Také mohu dalším voláním setInput() kdykoli zdroj znaků vyměnit a
 * pokračovat ve čtení z jiného zdroje s tím, že například při reportování chyb se reportují správná čísla řádků a sloupců.
 * <p>Všimněme si, že název souboru v proměnné <i>jmeno</i> je předáván řetězu objektů dvakrát. Poprvé se dává nejspodnějšímu readeru, aby otevřel soubor,
 * podruhé se dává našemu PositionTrackPushbackReader, pouze jako řetězec, jenž tento objekt umí vrátit. Dává se tam proto, aby při výpisu chyb bylo možné
 * vypsat správné jméno původního zdroje.
 * <p>Ve tetí sekci čteme jednotlivé tokeny. Třída QuickSimpleLExer0 implementuje obecné rozhraní cz.tconsult.tw.util.TokenInputStream,
 *  čímž zároven vystupuje jako proud tokenů. Rozhraní je velmi obecné, aby bylo možné spojovat různé zdroje tokenů a různé
 *  konzumery bez nutnosti být provázaný, jak je tomu například u unixových nástrojuů <i>yacc</i> a <i>lex</i>. Proud tokenů
 *  vrací tedy obecné tokeny reprezentované rozhraním cz.tconsult.tw.util.Token; Proto je nutné uvedené přetypování.
 *  Samozřejmě, že by celý token bylo možné vypsat volání <i>token.toString()</i>, pro větší názornost,
 *  jak přistupovat k atributům jdem zvolil složitější příklad.
 *  <p>Předhodíme li lexeru tento text:
 *  <pre>
 *   BEGIN
 *   let vysledek = 12 * 3 - 18.45e-19 / cislo1 + cislo40 * jedna
 *   tam je navic pismeno 14.x320
 *   END
 *   </pre>
 *   <p>Dostaneme tento výsledek:
 *
<pre>Token:  KWD_BEGIN: BEGIN           [begline=0, endline=0, begcolun=0, endcolumn=5, begpos=0, ,endpos=5]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  KWD_LET: let           [begline=1, endline=1, begcolun=0, endcolumn=3, begpos=7, ,endpos=10]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: vysledek           [begline=1, endline=1, begcolun=4, endcolumn=12, begpos=11, ,endpos=19]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  ERROR: =           [begline=1, endline=1, begcolun=13, endcolumn=13, begpos=20, ,endpos=20]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  INTEGER: 12           [begline=1, endline=1, begcolun=15, endcolumn=17, begpos=22, ,endpos=24]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  OPERATOR: *           [begline=1, endline=1, begcolun=18, endcolumn=19, begpos=25, ,endpos=26]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  INTEGER: 3           [begline=1, endline=1, begcolun=20, endcolumn=21, begpos=27, ,endpos=28]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  OPERATOR: -           [begline=1, endline=1, begcolun=22, endcolumn=23, begpos=29, ,endpos=30]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  REAL: 18.45e-1           [begline=1, endline=1, begcolun=24, endcolumn=32, begpos=31, ,endpos=39]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  INTEGER: 9           [begline=1, endline=1, begcolun=32, endcolumn=33, begpos=39, ,endpos=40]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  OPERATOR: /           [begline=1, endline=1, begcolun=34, endcolumn=35, begpos=41, ,endpos=42]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: cislo1           [begline=1, endline=1, begcolun=36, endcolumn=42, begpos=43, ,endpos=49]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  OPERATOR: +           [begline=1, endline=1, begcolun=43, endcolumn=44, begpos=50, ,endpos=51]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: cislo40           [begline=1, endline=1, begcolun=45, endcolumn=52, begpos=52, ,endpos=59]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  OPERATOR: *           [begline=1, endline=1, begcolun=53, endcolumn=54, begpos=60, ,endpos=61]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: jedna           [begline=1, endline=1, begcolun=55, endcolumn=60, begpos=62, ,endpos=67]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: tam           [begline=2, endline=2, begcolun=0, endcolumn=3, begpos=69, ,endpos=72]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: je           [begline=2, endline=2, begcolun=4, endcolumn=6, begpos=73, ,endpos=75]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: navic           [begline=2, endline=2, begcolun=7, endcolumn=12, begpos=76, ,endpos=81]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: pismeno           [begline=2, endline=2, begcolun=13, endcolumn=20, begpos=82, ,endpos=89]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  ERROR: 14.           [begline=2, endline=2, begcolun=21, endcolumn=24, begpos=90, ,endpos=93]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  IDENTIFIKATOR: x320           [begline=2, endline=2, begcolun=24, endcolumn=28, begpos=93, ,endpos=97]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
Token:  KWD_END: END           [begline=3, endline=3, begcolun=0, endcolumn=3, begpos=99, ,endpos=102]     X:\Tools\Source\ParserJavaLib\test\testlexerinput.txt
</pre>


 *  <p>
 *  <p>Chybový token:
 *  <p>V uvedeném příkladu je nastaven typ chybového tokenu. Token s tímto typem je vyhazován vždy při chybě, textem je text, který se nepodařilo parserovat. Na uvedeném
 *  příkladě je vidět, že lexer může být dočasně rozhozen. Tento režim je vhodný, pokud výstupy lexeru zpracovává LR parser, který nezná chybový token
 *  a provede příslušné ošetření chyby. V jiných případech se nám však hodí, aby lexer vyhodil výjimku při první chybě. Docílime toho tím, že nebudeme volat {@link #setErrorToken}
 *  <p>Koncový toke:
 *  <p>V uvedeném příkladu tok tokenů končí vrácením hodnoty null. Některé parsery však mohou požadovat nějaký speciální ukončovací token. Pokud jeho typ je nastaven
 *  metodu {@link #setEndToken}, bude proud tokenů nekonečný a každé volání {@link #read}, když je vstup vyčerpán vrátí koncový token.
 *  <p>Parserování hodnot a mapování typů tokenů:
 *  <p>Provádí se přepsáním metody {@link #mapToken}, je to dobré například pro odstranění uvozovek a escape znaků z řetězců nebo pro řešení klíčových slov, blížší informace
 *  jsou u uvedené metody.
 *  <p>Změna typů tokenů:
 *  <p>Některé konkrétní navazující parsery mohou vyžžadovat typy tokenů jistéh javovského typu. Tyto typy nemusí být serializovatelné nebo jejich hodnoty nemusí být k dispozici
 *  v okamžiku definice a kompilace lexeru. Za tím účelem, každá metoda defineXXXX() vrací hodnotu typu TokenDef. Tuto hodnotu je možné použít jako klíč k výměně
 *  vraceného typu tokenu metodami changeXXX. Výměnu mohu provést kdykoli ve fázi lexování, i když už byly čteny tokeny. Změna je účinná již v následujícím tokenu,
 *  předchozí vrácené tokeny neovlivní.
 *  <p>
 *  <p>Výkonnost: Lexer je udělán tak, aby byl co možná nejvýkonnější. Rychlost lexování nezávisí na složitosti definice lexeru! Ani na počtu definovaných
 *  tokenů ani na složitosti regulárních výrazů. Ovšem s počtem regulárních výrazů a sejich složitostí roste množství paměti, zabrané vygenerovaným automatem a doba
 *  kompilace, jenž se stráví v metodě compile(). Závislost je exponenciální. Pokud tato doba bude neakcpetovatelná, je možné celý objekt lexeru po kompilaci serializovat
 *  a uložit do souboru a uložit do resourců. Při běhu ho lze načíst. Ale zde pozor. V tom případě musí být serializovatelné i typy tokenů. Pokud plánujete kompilovat lexer
 *  a ukládat do souboru, doporučuji za token typy dát řetězce. Po načtení z disku pak programátor nahradí řetězcové tokeny,
 *  nejlépe za pomoci uschováných identtifikací definic vrací je metody defineXXXX požadovanými tokeny. K náhradě použije changeXXXX. Doporučuji pro tuto náhradu udělat metodu.
 *  Je nutno si uvědomit, že serializací se nezanechá nastavení vstupu pomocí setInput, po deserializaci je nutné vstup nastavit znovu.
 *  <p>Poznámky k implmentaci:
 *  <p>Jako základ implmentace byl vzat systém regulárních výrazů a konečných automatů vyvíjených v projektu GNU, původní balík <i>dk.brics.automaton</i>. Tyto třídy byly
 *  umístěny do vlastního balíku a upraveny, aby bylo možné poskládat lexer a vracet typy tokenů. Úpravy byly poměrně jednoduché. Nebyly využity standardní regulární výrazy,
 *  jenž jsou součástí JDK 1.4, protože nezveřejňují implementaci pomocí automatů, čímž znasnadňují implementaci lexeru. Lexer implmentovaný pomocí standardních regulárních
 *  výrazů by nebyl dostatečně výkonný. Z toho důvodu jsou drobné rozdíly v syntaxi regulárních výrazů, především chybí některé konstrukce,
 *  které nelze jednoduše konečným automatem řešit. Syntaxe je popsána u metody {@link #defineToken}.
 *  <p>Vláknovost:
 *  <p>Lexer není synchronizován. Při použití lexeru ve více vláknech je vhodné synchronizovat veškerá volání metod.
 *  V praxi si však nedovedu představit, že z jednoho zdroje bude číst více vláken.
 *
 * @author Martin Veverka
 * @version 1.0
 */

public abstract class QuickSimpleLexer0 implements TokenInputStream, java.io.Serializable  {

  private static final long serialVersionUID = 5850799391589012359L;

  // Proměnné potřebné ve fázi konstrukce automatů, po kompilaci budou nastaveny na null
  //private List iDefAutomaty = new LinkedList();
  //private List iDefTokentypy = new ArrayList(30);
  //private List iDefValueMappery = new ArrayList(30);  // mapovače jednotlivých tokenů
  private List<TokenDef> iDefDefinice = new ArrayList<TokenDef>(30);
  private Map<State, Integer> iDefStavyNaCisla = new HashMap<State, Integer>(); // mapa původních NDA stavů na čísla.

  private Object iIdentifierTokenType; // TokenType odpovídající identifikátoru.

  boolean iKeywordsCaseSensitive = true; // zda mají býz klíčová slova case sensitivní
  Map<String, TokenDef> iKeywords = new HashMap<String, TokenDef>();

  // proměnné definující automat a nutné pro běh
  private int[] iStavyNaCislaTokentypu;    // mapa čísel stavů na čísla typů tokenů, což jsou pořadová čísla vložených definic
  //  private Object[] iCislaTokenTypuNaTokentypy;    // mapa čísel tokentypů na tokentypy, mapa je proto, aby se mohly tokentypy měnit
  private TokenDef[] iCislaTokentypuNaDefinice;  // mapa čísel tokentypů na handly, jenž vznikly v okamžiku definice
  private RunAutomaton runauto;          // zkompilovaný rychlý automat
  private Object iErrorTokenType = null; // Token vracený při chybě, když null, vyhazuje se výjimka
  private Object iEndTokenType = null;   // Typ tokenu vraceném na konci, když null, tak se vrací null.

  // fáze
  private boolean iRunFaze = false; // false ... fáze tvorby, true ... fáze běhu

  // globální lokátor, bude instanciován a použit jako jediný lokátor pro všechny tokeny,
  // pokud tak bude požadováno, zároveň je to příznak, zda instancovat lokátor pro každý token (pokud je not null)
  // nebo zda použít společný lokátor
  //private LexerTokenLocator iGlobalniTokenLocator = null;

  // zásobník tokenů pro unread
  private LexerToken iVracenyToken = null;      // poslední vrácený token, pro optimalizaci, protože typicky bude jeden
  private List<LexerToken> iDalsiVraceneTokeny = null; // seznam vrácených tokenů, pokud je jich více.


  // proměnné pro předávání informací do mapovací virtuální metody a pomocné proměnné
  // pro čtení tokenu
  private boolean iJsemVMapovani = false;
  private Object iAktualniTokenType = null;
  private Object iAktualniTokenValue = null;
  private final StringBuffer iTokTextBuf = new StringBuffer();  // buffer pro sestavování tokenu, aby se nemusel tvořt znovu

  // Vstupní zdroj, drží stav
  private transient PositionTrackPushbackReader iInput;

  private LexerTokenFactory iTokFak = LexerTokenFactory.getInstance();

  /**
   * Zkonstruje prázný tokenizer.
   */
  public QuickSimpleLexer0() {
  }

  /**
   * Definuje nový token.
   * Regular expressions are built from the following abstract syntax:<p>
   * <table border=0 summary="">
   * <tr><td><i>regexp</i></td><td>::=</td><td><i>unionexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>unionexp</i></td><td>::=</td><td><i>interexp</i>&nbsp;<tt><b>|</b></tt>&nbsp;<i>unionexp</i></td><td>(union)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>interexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>interexp</i></td><td>::=</td><td><i>concatexp</i>&nbsp;<tt><b>&amp;</b></tt>&nbsp;<i>interexp</i></td><td>(intersection)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>concatexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>concatexp</i></td><td>::=</td><td><i>repeatexp</i>&nbsp;<i>concatexp</i></td><td>(concatenation)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>repeatexp</i></td><td>::=</td><td><i>repeatexp</i>&nbsp;<tt><b>?</b></tt></td><td>(zero or one occurrence)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>*</b></tt></td><td>(zero or more occurrences)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>+</b></tt></td><td>(one or more occurrences)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>}</b></tt></td><td>(<tt><i>n</i></tt> occurrences)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,}</b></tt></td><td>(<tt><i>n</i></tt> or more occurrences)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,</b><i>m</i><b>}</b></tt></td><td>(<tt><i>n</i></tt> to <tt><i>m</i></tt> occurrences, including both)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>complexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>complexp</i></td><td>::=</td><td><tt><b>~</b></tt>&nbsp;<i>complexp</i></td><td>(complement)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>charclassexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>charclassexp</i></td><td>::=</td><td><tt><b>[</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td><td>(character class)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>[^</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td><td>(negated character class)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>simpleexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>charclasses</i></td><td>::=</td><td><i>charclass</i>&nbsp;<i>charclasses</i></td><td></td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>charclass</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>charclass</i></td><td>::=</td><td><i>charexp</i>&nbsp;<tt><b>-</b></tt>&nbsp;<i>charexp</i></td><td>(character range, including end-points)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><i>charexp</i></td><td></td><td></td></tr>
   *
   * <tr><td><i>simpleexp</i></td><td>::=</td><td><i>charexp</i></td><td></td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>.</b></tt></td><td>(any single character)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>"</b></tt>&nbsp;&lt;Unicode string without double-quotes&gt;&nbsp;<tt><b>"</b></tt></td><td>(a string)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>(</b></tt>&nbsp;<tt><b>)</b></tt></td><td>(the empty string)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>(</b></tt>&nbsp;<i>unionexp</i>&nbsp;<tt><b>)</b></tt></td><td>(precedence override)</td><td></td></tr>
   *
   * <tr><td><i>charexp</i></td><td>::=</td><td>&lt;Unicode character&gt;</td><td>(a single non-reserved character)</td><td></td></tr>
   * <tr><td></td><td>|</td><td><tt><b>\</b></tt>&nbsp;&lt;Unicode character&gt;&nbsp;</td><td>(a single character)</td><td></td></tr>
   * </table>
   * <p>
   * The reserved characters used in the (enabled) syntax must be escaped with backslash
   * (<tt><b>\</b></tt>) or double-quotes (<tt><b>"..."</b></tt>). (In contrast to other
   * regexp syntaxes, this is required also in character classes.) An identifier is a string
   * not containing right angle bracket (<tt>&gt;</tt>).
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @return Identifikaci tokenu. Volající programátor ji může uložit a později použít třeba pro změnu typu tokenu nebo
   * pro rozlišení, že byl přijat jistý token a má být například vyčíslena hodnota.
   */
  protected TokenDef defineToken (final String aRegExp, final Object aTokenType) {
    return defineToken(aRegExp, aTokenType, null);
  }


  /**
   * Definuje token t value mapperem.
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @param aValueParser Objekt paraserující hodnotu. Pokud není definován, nebo bude null, bude hodnota odpovídat přečtenému
   * řetězci.
   * @return Identifikaci tokenu. Volající programátor ji může uložit a později použít třeba pro změnu typu tokenu nebo
   * pro rozlišení, že byl přijat jistý token a má být například vyčíslena hodnota.
   */
  protected TokenDef defineToken (final String aRegExp, final Object aTokenType, final ValueParser aValueParser) {
    if (aTokenType == null) {
      throw new NullPointerException("Parametr aTokenType metody definToken nesmi byt null");
    }
    return _defineToken(aRegExp, aTokenType, aValueParser, false, false);
  }

  /**
   * Definuje identifikátor. Výsledek je stejný jako posloupností:
   * <pre>
   *    TokenDef idident = defineToken(" ...... ");
   *    setIdentifier(idident);
   * </pre>
   * @param aRegExp Regulární výraz definující identifikátor
   * @param aTokenType Typ tokenu identifikátoru.
   * @return Definice tokenu.
   */
  protected TokenDef defineIdentifier(final String aRegExp, final Object aTokenType) {
    final TokenDef id = _defineToken(aRegExp, aTokenType, null, false, true);
    if (iIdentifierTokenType == null) {

      iIdentifierTokenType = aTokenType;
    }
    else {

      //Porovnávej opravdu instance. Parser by nemusel fungovat, pokud bychom se spoléhali na equals.
      if (iIdentifierTokenType != aTokenType) {

        throw new IllegalArgumentException("Only one token type for identifier allowed!"
            + " Old one: " + iIdentifierTokenType + ", new one: " + aTokenType);
      }
    }
    return id;
  }

  /**
   * Definuje nový token. Není uveden typ tokenu, za typ bude dosazeno id definice, což je objekt
   * vrácený tímto voláním.
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * bude identifikován token daný výrazem.
   * @return Identifikaci definice tokenu, což je objekt, jenž může být přiřazen do proměnné a použit v dalším volání.
   */
  protected TokenDef defineToken (final String aRegExp) {
    return _defineToken(aRegExp, null, null, true, false);
  }

  /**
   * Definuje token, jenž bude zcela ignorován, metodou read() nebude vrácen a přistoupí se hned ke čtení dalšího tokenu,
   * Vhodné pro definici mezer, komentářů a podobnýchz pochybných tokenů.
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @return Identifikaci definice tokenu, což je objekt, jenž může být přiřazen do proměnné a použit v dalším volání.
   */
  protected TokenDef defineIgnoredToken(final String aRegExp) {
    return _defineToken(aRegExp, null, null, false, false);
  }

  /**
   * Definuje token za účelem ignorování. Takový token nebude
   * přímo ignorován lexerem, bude vylízat jako normální token. V definici
   * tokenu je však poznačeno (nikoli ve vlastním tokenu), že parser má token ignorovat.
   * Pokud se tedy použije parser, který rozumí našemu lexeru, token ignruje. Může
   * ho však použít pro sestavení původního textu.
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @return Identifikaci definice tokenu, což je objekt, jenž může být přiřazen do proměnné a použit v dalším volání.
   */
  protected TokenDef defineTokenForIgnoring(final String aRegExp, final Object aTokenType) {
    final TokenDef  def = _defineToken(aRegExp, aTokenType, null, false, false);
    def.setForIgnoring(true);
    return def;
  }

  /**
   * Definuje token za účelem ignorování. Takový token nebude
   * přímo ignorován lexerem, bude vylízat jako normální token. V definici
   * tokenu je však poznačeno (nikoli ve vlastním tokenu), že parser má token ignorovat.
   * Pokud se tedy použije parser, který rozumí našemu lexeru, token ignruje. Může
   * ho však použít pro sestavení původního textu.
   * Není uveden typ tokenu, za typ bude dosazeno id definice, což je objekt
   * vrácený tímto voláním.
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @return Identifikaci definice tokenu, což je objekt, jenž může být přiřazen do proměnné a použit v dalším volání.
   */
  protected TokenDef defineTokenForIgnoring(final String aRegExp) {
    final TokenDef  def = _defineToken(aRegExp, null, null, true, false);
    def.setForIgnoring(true);
    return def;
  }

  /**
   * Definuje klíčové slovo. Klíčové slovo se stane tokenem a nevíce ja zařazeno
   * do mapy klíčových slov získatelné pomocí getKeywordMap
   * @param aKeyword Klíčové slovo. Musí odpovídat regulárníému výrazu, jenž je definován u tokenu nastaveného
   *  pomocí setIdentifier respektive defineIdentifier.
   * @param aTokenType Typ tokenu spojený s daným klíčovým slovem. Měl by být nastaven,
   * pokud je null, budou klíčová slova ignorována.
   * @return Definice tokenu jako u normálního tokenu.
   */

  protected TokenDef defineKeyword(final String aKeyword, final Object aTokenType) {
    if (StringUtils.isBlank(aKeyword)) {
      throw new IllegalArgumentException("Parameter aKeyword nesmí být prázdný");
    }
    final TokenDef h = new TokenDef(null, null, aTokenType
        , ValueParser.NULL, false);  // vytvořit definici z pořadovým číslem tokenu
    iDefDefinice.add(h);  // přidat normálně mezi definice
    String klic = aKeyword;
    if (! iKeywordsCaseSensitive) {
      klic = aKeyword.toLowerCase();
    }
    final Object bylotam = iKeywords.put(klic, h);

    if (bylotam != null) {
      throw new IllegalStateException("Keyword " + aKeyword + " allready defined");
    }
    return h;
  }


  /**
   * Nastaví citelivost klíčových slov na velká a malá písmena.
   * @param aIsCaseSensitive
   */
  protected void setKeywordsCaseSensitive(final boolean aIsCaseSensitive) {
    if (iKeywordsCaseSensitive == aIsCaseSensitive) {
      return;
    }
    if (! iKeywords.isEmpty()) {
      throw new IllegalStateException("Already defined some keyword by defineKeywordMethod, change case sensitivity before defining of keywords");
    }
    iKeywordsCaseSensitive = aIsCaseSensitive;
  }

  public boolean isKeywordsCaaseSensitive() {
    return iKeywordsCaseSensitive;
  }

  /**
   * Mapa mapuje texty klíčových slov na definice.
   * @return Mapa Stringů s texty definice slov na {@link TokenDef}. Klíče jsou řetězuce klíčových slov.
   * Pokud je zapnuta citlivost na velká a malá písmena metodou setKeywordCaseSensitive, jsou klíčová slova
   * tak, jak byla zadána, pokud je case sensitivita zapnuta, jsou jako klíče mapy klíčová slova malými písmeny. Nikdy nevrací null,
   * ani když není definováno jediné klíčové slovo.
   */
  public Map<String, TokenDef> getKeywordMap() {
    return Collections.unmodifiableMap(iKeywords);
  }

  private TokenDef _createToken (final String aRegExp, final Object aTokenType, final ValueParser aValueMapper
      , final boolean aTokenDefAsTokenType, final boolean aIsIdentifier) {

    final Automaton auto =  new RegExp(aRegExp, RegExp.INTERSECTION + RegExp.COMPLEMENT).toAutomaton();
    if (auto.getInitialState().isAccept()) {
      throw new IllegalArgumentException("Regularni vyraz \"" + aRegExp + "\" + prijima prazdny retezec, to je nepripustne");
    }

    final TokenDef h = new TokenDef(aRegExp, auto, aTokenType, aValueMapper, aIsIdentifier);  // vytvořit definici z pořadovým číslem tokenu
    if (aTokenDefAsTokenType) {
      h.setTokenType(h, h.getValueParser()); // sám sebou má být prý tokentypem
    }
    return h;
  }

  /**
   *
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @param aValueMapper Objekt - zpracovávač hodnoty, jejíž text je vykousnut
   * tímto tokenem. Pokud není definován, nebo bude null, bude hodnota odpovídat
   * přečtenému řetězci.
   * @param aTokenDefAsTokenType Zda má vytvořit TokenType ze své
   * definice - true má smysl pouze v tom případě, pokud jsi nezadal aTokenType
   * @return
   */
  protected TokenDef createToken (final String aRegExp, final Object aTokenType, final ValueParser aValueMapper
      , final boolean aTokenDefAsTokenType) {

    final TokenDef h = _createToken(aRegExp, aTokenType, aValueMapper, aTokenDefAsTokenType, false);
    return h;
  }

  /**
   *
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @param aValueMapper Objekt - zpracovávač hodnoty, jejíž text je vykousnut
   * tímto tokenem. Pokud není definován, nebo bude null, bude hodnota odpovídat
   * přečtenému řetězci.
   * @return
   */
  protected TokenDef createToken (final String aRegExp, final Object aTokenType, final ValueParser aValueMapper) {

    final boolean b = aTokenType == null;
    final TokenDef h = createToken(aRegExp, aTokenType, aValueMapper, b);
    return h;
  }

  /**
   *
   * @param aRegExp Regulární výraz, který má tokenu odpovídat.
   * @param aTokenType Typ tokenu = libovolný objekt, jenž bude vrácen, když
   * bude identifikován token daný výrazem.
   * @return
   */
  protected TokenDef createToken (final String aRegExp, final Object aTokenType) {

    final TokenDef h = createToken(aRegExp, aTokenType, null, false);
    return h;
  }

  private TokenDef _defineToken (final String aRegExp, final Object aTokenType, final ValueParser aValueMapper
      , final boolean aTokenDefAsTokenType, final boolean aIsIdentifier) {
    checkFaze(false);
    final TokenDef h = _createToken(aRegExp, aTokenType, aValueMapper, aTokenDefAsTokenType, aIsIdentifier);
    iDefDefinice.add(h);

    for (final State item : h.getAutomaton().getStates()) {
      iDefStavyNaCisla.put(item, new Integer(iDefDefinice.size() - 1)); // zapamatovat index do tokentypů
    }
    return h;
  }


  /**
   * Definuje typ tokenu, který má být vracen v případě chyby. Pokud žádný token není
   * definován, je při chybě vyhozena výjimka. Opakované volání metopdy přeplácne původní nastavení.
   * @param aTokenType Vyhazovaný typ tokenu, null znamená, že token není definován.
   */
  protected void setErrorToken(final Object aTokenType) {
    iErrorTokenType = aTokenType;
  }

  /**
   * Definuje typ tokenu, který má být vrácen metodou {@link #read} v případě, že se narazí na konec vstupního proudu znaků. Pokud žádný token není
   * definován, je vráceno null. Opakované volání metopdy přeplácne původní nastavení.
   * @param aTokenType Vyhazovaný typ tokenu, null znamená, že token není definován.
   */
  protected void setEndToken(final Object aTokenType) {
    iEndTokenType = aTokenType;
  }

  /**
   * Vrátí typ tokenu, který je identifikátorem.
   * @return Typ tokenu odpovídající identifikátoru nebo null, pokud žádný identifikátor nebyl
   * nastaven.
   */
  public Object getIdentifierTokenType() {
    return iIdentifierTokenType;
  }

  /**
   * Vrátí typ tokenu, jenž bude vrácen na konci proudu znaků.
   * @return Typ definovaný metodou {@link #setEndToken}, null pokud žádný token nebyl definován a má být při chybě vyhozena výjimka.
   */
  public Object getEndTokenType() { return iEndTokenType; }

  /**
   * Vrátí typ tokenu, jenž bude vracen při chybě.
   * @return Typ definovaný metodou {@link #setErrorToken}, null pokud žádný token nebyl definován a má být při chybě vyhozena výjimka.
   */
  public Object getErrorTokenType() { return iErrorTokenType; }


  /**
   *  Provede kompilaci zadaných definic a umožní používat metody rozhraní {@link cz.tconsult.parser.lexer.TokenInputStream}.
   *  OD této chvíle není již možné definovat další tokeny.
   *  Tato metoda také požívá uvnnitř rozhraní {@link java.util.Iterator} a dokonce i {@link java.lang.Integer#MAX_VALUE},
   *  to sem však píši jen proto, že chci vědět, jak bude reagovat Javadoc.
   */
  protected void compile() {
    checkFaze(false);
    if (! iKeywords.isEmpty() && iIdentifierTokenType == null) {
      throw new IllegalStateException("There are some keywords defined by defineKeyword, but no identifier was defined as the identifier (by setIdentifier method)");
    }
    final List<Automaton> automaty = new ArrayList<Automaton>(iDefDefinice.size());
    for (final TokenDef item : iDefDefinice) {
      if (item.getAutomaton() != null) {
        automaty.add(item.getAutomaton());
      }
    }
    final Automaton cely = Automaton.union(automaty, true);

    //    iDefAutomaty = null;  // automaty již nebudou potřeba
    //    System.out.println("Nedetermin");
    //    System.out.println(cely.toString());
    final Map<Set<State>, State> mapa = cely.determinize();
    //    System.out.println("Determin");
    //    System.out.println(cely.toString());
    //    System.out.println("----------------------------------------------------");
    //    System.out.println(mapa);
    runauto = new RunAutomaton(cely);  // musíme udělat brzy, aby se správně očíslovaly stavy
    iStavyNaCislaTokentypu = new int[runauto.getSize()];

    //    System.out.println("================================================");
    //    System.out.println(iStavyNaCisla);

    for (final Map.Entry<Set<State>, State> item : mapa.entrySet()) {
      //      System.out.println("     " + item.getKey() + "   =    " + item.getValue());
      final State novystav = item.getValue();
      final int cislostavu = novystav.getNumber();
      int mintokentyp = Integer.MAX_VALUE;
      // jedeme přes staré stavy k novému stavu
      for (final State staryStav : item.getKey()) {
        if (! staryStav.isAccept())
        {
          continue;  // nekoncové stavy nesmíme brát v úvahu
        }
        final Integer tokentyp = iDefStavyNaCisla.get(staryStav); //vytáhnout tokentyp
        if (tokentyp != null && tokentyp.intValue() < mintokentyp)
        {
          mintokentyp = tokentyp.intValue(); // hledáme nejdřívější pravidlo
        }
      }
      if (mintokentyp < Integer.MAX_VALUE) {
        iStavyNaCislaTokentypu[cislostavu] = mintokentyp;
      }
    }
    //iCislaTokenTypuNaTokentypy = iDefTokentypy.toArray();
    iCislaTokentypuNaDefinice    = iDefDefinice.toArray(new TokenDef[0]);

    //System.out.println("----------------------------------------------------");
    iDefStavyNaCisla = null; // také nebude potřeba
    iDefDefinice = null;
    iRunFaze = true;
  }

  /**
   * Nastavení zdroje vstupu. Zdroj musí být otevřen.
   * @param aInput Zdroj vstupu, ze kterého se bude číst.
   */
  public void setInput(final PositionTrackPushbackReader aInput) {
    iInput = aInput;
  }

  /**
   * Pomocná metoda pro pohodlí, která vytvoří PositionTrackPushbackReader čtoucí ze zadaného řetězce a ten bere jako vstup.
   * @param aRetez Řetězec, jenž má být lexován.
   * @param aResourceName Jméno zdroje, ze kterého se data četla, tedy jméno souboru, i třeba s položkou v zipu atd. Bude použito v lokátoru tokenu
   * a také v chybách.
   */
  public void setInput(final String aRetez, final String aResourceName)  {
    setInput(new PositionTrackPushbackReader(new PushbackReader(new StringReader(aRetez)), aResourceName));
  }

  /**
   * Pomocná metoda pro pohodlí, která vytvoří PositionTrackPushbackReader čtoucí ze zadaného souboru a ten bere jako vstup.
   * @param aFile Soubor, jenž má být lexován.
   * @throws IOException
   */
  public void setInput(final File aFile, final Charset charset) throws IOException {

    final Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), charset));
    final PositionTrackPushbackReader pushBack = new PositionTrackPushbackReader(
        new PushbackReader(r), aFile.getAbsolutePath());

    setInput(pushBack);
  }


  /**
   * Vrací stream, z něhož padají příslušné tokeny. Vrací vždy PositionTrackPushbackReader bez ohledu, jakým
   * způsobem byl zadán vstup.
   * @return Stream, z něhož padají příslušné tokeny.
   */
  public PositionTrackPushbackReader getInput() {
    return iInput;
  }

  public int getMemoryBytes() {
    return runauto.getMemoryBytes()
        + (iStavyNaCislaTokentypu == null ? 0 : iStavyNaCislaTokentypu.length * 4);
    //  + (iDefTokentypy == null ? 0 : iDefTokentypy.size() * 4);
  }

  /**
   * Zjistí, ve které fázi se lexer nachází.
   * @return True, pokud je ve fázi běhu, false, pokud je ve fázi vzniku.
   */
  public boolean isRunPhase() {
    return iRunFaze;
  }

  /**
   * Nastaví režim dávání informací o pozici uvnitř parserovaného proudu.
   * @param aUseTokenLocator Pokud je true, bude Token.getSourcePositionInfo()
   * vracet objekt typu {@link LexerTokenLocator} a informace tímto objektem bude svázána
   * s tokenem, nebude se dále měnit. Pokud je false, vrací jakýsi objekt, pozici
   * lze získat pouze pomocí toString() a je jen přibližná, protože je dynamicky
   * odvozována z Readeru, takže když se čtou další tokeny, mění se. Tento režim je rychlejší,
   * protože se nevytvářejí zbytečné instance, měl by se však používat pouze tehdy, kdy je ta rychlost důležitá.
   * Defaultní je true.
   * @deprecated Nemá na nic vliv
   */
  @Deprecated
  public void setUseTokenLocator(final boolean aUseTokenLocator) {
    //iGlobalniTokenLocator = aUseTokenLocator ? null : new LexerTokenLocator();
  }

  /**
   * Zjistí, zda se používá lokátor tokenů.
   * @return True, pokud je pro každý token vracena instance lokátoru (s pozicemi, kde token byl),
   * pokud je false, token vrací aktuální pozici na streamu.
   * @deprecated Nemá na nic vliv, vždy vrací true
   */
  @Deprecated
  public boolean isUsedTokenLocator() {
    return true; //iGlobalniTokenLocator == null;
  }

  /**
   * Čte token pomocí připraveného automatu. Vrací implementaci tokenu, jejíž metody
   * <p>getValue() Vrací vždy řetězec s tokenem
   * <p>getType()  Vrací objekt, který byl jako typ určen ve volání define()
   * <p>getSourcePosition() Defaultně vrací instanci TokenLocator, z níž lze zjistit informace
   * o počátku i konci tokenu ve vstupním proudu. Pokud je však zaplá optimalizace pomocí {@link #setUseTokenLocator},
   * vrací objekt anonymní třídy na němž lze zavolat jedině toString(), jenž vrátí řetězcovou reprezentaci.
   * @return Token, null, pokud již žádný token není k dispozici a celý vstup byl vyčerpán. Pokud dojde k chybě, tak
   * vrací token definován voláním defineErrorToken(). Pokud žádný takový token nebyl definován, vyhodí výjimku.
   * Pokud narazí na konec toku znaků, vyhodí token jenž byl definován metodou defineEndToken(), vracený text je null.
   * Pokud žádný koncový token nebyl definován, vrací null. Když však koncový token definován je, je vyhazován donekonečna.
   */
  @Override
  public LexerToken read() {
    LexerToken tok = popToken();
    tok = tok == null ? readTokenFromInput() : tok;

    //System.out.println("tok: " + tok);
    return tok;
  }

  /**
   * Načte token z přiděleného vstupu.
   * @return
   */
  protected LexerToken readTokenFromInput() {
    checkFaze(true);
    checkIsInput();

    TokenDef defid = null;
    ValueParser vamap = null;
    try {
      String sourcename;
      int begline, begcol, begpos, endline, endcol, endpos;
      LexerTokenLocator locator;
      do {
        iTokTextBuf.setLength(0);
        sourcename = iInput.getInputSourceName();
        begline = iInput.getLineNumber(); begcol = iInput.getColumnNumber(); begpos = iInput.getPosition();
        int stav = runauto.getInitialState();
        int c = iInput.read();
        if (c == -1) {
          if (iEndTokenType == null) {
            return null;
          } else {
            endline = iInput.getLineNumber(); endcol = iInput.getColumnNumber(); endpos = iInput.getPosition();
            return iTokFak.createToken(iEndTokenType, null, "",  iTokFak.createLocator(sourcename, begline, begcol, begpos, endline, endcol, endpos));
          }
        } // už nic není na řadě
        do {
          final int newstav = runauto.step(stav, (char)c);
          if (newstav == -1)
          {
            break; // konec tokenu nebo chyba, další znak tam nepatří
          }
          stav = newstav;   // jedeme na další stav
          iTokTextBuf.append((char)c);
          c = iInput.read();
        } while (c != -1);
        if (c != -1)
        {
          iInput.unread(c);  // vrátit poslední znak zpět
        }
        endline = iInput.getLineNumber(); endcol = iInput.getColumnNumber(); endpos = iInput.getPosition();
        locator = iTokFak.createLocator(sourcename, begline, begcol, begpos, endline, endcol, endpos);
        if (!runauto.isAccept(stav)) {
          if (iTokTextBuf.length() == 0)
          {
            iTokTextBuf.append((char)iInput.read()); // pokud nebyl lexován žádný znak, musíme jeden užrat, at se posuneme a třeba se chyytíme
          }
          if (iErrorTokenType == null)
          {
            throw new RuntimeException("LexError near \"" + iTokTextBuf + "\": " + locator); // není nastaven chybový token
          }
          iAktualniTokenType = iErrorTokenType;
        } else { // máme konec tokenu a je správně
          final int cislotokentypu = iStavyNaCislaTokentypu[stav];

          defid = iCislaTokentypuNaDefinice[cislotokentypu];
          defid = remapTokenDef(defid, locator, iTokTextBuf.toString());

          iAktualniTokenType = defid.getTokenType();
          vamap = defid.getValueParser();
        }
      } while (iAktualniTokenType == null); // ignorování ignorovatelných znaků
      final String textik = iTokTextBuf.toString();
      iAktualniTokenValue = textik;
      iJsemVMapovani = true;

      if (vamap != null) {  // mapování konkrétního tokenu
        iAktualniTokenValue = vamap.parseValue(textik);
      }
      //if (defid != null) mapToken(defid, textik); // neprovolávat pro chybový token Už se nic takového dělat nebude, používá se ValueParser.
      iJsemVMapovani = false;



      return iTokFak.createToken(iAktualniTokenType, iAktualniTokenValue, textik, locator);
    } catch (final Exception ex) {
      throw new RuntimeException(
          String.format("%s [%d, %d]", iInput.getInputSourceName(), iInput.getLineNumber(), iInput.getColumnNumber())
          , ex);
    }

  }

  /**
   * Vrátí token zpět do proudu tokenů. Následné volání read() budou číst tyto tokeny. Metoda z výkonnostních důvodů nevrací tokeny úplně do Readeru,
   * ale jen do vnitřního bufferu tohoto objektu. Pokud je nutné vrátit tokeny zpět, musí se volat {@link #unreadUnreadedTokens}, jenž je protlačí zpět.
   * @param aToken
   */
  @Override
  public void unread(final LexerToken aToken) {
    checkFaze(true);
    checkIsInput();
    pushToken(aToken);
  }

  /**
   * Metoda vrací, zda je pro čtení připraven token a volání read() nebude blokující.
   * Stav odvozuje od readeru ležícího pod ním.
   * @return Příznak, zda je připraven.
   */
  @Override
  public boolean isReady() {
    try {
      checkFaze(true);
      checkIsInput();
      return iInput.ready();
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Otestuje lexer nad daným řetězcem. Výsledek vypisuje na standardní výstup.
   * @param aRetezec Řetězec, jenž bude lexován.
   */
  public void testLexer(final String aRetezec) {
    final PositionTrackPushbackReader schovat = iInput;
    try {
      setInput(new PositionTrackPushbackReader(new PushbackReader(new StringReader(aRetezec)), "testing pattern"));
      LexerToken token;
      System.out.println("----- parser begining -----");
      while ((token = read()) != null) {
        System.out.println("Token: " + token);
      }
      System.out.println("----- parser ending -----");
    } finally {
      iInput = schovat;
    }
  }

  /**
   * Zlexuje zadaný řetězec a vrátí seznam tokenů z tohoto řetězce.
   * @param aRetez Řetězec, jenž má být lexován.
   * @param aResourceName Jméno zdroje, ze kterého se data četla, tedy jméno souboru, i třeba s položkou v zipu atd. Bude použito v lokátoru tokenu
   * @return Seznam tokenů, vytvoří nový seznam.
   */
  public List<LexerToken> lex(final String aRetez, final String aResourceName) {
    final PositionTrackPushbackReader schovat = iInput;
    final List<LexerToken> vysledek = new ArrayList<LexerToken>();
    try {
      setInput(aRetez, aResourceName);
      LexerToken token;
      while ((token = read()) != null) {
        vysledek.add(token);
        if (token.getType() == iEndTokenType) {
          break; // koncový token vrazíme do výsledku jen jednou
        }
      }
      return vysledek;
    } finally {
      iInput = schovat;
    }
  }

  /**
   * Zlexuje zadaný řetězec a vrátí seznam tokenů z tohoto řetězce.
   * @param aFile Soubor, jenž má být čten a lexován. Soubor musí být v systémově defaultním kódování.
   * @return Seznam tokenů (typ cz.toconsult.util.Token), vytvoří nový seznam.
   * @exception IOException vyhozena, pokud vyhozena podležícím readrem při čtení vstupního souboru.
   */
  public List<LexerToken> lex(final File aFile, final Charset charset) throws IOException {
    final PositionTrackPushbackReader schovat = iInput;
    final List<LexerToken> vysledek = new ArrayList<LexerToken>();
    try {
      setInput(aFile, charset);
      LexerToken token;
      while ((token = read()) != null) {
        vysledek.add(token);
      }
      return vysledek;
    } finally {
      iInput = schovat;
    }
  }


  public List<LexerToken> lex(final Reader aReader, final String aResourceName) throws IOException {
    final PositionTrackPushbackReader schovat = iInput;
    final List<LexerToken> vysledek = new ArrayList<LexerToken>();
    try {
      setInput(new PositionTrackPushbackReader(new PushbackReader(aReader), aResourceName));
      LexerToken token;
      while ((token = read()) != null) {
        vysledek.add(token);
      }
      return vysledek;
    } finally {
      iInput = schovat;
    }
  }

  ////////////////// Metody pro změny typů tokenů

  /**
   * Vrátí typtokenu na základě identifikace definice. metodu je možné použít i zvenku.
   * @param aHandle
   * @return Typ tokenů takový jenž bude vracen dále.
   */
  protected Object getTokentype(final TokenDef aHandle) {
    checkFaze(true);
    return aHandle.getTokenType();
  }

  /**
   * Vrátí seznam všech definic tokenů. Iterováním přes seznam lze postupně měnit tokentypy voláním changeTokenType
   * Zjištovat regulární vrázy, typy tokenů, zda se jedná o ignroovaný token a podobně.
   * @return Seznam všech handlů v pořadí v jakém byly volány příslušné metody define().
   */
  public List<TokenDef> getTokenDefs() {
    checkFaze(true);
    return Collections.unmodifiableList(Arrays.asList(iCislaTokentypuNaDefinice));
  }

  ////////////////// Chráněné metody na přepsání


  /**
   * Virtuální metoda, která slouží k dynamickému přemapování definic tokenů
   * za běhu programu. Tak dynamicky, že se to nezvládne pomocí metod "defineToken".
   * Využitelné v případě, kdy máme třeba pro dva tokeny stejný rozpoznávací vzor (regulární výraz),
   * ale potřebujeme rozlišovat v závislosti na jiných okolnostech (např. pozice v dokumentu).
   * Implementace na této třídě zajišťuje, že jsou místo identifikátorů vraceny
   * tokeny reprezentující klíčová slova.
   * Pokud budeš tuto metodu přepisovat, měl bys ale volat i super.remapTokenDef.
   */
  protected TokenDef remapTokenDef(final TokenDef aOldTokenDef
      , final LexerTokenLocator aLocation, final String aText) {

    TokenDef result = aOldTokenDef;
    if (result.isIdentifier()) {

      final TokenDef kwdid = iKeywords.get( iKeywordsCaseSensitive ? aText : aText.toLowerCase() );
      if (kwdid != null)
      {
        result = kwdid; // pokud máme registrováno jak klíčové slovo, je to prostě příslušný token a basta
      }
    }
    return result;
  }

  /**
   * Virtuální metoda zajišťující mapování tokenu. Následník může metodu přepsat a zajistit
   * změnu hodnoty nebo typu tokenu. Uvnitř metody se typ mění metodou {@link #setTokenType}. Hodnota metodou
   * {@link #setTokenValue}.
   * <p>Změna hodnoty:
   * <p>Typickým příkladem je řetězec definovaný v uvozovkách. Lexerem je rozpoznán celý token včetně escape znaků,
   * je vhodné obalující uvozovky a zanořené escape znaky odstranit, aby z metody read() vycházel již očištěný
   * řetězec nezávislý na lexikálních podrobnostech. Jiným příkladem je parserování čísla respektive datumu, kdy budeme vracet typ
   * Double respektive ADate.
   * <p>Změnba typu:
   * <p>Změna typu je užitečná například pokud máme více různých lexikálních konstrukce téhož. Například řetězec v uvozovkách, v apostrofech a složených závorkách.
   * Každý z řetězců je definován zvláštní definicí, aby se poznalo o jaký se jedná, v mapovací metodě jsou odstraněny uvozovací znaky a je vhodné
   * aby řetězce vystupovaly z lexikálního analyzátoru jako jediný typ tokenu bez ohledu na jejich lexikální podobu. V tom případě změníme typ dvou ze tří řetězců.
   * <p>Klíčová slova:
   * Jednou z možností, jak realizovat klíčová slova je definovat tokeny pro klíčová slova regulárním výrazem. Získáme zcela určtitě velmi rychlý lexer. Pokud však klíčových slov bude hodně a navíc bude požadována
   * jejich case nesensitivita, může reprezentace automatu nabýt obludných rozměrů. Též doba kompilace automatu díky exponenciální závislosti převodu automatu
   * na deterministický automat může být značná. V tom případě může být vhodnější klíčová slova nedefinovat regulárními výrazy, ale nechat je vypadávat
   * jako identifikátor. Právě tato metoda je místem, kde podle nějaké mapy a řetězcové hodntoy tokenu se určí, že daný token není identifikátor, ale klíčové slovo
   * a nastaví se příslušný typ tokenu.
   * <p>Metoda může vyhodit libovolnou výjimku. V tom případě se postupuje, jako by parserovaný token byl vadný, což závisí na použití metody setErrorToken.
   * @param aTokenDef Identifikace definice, které vyhovuje token. Hodnota je určena na roskočení podle jednotlivých lexikálních symbolů. Hodnota parametru bude postupně
   * programátor porovnávat operátorem == se schovanými výstupními hodnotami metod define. Oběktovější možností než rozskakování je parameter nepoužít,
   * zjistit přímo stávající ty tokenu metodou getCurrentTokenType(), hodnotu přetypovat na rozhraní typu tokenu, který pro konkrétní parser používám
   * a vyvolat na něm specifické metody.
   * <p>Metoda provede definitivní určení tokentypu i hodnoty. Je volána až poté, kdy byla hodnota parserována konkrétním parserem hodnoty.
   * @param aText Původní text tokenu, tak jak vstoupil ze vstupního proudu znaků. Tento text bude programátor parserovat za účelem stanovení hodnoty pro setTokenType.
   * @deprecated Použij rozhraní {@link ValueParser}
   */
  @Deprecated
  protected void mapToken(final TokenDef aTokenDef, final String aText) { mapToken((DefinitionId)aTokenDef, aText); }

  /**
   * @deprecated Použij rozhraní {@link ValueParser}
   */
  @Deprecated
  protected void mapToken(final DefinitionId aTokDef, final String aText) {}

  /**
   * Metoda se použije pouze v předefinované metodě {@link #mapToken}, ke zjištění typu zpracovávaného tokenu. Metoda zohlední
   * i nastavení metodou {@link #setTokenType}.
   * @return Typ právě zpracovávaného tokenu.
   */
  protected final Object getCurrentTokenType() {  checkJsemVMapovani(); return iAktualniTokenType; }

  /**
   * Metoda se použije pouze v předefinované metodě {@link #mapToken}, k nastavení typu tokenu, pokud metoda
   * vyvolána, lexer vrátí typ tokenu určený v metodě defineToken() respektive v metodác change...
   * @param aTokenType Typ tokenu, jenž má být vracen. Může to být libovolný objekt.
   */
  protected final void setTokenType  (final Object aTokenType) { checkJsemVMapovani();  iAktualniTokenType = aTokenType; }

  /**
   * Metoda se použije pouze v předefinované metodě {@link #mapToken}.
   * Metoda se použije pro nastavení hodnoty tokenu, pokud nevyhovuje přímo řetězec tokenu.
   * @param aValue Nová hodnota, libovolný typ.
   */
  protected final void setTokenValue (final Object aValue) { checkJsemVMapovani(); iAktualniTokenValue = aValue; }

  /////////////////// Privátní metody

  private void checkFaze(final boolean aMaBytRun) {
    if ( aMaBytRun &&  ! iRunFaze) {
      throw new IllegalStateException("Nebyla vyvolana metoda compile()");
    }
    if (!aMaBytRun &&    iRunFaze) {
      throw new IllegalStateException("Metodu jiz nelze volat, jiz byla vyvolana metoda compile()");
    }
  }

  private void checkIsInput() {
    if (iInput == null) {
      throw new IllegalStateException("Lexeru nebyl přiřazen vstup voláním setInput()");
    }
  }

  private void checkJsemVMapovani() {
    if (! iJsemVMapovani) {
      throw new IllegalStateException("Metodu lze volat pouze uvnitr metody mapToken()");
    }
  }

  private void pushToken(final LexerToken aToken) {
    if (aToken == null)
    {
      return; // prázdné tokeny se nevkládají
    }
    if (iVracenyToken != null) {
      if (iDalsiVraceneTokeny == null) {
        iDalsiVraceneTokeny = new LinkedList<LexerToken>();
      }
      iDalsiVraceneTokeny.add(iVracenyToken);
    }
    iVracenyToken = aToken;
  }

  private LexerToken popToken() {
    if (iVracenyToken != null) {
      final LexerToken vratime = iVracenyToken;
      if (iDalsiVraceneTokeny != null && ! iDalsiVraceneTokeny.isEmpty()) {
        iVracenyToken = iDalsiVraceneTokeny.remove(0);
      } else {
        iVracenyToken = null;
      }
      return vratime;
    } else {
      return null;
    }
  }

  /**
   * Veškeré tokeny, které byly metodou unread vráceny do proudu tokenů jsou vráceny zpět do PushBackReaderu
   * a je nastavena původní pozice. Metoda funguje pouze v případě, kdy tokeny předávané ve volání unread()
   * byly přečteny metodou read() téhož objektu.
   * <p>Metoda je vhodná tehdy, když tokeny byly vraceny napříkald následným LALR parserem,
   * parserování skončilo a zbytek vstupu chceme číst po znacích nebo použít jiný lexer.
   */
  public void unreadUnreadedTokens() {
    try {
      if (iVracenyToken == null)
      {
        return;  // nedělám nic, pokud takové tokeny nemám
      }
      if (iDalsiVraceneTokeny != null) {
        for (final ListIterator<LexerToken> i =  iDalsiVraceneTokeny.listIterator(iDalsiVraceneTokeny.size()); i.hasPrevious(); ) {
          final LexerToken tok = i.previous();
          if (! (tok instanceof LexerToken)) {
            throw new IllegalStateException("Byl pouzit token typu " + tok.getClass() + " je vyzadovan token typu CToken");
          }
          final LexerToken ctok = tok;
          iInput.unread(ctok.getText().toCharArray()); // vrátíme zpět
        }
      }
      if (! (iVracenyToken instanceof LexerToken)) {
        throw new IllegalStateException("Byl pouzit token typu " + iVracenyToken.getClass() + " je vyzadovan token typu CToken");
      }
      final LexerToken ctok = iVracenyToken;
      iInput.unread(ctok.getText().toCharArray()); // vrátíme zpět
      // nastavíme zpět pozici pro případ, že se vracely tokeny přes více řádků
      final LexerTokenLocator tl = ctok.getLocator();
      iInput.setLineNumber(tl.getBegLineNumber());
      iInput.setColumnNumber(tl.getBegColumnNumber());
      iInput.setPosition(tl.getBegPosition());
    }
    catch (final IOException ex) {
      throw new RuntimeException("vraceni tokenu do pushbacku ", ex);
    }
  }


  /**
   * Definice tokentypu. Vzniká při definici nového tokenu a nese veškeré infomrace, které jsou potřeba.
   */
  public class TokenDef extends DefinitionId {

    private static final long serialVersionUID = -2359402225248398988L;

    private TokenDef(final String aRegExp, final Automaton aAuto
        , final Object aTokenType, final ValueParser aValueMapper, final boolean aIsIdentifier) {
      super(aRegExp, aAuto, aTokenType, aValueMapper, aIsIdentifier);
    }
  }

  /**
   * Nastaví továrnu, jenž bude vyrábět tokeny. Metodu není potřeba obvykle použít,
   * protože továrna je implicitně nastavena a je použitelná.
   * @param aFaktory
   */
  public void setTokenFactory(final LexerTokenFactory aFaktory) {
    if (aFaktory == null) {
      throw new NullPointerException("Tovarna na tokeny musi byt definovana");
    }
    iTokFak = aFaktory;
  }

  /**
   * Vrátí továrnu, která generuje tokeny. Vhodné, pokud potřebuji vytvářet obdobné tokeny a tak švindlovat.
   * @return Továrna na vytváření tokenů.
   */
  public LexerTokenFactory getTokenFactory() {
    return iTokFak;
  }

  /**
   * Vlastní implementace TokenDef. Zůstává pouze z důvodu kombatibility.
   */
  public abstract class DefinitionId implements java.io.Serializable {

    private static final long serialVersionUID = -866647335580305582L;

    private final String iRegExp;
    private Object iTokenType;
    private ValueParser iValueMapper;
    private final Automaton iAuto;
    private boolean iForIgnoring;

    //Zda představuje identifikátor či nikoliv
    private final boolean isIdentifier;

    private DefinitionId(final String aRegExp, final Automaton aAuto
        , final Object aTokenType, final ValueParser aValueMapper, final boolean aIsIdentifier) {

      iRegExp = aRegExp;
      iTokenType = aTokenType;
      iValueMapper = aValueMapper;
      iAuto = aAuto;
      isIdentifier = aIsIdentifier;
    }

    @Override
    public String toString() {
      if (iTokenType == this) {
        return "";
      } else {
        return "RegExp: '" + iRegExp + "' TokenType=" + iTokenType + (iTokenType == null ? "" : ":" + iTokenType.getClass().getName());
      }
    }


    /**
     * Vrátí hodnotu parserovače hodnoty přiřazenou danému tokenu.
     * @return Hodntoa parserovače.
     */
    public ValueParser getValueParser() { return iValueMapper; }

    /**
     * Vrátí token typ přiřazený danému tokenu.
     * @return Token typ, pokud je vráceno null, znamená to, že se jedná o ignorovaný token.
     */
    public Object  getTokenType() { return iTokenType; }

    /**
     * Nastaví typ tokenu, jenž bude vracen. Může být provedeno naprosto kdykoli i během lexerování.
     * @param aTokentype Typ tokenu, jenž bue vracen. Je to zcela libovolný objekt.
     */
    public void setTokenType(final Object aTokentype, final ValueParser aValueParser) {

      iTokenType = aTokentype;
      iValueMapper = aValueParser;
    }

    /**
     * Vrátí regulární výraz na jehož ákladě vniknul token.
     * @return Jen pro infomraci případně ladění. Vrací null, pokud token není definován na základě regulárního výrazu.
     */
    public String  getRegExp() { return iRegExp; }

    /**
     * Zjsití, zda token definuje klíčoví slovo.
     * @return True, pokud token definuje klíčové slovo, false jinak.
     */
    public boolean isKeyword() { return iAuto == null; }

    /**
     * Zjistí, zda token definuje identifikátor.
     * @return True, pokud token definuje identifikátor, false jinak.
     * Identifikátor je použit pro případné rozpoznání klíčových slov.
     */
    public boolean isIdentifier() { return isIdentifier; }

    /**
     * Nastaví token pro ignorování parserem, nikoli lexerem.
     * @param aForIgnoring True, pokud je daný token určen pro ignorování parserem.
     */
    public void setForIgnoring(final boolean aForIgnoring) {

      iForIgnoring = aForIgnoring;
    }

    /**
     * Zjistí, zda je token nastaven pro ignorování parserem. To neznamená, že bude též ignorován lexerem. Naopak,
     * parseru bude předán, aby ho ignoroval.
     * @return True, pokud je token určen pro ignorování parserem
     */
    public boolean isForIgnoring() {
      return iForIgnoring;
    }

    /**
     * Vrací lexer, v němž je definice trokenu zařazena.
     * @return Lexer, v němž je definice trokenu zařazena.
     */
    public QuickSimpleLexer0 getOwnerLexer() {
      return QuickSimpleLexer0.this;
    }


    Automaton getAutomaton() { return iAuto; }

  }

}




