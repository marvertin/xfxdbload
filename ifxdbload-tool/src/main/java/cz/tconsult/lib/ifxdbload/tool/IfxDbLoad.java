package cz.tconsult.lib.ifxdbload.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.Trida;

public class IfxDbLoad {


  private static final Logger log = LoggerFactory.getLogger(IfxDbLoad.class);

  public static void main(final String[] args) {
    new Trida();
    log.info("Prázdný IfxDbLoad - loadování informix databáze");
  }
}
