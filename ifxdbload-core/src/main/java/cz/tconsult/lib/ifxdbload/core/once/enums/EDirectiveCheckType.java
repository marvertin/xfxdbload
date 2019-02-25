package cz.tconsult.lib.ifxdbload.core.once.enums;

public enum EDirectiveCheckType {
  DBKIND("Script is not intended for DB of this kind: %s"),
  VARIANT("Script is not intended for DB of this variant: %s"),
  VARIANT_DIRECTORY("Script is placed in directory, that is not intended for DB of this variant: %s"),
  
  LOAD_IF_LOADED("Script was not loaded due to directive LOAD_IF_LOADED = %s"),
  LOAD_UNLESS_LOADED("Script was not loaded due to directive LOAD_UNLESS_LOADED = %s"),
  DBMS("Script is not intended for DB machine: %s"),
  
  ;
  
  
  private String iMessage;
  
  EDirectiveCheckType(String aMessage){
    iMessage=aMessage;
  }
  
  public String getMessage(){
    return iMessage;  
  }
}
