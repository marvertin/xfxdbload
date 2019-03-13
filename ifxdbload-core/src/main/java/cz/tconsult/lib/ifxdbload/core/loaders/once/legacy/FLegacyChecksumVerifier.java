package cz.tconsult.lib.ifxdbload.core.loaders.once.legacy;

import cz.tconsult.lib.ifxdbload.core.loaders.once.OnceScript;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;

public class FLegacyChecksumVerifier {


  public static boolean verify(final OnceScript once, final long checksum) {

    if (checksum == once.computeChecksum()) {
      return true; // to je ponovu flexibilně vypočtené checksum
    }

    final Kontrolnici kontrolnici = getCheckSumOfFile(once.getPs(), checksum, ! once.getGlobalDirectives().isNoTransactionControl());
    final boolean b = kontrolnici.verifyChecksum(checksum);
    return b;

  }



  /**
   * Vrati CRC32 kontrolni soucet souboru.
   */
  private static Kontrolnici getCheckSumOfFile(final ParseredSource  onceScript, final Long aExpectedChecksum, final boolean ignorovatTransakcnosti){

    final ChecksumComputer cscompPoCastech = new ChecksumComputer();
    final ChecksumComputer cscompVCelku = new ChecksumComputer();

    cscompVCelku.update(onceScript.getSource().getData(), aExpectedChecksum);

    //    System.out.println("*******************: " + onceScript.getFileNameOnly());
    for (final SplStatement st: onceScript.getStatements()) {
      //      System.out.println("                 ***************************: " + st.getStmType() + " " + st.getText());
      //System.err.println("XX: " + st.getText());
      st.getText();
      if (ignorovatTransakcnosti && (st.getStmType() == EStmType.BEGINWORK || st.getStmType() == EStmType.ROLLBACK)) {

      } else {
        cscompPoCastech.update(st.getText().trim().replaceAll("\r", ""), aExpectedChecksum);
      }

    }
    //    System.out.println("*******************");

    final Kontrolnici kontrolnici = new Kontrolnici();
    cscompVCelku.apllyTo(kontrolnici);
    cscompPoCastech.apllyTo(kontrolnici);

    return kontrolnici;
  }
}
