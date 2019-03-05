package cz.tconsult.lib.parser;

import java.util.List;

import cz.tconsult.lib.ifxdbload.core.tw.ASourceName;
import lombok.Data;

/**
 * Reprezentuje parsrované etry, to znamená soubor nebo pentry ze zipu.
 *
 * @author veverka
 *
 */
@Data
public class ParseredSource {


  private final ASourceName sourceName;

  /** rozpársrované příkazy */
  private final List<SplStatement> statements;

}
