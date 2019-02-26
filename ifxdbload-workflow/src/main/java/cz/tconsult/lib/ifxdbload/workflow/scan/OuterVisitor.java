package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import lombok.RequiredArgsConstructor;

/**
 * Vnější visitor navštěvuje jen adresáře a pokud v nich najde dbpack.properties, tak pouští vnitční visitory.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class OuterVisitor extends SimpleFileVisitor<Path> {

  private final FileContentReceiver builder;

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    final Optional<DbpackProperties> dbpackProps = FDbpackPropertis.readDbpackDirProperties(dir);
    if (dbpackProps.isPresent()) {
      Files.walkFileTree(dir, new InnerVisitor(dbpackProps.get(), builder));
      return FileVisitResult.SKIP_SUBTREE; // prohleali jsme vnitřním visitorem, tak není co řešit
    } else {
      return FileVisitResult.CONTINUE; // pokračujeme dolů a dále
    }
  }
}
