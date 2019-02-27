/**
 *
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;



/**
 * @author veverka
 *
 */
class SimpleParser  {


  /**
   * Zparsruje jednoduchým způsobem a vrátí seznam příkazů.
   * @return
   */
  public List<LoCommand> parse(final String aDataStr) {
    try {
      final List<LoCommand> commands = new ArrayList<LoCommand>();
      // převedeme data na řetězec s platformovým kódováním
      String sdata = aDataStr;
      sdata = ukousniBíléZnakyALomítkaZKonce(sdata); // bílé znaky hlavně z konce pryč
      final Reader srdr = new StringReader(sdata);
      try (final LineNumberReader rdr = new LineNumberReader(srdr)) {
        String line;
        final StringBuilder sb = new StringBuilder();
        int firstLine = rdr.getLineNumber();
        while ((line = rdr.readLine()) != null) {
          if (totoJeOddelovac(line)) {
            addLoCommand(commands, sb.toString(), firstLine);
            sb.setLength(0);
            firstLine = rdr.getLineNumber();
          } else {
            // Pozor, když tam budevCR-LF, tak to nebude fungovat
            //      System.out.println("xxx " + line);
            sb.append(line + '\n');
          }
        }
        addLoCommand(commands, sb.toString(), firstLine);
      }
      return commands;
    } catch (final IOException e) {
      throw new RuntimeException("Při čtení ze stringu nemůže nastat výjimka");
    }
  }

  /**
   * @param aString
   * @param aFirstLine
   */
  private void addLoCommand(final List<LoCommand> commands, final String cmd, final int aFirstLine) {
    if (StringUtils.isBlank(cmd)) {
      return;
    }
    final LoCommand loCommand = new LoCommand();
    loCommand.command = cmd;
    loCommand.lineNumber = aFirstLine;
    commands.add(loCommand);
  }


  /**
   * @param aSdata
   * @return
   */
  private String ukousniBíléZnakyALomítkaZKonce(final String sdata) {
    int n;
    for (n = sdata.length() - 1; n >=0; n--) {
      final char c = sdata.charAt(n);
      if (! (c <= ' ' || c == '/')) {
        break;
      }
    }
    if (n >=0 && StringUtils.substring(sdata, n, n+2).equals("*/")) {
      n++;
    }
    final String result = n+1 == sdata.length() ? sdata : sdata.substring(0, n+1);
    return result;
  }



  /**
   * @param line
   * @return
   */
  private boolean totoJeOddelovac(final String line) {
    //    System.out.println("haha" + " | " + line);
    final boolean zacinaLomitkem = line.startsWith("/");
    if (!zacinaLomitkem) {
      return false;
    }
    final boolean result = ! line.startsWith("/*");
    //    System.out.println(result + " | " + line);

    return result;
  }


}
