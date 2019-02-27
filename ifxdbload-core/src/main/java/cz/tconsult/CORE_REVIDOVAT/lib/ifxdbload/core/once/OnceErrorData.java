package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;


public class OnceErrorData  {

  private final String iErrorType;
  private final String iPathForLog;
  private final int iBeginRow;
  private final int iBeginCol;
  private final int iBeginPosition;
  private final int iEndRow;
  private final int iEndCol;
  private final int iEndPosition;
  private final Throwable cause;

  private OnceErrorData(String aErrorType
      ,String aPathForLog
      ,int aBeginRow,int aBeginCol, int aBeginPosition
      ,int aEndRow,int aEndCol, int aEndPosition
      , Throwable aCause){
    iErrorType=aErrorType;
    iPathForLog=aPathForLog;
    iBeginRow = aBeginRow;
    iBeginCol = aBeginCol;
    iBeginPosition = aBeginPosition;
    iEndRow = aEndRow;
    iEndCol = aEndCol;
    iEndPosition = aEndPosition;
    cause = aCause;
  }

 
  public static OnceErrorData create(String aErrorType,String aSourceName) {

    return new OnceErrorData(aErrorType, aSourceName, -1, -1, -1, -1, -1, -1, null);
  }

  public static OnceErrorData create(String aErrorType, OnceScriptData aSource) {

    return new OnceErrorData(aErrorType, aSource.getPath(), -1, -1, -1, -1, -1, -1, null);
  }

  public static OnceErrorData create(String aErrorType
      ,String aSourceName
      , int aBeginRow,int aBeginCol, int aBeginPosition
      , int aEndRow,int aEndCol, int aEndPosition) {

    return new OnceErrorData(aErrorType, aSourceName
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, null);
  }

  public static OnceErrorData create(String aErrorType
      ,OnceScriptData aSource
      , int aBeginRow,int aBeginCol, int aBeginPosition
      , int aEndRow,int aEndCol, int aEndPosition) {

    return new OnceErrorData(aErrorType, aSource.getPath()
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, null);
  }

  public static OnceErrorData create(String aErrorType
      ,String aSourceName
      , Throwable aCause) {

    return new OnceErrorData(aErrorType, aSourceName
        , -1, -1, -1, -1, -1, -1
        , aCause);
  }

  public static OnceErrorData create(String aErrorType
      ,OnceScriptData aSource
      , Throwable aCause) {

    return new OnceErrorData(aErrorType, aSource.getPath()
        , -1, -1, -1, -1, -1, -1
        , aCause);
  }
  
  public static OnceErrorData create(String aErrorType
      ,String aSourceName
      , int aBeginRow,int aBeginCol, int aBeginPosition
      , int aEndRow,int aEndCol, int aEndPosition, Throwable aCause) {

    return new OnceErrorData(aErrorType, aSourceName
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, aCause);
  }

  public static OnceErrorData create(String aErrorType
      ,OnceScriptData aSource
      , int aBeginRow,int aBeginCol, int aBeginPosition
      , int aEndRow,int aEndCol, int aEndPosition, Throwable aCause) {

    return new OnceErrorData(aErrorType, aSource.getPath()
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, aCause);
  }


  public String getErrorType(){
    return iErrorType;
  }


  /**
   * @return the file
   */
  public String getPathForLog() {
    return iPathForLog;
  }

  /**
   * @return the cause
   */
  public Throwable getCause() {
    return cause;
  }

  public String toString() {

    String result = iErrorType 
    + " [" + iBeginRow + ", " + iBeginCol + "]->[" 
    + iEndRow + ", " + iEndCol + "](pos: " 
    + iBeginPosition + "->" + iEndPosition + ")";
    if (cause != null) {

      result += "{Cause: " + cause + "}";
    }
    return result;
  }

  public String getMessage() {
    StringBuilder message = new StringBuilder();
    message.append(getPathForLog()+":" + iErrorType);
    int row = getBeginRow();
    if (row >= 0) {

      message.append("["+ row + " COLUMN:"+ getBeginCol()+"]");
    }
    message.append("("+ getPathForLog()+")");
    return message.toString();
  }


  /**
   * @return the beginRow
   */
  public int getBeginRow() {
    return iBeginRow;
  }

  /**
   * @return the beginCol
   */
  public int getBeginCol() {
    return iBeginCol;
  }

  /**
   * @return the beginPosition
   */
  public int getBeginPosition() {
    return iBeginPosition;
  }

  /**
   * @return the endRow
   */
  public int getEndRow() {
    return iEndRow;
  }

  /**
   * @return the endCol
   */
  public int getEndCol() {
    return iEndCol;
  }

  /**
   * @return the endPosition
   */
  public int getEndPosition() {
    return iEndPosition;
  }

}
