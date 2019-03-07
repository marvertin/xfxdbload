package cz.tconsult.lib.ifxdbload.workflow.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.tw.RowCol;

/**
 * Vypisovač chyb
 * @author veverka
 *
 */
public class FSqlExcFormatter {



  public static String format(final Exception exc, final String sourceName, final RowCol startPoint, final String statement, final int offset) {
    final TextStringBuilder tsb = new TextStringBuilder(statement.length() * 2);
    final RowCol rowCol = compute(startPoint, offset, statement);
    final String popisPoziceChyby = String.format("%s (%d,%d)", sourceName, rowCol.getRow() + 1, rowCol.getCol() + 1);
    tsb.append("!!!!!!!!!!!! %s !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n", popisPoziceChyby);
    final String textSChybou = includeErroTextInStatement(startPoint, offset, statement, extractIfxErrInfo(exc));
    tsb.append("%s\n", textSChybou);
    return tsb.toString();
  }

  /**
   * Vypočte sloupec a řádek s znalostí počátku příkazu ve zdroji, offsetu ve stringu příkazu a textu příkazu
   * @param startPoint Řádky a sloupce se počítají od jedničky
   * @param offset Snad se počítá od nuly
   * @param data Data, ve kterých problém je
   * @return
   */
  private static RowCol compute(final RowCol startPoint, final int offset, final String data) {
    int row = startPoint.getRow();
    int col = startPoint.getCol(); // lexer počítá normálně od nuly
    for (int i = 0; i < Math.min(offset,  data.length()); i++) {
      if (data.charAt(i) == '\n') {
        row++;
        col = 0;
      } else {
        col++;
      }
    }
    return new RowCol(row, col);
  }

  /**
   * Formátuje text tak, aby tam byla čísla řádku a označení chyby
   * @param startPoint Řádky a sloupce se počítají od jedničky
   * @param offset Snad se počítá od nuly
   * @param statement Data, ve kterých problém je
   * @return
   */
  private static String includeErroTextInStatement(final RowCol startPoint, final int offset, final String statement, final List<String> errorTexts) {
    final TextStringBuilder tsb = new TextStringBuilder(statement.length() * 2);
    final List<String> lines = Arrays.asList(statement.split("\n"));
    final RowCol chybka = compute( new RowCol(0,0), offset, statement);
    int lineNum = startPoint.getRow();
    // řádky před chybou a on
    for (final String s : lines.subList(0, chybka.getRow() + 1)) {
      tsb.append("%6d: %s\n", lineNum + 1, s);
      lineNum ++;
    }
    final String vyplnTecky = StringUtils.repeat(".", chybka.getCol());
    final String vyplnMezery = StringUtils.repeat(" ", chybka.getCol());
    tsb.append("~ERROR..%s^ %s\n", vyplnTecky, errorTexts.get(0));
    for (final String s : errorTexts.subList(1, errorTexts.size())) {
      tsb.append("        %s  %s\n", vyplnMezery, s);
    }
    // řádky po chybě a on
    for (final String s : lines.subList(chybka.getRow() + 1, lines.size())) {
      tsb.append("%6d: %s\n", lineNum + 1, s);
      lineNum ++;
    }
    return tsb.toString();
  }

  private static List<String> extractIfxErrInfo(final Exception exc) {
    return FThrowable.findThrowableType(exc, SQLException.class)
        .map( sqlexc -> {
          return chainToList(sqlexc).stream()
              .filter(e -> e.getErrorCode() != 0)
              .map(FSqlExcFormatter::extractIfxErrInfoSql).collect(Collectors.toList());
        }).orElse(Collections.singletonList(exc.toString()));
  }

  /**
   * Převede řetěz SQL výjimek na seznam SQL výjimek
   * @param exc
   * @return
   */
  private static List<SQLException> chainToList(final SQLException exc) {
    final List<SQLException> result = new ArrayList<>();
    for (SQLException e = exc; e != null; e = e.getNextException()) {
      result.add(e);
    }
    return result;
  }

  private static String extractIfxErrInfoSql(final SQLException exc) {
    return exc.getErrorCode() + ": " + exc.getMessage();
  }

}
