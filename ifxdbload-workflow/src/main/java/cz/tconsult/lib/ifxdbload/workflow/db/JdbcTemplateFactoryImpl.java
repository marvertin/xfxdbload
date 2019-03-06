package cz.tconsult.lib.ifxdbload.workflow.db;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.process.JdbcTemplateFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Továrna na JdbcTemplate.
 * Jejích ukolem je dodávat templaty pro daný druh databáze a schema. Templatey jsou přes datadsourcy, teré poskytuje
 * factory.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class JdbcTemplateFactoryImpl implements JdbcTemplateFactory {

  private final DsFactory dsFactory;

  private final Map<KindSchema, JdbcTemplate> jdbcTemplates = new HashMap<>();

  @Override
  public JdbcTemplate jt(final ADbkind dbkind, final ASchema schema) {

    return jdbcTemplates.computeIfAbsent(new KindSchema(dbkind, schema),
        __ -> new JdbcTemplate(dsFactory.createDs(dbkind, schema)));
  }

  @Override
  public boolean canCreate(final ADbkind dbkind) {
    return dsFactory.canCreateDs(dbkind);
  }


  @Data
  private static class KindSchema {
    private final ADbkind dbkind;
    private final ASchema schema;
  }
}
