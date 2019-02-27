package cz.tconsult.lib.ifxdbload.core.core;

import java.util.EnumSet;

import cz.tconsult.dbloader.itf.EFileCategory;
import lombok.Data;

/**
 * @author veverka
 *
 */
@Data
public class FazeAnalyzeResult {
  private final AEntryName analyzedEntryName;
  private final EFileCategory fileCategory;
  private final EnumSet<EFazeZavedeni> fazes;
}
