package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class ChecksumComputer {

  private static final Charset win1250Charset = Charset.forName("windows-1250");
  private static final long bulgarianAsciiConstant = 990000000000l;
  private static class Tries {

    final Checksum checksumOldChecksum = new CRC32();
    final Checksum checksumOldLfChecksum = new CRC32();
    final Checksum checksumOldCrLfChecksum = new CRC32();

    //U jistého počtu ONCE skriptů byl asi dogenerován (zřejmě ECLIPSEm) příkaz ROLLBACK s blbým
    //řádkovým oddělovačem (v celém souboru "0D 0A", jen před ROLLBACKem samotné 0D nebo 0A :-/
    final Checksum checksumOldCrLfRollbackCrChecksum = new CRC32();
    final Checksum checksumOldCrLfRollbackLfChecksum = new CRC32();
  }

  final Checksum checksumNonPrintableAsciiChecksum = new CRC32();

  private final Tries oldTries = new Tries();
  private final Tries oldTriesTrimmed = new Tries();
  private final Tries old1250Tries = new Tries();
  private final Tries old1250TriesTrimmed = new Tries();

  private static void _updateRaw(final Tries aStorage, final byte[] aData) {

    int index = 0;
    for (final byte b : aData) {
      aStorage.checksumOldChecksum.update(b);
      if (b == '\r') {
        // nic nedavame
      } else if (b == '\n') {
        aStorage.checksumOldCrLfChecksum.update('\r');  // jako by drive bylo CRLF
        aStorage.checksumOldCrLfChecksum.update(b);  // jako by drive bylo CRLF
        aStorage.checksumOldLfChecksum.update(b);

        boolean skipBeforeRollback = false;
        if (aData.length > index + 8) {
          if (
              (aData[index + 1] == 'R' || aData[index + 1] == 'r')
              && (aData[index + 2] == 'O' || aData[index + 2] == 'o')
              && (aData[index + 3] == 'L' || aData[index + 3] == 'l')
              && (aData[index + 4] == 'L' || aData[index + 4] == 'l')
              && (aData[index + 5] == 'B' || aData[index + 5] == 'b')
              && (aData[index + 6] == 'A' || aData[index + 6] == 'a')
              && (aData[index + 7] == 'C' || aData[index + 7] == 'c')
              && (aData[index + 8] == 'K' || aData[index + 8] == 'k')
              ) {

            skipBeforeRollback = true;
          }
        }
        if (skipBeforeRollback) {
          aStorage.checksumOldCrLfRollbackCrChecksum.update('\r');
          aStorage.checksumOldCrLfRollbackLfChecksum.update('\n');
        }
        else {
          //ponech řádkování beze změn -> tedy předpokládáme CRLF
          aStorage.checksumOldCrLfRollbackCrChecksum.update('\r');  // jako by drive bylo CRLF
          aStorage.checksumOldCrLfRollbackCrChecksum.update(b);  // jako by drive bylo CRLF
          aStorage.checksumOldCrLfRollbackLfChecksum.update('\r');  // jako by drive bylo CRLF
          aStorage.checksumOldCrLfRollbackLfChecksum.update(b);  // jako by drive bylo CRLF
        }

      } else { // neodradkovavac
        aStorage.checksumOldCrLfChecksum.update(b);
        aStorage.checksumOldLfChecksum.update(b);
        aStorage.checksumOldCrLfRollbackCrChecksum.update(b);
        aStorage.checksumOldCrLfRollbackLfChecksum.update(b);
      }
      index++;
    }
  }

  public void update(final String aText, final Long aExpectedChecksum) {


    for (final char c : aText.toCharArray()) {
      if (c > 32 && c <127) { // jen tisknutelne standardni znaky ASCII
        checksumNonPrintableAsciiChecksum.update(c);
      }
    }
    _updateRaw(oldTries, aText.getBytes(StandardCharsets.UTF_8));

    {
      //Tady v tomto bloku je funkcionalita, která je nutná pro zpětnou kompatibilitu
      //po přechodu na GIT (přidání zkoušených checksumů s UTF-8 a prázdný koncový řádek (+ varianty CRLF a LF)
      //Tento blok bude moct být odstraněn, až úplně všude přejdem na GIT a staré once skripty
      //(vzniklé původem ve UTF-8) budou zaarchivovány
      //[polakm;2015-09-07 16:13:06]
      boolean tryOtherPossibilities;
      //Je otázkou, zda pokud nedostanu nápovědu s očekávaným checksumem, jestli počítat a porovnávat
      //další a další alternativy. Protože zatím posílá očekávaný checksum pouze
      //funkcionalita ověřování, zda už bylo v DB zavedeno, tak se rozhoduji pro tuto možnost
      if (aExpectedChecksum == null) {

        tryOtherPossibilities = false;
      }
      else {

        //Pokud jsme se do nějakého tradičního checksumu trefili, nebudeme přece zbytečně zkoušet další....
        if (aExpectedChecksum == checksumNonPrintableAsciiChecksum.getValue() + bulgarianAsciiConstant
            || aExpectedChecksum == oldTries.checksumOldChecksum.getValue()
            || aExpectedChecksum == oldTries.checksumOldCrLfChecksum.getValue()
            || aExpectedChecksum == oldTries.checksumOldLfChecksum.getValue()
            || aExpectedChecksum == oldTries.checksumOldCrLfRollbackCrChecksum.getValue()
            || aExpectedChecksum == oldTries.checksumOldCrLfRollbackLfChecksum.getValue()
            ) {

          tryOtherPossibilities = false;
        }
        else {

          tryOtherPossibilities = true;
        }
      }

      if (tryOtherPossibilities) {

        _updateRaw(old1250Tries, aText.getBytes(win1250Charset));
        String s = aText;
        int len = s.length();
        if (s.charAt(len - 1) == '\r') {s = s.substring(0, --len);}
        if (s.charAt(len - 1) == '\n') {s = s.substring(0, --len);}
        if (!s.equals(aText)) {

          _updateRaw(oldTriesTrimmed, s.getBytes(StandardCharsets.UTF_8));
          _updateRaw(old1250TriesTrimmed, s.getBytes(win1250Charset));
        }
      }
    }
  }

  public void update(final String s) {

    update(s, null);
  }

  private static void _applyToRaw(final Kontrolnici aStorage, final Tries aData) {

    if (aData != null) {

      aStorage.addCheksum(aData.checksumOldChecksum.getValue());
      aStorage.addCheksum(aData.checksumOldLfChecksum.getValue());
      aStorage.addCheksum(aData.checksumOldCrLfChecksum.getValue());
      aStorage.addCheksum(aData.checksumOldCrLfRollbackCrChecksum.getValue());
      aStorage.addCheksum(aData.checksumOldCrLfRollbackLfChecksum.getValue());
    }
  }

  Kontrolnici apllyTo(Kontrolnici kontrolnici) {
    if (kontrolnici == null) {
      kontrolnici = new Kontrolnici();
    }
    kontrolnici.setMainChecksum(checksumNonPrintableAsciiChecksum.getValue() + bulgarianAsciiConstant);
    _applyToRaw(kontrolnici, oldTries);
    _applyToRaw(kontrolnici, oldTriesTrimmed);
    _applyToRaw(kontrolnici, old1250Tries);
    _applyToRaw(kontrolnici, old1250TriesTrimmed);
    return kontrolnici;
  }



}


