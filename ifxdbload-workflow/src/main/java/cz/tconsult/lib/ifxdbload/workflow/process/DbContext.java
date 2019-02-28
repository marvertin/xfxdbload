package cz.tconsult.lib.ifxdbload.workflow.process;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Kontext jedné databáze.
 * @author veverka
 *
 */
public interface DbContext {
  JdbcTemplate jt();
}
