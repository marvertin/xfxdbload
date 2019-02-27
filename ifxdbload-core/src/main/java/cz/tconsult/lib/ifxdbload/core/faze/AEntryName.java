package cz.tconsult.lib.ifxdbload.core.faze;

import cz.tconsult.lib.ifxdbload.core.tw.AScalar0;

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
public class AEntryName extends AScalar0 {

  public AEntryName(final String value) {
    super(value);
  }

  public static AEntryName of(final String s) {
    return s == null ? null : new AEntryName(s);
  }

}
