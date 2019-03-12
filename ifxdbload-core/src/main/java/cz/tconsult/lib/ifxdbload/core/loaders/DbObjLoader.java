package cz.tconsult.lib.ifxdbload.core.loaders;

import java.util.EnumSet;
import java.util.List;

import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;

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
public interface DbObjLoader {

  void load(final List<SplStatement> stms);

  void readAllFromCatalog();

  EnumSet<EStmType> getSupportedTypes();

}