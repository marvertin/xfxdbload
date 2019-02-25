package cz.tconsult.spl.parser;

import java.util.List;
import java.util.Set;

import cz.tconsult.parser.lexer.LexerTokenLocator;

public interface SplDocument {

  LexerTokenLocator getLocator();

  List<SplBase0> getChildrenList();

  Set<LexerTokenLocator> getComments();

  String getParseredText(LexerTokenLocator loc);

}
