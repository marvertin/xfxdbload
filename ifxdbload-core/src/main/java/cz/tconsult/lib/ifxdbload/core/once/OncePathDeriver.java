/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.once;

import java.io.File;

import cz.tconsult.lib.ifxdbload.core.once.enums.EOncePhase;
import cz.tconsult.lib.ifxdbload.core.once.enums.EOnceType;
import cz.tconsult.lib.ifxdbload.core.once.enums.EVariant;

/**
 * Dočasná třída, která umoňuje reorganizaci onceloaderu
 * ze soubrů na stringy.
 * @author veverka
 *
 */
public class OncePathDeriver  {
  /** Přípona souboru podle které se pozná, že se jedná o informix script */
  protected final static String SCRIPT_IFX=".isql";
  /** Přípona souboru podle které se pozná, že se jedná o oracle script */
  protected final static String SCRIPT_ORA=".osql";
  /** Přípona souboru podle které se pozná, že se jedná o exec script */
  protected final static String EXEC_EXT=".exec";

  /** Přípona souboru podle které se pozná, že se jedná o mysql script */
  protected final static String SCRIPT_MYSQL=".mysql";



  /** Cesta k onescriptu. Je používáno pro logování a konec cesty
   * je použit pro zjišťování dalších informacích jako je:
   *    prc ... je to oncová procedura
   *    VCzech, VSlovak ... je tovarianta
   *    o kterou se jedná fázi
   * Cesta může být oddělena lomítky nebo beksleši..
   */
  private final String path;

  public OncePathDeriver(final String path) {
    // Vykricnikem oddelujeme zip od entry
    this.path = path.replace('!', '/');
  }

  /**
  public BufferedReader createReader() throws FileNotFoundException {
    BufferedReader br = new BufferedReader(new StringReader(data));
    return br;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return path;
  }

  public EOnceType ziskejTypSkriptuZeJmena() {
    EOnceType onceType;
    final String filenameLC=new File(path).getName().toLowerCase().trim();
    if(filenameLC.endsWith(EXEC_EXT)) {
      onceType=EOnceType.EXTERNAL_EXECUTE;
    }
    else if(filenameLC.endsWith(SCRIPT_IFX)) {

      onceType=EOnceType.IFX_SCRIPT;
    }
    else if(filenameLC.endsWith(SCRIPT_ORA)) {

      onceType=EOnceType.ORA_SCRIPT;
    }
    else if(filenameLC.endsWith(SCRIPT_MYSQL)) {

      onceType=EOnceType.MYSQL_SCRIPT;
    }

    else {

      throw new RuntimeException("Unknow extension of once script "+filenameLC);
    }
    return onceType;
  }

  public String extractujSkriptIdZeJmena() {
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


  public File odvodExtenniSpoustec(final String s){
    final File result = new File(new File(path).getParentFile(), s);
    return result;
  }

  public EOncePhase getPhase() {

    for(File f = new File(path); f != null; f = f.getParentFile()) {
      final EOncePhase phase =  EOncePhase.getTypeByString(f.getName());
      if (phase != null) {
        return phase;
      }
    }
    return null;
  }

  public EVariant getVariant() {
    EVariant variantByDir = null;
    for(File f = new File(path); f != null; f = f.getParentFile()) {
      variantByDir =  EVariant.getVariantByString(f.getName());
      if (variantByDir != null) {
        return variantByDir;
      }
    }
    return null;
  }

}
