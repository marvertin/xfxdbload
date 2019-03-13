package cz.tconsult.lib.ifxdbload.core.tw;

/**
 * Reprezentuje jméno nějakého zdroje. Může to být plná cesta k souboru nebo v položce zipu
 *
 * @author veverka
 *
 */
public class ASourceName extends AScalar0 {

  public ASourceName(final String value) {
    super(value);
  }

  public static ASourceName of(final String s) {
    return s == null ? null : new ASourceName(s);
  }

}
