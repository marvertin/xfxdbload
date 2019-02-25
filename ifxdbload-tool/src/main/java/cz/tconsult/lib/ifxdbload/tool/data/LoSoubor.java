/**
 *
 */
package cz.tconsult.lib.ifxdbload.tool.data;

import java.io.File;

import org.apache.commons.lang3.builder.CompareToBuilder;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.core.core.Zavadenec;
import cz.tconsult.tw.util.TcSourceCodeInfo;

/**
 * @author veverka
 *
 */
public class LoSoubor implements Comparable<LoSoubor> {

  /** Jméno fáze, ve které má být zaváděn (1. položka pro řazení) */
  final EFazeZavedeni faze;

  /** Jméno souboru. Druhá položka pro řazení */
  final public String nameForSort;


  /** Jméno dbpacku nebo adresáře, ve kterém se soubor nacházel */
  File root;

  /** Jméno entry v tomto dbpacku */
  final String entryName;

  /** Schéma, do kterého se má zavádět */
  String schema;

  /** Načtená data ze souboru */
  byte[] data;



  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return faze.titleToLoad()  + "-" + nameForSort + " (" + getLocator() + ")";
  }


  /**
   * Vhodné pro explicitní urční fáze
   */
  public LoSoubor(final String aEntryName, final EFazeZavedeni aFaze) {
    entryName = aEntryName;
    faze = aFaze;
    nameForSort = urciNameForSort(entryName);
  }


  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final LoSoubor aO) {
    return new CompareToBuilder()
        .append(faze, aO.faze)
        .append(nameForSort, aO.nameForSort)
        .toComparison();
  }


  public String getLocator() {
    return root + "!" + entryName;
  }


  /**
   * @return the name
   */
  public String getNameForSort() {
    return nameForSort;
  }

  /**
   * @return the entryName
   */
  public String getEntryName() {
    return entryName;
  }


  /**
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }


  /**
   * @return the data
   */
  public byte[] getData() {
    return data;
  }



  /**
   * @return
   */
  public String getDataAsString() {

    final String sdata = new String(data, TcSourceCodeInfo.getDefaultCharset());
    return sdata;
  }

  /**
   * @param aEntryName
   * @return
   */
  private String urciNameForSort(final String aEntryName) {
    String[] xx = aEntryName.split("/");
    if (xx[0].equals("dbobj")) {
      xx = aEntryName.split("/", 2);
      return xx[1];
    } else if (xx[0].equals("once")){
      return xx[xx.length-1];
    } else {
      return aEntryName;
    }
  }


  /**
   * @return the faze
   */
  public EFazeZavedeni getFaze() {
    return faze;
  }


  /**
   * @param aData
   */
  public void setData(final byte[] aData) {
    data = aData;
  }


  /**
   * @return the root
   */
  public File getRoot() {
    return root;
  }

  public Zavadenec getZavadenec()  {
    final Zavadenec zavadenec = new Zavadenec(root, entryName, data, TcSourceCodeInfo.getDefaultCharset());
    zavadenec.setSchema(schema);
    return zavadenec;
  }

}
