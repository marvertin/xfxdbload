package cz.tconsult.lib.ifxdbload.core.once.datatypes;

import java.io.File;

import cz.tconsult.lib.ifxdbload.core.once.enums.EExecuteType;

public class ExecuteElement {
  EExecuteType iExecuteType;
  File iExecPath;
  String iArgs;
  public ExecuteElement(EExecuteType aExecuteType,File aExecPath,String aArgs) {
    iExecuteType=aExecuteType;
    iExecPath=aExecPath;
    iArgs=aArgs;
  }
  public EExecuteType getExecuteType(){
    return iExecuteType;
  }
  public File getExecPath(){
    return iExecPath;
  }
  public String getArgs() {
    return iArgs;
  }
}
