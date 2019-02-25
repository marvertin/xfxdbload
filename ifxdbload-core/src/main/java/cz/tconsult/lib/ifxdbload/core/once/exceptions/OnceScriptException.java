package cz.tconsult.lib.ifxdbload.core.once.exceptions;

import java.util.List;

import cz.tconsult.lib.ifxdbload.core.once.datatypes.OnceError;

@SuppressWarnings("serial")
public class OnceScriptException extends RuntimeException {
  
  private List<OnceError> iErrors;
  
  public OnceScriptException(String aMessage){
    super(aMessage);
  }
  
  public OnceScriptException(List<OnceError> aErrors){
    iErrors=aErrors;
  }
  
  public List<OnceError> getErrors(){
    return iErrors;
  }
  
  
}
