package cz.tconsult.lib.ifxdbload.workflow.process;

import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;

/**
 * Exekuční kontext, určuje především databázi, do které se zavádí.
 * @author veverka
 *
 */
public interface ExecutionContext {
  /**
   * Vrací template, který bude provádět SQL v daném schématu a vkontextu dbkind tedy správné databáze().
   * Vracený template je možné použít i ve více vláknech.
   * @param schema
   * @return
   */
  JdbcTemplate jt(ASchema schema);

}
