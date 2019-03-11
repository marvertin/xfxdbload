package cz.tconsult.lib.ifxdbload.core.loaders.hasher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Záznam v tabulce hashů.
 * @author veverka
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IfxdbloaderObjhash {
  private String objtype;
  private String objname;
  private String srchash;
  private String cathash;
}
