package cz.tconsult.lib.ifxdbload.workflow.db;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.p6spy.engine.spy.P6DataSource;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.process.DbContextFactory;
import lombok.RequiredArgsConstructor;

/**
 * Továrna na JdbcTemplate.
 * Jejích ukolem je dodávat templaty pro daný druh databáze a schema. Templatey jsou přes datadsourcy, které poskytuje
 * factory.
 * @author veverka
 *
 */
@RequiredArgsConstructor
public class JdbcTemplateFactoryImpl implements DbContextFactory {


  private static final Logger log = LoggerFactory.getLogger(JdbcTemplateFactoryImpl.class);

  private final DsFactory dsFactory;

  private final Map<ADbkind, DbContext> jdbcTemplates = new HashMap<>();


  @Override
  public boolean canCreate(final ADbkind dbkind) {
    return dsFactory.canCreateDs(dbkind);
  }


  @Override
  public DbContext dc(final ADbkind dbkind) {

    return jdbcTemplates.computeIfAbsent(dbkind,
        __ -> {
          log.info("Creating emplates for \"" + dbkind + "\"");
          final DataSource realDs = dsFactory.createDs(dbkind);
          final P6DataSource ds = new P6DataSource(realDs);
          ds.setJdbcEventListenerFactory(() -> new P6SpyExceptionEnrichmentEventListener());
          final TransactionTemplate tt = new TransactionTemplate(new DataSourceTransactionManager(ds));
          final JdbcTemplate jt = new JdbcTemplate(ds);
          // jt.setExceptionTranslator(new IfxExceptionTranslator());

          return new DbContext(tt, jt);
        });
  }




}
