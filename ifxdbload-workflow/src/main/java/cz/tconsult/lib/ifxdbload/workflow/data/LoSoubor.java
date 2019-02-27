/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.lang3.builder.CompareToBuilder;

import cz.tconsult.lib.ifxdbload.core.core.AEntryName;
import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.core.Zavadenec;
import lombok.Data;

/**
 * @author veverka
 *
 */
@Data
public class LoSoubor implements Comparable<LoSoubor> {

  /** Jméno entry v tomto dbpacku */
  private final AEntryName entryName;

  /** Jméno fáze, ve které má být zaváděn (1. položka pro řazení) */
  private final EFazeZavedeni faze;


  /** Jméno dbpacku nebo adresáře, ve kterém se soubor nacházel */
  private final Path root;

  /** Schéma, do kterého se má zavádět */
  private final ASchema schema;

  /** Načtená data ze souboru */
  private final byte[] data;


  /** Jméno souboru. Druhá položka pro řazení */
  private String cachedNameForSort;

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final LoSoubor aO) {
    return new CompareToBuilder()
        .append(getFaze(), aO.getFaze())
        .append(getNameForSort(), aO.getNameForSort())
        .toComparison();
  }


  public String getLocator() {
    return root + "!" + entryName;
  }

  /**
   * @return
   */
  public String getDataAsString() {

    final String sdata = new String(data, StandardCharsets.UTF_8);
    return sdata;
  }

  /**
   * @param aEntryName
   * @return
   */
  public String getNameForSort() {
    if (cachedNameForSort == null) {
      cachedNameForSort = urciNameForSort(entryName);
    }
    return cachedNameForSort;
  }

  private static String urciNameForSort(final AEntryName entryName) {
    final String entname = entryName.toString();
    String[] xx = entname.split("/");
    if (xx[0].equals("dbobj")) {
      xx = entname.split("/", 2);
      return xx[1];
    } else if (xx[0].equals("once")){
      return xx[xx.length-1];
    } else {
      return entname;
    }
  }




  public Zavadenec getZavadenec()  {
    final Zavadenec zavadenec = new Zavadenec(root.toFile(), entryName, data, StandardCharsets.UTF_8);
    zavadenec.setSchema(schema.toString());
    return zavadenec;
  }



}
