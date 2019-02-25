package cz.tconsult.lib.ifxdbload.tool;

import java.sql.Connection;

import cz.tconsult.lib.ifxdbload.workflow.TwConnectionFactoryyy;
import cz.tconsult.tw.sql.TwConnectionFactory;

public class TwConnectionFactoryyyImpl implements TwConnectionFactoryyy {

  private final TwConnectionFactory tcf;

  public TwConnectionFactoryyyImpl(final TwConnectionFactory factoryForAdditionalConnections) {
    tcf = factoryForAdditionalConnections;
  }

  @Override
  public Connection createConnection() {
    return tcf.createConnection();
  }

}
