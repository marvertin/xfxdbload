package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes;

import org.apache.commons.io.IOUtils;

import cz.tconsult.CORE_REVIDOVAT.dbutil.core.FDb;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.OnceScriptData;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EErrorType;
import cz.tconsult.CORE_REVIDOVAT.parser.lexer.LexerTokenLocator;

public class OnceError  {

  private final EErrorType iErrorType;
  private final String iPathForLog;
  private final int iBeginRow;
  private final int iBeginCol;
  private final int iBeginPosition;
  private final int iEndRow;
  private final int iEndCol;
  private final int iEndPosition;
  private final Throwable cause;

  private OnceError(final EErrorType aErrorType
      ,final String aPathForLog
      ,final int aBeginRow,final int aBeginCol, final int aBeginPosition
      ,final int aEndRow,final int aEndCol, final int aEndPosition
      , final Throwable aCause){
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

  public static OnceError create(final EErrorType aErrorType, final String aSourceName, final LexerTokenLocator aLoc) {

    return new OnceError(aErrorType, aSourceName
        , aLoc.getBegLineNumber(), aLoc.getBegColumnNumber(), aLoc.getBegPosition()
        , aLoc.getEndLineNumber(), aLoc.getEndColumnNumber(), aLoc.getEndPosition()
        , null);
  }

  public static OnceError create(final EErrorType aErrorType,final OnceScriptData aSource, final LexerTokenLocator aLoc) {

    return new OnceError(aErrorType, aSource.getPath()
        , aLoc.getBegLineNumber(), aLoc.getBegColumnNumber(), aLoc.getBegPosition()
        , aLoc.getEndLineNumber(), aLoc.getEndColumnNumber(), aLoc.getEndPosition()
        , null);
  }

  public static OnceError create(final EErrorType aErrorType,final String aSourceName) {

    return new OnceError(aErrorType, aSourceName, -1, -1, -1, -1, -1, -1, null);
  }

  public static OnceError create(final EErrorType aErrorType, final OnceScriptData aSource) {

    return new OnceError(aErrorType, aSource.getPath(), -1, -1, -1, -1, -1, -1, null);
  }

  public static OnceError create(final EErrorType aErrorType
      ,final String aSourceName
      , final int aBeginRow,final int aBeginCol, final int aBeginPosition
      , final int aEndRow,final int aEndCol, final int aEndPosition) {

    return new OnceError(aErrorType, aSourceName
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, null);
  }

  public static OnceError create(final EErrorType aErrorType
      ,final OnceScriptData aSource
      , final int aBeginRow,final int aBeginCol, final int aBeginPosition
      , final int aEndRow,final int aEndCol, final int aEndPosition) {

    return new OnceError(aErrorType, aSource.getPath()
        , aBeginRow, aBeginCol, aBeginPosition, aEndRow, aEndCol, aEndPosition, null);
  }

  public static OnceError create(final EErrorType aErrorType
      ,final String aSourceName
      , final Throwable aCause) {

    return new OnceError(aErrorType, aSourceName
        , -1, -1, -1, -1, -1, -1
        , aCause);
  }

  public static OnceError create(final EErrorType aErrorType
      ,final OnceScriptData aSource
      , final Throwable aCause) {

    return new OnceError(aErrorType, aSource.getPath()
        , -1, -1, -1, -1, -1, -1
        , aCause);
  }


  public EErrorType getErrorType(){
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

  @Override
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
    final StringBuilder message = new StringBuilder();
    message.append(getPathForLog()+":" + iErrorType.getMessage());
    message.append(IOUtils.LINE_SEPARATOR);
    switch(getErrorType()){
    case SYNTAX_ERROR: message.append("(ROW:"+ getBeginRow() + " COLUMN:"+ getBeginCol()+")");
    break;
    case BAD_DIR: message.append("("+ getPathForLog()+")");
    break;
    }
    message.append(" ");
    message.append(FDb.locateAndFormatSqlException(cause));
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

  public String getSourceName() {

    return null;
  }


}
