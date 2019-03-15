package cz.tconsult.lib.ifxdbload.core.loaders.once;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingLong;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.io.Resources;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EStatementStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class OnceScriptDao {
  private final JdbcTemplate jt;


  public Map<String, Long> readChecksums() {
    return jt.queryForList("SELECT scriptid, checksum FROM xonce_scripts").stream()
        .collect(Collectors.groupingBy(y -> (String)y.get("scriptid"), // rozřadit podle názvu
            mapping(z -> (Long)z.get("checksum"), summingLong(a -> a)))); // a hodnoty zesumovat, prakticky se však nic nesumuje, protože hodnota je tam jen jednou
  }

  //  checksum, status, comments, description,
  public void updateEvidence(final Record record, final EStatementStatus status) {

    final Object[] params = {
        status.toString(),
        record.getChecksum(),
        record.getDescription(),
        record.getComments(),
        record.getScriptid(),
    };

    if (jt.update(sql("xonceScriptsUpdate.sql"), params) == 0) { // zkusit updatnout
      jt.update(sql("xonceScriptsInsert.sql"), params); // a když to tam nebylo, tak vložit
    }
  }



  @SneakyThrows
  private String sql(final String sqlResoruceName) {
    return Resources.toString(Resources.getResource("sql/" + sqlResoruceName), StandardCharsets.UTF_8);
  }

  @Data
  public static final class Record {
    private final long checksum;
    private final String description;
    private final String comments;
    private final String scriptid;
  }
}
