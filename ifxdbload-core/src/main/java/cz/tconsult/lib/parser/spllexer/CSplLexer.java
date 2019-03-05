package cz.tconsult.lib.parser.spllexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import cz.tconsult.lib.parser.lexer.FBasicTokenRegExps;
import cz.tconsult.lib.parser.lexer.QuickSimpleLexer0;
import cz.tconsult.lib.parser.lexer.ValueParser;

/**
 * Lexikální analyzátor tetu SQL a SPL pro Informix, bez SPL loadrovských mřížních direktiv.
 * @author Michal Polák, Martin Veverka
 * @version 1.0
 */
public class CSplLexer extends QuickSimpleLexer0 {

  /**
   *
   */
  private static final long serialVersionUID = 1L;


  /**
   * nastaví režim, zda mají být bílé znaky a komentáře ignorovány či nikoli. Režim je možné změnit kdykoli během lexerování,
   * změna začíná platit okamžitě, to znamená od následného tokenu. Pokud byl ignorující režim změněn na neignorující,
   * je vrácen bezprostřední token s bílími znaky nebo komentáři za předchozím přečteným tokenem.
   * @param aIgnore Pokud je true, budou veškeré tokeny sestávající s bílých znaků ignorovány
   * a nebudou volajícímu vraceny jako tokeny. Pokud je false, ignorovány nebudou, ale budou vraceny volajícímu
   * jako normální tokeny. Tento režim je vhodný v případě, kdy při zpracování potřebuji bílé znaky případně zachovat.
   */
  public void setIgnoreWhiteSpacesAndComments(final boolean aIgnore) {
    for (final TokenDef item : getTokenDefs()) {
      if (item.isForIgnoring()) {
        if (aIgnore) {
          item.setTokenType(null, item.getValueParser());  // nastavením tokentypu na null zapneme ignorování
        } else {
          item.setTokenType(item, item.getValueParser());  // nastavením tokentypu na sebe vypneme ignorování. Token je však již z inicializace nastaven, ýže má být parserem ignorován.
        }
      }
    }
  }

  /**
   * Vytvoří instanci SPL lexeru. Implicitně neignoruje bílé znaky. Pokud je chcete ignorovat,
   * je nutné ignorování zapnout metodou {@link #setIgnoreWhiteSpacesAndComments}
   * @param aKompilujeLexer Pokud je true, kompilue se lexer přímo v konstruktoru,
   * pokud je false, zkonstruuje se, volající může provést ještě nějaké dodatečné operace a pak zkompilovat ručně.
   */
  public CSplLexer(final boolean aKompilujeLexer) {
    init();
    if  (aKompilujeLexer) {
      compile();
    }
  }

  /**
   * Vytvoří instanci SPL lexeru. Implicitně neignoruje bílé znaky. Pokud je chcete ignorovat,
   * je nutné ignorování zapnout metodou {@link #setIgnoreWhiteSpacesAndComments}
   * Lexer je zkompilován a jeho definici již není možné měnit.
   */
  public CSplLexer() {
    this(true);
  }


  /**
   * Vytvoří instanci SPL lexeru. Implicitně neignoruje bílé znaky. Pokud je chcete ignorovat,
   * je nutné ignorování zapnout metodou {@link #setIgnoreWhiteSpacesAndComments}
   *
   */
  private void init() {

    final ValueParser stringValueParser = new ValueParser() {
      @Override
      public Object parseValue(final String aText) {
        final char quoteMark = aText.charAt(0);
        if (quoteMark == '"' || quoteMark == '\'') {

          final String s = new String(new char[]{quoteMark});
          String result = aText.substring(1, aText.length() - 1);
          result = result.replaceAll(s + s, s);
          return result;
        } else {
          return aText;
        }
      }
    };


    defineToken("-- *@TC:[A-Z]+:[^\r\n]*[\r\n]?", ESplTokenNoKeyword.TCDIRECTIVE, TCDIRECTIVE_VALUE_PARSER);

    defineTokenForIgnoring(FBasicTokenRegExps.WHITE_SPACES, ESeplTokenForIgnoring.WITESPACEC);
    defineTokenForIgnoring(FBasicTokenRegExps.COMMENTARY_C, ESeplTokenForIgnoring.COMMENTARY);
    defineTokenForIgnoring(FBasicTokenRegExps.COMMENTARY_PASCAL, ESeplTokenForIgnoring.COMMENTARY);
    defineTokenForIgnoring(FBasicTokenRegExps.COMMENTARY_SLASH, ESeplTokenForIgnoring.COMMENTARY);
    defineTokenForIgnoring(FBasicTokenRegExps.COMMENTARY_DASH, ESeplTokenForIgnoring.COMMENTARY);


    //TC directives
    //defineToken("{@TCD[^}]*}", ESplTokenNoKeyword.TCDIRECTIVE);


    defineIdentifier(FBasicTokenRegExps.IDENT_UNDERSCORE_DOLLAR, ESplTokenNoKeyword.IDENT);
    defineToken(FBasicTokenRegExps.PUREINT, ESplTokenNoKeyword.PUREINT, ValueParser.BIGINTEGER);

    defineToken(FBasicTokenRegExps.STRING_APOSTROPHE_ESCAPE_BY_APOSTROPHE, ESplTokenNoKeyword.STRING, stringValueParser);
    defineToken(FBasicTokenRegExps.STRING_QUOTATION_MARK_ESCAPE_BY_QUOTATION_MARK, ESplTokenNoKeyword.STRING, stringValueParser);

    final String re = "(" + FBasicTokenRegExps.PUREINT + "(\\." + FBasicTokenRegExps.CIPHER +
        "*)?|" + FBasicTokenRegExps.CIPHER + "*\\." + FBasicTokenRegExps.PUREINT +
        ")([eE][\\-\\+]?" + FBasicTokenRegExps.PUREINT + ")?";

    //re = "[0-9]+([eE][0-9]+)?";
    //System.out.println(re);

    defineToken(re, ESplTokenNoKeyword.PUREREAL, ValueParser.BIGDECIMAL);

    defineToken("\\(", ESplTokenNoKeyword.SYMBOL_LEFT_PARENTHESIS, ValueParser.NULL);
    defineToken("\\)", ESplTokenNoKeyword.SYMBOL_RIGHT_PARENTHESIS, ValueParser.NULL);
    defineToken("\\[", ESplTokenNoKeyword.SYMBOL_LEFT_SQUARE_BRACKET, ValueParser.NULL);
    defineToken("\\]", ESplTokenNoKeyword.SYMBOL_RIGHT_SQUARE_BRACKET, ValueParser.NULL);
    defineToken(",", ESplTokenNoKeyword.SYMBOL_COMMA, ValueParser.NULL);
    defineToken("@", ESplTokenNoKeyword.SYMBOL_COMMERCIAL_AT, ValueParser.NULL);
    defineToken(":", ESplTokenNoKeyword.SYMBOL_COLON, ValueParser.NULL);
    defineToken(";", ESplTokenNoKeyword.SYMBOL_SEMICOLON, ValueParser.NULL);
    defineToken("\\<", ESplTokenNoKeyword.SYMBOL_LESS_THAN_SIGN, ValueParser.NULL);
    defineToken("=", ESplTokenNoKeyword.SYMBOL_EQUALS_SIGN, ValueParser.NULL);
    defineToken(">", ESplTokenNoKeyword.SYMBOL_GREATHER_THAN_SIGN, ValueParser.NULL);
    defineToken("\\+", ESplTokenNoKeyword.SYMBOL_PLUS_SIGN, ValueParser.NULL);
    defineToken("\\*", ESplTokenNoKeyword.SYMBOL_ASTERISK, ValueParser.NULL);
    defineToken("/", ESplTokenNoKeyword.SYMBOL_SLASH, ValueParser.NULL);
    defineToken("-", ESplTokenNoKeyword.SYMBOL_MINUS_SIGN, ValueParser.NULL);
    defineToken("\\.", ESplTokenNoKeyword.SYMBOL_PERIOD, ValueParser.NULL);
    defineToken("\\<=", ESplTokenNoKeyword.SYMBOL_LESS_OR_EQUALS_SIGN, ValueParser.NULL);
    defineToken(">=", ESplTokenNoKeyword.SYMBOL_GREATER_OR_EQUALS_SIGN, ValueParser.NULL);
    defineToken("==", ESplTokenNoKeyword.SYMBOL_EQUALS_EQUALS_SIGN, ValueParser.NULL);
    defineToken("!=", ESplTokenNoKeyword.SYMBOL_NOT_EQUALS_SIGN, ValueParser.NULL);
    defineToken("\\<>", ESplTokenNoKeyword.SYMBOL_NOT_EQUALS_SIGN, ValueParser.NULL);
    defineToken("\\|\\|", ESplTokenNoKeyword.SYMBOL_DOUBLE_VERTICAL_LINE, ValueParser.NULL);
    defineToken("::", ESplTokenNoKeyword.SYMBOL_DOUBLE_COLON, ValueParser.NULL);
    defineToken("\\<\\<", ESplTokenNoKeyword.SYMBOL_DOUBLE_LESS_THAN_SIGN, ValueParser.NULL);
    defineToken("\\>\\>", ESplTokenNoKeyword.SYMBOL_DOUBLE_GREATHER_THAN_SIGN, ValueParser.NULL);

    setKeywordsCaseSensitive(false);
    for (final ESplTokenKeyword kwd : ESplTokenKeyword.values()) {
      defineKeyword(kwd.getKeywordName(), kwd);
    }

    setErrorToken(ESplTokenNoKeyword.LEXERROR);
  }

  public static CSplLexer create() {
    return new CSplLexer();
  }


  /**
   * Virtuální metoda zajišťující mapování tokenu.<BR>
   * Typ tokenu IDENT převeden na odpovídající keyword výčtovou hodnotu, existuje-li hodnota tokenu mezi keywordy.<BR>
   * Hodnota převedena na třídu java.math.BigInteger, pokud typ tokenu PUREINT.<BR>
   * Hodnota převedena na třídu java.math.BigDecimal, pokud typ tokenu PUREREAL.<BR>
   * Z hodnoty odstraněny vnější apostrofy, ostatní zdvojené apostrofy nahrazeny jedním, pokud typ tokenu STRING (to samé pro uvozvoky).<BR>
   * @param aTokenHandle
   * @param aText
   */
  @Override
  protected void mapToken(final QuickSimpleLexer0.DefinitionId aTokenHandle,
      final java.lang.String aText) {
    //    ESplTokenType tokenType = (ESplTokenType)getCurrentTokenType();
    /*

    if (tokenType.equals(ESplTokenNoKeyword.STRING)) {

    }
    else if (tokenType.equals(ESplTokenNoKeyword.PUREREAL)) {

    }
     */

  }

  private static final Pattern DIRECTIVE_PATTERN = Pattern.compile("--\\s*@TC:([A-Z]+):\\s*(\\w+)\\s*(?:=(.*))?");
  private static final ValueParser TCDIRECTIVE_VALUE_PARSER = aValue -> {
    final Matcher matcher = DIRECTIVE_PATTERN.matcher(aValue.trim());
    if (matcher.matches()) {
      return new SplDirective(matcher.group(1).trim(), matcher.group(2).trim(), StringUtils.trim(matcher.group(3)));
    } else {
      throw new RuntimeException("nemůže nastat, aby \"" + aValue + "\" nevyhovovalo /" +  DIRECTIVE_PATTERN + "/");
    }
  };



  @Override
  public void setEndToken(final Object aTokenType) {
    super.setEndToken(aTokenType);
  }

}
