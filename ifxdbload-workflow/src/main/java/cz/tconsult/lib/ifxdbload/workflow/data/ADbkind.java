package cz.tconsult.lib.ifxdbload.workflow.data;

import cz.tconsult.lib.ifxdbload.core.tw.AScalar0;

/**
 * Druh databáze jako je hlavní, archivní, statistická.
 * @author veverka
 *
 */
public class ADbkind extends AScalar0 {

  public ADbkind(final String value) {
    super(value);
  }

  public static ADbkind of(final String s) {
    return s == null ? null : new ADbkind(s);
  }

}
