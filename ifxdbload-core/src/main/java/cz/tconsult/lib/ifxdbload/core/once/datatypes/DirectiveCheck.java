package cz.tconsult.lib.ifxdbload.core.once.datatypes;

import cz.tconsult.lib.ifxdbload.core.once.enums.EDirectiveCheckType;

public class DirectiveCheck {
  
  private final EDirectiveCheckType error;
  
  private final String source;
  
  private DirectiveCheck (EDirectiveCheckType aError, String aSource) {
    error = aError;
    source = aSource;
  }
  
  public static DirectiveCheck create(EDirectiveCheckType aError, String aSource){
    return new DirectiveCheck(aError, aSource);
  }
  
  public static DirectiveCheck create(EDirectiveCheckType aError){
    return new DirectiveCheck(aError, null);
  }
  
  public String toString(){
    if (source!=null) {
      return error.getMessage().replaceFirst("%s", source);
    } else {
      return error.getMessage();
    }
    
  }
  
}
