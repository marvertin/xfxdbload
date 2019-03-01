package cz.tconsult.lib.parser.spllexer;


import cz.tconsult.lib.parser.lexer.LexerToken;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface SplToken extends LexerToken
{
  int getTokenId();
  String getTokenIdAsString();
  String getTokenText();
  int getLineNumber();
}
