/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;

/**
 * @author veverka
 *
 */
public class Builder {

  private final LoData data = new LoData();


  public LoSoubor addLoSoubor (final String aEntryName, final EFazeZavedeni faze, final DbpackProperties aDbprops, final byte[] aData) {
    final LoSoubor losoubor = new LoSoubor(aEntryName, faze);
    losoubor.root = aDbprops.root;
    losoubor.schema = aDbprops.dbschema;
    losoubor.data = aData;


    final LoDbkind lodbkind = makeLoDbKind(aDbprops);
    final LoFaze lofaze = lodbkind.makeLoFaze(losoubor.faze);

    lofaze.add(losoubor);

    lodbkind.filesForDbpacksCounter.inc(losoubor.root);
    lodbkind.filesForSchemaCounter.inc(losoubor.schema);
    lodbkind.filesForPhases.inc(losoubor.faze);
    lodbkind.filesForSchemasAndPhases.inc(losoubor.schema + "-" + losoubor.faze);

    data.filesForRoots.inc(aDbprops);
    data.filesForDbkinds.inc(lodbkind.name);
    return losoubor;
  }

  public LoDbkind makeLoDbKind(final DbpackProperties aDbprops){
    return data.makeLoDbkind(aDbprops.dbkind);
  }

  /**
   * @return the data
   */
  public LoData getData() {
    return data;
  }



}
