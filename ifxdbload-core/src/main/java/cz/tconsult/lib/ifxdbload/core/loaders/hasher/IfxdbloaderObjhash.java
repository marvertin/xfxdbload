package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import lombok.Data;

/**
 * Záznam v tabulce hashů.
 * @author veverka
 *
 */
@Data
public class IfxdbloaderObjhash {
  private String objtype;
  private String objname;
  private String owner;
  private String srchash;
  private String cathash;
}
