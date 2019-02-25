package cz.tconsult.lib.ifxdbload.core.once.enums;

/**
 * Výčet typů pro evidenci stavu zpracování SQL příkazu v tabulce xonce_scripts.
 * @author kovarikj
 *
 */
public enum EStatementStatus {
  EXECUTING("EXECUTING"),
  DONE("DONE"),
  SKIPPED("SKIPPED"),
  FAILED("FAILED");
  
  EStatementStatus(String textValue){
        this.text = textValue;
  }
  
  private final String text;
  
  public String getTextValue(){
    return text;
  }
}
