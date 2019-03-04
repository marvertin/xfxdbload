package cz.tconsult.lib.parser.lexer;

/** Třída poskytuje často používané regulární výrazy pro definici tokenů v lexerech.
 * Regulární výrazy se dají použít ve funkcích defineToken, defineIgnoredToken apod. na třídě
 * <A HREF="QuickSimpleLexer0.html">QuickSimpleLexer0</A>.
 * Pozor, nejedná se o regulární výrazy obsažené v JDK ale o regulární výrazy
 * implementované podle GNU, viz. QuickSimpleLexer0.defineToken(java.lang.String, java.lang.Object).
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Michal Polák, Martin Veverka
 * @version 1.0
 */
public class FBasicTokenRegExps {

  /**
   * Obyčejný identifikátor. Specifikace: <pre>[a-zA-Z][a-zA-Z0-9]*</pre>
   */
  public static final String IDENT = "[a-zA-Z][a-zA-Z0-9]*";

  /**
   * Obyčejný identifikátor s podtržítky (nesmí začínat cifrou). Specifikace: <pre>[a-zA-Z_][a-zA-Z0-9_]*</pre>
   */
  public static final String IDENT_UNDERSCORE = "[a-zA-Z_][a-zA-Z0-9_]*";

  /**
   * Obyčejný identifikátor s podtržítky a dolarem (dolarem a cifrou nesmí začínat). Specifikace: <pre>[a-zA-Z_][a-zA-Z0-9_$]*</pre>
   */
  public static final String IDENT_UNDERSCORE_DOLLAR = "[a-zA-Z_][a-zA-Z0-9_$]*";

  /**
   * Oracle identifikátor obalen uvozovkami, pak se v něm může vyskytovat prakticky cokoliv (kromě uvozovek).
   * Specifikace: <pre>"[^"]*"</pre>
   */
  public static final String ORACLE_QUOTED_IDENT = "\\\"[^\\\"]*\\\"";


  /**
   * Číslice. Specifikace: <pre>[0-9]</pre>
   */
  public static final String CIPHER = "[0-9]";

  /**
   * Bezznaménkové holé celé číslo (polsoupnost cifer). Specifikace: <pre>[0-9]+</pre>
   */
  public static final String PUREINT = CIPHER + "+";

  /**
   * Bílý znak. Specifikace: <pre>[\t\r\n ]</pre>
   */
  public static final String WHITE_SPACE = "[\uFEFF\t\r\n ]";// to první je BOM

  /**
   * Posloupnost bílých znaků. Specifikace: <pre>[\t\r\n ]+</pre>
   */
  public static final String WHITE_SPACES = WHITE_SPACE + "+";

  /**
   * Komentář <cite> /&#42; &#46;&#46;&#46; &#42;/ </cite>(jako v například v C).
   * <BR>Specifikace v lex: <pre> "/&#42;"([^\*])*"*"("*"|([^\&#42;/]([^\*])*"*"))*"/" </pre>
   * Specifikace v java: <pre> /\\*([^\\*])*\\*(\\*|([^\\&#42;/]([^\\*])*\\*))&#42;/ </pre>
   */
  public static final String COMMENTARY_C = "/\\*([^\\*])*\\*(\\*|([^\\*/]([^\\*])*\\*))*/";

  /**
   * Komentář ve složených uvozovkách (jako v například v Pascal). Specifikace: <pre>{[^}]*}</pre>
   */
  public static final String COMMENTARY_PASCAL = "{[^}]*}";

  /**
   * Komentář od dvou slashů do konce řádku. Specifikace: <pre>//[^\r\n]*[\r\n]?</pre>
   */
  public static final String COMMENTARY_SLASH = "//[^\r\n]*[\r\n]?";

  /**
   * Komentář od dvou pomlček do konce řádku. Specifikace: <pre>--[^\r\n]*[\r\n]?</pre>
   */
  public static final String COMMENTARY_DASH = "--[^\r\n]*[\r\n]?";

  /**
   * Řetězcový literál pomocí apostrofů, znak apostrofu (literál) se specifikuje zdvojením apostrofů. Specifikace: <pre>('[^'\r\n]*')+</pre>
   */
  public static final String STRING_APOSTROPHE_ESCAPE_BY_APOSTROPHE = "('[^'\r\n]*')+";

  /**
   * Řetězcový literál pomocí uvozovek, znak uvozovek (literál) se specifikuje zdvojením uvozovek.
   * <BR>Specifikace: <pre>("[^"\r\n]*")+</pre>
   * Specifikace v java: <pre>(\\\"[^\\\"\r\n]*\\\")+</pre>
   */
  public static final String STRING_QUOTATION_MARK_ESCAPE_BY_QUOTATION_MARK = "(\\\"[^\\\"\r\n]*\\\")+";

  /**
   * Řetězcový literál pomocí apostrofů, znak apostrofu (literál) se specifikuje pomocí tildy a apostrofu. Specifikace: <pre>'([^'~]|~.)*'</pre>
   */
  public static final String STRING_APOSTROPHE_ESCAPE_BY_TILDE = "'([^'\\~]|\\~.)*'";

  /**
   * Řetězcový literál pomocí uvozovek, znak uvozovek (literál) se specifikuje pomocí tildy a uvozovky. Specifikace: <pre>"([^"~]|~.)*"</pre>
   */
  public static final String STRING_QUOTATION_MARK_ESCAPE_BY_TILDE = "\\\"([^\\\"\\~]|\\~.)*\\\"";

  /**
   * Řetězcový literál pomocí apostrofů, znak apostrofu (literál) se specifikuje pomocí backslashe a apostrofu. Specifikace: <pre>'([^'\\]|(\\'?))*'</pre>
   */
  public static final String STRING_APOSTROPHE_ESCAPE_BY_BACKSLASH = "'([^'\\\\]|(\\\\'?))*'";

  /**
   * Řetězcový literál pomocí uvozovek, znak uvozovek (literál) se specifikuje pomocí backslashe a uvozovky. Specifikace: <pre>"([^"\\]|(\\"?))*"</pre>
   */
  public static final String STRING_QUOTATION_MARK_ESCAPE_BY_BACKSLASH = "\\\"([^\\\"\\\\]|(\\\\\\\"?))*\\\"";

  public static final String BACKSLASH_ON_ROW_END = "\\\\[\r\n]";
}
