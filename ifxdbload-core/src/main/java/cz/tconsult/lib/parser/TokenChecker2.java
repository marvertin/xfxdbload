package cz.tconsult.lib.parser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cz.tconsult.parser.lexer.LexerToken;
import cz.tconsult.spl.lexer.ESplTokenNoKeyword;
import cz.tconsult.spl.lexer.SplDirective;

class TokenChecker2 extends TokenChecker {

  private StringBuilder sb;

  public TokenChecker2(final TokenIterator<LexerToken> it) {
    super(it);
  }


  @Override
  protected void onShift(final LexerToken token) {
    if (sb != null) {
      sb.append(token.getText());
    }
  }

  /**
   * Vrátí nashromážděný text oprostěný od případných bílých znaků.
   * @return
   */
  String currentText() {
    return sb.toString().trim();
  }

  public void startCollecting() {
    sb = new StringBuilder();
  }

  public SplStatement createStatement(final List<LexerToken> directives, final EStmType type, final LexerToken nazev) {
    final Set<SplDirective> directs = directives == null ? Collections.emptySet() :
      directives.stream()
      .map(LexerToken::getValue)
      .map( x -> (SplDirective) x)
      .collect(Collectors.toSet());
    return new SplStatement(directs, type, nazev == null ? null : (String) nazev.getValue(), sb.toString());
  }

  /**
   * Očekává se název v jednom z tvarů:
   *    ab_procedura
   *    "aris".ab_procedura
   *    aris.ab_procedura
   *
   * @return
   * @throws YCannotParse
   */
  public LexerToken expectNazev() throws YCannotParse {

    if (optional(ESplTokenNoKeyword.STRING).isPresent()) { // to by byl vlastník
      expect(ESplTokenNoKeyword.SYMBOL_PERIOD); // a když tam je, musí být tečka
      return expect(ESplTokenNoKeyword.IDENT);
    } else {
      final LexerToken kandidatNaNazev = expect(ESplTokenNoKeyword.IDENT);
      if (optional(ESplTokenNoKeyword.SYMBOL_PERIOD).isPresent()) {
        return expect(ESplTokenNoKeyword.IDENT); // název je jako druhý za vlastníkem
      } else {
        return kandidatNaNazev;
      }
    }
  }


}
