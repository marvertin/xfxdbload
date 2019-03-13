package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cz.tconsult.lib.lexer.LexerToken;
import cz.tconsult.lib.spllexer.ESeplTokenForIgnoring;
import cz.tconsult.lib.spllexer.ESplTokenKeyword;
import cz.tconsult.lib.spllexer.ESplTokenNoKeyword;
import cz.tconsult.lib.spllexer.SplDirective;

class TokenChecker2 extends TokenChecker {

  /** Posbíraný aktuálně parsrovaný příkaz */
  private StringBuilder sb;

  /** Posbíraný aktuálně parsrovaný příkaz pro účely výpoču heše, tedy bez bílých znaků, jen tokeny. */
  private StringBuilder sbHash;

  /** první token příkazu, jenž se začínal sbírat do sb */
  private LexerToken firstToken;

  public TokenChecker2(final TokenIterator<LexerToken> it) {
    super(it);
  }


  @Override
  protected void onShift(final LexerToken token) {
    if (sb != null) {
      if (token.getType() == ESeplTokenForIgnoring.COMMENTARY && token.getText().startsWith("//")) {
        // Komentář udělaný z dvojice lomítek převedeme na stnadardní komentář
        sb.append("--");
        sb.append(token.getText().substring(2));
      } else {
        sb.append(token.getText());

      }
    }
    if (sbHash != null) {
      final Object tokenType = token.getType();
      if (tokenType instanceof ESplTokenKeyword) {
        sbHash.append(token.getText().toUpperCase());
        sbHash.append('|');
      } else if (tokenType instanceof ESplTokenNoKeyword) {
        final ESplTokenNoKeyword tt = (ESplTokenNoKeyword) tokenType;
        switch (tt) {
        case IDENT:
          sbHash.append(token.getText().toLowerCase());
          sbHash.append('|');
          break;
        case STRING:
          sbHash.append('"');
          sbHash.append(token.getValue().toString());
          sbHash.append('"');
          sbHash.append('|');
          break;
        case TCDIRECTIVE:
          break;
        default:
          sbHash.append(token.getText());
        }
      }
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
    sbHash = new StringBuilder();
    firstToken = rawGet();
  }

  public SplStatement createStatement(final List<LexerToken> directives, final EStmType type, final LexerToken nazev) {
    final Set<SplDirective> directs = directives == null ? Collections.emptySet() :
      directives.stream()
      .map(LexerToken::getValue)
      .map( x -> (SplDirective) x)
      .collect(Collectors.toSet());
    return new SplStatement(directs, type, nazev == null ? null : (String) nazev.getValue(), sb.toString().trim(), sbHash.toString().trim(), firstToken.getLocator());
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
