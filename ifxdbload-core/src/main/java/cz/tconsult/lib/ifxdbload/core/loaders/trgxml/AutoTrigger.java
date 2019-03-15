package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;

import java.util.Set;


public class AutoTrigger {
  private Set<AColumnName> iColumnsToIncludeA; // Seznam sloupců, které spouštějí trigger
  private Set<AColumnName> iColumnsToExcludeA; // Seznam sloupců, které nespouštějí trigger
  private ATableName iTableNameA;
  private ETriggerType iTriggerType;
  private ATableName iArchTableNameA;
  private ETriggerEvent iTriggerEvent;

  public AutoTrigger(final Set<AColumnName> aColumnsToIncludeA,final Set<AColumnName> aColumnsToExcludeA,final ATableName aTableNameA,final ETriggerType aTriggerType,final ATableName aArchTableNameA, final ETriggerEvent aTriggerEvent){
    iColumnsToIncludeA=aColumnsToIncludeA; // Seznam sloupců, které spouštějí trigger
    iColumnsToExcludeA=aColumnsToExcludeA; // Seznam sloupců, které nespouštějí trigger
    iTableNameA=aTableNameA;
    iTriggerType=aTriggerType;
    iArchTableNameA=aArchTableNameA;
    iTriggerEvent=aTriggerEvent;
  }

  public AutoTrigger(){
  }

  public Set<AColumnName> getColumnsToIncludeA() {
    return iColumnsToIncludeA;
  }

  public void setColumnsToIncludeA(final Set<AColumnName> aColumnsToIncludeA) {
    iColumnsToIncludeA = aColumnsToIncludeA;
  }

  public Set<AColumnName> getColumnsToExcludeA() {
    return iColumnsToExcludeA;
  }

  public void setColumnsToExcludeA(final Set<AColumnName> aColumnsToExcludeA) {
    iColumnsToExcludeA = aColumnsToExcludeA;
  }

  public ATableName getTableNameA() {
    return iTableNameA;
  }

  public void setTableNameA(final ATableName aTableNameA) {
    iTableNameA = aTableNameA;
  }

  public ATableName getArchTableNameA() {
    return iArchTableNameA;
  }

  public void setArchTableNameA(final ATableName aArchTableNameA) {
    iArchTableNameA = aArchTableNameA;
  }

  public ETriggerEvent getTriggerEvent() {
    return iTriggerEvent;
  }

  public void setTriggerEvent(final ETriggerEvent aTriggerEvent) {
    iTriggerEvent = aTriggerEvent;
  }

  public ETriggerType getTriggerType() {
    return iTriggerType;
  }

  public void setTriggerType(final ETriggerType aTriggerType) {
    iTriggerType = aTriggerType;
  }

}
