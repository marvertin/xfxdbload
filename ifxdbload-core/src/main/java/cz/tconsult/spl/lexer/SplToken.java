package cz.tconsult.spl.lexer;


import cz.tconsult.tw.util.Token;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface SplToken
  extends Token
{
  int getTokenId();
  String getTokenIdAsString();
  String getTokenText();
  int getLineNumber();
}
