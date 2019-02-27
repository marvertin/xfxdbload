package cz.tconsult.CORE_REVIDOVAT.spl.parser;

import java.util.List;
import java.util.Set;

import cz.tconsult.CORE_REVIDOVAT.parser.lexer.LexerTokenLocator;

public interface SplDocument {

  LexerTokenLocator getLocator();

  List<SplBase0> getChildrenList();

  Set<LexerTokenLocator> getComments();

  String getParseredText(LexerTokenLocator loc);

}
