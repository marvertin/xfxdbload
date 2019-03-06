package cz.tconsult.lib.ifxdbload.core.db;

import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ASchema;

/**
 * Exekuční kontext, určuje především databázi, do které se zavádí.
 * @author veverka
 *
 */
public interface LoadContext {
  /**
   * Vrací template, který bude provádět SQL v daném schématu a vkontextu dbkind tedy správné databáze().
   * Vracený template je možné použít i ve více vláknech.
   * @param schema
   * @return
   */
  DbContext dc(ASchema schema);

  /**
   * Nahlásí chybu, která nastala při zavádění daného příkazu.
   * Jen chyby vlastního zavádění, ne operace kolem s pomocnými tabulkami a podobně, ty se nechají proletět.
   *
   * @param exc
   * @param stm
   */
  void reportError(DataAccessException exc, SplStatement stm);

}
