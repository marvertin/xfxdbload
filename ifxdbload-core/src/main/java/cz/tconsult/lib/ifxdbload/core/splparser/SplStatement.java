package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.Set;

import cz.tconsult.lib.lexer.LexerTokenLocator;
import cz.tconsult.lib.spllexer.SplDirective;
import lombok.Data;

/**
 * Rozpársrovaný SPL příkaz.
 * @author veverka
 *
 */
@Data
public class SplStatement {
  /** seznam direktiv u tohoto příkazu */
  private final Set<SplDirective> directives;

  /** typ objektu */
  private final EStmType stmType;

  /** jméno objektu, tedy procedury, triggeru atd, pokud jméno existuje */
  private final String name;

  /** Text SQL příkazu */
  private final String text;

  /** Lokátor prvního tokenu příkazu, je zajímavý pro určení pozice */
  private final LexerTokenLocator firstTokenLocator;


  public String getNameLower() {
    if (name == null) {
      throw new NullPointerException("Chtěno jména po bezemenném objektu: " + this);
    }
    return name.toLowerCase();
  }
}
