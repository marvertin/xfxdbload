package cz.tconsult.lib.parser.lexer;

import java.util.List;
import java.util.Map;

/**
 * Jednoduchá obecná implementace lexeru. Je poskytnuta proto, aby nemusely pro každý lexer být vytvářeny zvláštní třídy. Tato třída
 * nepřidává žádné metody, pouze některé metody, které jseu v předkovi jako protected zveřeňuje, aby mohli být použity mimo.
 * 
 * <pre>
 *   QuickSimpleLexer lex = new QuickSimpleLexer();
 *   lex.defineToken(&quot;JEDNA&quot;, &quot;JEDNA&quot;);
 *   lex.defineToken(&quot;DVA&quot;, &quot;DVA&quot;);
 *   lex.defineToken(&quot;0x[0-9a-fA-F]+&quot;);
 *   lex.defineToken(&quot;[a-zA-Z][a-zA-Z0-9]*&quot;);
 *   lex.defineToken(&quot;[0-9]+&quot;, &quot;INTEGER&quot;);
 *   lex.defineToken(&quot;[0-9]+\\.&quot;, &quot;ZMRSENYINTEGER&quot;);
 *   lex.defineToken(&quot;[0-9]\\.&quot;, &quot;INTEGER&quot;);
 *   lex.defineToken(&quot;[0-9]+\\.[0-9]+(e([\\+\\-])?[0-9])?&quot;, &quot;REAL&quot;);
 *   lex.defineToken(&quot;+|-|\\*|/|=&quot;, &quot;OPERATOR&quot;);
 *   lex.defineIgnoredToken(&quot;[\r\n ]+&quot;);
 *   lex.compile();
 *   List vysl1 = lex.lex(&quot;slovo         dalsi DVA\n dva 124 0x145fce  18.3abc + 147 * \n JEDNA ahoj 19.0e3 asdf - / \r\n neco ji23Ne slovo dalsi 18.45e-19       &quot;);
 *   List vysl2 = lex.lex(&quot;POMOCNE: &quot; + lex.lex(new File(&quot;X:\\Tools\\Source\\ParserJavaLib\\test\\testlexerinput.txt&quot;)));
 * }
 * </pre>
 * 
 * <p>
 * Na uvedeném příkladu jsou použity pomocné metody, které zjednodušují práci, lze však použít standardní způdob popsaný u třídy
 * předka.
 * 
 * @author Martin Veverka
 * @version 1.0
 */
public final class QuickSimpleLexer extends QuickSimpleLexer0 {

  private static final long serialVersionUID = -4268736381825371225L;

  /**
   * Zkonstruuje prázdný lexer.
   */
  public QuickSimpleLexer() {}

  public TokenDef defineToken(String aRegExp, Object aTokenType) {
    return super.defineToken(aRegExp, aTokenType);
  }

  public void setEndToken(Object aTokenType) {
    super.setEndToken(aTokenType);
  }

  public void compile() {
    super.compile();
  }

  public void setErrorToken(Object aTokenType) {
    super.setErrorToken(aTokenType);
  }

  public TokenDef defineIgnoredToken(String aRegExp) {
    return super.defineIgnoredToken(aRegExp);
  }

  public TokenDef defineToken(String aRegExp) {
    return super.defineToken(aRegExp);
  }

  protected TokenDef defineToken(String aRegExp, Object aTokenType, ValueParser aValueParser) {
    return super.defineToken(aRegExp, aTokenType, aValueParser);
  }

  protected TokenDef defineIdentifier(String aRegExp, Object aTokenType) {
    return super.defineIdentifier(aRegExp, aTokenType);
  }

  protected void setKeywordsCaseSensitive(boolean aIsCaseSensitive) {
    super.setKeywordsCaseSensitive(aIsCaseSensitive);
  }

  protected TokenDef defineKeyword(String aKeyword, Object aTokenType) {
    return super.defineKeyword(aKeyword, aTokenType);
  }

  public List<TokenDef> getTokenDefs() {
    return super.getTokenDefs();
  }

  public Map<String, TokenDef> getKeywordMap() {
    return super.getKeywordMap();
  }

}
