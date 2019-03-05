package cz.tconsult.spl.lexer;

/**
 * @since 2003
 * @version 1.0
 */
public enum ESplTokenNoKeyword {
  IDENT("dataIDENT"),
  PUREINT("dataINTCISLOBEZZNAM"),
  STRING("dataRETEZEC"),
  TCDIRECTIVE("dataTCDIRECTIVE"),
  PUREREAL("dataREALCISLOBEZZNAM"),
  LEXERROR("LEXERROR"),

  //názvy symbolů získány z http://www.unicode.org/charts/PDF/U0000.pdf
  SYMBOL_LEFT_PARENTHESIS("("),
  SYMBOL_RIGHT_PARENTHESIS(")"),
  SYMBOL_LEFT_SQUARE_BRACKET("["),
  SYMBOL_RIGHT_SQUARE_BRACKET("]"),
  SYMBOL_COMMA(","),
  SYMBOL_COMMERCIAL_AT("@"),

  SYMBOL_COLON(":"),
  SYMBOL_SEMICOLON(";"),
  SYMBOL_LESS_THAN_SIGN("<"),
  SYMBOL_EQUALS_SIGN("="),
  SYMBOL_GREATHER_THAN_SIGN(">"),
  SYMBOL_PLUS_SIGN("+"),
  SYMBOL_ASTERISK("*"),
  SYMBOL_SLASH("/"),
  SYMBOL_MINUS_SIGN("-"),
  SYMBOL_PERIOD("charFULLSTOP"),

  SYMBOL_LESS_OR_EQUALS_SIGN("operMENSIROVNO"),
  SYMBOL_GREATER_OR_EQUALS_SIGN("operVETSIROVNO"),
  SYMBOL_EQUALS_EQUALS_SIGN("operROVNOROVNO"),
  SYMBOL_NOT_EQUALS_SIGN("operNEROVNO"),
  SYMBOL_DOUBLE_VERTICAL_LINE("operCONCATENATE"),
  SYMBOL_DOUBLE_COLON("operDVOJTECKADVOJTECKA"),
  SYMBOL_DOUBLE_LESS_THAN_SIGN("operMENSIMENSI"),
  SYMBOL_DOUBLE_GREATHER_THAN_SIGN("operVETSIVETSI"),
  ;
  private final String iPublicName;

  ESplTokenNoKeyword(final String aPublicName) {
    iPublicName = aPublicName;
  }

  public String getPublicName() {
    return iPublicName;
  }

  public String asString() {
    return getPublicName();
  }

  @Override
  public String toString() {
    return getPublicName();
  }
}
