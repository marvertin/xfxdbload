package cz.tconsult.lib.ifxdbload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.Trida;

public class Pokus {


  private static final Logger log = LoggerFactory.getLogger(Pokus.class);

  public static void main(final String[] args) {
    new Trida();
    System.out.println("JEDU");
    log.info("LOGUJU");
  }
}
