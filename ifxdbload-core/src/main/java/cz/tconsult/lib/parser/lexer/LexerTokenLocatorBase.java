package cz.tconsult.lib.parser.lexer;

/** Použijte tuto třídu jako předka všech LexerTokenLocatorů, aby měly společný {@link #toString()}.
 * @author roztocil
 */
public abstract class LexerTokenLocatorBase implements LexerTokenLocator {
  
  public String toString() {
    String result = String.format(
        "\"%s\" (%d,%d -> %d,%d)",
        getInputSourceName(),
        getBegLineNumber() + 1, getBegColumnNumber()+ 1,
        getEndLineNumber() + 1, getEndColumnNumber() + 1
    );
    return result;
  }
}
