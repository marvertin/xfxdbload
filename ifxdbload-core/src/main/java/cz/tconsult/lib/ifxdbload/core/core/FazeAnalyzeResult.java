package cz.tconsult.lib.ifxdbload.core.core;

import cz.tconsult.dbloader.itf.EFileCategory;

/**
 * @author veverka
 *
 */
public class FazeAnalyzeResult {
  private String analyzedEntryName;
  private EFileCategory iFileCategory;
  private EFazeZavedeni[] fazes;


  /**
   * @return the fazes
   */
  public EFazeZavedeni[] getFazes() {
    return fazes;
  }

  /**
   * @param aFazes the fazes to set
   */
  public void setFazes(final EFazeZavedeni[] aFazes) {
    fazes = aFazes;
  }

  /**
   * @return the analyzedEntryName
   */
  public String getAnalyzedEntryName() {
    return analyzedEntryName;
  }

  /**
   * @param aAnalyzedEntryName the analyzedEntryName to set
   */
  public void setAnalyzedEntryName(final String aAnalyzedEntryName) {
    analyzedEntryName = aAnalyzedEntryName;
  }

  /**
   * Vrátí kategorii souboru.
   * Pokud vrátí null, znamená to, že je to špatná cesta
   * @return
   */
  public EFileCategory getFileCategory() {
    return iFileCategory;
  }

  /**
   * @param aFileCategory the fileCategory to set
   */
  public void setFileCategory(final EFileCategory aFileCategory) {
    iFileCategory = aFileCategory;
  }
}
