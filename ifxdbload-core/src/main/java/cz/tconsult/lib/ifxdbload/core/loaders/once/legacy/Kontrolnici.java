package cz.tconsult.lib.ifxdbload.core.loaders.once.legacy;

import java.util.HashSet;
import java.util.Set;

public class Kontrolnici {

  private long mainChecksum;
  private final Set<Long> verifiedCheckSums = new HashSet<Long>();



  public long getMainChecksum() {
    return mainChecksum;
  }

  public boolean verifyChecksum(final long expectedCheckum) {
    if (expectedCheckum == mainChecksum) {
      return true;
    }
    for (final long c : verifiedCheckSums) {
      if (c == expectedCheckum) {
        return true;
      }
    }
    return false;
  }

  void addCheksum(final long checksum) {
    verifiedCheckSums.add(checksum);
  }

  @Override
  public String toString() {
    return mainChecksum + "" + verifiedCheckSums;
  }

  public void setMainChecksum(final long aMainChecksum) {
    mainChecksum = aMainChecksum;
    verifiedCheckSums.add(mainChecksum);
  }

}
