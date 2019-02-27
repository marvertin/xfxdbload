package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;

import cz.tconsult.lib.ifxdbload.core.core.AEntryName;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DbpakZipReader {

  private final FileContentReceiver fcb;

  /**
   * @param aFdbpackZip
   * @throws IOException
   */
  public void readOneDbpackZip(final Path aFdbpackZip, final AEntryName aNadrizeneEntryName, final DbpackProperties parentDbprops) throws IOException {
    try (FileInputStream fis = new FileInputStream(aFdbpackZip.toFile())) {
      try (final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
        ZipEntry entry;

        final Optional<DbpackProperties> optionalDbProps = FDbpackPropertis.readDbpackZipProperties(aFdbpackZip);
        final DbpackProperties dbprops = optionalDbProps.orElse(parentDbprops);
        String korenEntryName = aNadrizeneEntryName == null ? "" : aNadrizeneEntryName + "/";
        if (optionalDbProps.isPresent()) {
          korenEntryName = "";
        }
        //    log.info("Unzipping : \"%s\" into folder \"%s\".", aFile, aAdrearProArchivaciExportu);
        while ((entry = zis.getNextEntry()) != null) {
          if (! entry.isDirectory() && ! FDbpackPropertis.isDbpackProperties(AEntryName.of(entry.getName()))) {
            final String entryName = entry.getName();
            fcb.add(dbprops, () -> byteStreams_toByteArray(zis), AEntryName.of(korenEntryName + entryName));
          }
        }
      }
    }
  }


  @SneakyThrows
  private byte[] byteStreams_toByteArray(final InputStream istm) {
    return ByteStreams.toByteArray(istm);
  }

}
