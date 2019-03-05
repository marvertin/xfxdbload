package cz.tconsult.lib.ifxdbload.core.tw;

/**
 * Reprezentuje plnou cestu k souboru s záváděnými daty.
 * Je to důležitý údaj, podle nějž se určuje fáze zavádění.
 *
 * Například
 *
 *    dbobj/prc/cosi/ab_cdef.isql
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
