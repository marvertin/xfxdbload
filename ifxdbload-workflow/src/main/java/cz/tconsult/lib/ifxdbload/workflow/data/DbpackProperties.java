/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.nio.file.Path;

import cz.tconsult.lib.ifxdbload.core.tw.ASchema;
import lombok.Data;


@Data
public class DbpackProperties {
  private final Path root;  // kořen, kde byly property nalezeny
  private final ADbkind dbkind;
  private final ASchema dbschema;
}
