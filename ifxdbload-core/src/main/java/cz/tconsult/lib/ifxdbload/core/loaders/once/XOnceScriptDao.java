package cz.tconsult.lib.ifxdbload.core.loaders.once;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingLong;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class XOnceScriptDao {
  private final JdbcTemplate jt;


  public Map<String, Long> readChecksums() {
    return jt.queryForList("SELECT scriptid, checksum FROM xonce_scripts").stream()
        .collect(Collectors.groupingBy(y -> (String)y.get("scriptid"), // rozřadit podle názvu
            mapping(z -> (Long)z.get("checksum"), summingLong(a -> a)))); // a hodnoty zesumovat, prakticky se však nic nesumuje, protože hodnota je tam jen jednou
  }
}
