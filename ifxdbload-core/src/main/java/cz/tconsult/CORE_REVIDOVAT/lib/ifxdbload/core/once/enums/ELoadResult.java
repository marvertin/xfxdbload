package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums;

public enum ELoadResult {
  LOADED_NOW("Skrit %s nacten."),
  ALREADY_LOADED_BEFORE("Skript %s uz byl jednou spusten"),
  SKIPPED_NOW("Zpracovani skriptu %s bylo preskoceno - duvod viz tabulka xonce_scripts."),
  ;
  
  private final String iMessage;
  
  ELoadResult(String aText){
    iMessage=aText;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return iMessage;
  }
  
  
}
