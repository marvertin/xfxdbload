package cz.tconsult.lib.ifxdbload.core.splparser;

import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_ALTER;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_BEGIN;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_CALL;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_COMMIT;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_CREATE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_DBA;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_DELETE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_DROP;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_END;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_EXECUTE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_FUNCTION;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_INDEX;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_INSERT;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_INTO;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_PROCEDURE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_RENAME;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_ROLLBACK;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_SELECT;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_SET;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_SYNONYM;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_TABLE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_TEMP;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_TRIGGER;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_UNIQUE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_UPDATE;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_VIEW;
import static cz.tconsult.lib.spllexer.ESplTokenKeyword.KEYWORD_WORK;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import cz.tconsult.lib.ifxdbload.core.tw.NamedString;
import cz.tconsult.lib.lexer.LexerToken;
import cz.tconsult.lib.spllexer.CSplLexer;
import cz.tconsult.lib.spllexer.ESplTokenKeyword;
import cz.tconsult.lib.spllexer.ESplTokenNoKeyword;
import cz.tconsult.lib.tuples.Tuple2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Parsruje Spl
 * @author veverka
 *
 */
public class SplParser {


  /** za všemi tokeny je toto, pro jednodušší zpracování  pro možnost nekončit středníkem */
  private enum KONECNIK { KONECNIK };
  private final CSplLexer lexer;


  /**
   * Stvoření parseru relativně dlouho trvá, protož se zde vytváří i lexer.
   * Proto je vhodné jednou vtvořit parser a pak parsrovat jednotlivé procedury.
   * Například pro procedury v RSTS se jednorázovým vytvoením parseru zrychlylo zpracování všech procedur 10 z minuty na 6 sekund.
   * Nemá však smysl optimalizovat mezi fázemi
   *
   */
  public SplParser() {
    lexer = new CSplLexer();
    //lexer.setIgnoreWhiteSpacesAndComments(true);
    lexer.setEndToken(KONECNIK.KONECNIK);
  }

  /**
   * Parsruje zadaná data, přicházející ze zadaného zdroje.
   * @param data
   * @param sourceName
   * @return
   */
  public ParseredSource parse(final NamedString source) {
    try {
      final List<LexerToken> tokens = lexer.lex(source.getData(), source.getName().toString());
      final List<SplStatement> result = parseAll(new TokenIterator<LexerToken>(tokens));
      OnceDirectiveV1Hack.hackniV1DirektivyDoVysledku(result, source.getData());
      return new ParseredSource(source, result);
    } catch (final YParseError e) {
      // toto je výjimkka způsobená špatným tokenem, je prostě očekáváno něco jiného
      return new ParseredSource(source, e.getBadToken());
    } catch (final Exception e) {
      // toto je výjika způsobená sšpatným parsrem, ne chybou v parsrovaném
      throw new RuntimeException("Parsing \"" + source.getName() + "\"", e);
    }
  }

  private Tuple2<Optional<SplStatement>,  TokenIterator<LexerToken>> parseOneStatement(final TokenIterator<LexerToken> tokenIterator) throws YParseError {
    final Set<LexerToken> badTokens = new LinkedHashSet<>();
    for(final ParserTry t : tries) {
      try {
        final TokenIterator<LexerToken> ti = tokenIterator.copy(); // vždy zkoušíme na nové kopii, aby to jelo od začátku
        final Optional<SplStatement> stm = t.tryParse(new TokenChecker2(ti));
        return new Tuple2<>(stm, ti);  // vrátíme rozparsrovaný příkaz a tu kopii iterátoru, na které skončil úspěšně rozparsrovaný token
      } catch (final YCannotParse e) { // když pokus selhal, nezbývá, než jet dál a zkusit něco jiného, to je úplně normální stav
        badTokens.add(e.getBadToken());
      }
    }
    // Je tam neznámý token, takže se parsrování souboru ukončí a nahlásí chyba. Nic z něj není k dispozici.
    final Optional<LexerToken> lastToken = badTokens.stream().collect(Collectors.maxBy( (t1,t2) -> t1.getLocator().getBegPosition() -  t2.getLocator().getBegPosition()));
    throw new YParseError(lastToken.get()); // jeden token tam určitě bude
  }

  /**
   * Parsruje všechny tokeny z daného iterátoru do konce a vrátí jejich seznam.
   * @param tokenIterator
   * @return
   */
  private List<SplStatement> parseAll(final TokenIterator<LexerToken> tokenIterator) throws YParseError {
    // elegantní funkcionální řešení, ale v Javě ta elegance moc nevynikne
    if (tokenIterator.isAtEnd()) {
      return Collections.emptyList();
    } else {
      final Tuple2<Optional<SplStatement>, TokenIterator<LexerToken>> result = parseOneStatement(tokenIterator);
      final List<SplStatement> list = parseAll(result.get2());
      return ListUtils.union(result.get1().map(Collections::singletonList).orElse(Collections.emptyList()), list);
    }
  }



  @SuppressWarnings("serial")
  @RequiredArgsConstructor
  @Getter
  private static class YParseError extends Exception {
    private final LexerToken badToken;
  }

  ///////// Parsery jednotlivých objektů //////


  static ParserTry RUTINY = tok -> {
    tok.expect(ESplTokenKeyword.KEYWORD_CREATE);
    tok.optional(ESplTokenKeyword.KEYWORD_DBA);
    final Object procOrFunc = tok.expect(KEYWORD_PROCEDURE, KEYWORD_FUNCTION).getType();
    tok.shiftUntil(ESplTokenKeyword.KEYWORD_END);
    tok.expect(procOrFunc);
    return null;
  };


  private static EStmType convertStmType(final LexerToken keyword) {
    final ESplTokenKeyword kwd = (ESplTokenKeyword) keyword.getType();
    switch (kwd) {
    case KEYWORD_PROCEDURE: return EStmType.PROCEDURE;
    case KEYWORD_FUNCTION: return EStmType.FUNCTION;
    case KEYWORD_VIEW: return EStmType.VIEW;
    case KEYWORD_TRIGGER: return EStmType.TRIGGER;
    case KEYWORD_SYNONYM: return EStmType.SYNONYM;
    case KEYWORD_TABLE: return EStmType.TABLE;
    case KEYWORD_INDEX: return EStmType.INDEX;
    default: throw new RuntimeException("Nečekané klíčové slovo, je to chyba v zavaděči!");
    }
  }

  private static final ParserTry[] tries = new ParserTry[] {
      // Středník
      tok -> {
        tok.expect(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK);
        return Optional.empty();
      },

      // Rutina, tedy perocedura nebo funkce
      tok -> {
        final List<LexerToken> directives = tok.shiftWhile(ESplTokenNoKeyword.TCDIRECTIVE);
        tok.startCollecting();
        tok.expect(KEYWORD_CREATE);
        tok.optional(KEYWORD_DBA);
        final LexerToken keyword = tok.expect(KEYWORD_PROCEDURE, KEYWORD_FUNCTION);
        final LexerToken nazev = tok.expectNazev();
        do {
          tok.shiftUntil(KEYWORD_END);
          tok.expect(KEYWORD_END);
        } while (! tok.isOneOf(keyword.getType()));
        tok.expect(keyword.getType());
        return Optional.of(tok.createStatement(directives, convertStmType(keyword), nazev));
      },

      // view, triggery, synonyma
      tok -> {
        final List<LexerToken> directives = tok.shiftWhile(ESplTokenNoKeyword.TCDIRECTIVE);
        tok.startCollecting();
        tok.expect(KEYWORD_CREATE);
        tok.optional(KEYWORD_UNIQUE); // kvůli indexům
        tok.optional(KEYWORD_TEMP); // kvůli temporárním tabulkám
        final LexerToken keyword = tok.expect(
            KEYWORD_VIEW,
            KEYWORD_TRIGGER,
            KEYWORD_SYNONYM,
            KEYWORD_TABLE,
            KEYWORD_INDEX
            );
        final LexerToken nazev = tok.expectNazev();
        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(directives, convertStmType(keyword), nazev));
      },

      // begin work
      tok -> {
        final List<LexerToken> directives = tok.shiftWhile(ESplTokenNoKeyword.TCDIRECTIVE);
        tok.startCollecting();
        tok.expect(KEYWORD_BEGIN);
        tok.expect(KEYWORD_WORK);
        return Optional.of(tok.createStatement(directives, EStmType.BEGINWORK, null));
      },

      // commit
      tok -> {
        tok.startCollecting();
        tok.expect(KEYWORD_COMMIT);
        tok.optional(KEYWORD_WORK);
        return Optional.of(tok.createStatement(null, EStmType.COMMIT, null));
      },

      // rollback
      tok -> {
        tok.startCollecting();
        tok.expect(KEYWORD_ROLLBACK);
        tok.optional(KEYWORD_WORK);
        return Optional.of(tok.createStatement(null, EStmType.ROLLBACK, null));
      },


      // DDL
      tok -> {
        final List<LexerToken> directives = tok.shiftWhile(ESplTokenNoKeyword.TCDIRECTIVE);
        tok.startCollecting();
        tok.expect(
            KEYWORD_ALTER,
            KEYWORD_DROP,
            KEYWORD_RENAME,
            KEYWORD_SET);

        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(directives, EStmType.DDL, null));
      },

      // DML
      tok -> {
        tok.startCollecting();
        tok.expect(
            KEYWORD_UPDATE,
            KEYWORD_INSERT,
            KEYWORD_DELETE

            );

        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(null, EStmType.DML, null));
      },

      // DML
      tok -> {
        tok.startCollecting();
        tok.expect(KEYWORD_SELECT);
        tok.shiftUntil(KEYWORD_INTO);
        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(null, EStmType.DML, null));
      },

      // CALL
      tok -> {
        final List<LexerToken> directives = tok.shiftWhile(ESplTokenNoKeyword.TCDIRECTIVE);
        tok.startCollecting();
        tok.expect(
            KEYWORD_EXECUTE,
            KEYWORD_CALL

            );

        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(directives, EStmType.CALL, null));
      },


  };

}
