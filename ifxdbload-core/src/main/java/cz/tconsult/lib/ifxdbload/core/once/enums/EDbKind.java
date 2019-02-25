package cz.tconsult.lib.ifxdbload.core.once.enums;

public enum EDbKind {
  MAIN("MAIN"),
  ARCHIV("ARCHIV"),
  STAT("STAT");
  
  private final String text;
  
  
  EDbKind(String textValue){
    this.text = textValue;
  }
  
  public String getTextValue(){
    return text;
  }
}
