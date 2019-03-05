package cz.tconsult.spl.lexer;

import lombok.Data;

@Data
public class SplDirective {

  /** scope je typicky "ONCE" */
  private final String scope;

  /** klíč direktivy */
  private final String key;

  /** hodnota direktivy*/
  private final String value;

}
