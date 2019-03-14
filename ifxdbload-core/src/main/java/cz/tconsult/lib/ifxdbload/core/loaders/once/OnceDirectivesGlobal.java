package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.List;
import java.util.Optional;

import lombok.Data;

@Data
public class OnceDirectivesGlobal {

  private final String scriptid;
  private final String description;
  private final boolean noTransactionControl;
  private final boolean ignoreChecksum;
  private final boolean reloadAllways;
  private final Optional<String> loadUnlessLoaded;
  private final Optional<String> loadIfLoaded;
  private final List<String> dbkinds;


}
