package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.loaders.once.legacy.FLegacyChecksumVerifier;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.spllexer.SplDirective;
import lombok.Data;

/**
 * Reprezentuje jeden onceskript.
 * @author veverka
 *
 */
@Data
public class OnceScript implements Comparable<OnceScript> {

  private final ParseredSource ps;

  public boolean verify(final long checksum) {

    if (checksum == computeChecksum()) {
      return true; // to je ponovu flexibilně vypočtené checksum
    }

    return FLegacyChecksumVerifier.verify(this, checksum);
  }


  public OnceDirectivesGlobal getGlobalDirectives() {
    // TODO [veverka] Dodělat a zrozumnit, zvýkonit -- 13. 3. 2019 17:13:48 veverka
    final Set<SplDirective> directives = ps.getStatements().get(0).getDirectives();
    final OnceDirectivesGlobal globalDirectives = new OnceDirectivesParser(directives).parse();
    return globalDirectives;
  }


  /**
   * spočte checksum onceskriptu
   * @return
   */
  public long computeChecksum() {
    long suma = 1;
    final List<Long> sumy = ps.getStatements().stream()
        .map(SplStatement::getLongHash)
        .collect(Collectors.toList());
    // nelze použít reduce, protože reduce vyžaduje monoid a já neznám žádný nekomutativní monoid nad čísly (jednoduše realizovatelný). [veverka;2019-03-13 15:17:24]
    for (final long n : sumy) {
      suma = suma * 13 + n;
    }
    return suma;
  }

  public String getScriptId() {
    return extractujSkriptIdZeJmena(ps.getFileNameOnly());
  }

  private static String extractujSkriptIdZeJmena(final String path) {
    //odstranim z nazvu souboru priponu a a usporadavac (A12312-......)
    final String pureName = new File(path).getName();
    final String nameWithoutExt=pureName.substring(0,pureName.lastIndexOf("."));
    final int indexOfPom=nameWithoutExt.indexOf("-");
    final int indexOfRn=nameWithoutExt.toLowerCase().indexOf("rn");
    String nameWithoutUsporadavac=nameWithoutExt;
    if(indexOfPom<indexOfRn){
      nameWithoutUsporadavac=indexOfPom > -1?nameWithoutExt.substring(indexOfPom+1,nameWithoutExt.length()):nameWithoutExt;
    }
    return nameWithoutUsporadavac;
  }


  @Override
  public int compareTo(final OnceScript o) {
    return ps.getFileNameOnly().compareTo(o.getPs().getFileNameOnly());
  }
}
