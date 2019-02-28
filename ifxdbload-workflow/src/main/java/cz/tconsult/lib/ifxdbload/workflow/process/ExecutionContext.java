package cz.tconsult.lib.ifxdbload.workflow.process;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Exekuční kontext, určuje především databázi, do které se zavádí.
 * @author veverka
 *
 */


public interface ExecutionContext {
  JdbcTemplate jt();

}
