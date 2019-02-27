package cz.tconsult.lib.ifxdbload.core.faze;

import cz.tconsult.dbloader.itf.EFileCategory;

/**
 * Enum pre specifikovanie presnejsich nazvov pro logovani
 *
 * @author tomas
 */
public enum EFazeZavedeni {

  f020clean("Čištění", "Čištění schématu", EFileCategory.DBOBJ),

  f050before("Once skript - fáze 0before", "Zavádění once skriptů - fáze 0before", EFileCategory.ONCE),

  f100alter("Once skript - fáze 1alter", "Zavádění once skriptů - fáze 1alter", EFileCategory.ONCE),

  f205seq("Sekvence", "Zavádění sekvencí", EFileCategory.DBOBJ),

  f210tmp("Temporární tabulka", "Zavádění temporárních tabulek", EFileCategory.DBOBJ),

  f220pkg("Balík", "Zavádění packagů", EFileCategory.DBOBJ),

  f230prc("Procedura", "Zavádění procedur", EFileCategory.DBOBJ),

  f240vue("View", "Zavádění view", EFileCategory.DBOBJ),

  f250trg("Trigger", "Zavádění triggerů", EFileCategory.DBOBJ),

  f255trgxml("Automatický trigger", "Zavádění automatických triggerů", EFileCategory.XMLTRIGER),

  f260afterobj("Fáze po zavedení objektu", "Zavádění - fáze po zavedení objektů", EFileCategory.ALWAYS),

  f265materview("Materializované view", "Zavádění materializovaného view", EFileCategory.DBOBJ),

  f270reg("Registrace", "Zavádění registrací", EFileCategory.ALWAYS),

  f300settings("Once skript - fáze 3settings", "Zavádění once skriptů - fáze 3settings", EFileCategory.ONCE),

  f400migration("Once skript - fáze 4migration", "Zavádění once skriptů - fáze 4migration", EFileCategory.ONCE),

  f500alter("Once skript - fáze 5alter", "Zavádění once skriptů - fáze 5alter", EFileCategory.ONCE),

  f555trgxml("Automatický trigger", "Zavádění automatických triggerů", EFileCategory.XMLTRIGER),

  f600tidy("Once skript - fáze 6tidy", "Zavádění once skriptů - fáze 6tidy", EFileCategory.ONCE),

  f700finish("Fáze po zavedení všeho", "Zavádění - fáze po zavedení všeho", EFileCategory.ALWAYS),
  ;

  private String title;
  private String iTitleToLoad;
  private EFileCategory iEFileCategory;

  EFazeZavedeni(final String aTitle, final String aTitleToLoad, final EFileCategory aEFileCategory){
    title = aTitle;
    iTitleToLoad = aTitleToLoad;
    iEFileCategory = aEFileCategory;
    assert title != null;
    assert iTitleToLoad != null;
    assert iEFileCategory != null;
  }

  @Override
  public String toString() {
    return name();
  }

  public String title(){
    return title;
  }

  public String titleToLoad(){
    return iTitleToLoad;
  }

  public EFileCategory getFileCategory(){
    return iEFileCategory;
  }
}
