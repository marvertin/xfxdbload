package cz.tconsult.lib.ifxdbload.core.once.enums;

public enum EVariant {
  VCzech("VCzech"),
  VSlovak("VSlovak");
  
  private final String text;
  
  
  EVariant(String textValue){
    this.text = textValue;
  }
  
  public String getTextValue(){
    return text;
  }
  
  public static EVariant getVariantByString(String s){
    if(s.equalsIgnoreCase("VCzech")) return VCzech;
    if(s.equalsIgnoreCase("VSlovak")) return VSlovak;
    return null;
  }
}
