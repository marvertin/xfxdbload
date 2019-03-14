package cz.tconsult.lib.ifxdbload.core.loaders.once;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import lombok.Data;

@Data
public class OnceCommand {
  private SplStatement stm;

}
