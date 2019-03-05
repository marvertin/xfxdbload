package cz.tconsult.lib.parser;

import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_ALTER;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_BEGIN;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_CALL;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_COMMIT;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_CREATE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_DBA;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_DELETE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_DROP;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_END;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_FUNCTION;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_INDEX;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_INSERT;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_PROCEDURE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_RENAME;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_ROLLBACK;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_SYNONYM;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_TABLE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_TRIGGER;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_UNIQUE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_UPDATE;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_VIEW;
import static cz.tconsult.lib.parser.spllexer.ESplTokenKeyword.KEYWORD_WORK;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;

import cz.tconsult.lib.parser.lexer.LexerToken;
import cz.tconsult.lib.parser.spllexer.CSplLexer;
import cz.tconsult.lib.parser.spllexer.ESplTokenKeyword;
import cz.tconsult.lib.parser.spllexer.ESplTokenNoKeyword;
import cz.tconsult.lib.tuples.Tuple2;

/**
 * Parsruje Spl
 * @author veverka
 *
 */
public class SplParser {


  /** za všemi tokeny je toto, pro jednodušší zpracování  pro možnost nekončit středníkem */
  private enum KONECNIK { KONECNIK };


  public SplParser() {
  }

  public List<SplStatement> parse(final String data, final String resourceName) {
    try {
      final CSplLexer lexer = new CSplLexer();
      //lexer.setIgnoreWhiteSpacesAndComments(true);
      lexer.setEndToken(KONECNIK.KONECNIK);
      final List<LexerToken> tokens = lexer.lex(data, resourceName);
      return parseAll(new TokenIterator<LexerToken>(tokens));
    } catch (final Exception e) {
      throw new RuntimeException("Parsing \"" + resourceName + "\"", e);
    }
  }

  private Tuple2<Optional<SplStatement>,  TokenIterator<LexerToken>> parseOneStatement(final TokenIterator<LexerToken> tokenIterator) {
    final List<Object> badTokens = new LinkedList<>();
    for(final ParserTry t : tries) {
      try {
        final TokenIterator<LexerToken> ti = tokenIterator.copy(); // vždy zkoušíme na nové kopii, aby to jelo od začátku
        final Optional<SplStatement> stm = t.tryParse(new TokenChecker2(ti));
        return new Tuple2<>(stm, ti);  // vrátíme rozparsrovaný příkaz a tu kopii iterátoru, na které skončil úspěšně rozparsrovaný token
      } catch (final YCannotParse e) { // když pokus selhal, nezbývá, než jet dál a zkusit něco jiného, to je úplně normální stav
        badTokens.add(e.getBadToken());
      }
    }
    // TODO [veverka] parserové chyby nesmí shazovat -- 4. 3. 2019 13:16:45 veverka
    throw new RuntimeException("PARSE SYNTAX ERROR on one of this tokens: " + new HashSet<>(badTokens));
  }

  /**
   * Parsruje všechny tokeny z daného iterátoru do konce a vrátí jejich seznam.
   * @param tokenIterator
   * @return
   */
  private List<SplStatement> parseAll(final TokenIterator<LexerToken> tokenIterator) {
    // elegantní funkcionální řešení, ale v Javě ta elegance moc nevynikne
    if (tokenIterator.isAtEnd()) {
      return Collections.emptyList();
    } else {
      final Tuple2<Optional<SplStatement>, TokenIterator<LexerToken>> result = parseOneStatement(tokenIterator);
      final List<SplStatement> list = parseAll(result.get2());
      return ListUtils.union(list, result.get1().map(Collections::singletonList).orElse(Collections.emptyList()));
    }
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
        tok.startCollecting();
        tok.expect(
            KEYWORD_ALTER,
            KEYWORD_DROP,
            KEYWORD_RENAME);

        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(null, EStmType.DDL, null));
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

      // CALL
      tok -> {
        tok.startCollecting();
        tok.expect(
            KEYWORD_CALL
            );

        tok.shiftUntil(ESplTokenNoKeyword.SYMBOL_SEMICOLON, KONECNIK.KONECNIK, ESplTokenNoKeyword.TCDIRECTIVE);
        return Optional.of(tok.createStatement(null, EStmType.CALL, null));
      },


  };

}
