package cz.tconsult.lib.ifxdbload.workflow.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.ifxdbload.core.tw.ErrorReporter;
import cz.tconsult.lib.ifxdbload.core.tw.NamedString;
import cz.tconsult.lib.ifxdbload.core.tw.RowCol;
import cz.tconsult.lib.ifxdbload.workflow.db.P6SpyExceptionEnrichmentEventListener;
import cz.tconsult.lib.lexer.LexerToken;
import cz.tconsult.lib.lexer.LexerTokenLocator;

/**
 * Vypisovač chyb
 * @author veverka
 *
 */
public class ErrorReporterImpl implements ErrorReporter {

  // TODO [veverka] Formátovat lépe výpisy chyb -- 14. 3. 2019 12:55:02 veverka


  private static final Logger log = LoggerFactory.getLogger(ErrorReporterImpl.class);

  @Override
  public void parsing(final LexerToken badToken, final NamedString source) {
    log.error("{} - {}", badToken, source);
  }


  @Override
  public synchronized void sql(final DataAccessException exc, final SplStatement stm) {
    final int offset = P6SpyExceptionEnrichmentEventListener.pickErrorOffset(exc);

    final LexerTokenLocator locator = stm.getFirstTokenLocator();
    final RowCol startPoint = new RowCol(locator.getBegLineNumber(), locator.getBegColumnNumber());
    final String str = FSqlExcFormatter.format(exc, locator.getInputSourceName(), startPoint, stm.getText(), offset);

    log.error("{}", str);
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
   * @param data Data, ve kterých problém je
   * @return
   */
  private static String format(final RowCol startPoint, final int offset, final String data, final List<String> errorTexts) {
    final TextStringBuilder tsb = new TextStringBuilder(data.length() * 2);
    final List<String> lines = Arrays.asList(data.split("\n"));
    final RowCol chybka = compute( new RowCol(0,0), offset, data);
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

  private List<String> extractIfxErrInfo(final DataAccessException exc) {
    return FThrowable.findThrowableType(exc, SQLException.class)
        .map( sqlexc -> {
          return chainToList(sqlexc).stream()
              .filter(e -> e.getErrorCode() != 0)
              .map(this::extractIfxErrInfoSql).collect(Collectors.toList());
        }).orElse(Collections.singletonList(exc.toString()));
  }

  /**
   * Převede řetěz SQL výjimek na seznam SQL výjimek
   * @param exc
   * @return
   */
  private List<SQLException> chainToList(final SQLException exc) {
    final List<SQLException> result = new ArrayList<>();
    for (SQLException e = exc; e != null; e = e.getNextException()) {
      result.add(e);
    }
    return result;
  }

  private String extractIfxErrInfoSql(final SQLException exc) {
    return exc.getErrorCode() + ": " + exc.getMessage();
  }


  @Override
  public void goodObjectOnBadPlace(final SplStatement stm) {
    log.error("reportSpravnyObjektNaNespravnemMiste: {}", stm);
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 15:47:38]

  }


  @Override
  public void badOnceDirective(final List<String> errors, final NamedString source) {
    log.error("Direktivy " + errors + "   |  " + source.getName());
  }

}
