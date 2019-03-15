package cz.tconsult.lib.ifxdbload.core.splparser;

public enum EStmType {
  PROCEDURE, FUNCTION, VIEW, TRIGGER, SYNONYM,
  TABLE, INDEX,
  CALL,
  DDL, DML,
  BEGINWORK, COMMIT, ROLLBACK,
  ;

  public boolean isTransactionControl() {
    return this == EStmType.BEGINWORK || this == EStmType.COMMIT || this == EStmType.ROLLBACK;
  }
}

