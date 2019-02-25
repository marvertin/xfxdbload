package cz.tconsult.spl.parser;

import cz.tconsult.parser.lexer.LexerTokenLocator;

public interface SplBase0 {

  String getParseredText();

  CharSequence getOriginText();

  LexerTokenLocator getParseredTextPosition();

}
