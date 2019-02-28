package cz.tconsult.lib.ifxdbload.workflow.process;

import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;

/**
 * Továrna na template. Implementace musí poskytnout.
 * @author veverka
 *
 */
public interface JdbcTemplateFactory {

  public JdbcTemplate jt(ADbkind dbkind, ASchema schema);
}
