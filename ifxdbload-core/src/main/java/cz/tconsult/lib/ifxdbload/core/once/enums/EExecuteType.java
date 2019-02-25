package cz.tconsult.lib.ifxdbload.core.once.enums;

public enum EExecuteType {
 JAVAJAR,UNKNOWN;
 
 public static EExecuteType getExecuteTypeFrom(String s){
   if(s.startsWith("java")) return JAVAJAR;
   return UNKNOWN;
 }
}
