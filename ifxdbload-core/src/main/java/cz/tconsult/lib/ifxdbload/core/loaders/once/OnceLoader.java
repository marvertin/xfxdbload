package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.List;

import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;

/**
 * Rozhraní zavaděče objektů. Objekty jsou:
 *    procedury
 *    triggery
 *    view
 *    synonyma
 *    sekvence
 *
 * Nepatří sem altery ani registrace.
 *
 * @author veverka
 *
 */
public interface OnceLoader {

  void readAllFromCatalog();

  /**
   * Zavede zadané oncy. Sám si je musí seřadit, zjistit, ci se zavádí apod.
   * @param ps
   */
  void load(final List<ParseredSource> ps);

}