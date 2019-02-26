package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbpackScaner {

  //  private static final ADbkind DBKIND_MAIN = ADbkind.of("main");
  //  // TODO [veverka] co znamená tkové defaultn íschema? -- 26. 2. 2019 9:23:18 veverka
  //  private static final ASchema DEFAULT_SCHEMA = ASchema.of("<defalut>");


  private final FileContentReceiver fcb;


  public void read(final Path dir) throws IOException {
    Files.walkFileTree(dir, new OuterVisitor(fcb));
  }
}
