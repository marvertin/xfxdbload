package cz.tconsult.lib.ifxdbload.core.faze;

import java.util.EnumSet;

import cz.tconsult.dbloader.itf.EFileCategory;
import lombok.Data;

/**
 * Výsledek analýy entry, do kterých patří fází? Která je to kategorie.
 * @author veverka
 *
 */
@Data
public class FazeAnalyzeResult {
  private final AEntryName analyzedEntryName;
  private final EFileCategory fileCategory;
  private final EnumSet<EFazeZavedeni> fazes;
}
