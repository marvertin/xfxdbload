package cz.tconsult.lib.parser;

import java.util.Set;

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
}
