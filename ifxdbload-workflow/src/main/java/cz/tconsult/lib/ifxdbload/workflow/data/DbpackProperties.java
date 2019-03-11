/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow.data;

import java.nio.file.Path;

import lombok.Data;


@Data
public class DbpackProperties {
  private final Path root;  // kořen, kde byly property nalezeny
  private final ADbkind dbkind;

  // Kdysi bývalo jako propety i schéma, te%d na to rezignujeme
}
