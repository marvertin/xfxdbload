package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import cz.tconsult.lib.ifxdbload.core.core.AEntryName;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Vnější visitor navštěvuje jen adresáře a pokud v nich najde dbpack.properties, tak pouští vnitční visitory.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class InnerVisitor extends SimpleFileVisitor<Path> {

  private static PathMatcher ZIP_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**/*.zip");

  private final DbpackProperties dbpackPros;
  private final FileContentReceiver builder;


  @Override
  public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
    if (ZIP_MATCHER.matches(file)) {
      final AEntryName entryName = entryName(file.getParent());
      new DbpakZipReader(builder).readOneDbpackZip(file, entryName, dbpackPros);
    } else {
      final AEntryName entryName = entryName(file);
      if (!FDbpackPropertis.isDbpackProperties(entryName)) {
        builder.add(dbpackPros, () -> readAllBytes(file), entryName);
      }
    }
    return FileVisitResult.CONTINUE;
  }


  private AEntryName entryName(final Path file) {
    return AEntryName.of(dbpackPros.getRoot().relativize(file).toString().replace('\\', '/'));
  }

  @SneakyThrows
  private byte[] readAllBytes(final Path file) {
    return Files.readAllBytes(file);
  }
}
