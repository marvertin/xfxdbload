package cz.tconsult.lib.ifxdbload.workflow.data;

/**
 * Schéma databáze, do které se zavádí.Druh databáze jako je hlavní, archivní, statistická.
 * @author veverka
 *
 */
public class ASchema extends AScalar0 {

  public ASchema(final String value) {
    super(value);
  }

  public static ASchema of(final String s) {
    return s == null ? null : new ASchema(s);
  }

}
