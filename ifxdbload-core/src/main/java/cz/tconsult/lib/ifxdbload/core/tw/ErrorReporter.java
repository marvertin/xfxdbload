package cz.tconsult.lib.ifxdbload.core.tw;

import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.lexer.LexerToken;

public interface ErrorReporter {

  void sql(final DataAccessException exc, final SplStatement stm);

  void parsing(LexerToken badToken, NamedString source);

  void goodObjectOnBadPlace(SplStatement stm);

}