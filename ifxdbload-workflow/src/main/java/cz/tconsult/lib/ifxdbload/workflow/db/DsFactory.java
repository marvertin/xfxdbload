package cz.tconsult.lib.ifxdbload.workflow.db;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;

/**
 * Bude vyrábět datasourcy
 * @author veverka
 *
 */
public class DsFactory {


  public boolean canCreateDs(final ADbkind dbkind) {
    return true;
  }

  public DataSource createDs(final ADbkind dbkind, final ASchema schema) {

    // TODO

    final HikariConfig hc = new HikariConfig();
    hc.setJdbcUrl("jdbc:informix-sqli://kosatka:1529/zavadec:;INFORMIXSERVER=kosatkarn;CLIENT_LOCALE=cs_CZ.1250;DB_LOCALE=cs_CZ.1250");
    hc.setUsername("aris");
    hc.setPassword("aris741");

    hc.setConnectionInitSql("SET SESSION AUTHORIZATION TO '" + schema + "'");
    // to jsem opsal z nějakého píkladu a nevím, jestli je to dobře
    hc.addDataSourceProperty( "cachePrepStmts" , "true" );
    hc.addDataSourceProperty( "prepStmtCacheSize" , "250" );
    hc.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
    return new HikariDataSource(hc);

  }
}
