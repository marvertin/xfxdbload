package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.tconsult.lib.ifxdbload.core.faze.AEntryName;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;

public class FDbpackPropertis {


  private static final String DBPACK_PROPERTIES = "dbpack.properties";


  public static boolean isDbpackProperties(final AEntryName entry) {
    // TODO [veverka] Shodit, když je tam zanořenec, který se nebere nebo aspoň varovat -- 26. 2. 2019 16:51:55 veverka
    return entry.toString().endsWith(DBPACK_PROPERTIES);
  }

  public static Optional<DbpackProperties> readDbpackDirProperties(final Path aDir) throws IOException {
    final File file = aDir.resolve(DBPACK_PROPERTIES).toFile();
    if (!file.exists()) {
      return Optional.empty();
    }
    final FileInputStream fis = new FileInputStream(file);
    try {
      final Properties properties = new Properties();
      properties.load(fis);
      final DbpackProperties dbpackProperties = convertDbpackProperties(properties, aDir);
      return Optional.of(dbpackProperties);
    } finally {
      fis.close();
    }
  }


  public static  Optional<DbpackProperties> readDbpackZipProperties(final Path aFdbpackZip) throws IOException {
    final FileInputStream fis = new FileInputStream(aFdbpackZip.toFile());
    final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    try {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        final String name = entry.getName();
        if (name.equals(DBPACK_PROPERTIES)) {
          final Properties properties = new Properties();
          properties.load(zis);
          final DbpackProperties dbpackProperties = convertDbpackProperties(properties, aFdbpackZip);
          return Optional.of(dbpackProperties);
        }
      }
      return Optional.empty();
    } finally {
      zis.close();
      fis.close();
    }
  }


  /**
   * @param properties
   * @return
   */
  private static  DbpackProperties convertDbpackProperties(final Properties properties, final Path root) {
    final DbpackProperties dbpackProperties = new DbpackProperties(root,
        ADbkind.of(properties.getProperty("dbkind"))
        );
    if (dbpackProperties.getDbkind() == null) {
      throw new RuntimeException("Dbpack neobsahuje v 'dbpack.properties' property 'dbkind' ");
    }
    return dbpackProperties;
  }

}
