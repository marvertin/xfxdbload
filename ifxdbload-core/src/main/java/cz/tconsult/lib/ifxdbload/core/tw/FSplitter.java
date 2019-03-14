package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class FSplitter {

  public static List<String> splitByComma(final String s) {
    if (s == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(s.split("\\s*,[\\s,]*"))
        .filter(x -> ! StringUtils.isBlank(x))
        .map(String::trim)
        .collect(Collectors.toList());
  }
}
