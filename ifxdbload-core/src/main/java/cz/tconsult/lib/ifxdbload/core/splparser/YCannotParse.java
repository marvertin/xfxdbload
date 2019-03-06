package cz.tconsult.lib.ifxdbload.core.splparser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Výjimka vyhozená při pokusu parsrovat, pokud daný parser parsrovat neumí ttuto sekvenci.
 * @author veverka
 *
 */
@RequiredArgsConstructor
@Getter
class YCannotParse extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = -4594017227070937849L;

  private final Object badToken;

}
