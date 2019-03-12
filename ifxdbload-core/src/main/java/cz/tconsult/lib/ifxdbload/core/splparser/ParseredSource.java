package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.List;

import cz.tconsult.lib.ifxdbload.core.tw.NamedString;
import cz.tconsult.lib.lexer.LexerToken;
import lombok.Data;

/**
 * Reprezentuje parsrované etry, to znamená soubor nebo pentry ze zipu.
 *
 * Může být ve stavu, kdy je rozpársrováno nebo kdy obsahuje parsovací chyby.
 * Buď jedno nebo druhé.
 *
 * @author veverka
 *
 */
@Data
public class ParseredSource {


  private final NamedString source;

  /** rozpársrované příkazy */
  private final List<SplStatement> statements;

  private final LexerToken badToken;

  public ParseredSource(final NamedString source, final List<SplStatement> stms) {
    this.source = source;
    this.statements = stms;
    this.badToken = null;
  }

  public ParseredSource(final NamedString source, final LexerToken badTokens) {
    assert badTokens != null;
    this.source = source;
    this.statements = null;
    this.badToken = badTokens;
  }

  public List<SplStatement> getStatements() {
    if (statements == null) {
      throw new IllegalStateException("Byly chyby detekovány v " + source + ", není možné získávat seznam příkazů: " + badToken);
    }
    return statements;
  }

  public LexerToken getBadToken() {
    if (badToken == null) {
      throw new IllegalStateException("Žádné chyby nebyly detekovány v " + source + ", není možné získávat seznam chyb");
    }
    return badToken;
  }


  public boolean hasParseError() {
    return badToken != null;
  }

}
