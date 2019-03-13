package cz.tconsult.lib.ifxdbload.core.loaders.once;

import lombok.Data;

@Data
public class OnceDirectivesGlobal {

  private final String scriptid;
  private final String description;
  private final boolean noTransactionControl;
  private final boolean ignoreChecksum;

}
