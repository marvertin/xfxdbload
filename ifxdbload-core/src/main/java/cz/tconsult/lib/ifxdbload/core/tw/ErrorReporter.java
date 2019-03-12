package cz.tconsult.lib.ifxdbload.core.tw;

import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.lexer.LexerToken;

public interface ErrorReporter {

  void reportError(final DataAccessException exc, final SplStatement stm);

  void reportError(LexerToken badToken, NamedString source);

  void reportSpravnyObjektNaNespravnemMiste(SplStatement stm);

}