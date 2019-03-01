package cz.tconsult.lib.parser.spllexer;

import lombok.Data;

@Data
public class SplTokenDirective {

  /** scope je typicky "ONCE" */
  private final String scope;

  /** klíč direktivy */
  private final String key;

  /** hodnota direktivy*/
  private final String value;

}
