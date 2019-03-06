package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import lombok.RequiredArgsConstructor;

/**
 * Vnější visitor navštěvuje jen adresáře a pokud v nich najde dbpack.properties, tak pouští vnitční visitory.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class OuterVisitor extends SimpleFileVisitor<Path> {

  private static Pattern patDbpackRootDirName = Pattern.compile("([a-z]+)-([a-z][a-z0-9]+)");

  private final FileContentReceiver builder;

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    final Optional<DbpackProperties> dbpackProps = extractDbpackProps(dir);
    if (dbpackProps.isPresent()) {
      Files.walkFileTree(dir, new InnerVisitor(dbpackProps.get(), builder));
      return FileVisitResult.SKIP_SUBTREE; // prohleali jsme vnitřním visitorem, tak není co řešit
    } else {
      return FileVisitResult.CONTINUE; // pokračujeme dolů a dále
    }
  }

  /**
   * Zkusí extrahovat dbpack properties nejdříve z příslušného souboru, a když to není, tak ze jména adresáře
   * @param dir
   * @return
   * @throws IOException
   */
  private Optional<DbpackProperties> extractDbpackProps(final Path dir) throws IOException {
    return FDbpackPropertis.readDbpackDirProperties(dir)
        .map(Optional::of)  /// to je idiom výběru drohého optional, pokdu první selhal
        .orElseGet(() -> extractDbpackPropsFromDirName(dir));
  }

  /**
   * Pokusí se extrahovat ze jména adresáře dbkind a schéma.
   * Jsou to jména ve tvaru:
   *    main-aris
   *    arch-aris
   *    stat-aris
   * @param dir
   * @return
   * @throws IOException
   */
  private Optional<DbpackProperties> extractDbpackPropsFromDirName(final Path dir) {
    final Matcher matcher = patDbpackRootDirName.matcher(dir.getFileName().toString());
    if (matcher.matches()) {
      return  Optional.of(new DbpackProperties(dir,
          ADbkind.of(matcher.group(1)),
          ASchema.of(matcher.group(2))));
    } else {
      return Optional.empty();
    }
  }
}
