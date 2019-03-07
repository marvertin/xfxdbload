package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Záznam v tabulce hashů.
 * @author veverka
 *
 */
@Data
@AllArgsConstructor
public class IfxdbloaderObjhash {
  private String objtype;
  private String objname;
  private String owner;
  private String srchash;
  private String cathash;
}
