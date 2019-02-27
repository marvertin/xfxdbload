package cz.tconsult.CORE_REVIDOVAT.spl.parser;

import cz.tconsult.CORE_REVIDOVAT.parser.lexer.LexerTokenLocator;

public interface SplBase0 {

  String getParseredText();

  CharSequence getOriginText();

  LexerTokenLocator getParseredTextPosition();

}
