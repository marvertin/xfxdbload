package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.Optional;

@FunctionalInterface
interface ParserTry {

  /**
   * Vyzkouší, zda lze parsrovat SQL příkaz z danéh ostreamu,
   * pokud ano, vrátí ho, pokud ne, vyhofi vyjimu
   * @param ts
   */
  public Optional<SplStatement> tryParse(TokenChecker2 tok) throws YCannotParse;

}
