package cz.tconsult.lib.ifxdbload.core.splparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.tconsult.lib.spllexer.SplDirective;
import lombok.SneakyThrows;

/**
 * Přihekuje první verzi direktiv, prootže se to stále ještě používá.
 * @author veverka
 *
 */
public class OnceDirectiveV1Hack {

  private static final String V1_HEADER = "--V1,ID:"; // :RSTS_31974_make_D_exec

  @SneakyThrows(IOException.class)
  public static void hackniV1DirektivyDoVysledku(final List<SplStatement> result, final String data) {

    if (data.startsWith(V1_HEADER)) {
      final Set<SplDirective> dires = new HashSet<>();
      final BufferedReader br = new BufferedReader(new StringReader(data));
      final String line1 = br.readLine();
      dires.add(new SplDirective("ONCE", "ID", "V2, " + line1.substring(V1_HEADER.length()).trim()));
      final String line2 = br.readLine();
      if (line2 != null && line2.startsWith("--notran")) {
        dires.add(new SplDirective("ONCE", "NO_TRANSACTION_CONTROL", ""));
      }
      // a přebalit první příkaz
      if (result.size() > 0) {
        final SplStatement ss = result.get(0);
        result.set(0, new SplStatement(dires, ss.getStmType(), ss.getName(), ss.getText(), ss.getTextForHash(), ss.getFirstTokenLocator()));
      }
    }

  }

}
