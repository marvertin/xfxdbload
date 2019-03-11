/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import cz.tconsult.lib.ifxdbload.core.faze.AEntryName;
import cz.tconsult.lib.ifxdbload.core.faze.EFazeZavedeni;

/**
 * @author veverka
 *
 */
public class Builder {

  private final LoData data = new LoData();


  public LoSoubor addLoSoubor (final AEntryName aEntryName, final EFazeZavedeni faze, final DbpackProperties aDbprops, final byte[] aData) {
    final LoSoubor losoubor = new LoSoubor(aEntryName, faze, aDbprops.getRoot(), aData);

    final LoDbkind lodbkind = makeLoDbKind(aDbprops);
    final LoFaze lofaze = lodbkind.getFaze(losoubor.getFaze());

    lofaze.add(losoubor);

    lodbkind.getFilesForDbpacksCounter().inc(losoubor.getRoot());
    lodbkind.getFilesForPhases().inc(losoubor.getFaze());

    data.filesForRoots.inc(aDbprops);
    data.filesForDbkinds.inc(lodbkind.getName());
    return losoubor;
  }

  public LoDbkind makeLoDbKind(final DbpackProperties aDbprops){
    return data.makeLoDbkind(aDbprops.getDbkind());
  }

  /**
   * @return the data
   */
  public LoData getData() {
    return data;
  }



}
