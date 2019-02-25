/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.core.FFaze;

/**
 * @author veverka
 *
 */
public class FazeIntervalParser {

  private final Set<EFazeZavedeni> fazes = new TreeSet<EFazeZavedeni>();

  private FazeIntervalParser(final String aZAdaneFazeJakoRetezec) {

    final String[] polozky = aZAdaneFazeJakoRetezec.split(",");
    for (final String polozka : polozky) {
      final int minuspos = polozka.indexOf('-');
      if (minuspos < 0) {
        fillInterval(polozka, polozka);
      } else {
        fillInterval(polozka.substring(0, minuspos), polozka.substring(minuspos+1));
      }
    }

  }

  /**
   * @param aPolozka
   * @param aPolozka2
   */
  private void fillInterval(String aOd, String aDo) {
    aOd = upravProPorovnani(aOd);
    aDo = upravProPorovnani(aDo);

    for (final EFazeZavedeni faze : FFaze.getAllfazes()) {
      if (between(faze, aOd, aDo)) {
        fazes.add(faze);
      }
    }
  }

  private boolean between(final EFazeZavedeni faze, final String aOd, final String aDo) {
    if (aOd != null) { // porovnáváme vlevo
      final String fastr = faze.toString().substring(0, aOd.length());
      if (fastr.compareTo(aOd) < 0) {
        return false;
      }
    }
    if (aDo != null) { // porovnáváme vlevo
      final String fastr = faze.toString().substring(0, aDo.length());
      if (fastr.compareTo(aDo) > 0) {
        return false;
      }
    }
    return true;
  }

  private String upravProPorovnani(String s) {
    s = s.trim().toLowerCase();
    if (s.length() == 0)
    {
      return null; // hranici neporovnávat
    }
    final Pattern pat = Pattern.compile("[fF]?([0-9]+)[a-z]*");
    final Matcher mat = pat.matcher(s);
    if (! mat.matches()) {
      throw new RuntimeException("'" + s + "'" + " nemečuje: " + pat);
    }
    final String result = "f" + mat.group(1);
    return result;
  }

  public static Set<EFazeZavedeni> parse(final String aZAdaneFazeJakoRetezec) {
    if (StringUtils.isBlank(aZAdaneFazeJakoRetezec)) {
      return null;
    }
    final Set<EFazeZavedeni> result = new FazeIntervalParser(aZAdaneFazeJakoRetezec).fazes;
    return result;

  }

}
