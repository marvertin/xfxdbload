package cz.tconsult.parser.lexer;


import cz.tconsult.tw.util.Token;

/**
 * Továrna na tokeny lexeru.
 * @author Martin Veverka
 * @version 1.0
 */

public class LexerTokenFactory {

  private static LexerTokenFactory sFactory;

  private LexerTokenFactory() {
  }

  /**
   * Vytvoří instanci továrny na tokeny.
   * @return
   */
  public static LexerTokenFactory getInstance() {
    if (sFactory == null) sFactory = new LexerTokenFactory();
    return sFactory;
  }

  public LexerToken createToken(Object aTokenTyp, Object aTokenValue, String aTokenText, LexerTokenLocator aLocator) {
    return new CLexerToken(aTokenTyp, aTokenValue, aTokenText, aLocator);

  }

  public LexerTokenLocator createLocator(String aInputSourceName,
       int aBegLineNumber,     int aBegColumnNumber,   int aBegPosition,
       int aEndLineNumber,     int aEndColumnNumber,   int aEndPosition) {
      return new CLexerTokenLocator(aInputSourceName, aBegLineNumber, aBegColumnNumber, aBegPosition,
           aEndLineNumber, aEndColumnNumber, aEndPosition);

  }
  
  public LexerTokenLocator createLocator(String aInputSourceName,
      LexerTokenLocator aBegin, LexerTokenLocator aEnd) {
    
    return new CLexerTokenLocator(aInputSourceName
        , aBegin.getBegLineNumber(), aBegin.getBegColumnNumber(), aBegin.getBegPosition()
        , aEnd.getEndLineNumber(), aEnd.getEndColumnNumber(), aEnd.getEndPosition());
  }
  
  public LexerTokenLocator createLocator(
      LexerTokenLocator aBegin, LexerTokenLocator aEnd) {
    
    return new CLexerTokenLocator(aBegin.getInputSourceName()
        , aBegin.getBegLineNumber(), aBegin.getBegColumnNumber(), aBegin.getBegPosition()
        , aEnd.getEndLineNumber(), aEnd.getEndColumnNumber(), aEnd.getEndPosition());
  }

  /////////////////////////////////////////////////////////////////////////
private static class CLexerToken implements Token, java.io.Serializable, LexerToken {

  private static final long serialVersionUID = 3351811220087986441L; 
  
   private final String iTokenText;
   private final Object iTokenTyp;
   private final Object iTokenValue;
   private final LexerTokenLocator iLocator;

  CLexerToken(Object aTokenTyp, Object aTokenValue, String aTokenText, LexerTokenLocator aLocator) {
    iTokenTyp = aTokenTyp;
    iTokenValue = aTokenValue;
    iTokenText = aTokenText;
    iLocator = aLocator;
  }

  /**
   * Vrátí typ tokenu, je to hodnota definovaná metodu {@link QuickSimpleLexer#defineToken}
   * @return Typ tokenu.
   */
  public Object getType() {
    return iTokenTyp;
  }

  /**
   * Vrátí hodnotu tokenu, hodnota může být přímo zadaný řetězec, nebo se jedná o konvertovanou hodnotu.
   * @return Hodnota tokenu.
   */
  public Object getValue() {
    return iTokenValue;
  }


  /**
   * Vrátí infomraci o pozici.
   * @return Vrácená hodnota je typu {@link LexerTokenLocator}, nikdy nevrací null.
   */
  public Object getSourcePositionInfo() {
    return iLocator;
  }

  /**
   * Vrátí infomraci o pozici.
   * @return Vrácená hodnota je typu {@link LexerTokenLocator}, nikdy nevrací null.
   */
  public LexerTokenLocator getLocator() {
    return iLocator;
  }

  /**
   * Vrátí vždy původní nekonvertovaný text token, jenž byl přečten ze vstupního proudu.
   * @return Text tokenu.
   */
  public String getText() {
    return iTokenText;
  }


  /**
   * Vrací řetězcovou reprezentaci tokenu obsahující typ, hodnotu a pozici, kde byl token přečten.
   * @return Řetězcová reprezentace tokenu.
   */
  public String toString() {
  	
    return  "tokentype=" +iTokenTyp + " value={" + getTokenValueWithoutNL() + "} " + getSourcePositionInfo();
  }

  /**
   * Vrátí hodnotu tokenu bez odřádkování.
   * @return
   */  
  private String getTokenValueWithoutNL() {
  	String tokentext = iTokenText.trim();
  	StringBuffer sb = null;
  	int i;
  	for (i = 0; i < tokentext.length(); i++) {
      char c = tokentext.charAt(i);
      if (c == '\r' || c =='\n' || c == '\t' ) { // těchto znaků se chceme zbavit
      	sb = new StringBuffer(tokentext.substring(0, i));
      	break;
      }
    }
    if (sb == null) return tokentext; // žádný pochybný znak nenalezen
		for ( ; i < tokentext.length(); i++) {
			char c = tokentext.charAt(i);
			if (c == '\r' || c =='\n' || c == '\t' ) { // těchto znaků se chceme zbavit
				sb.append(' ');
			} else sb.append(c);
		}
		return sb.toString();
  }

}

/**
 * Třída reprezentující pozici tokenu ve vstupním proudu. Instance vznbiká
 * zvlášt pro každý token, pokud není na lexeru nastaveno jinak. Token si nese
 * infomraci s sebou a lze ji zjistit pomocí  {@link cz.tconsult.tw.util.Token#getSourcePositionInfo}
 * @author Martin Veverka
 * @version 1.0
 */


  private static class CLexerTokenLocator extends LexerTokenLocatorBase implements java.io.Serializable {

  private static final long serialVersionUID = 5048289924116015312L;


  CLexerTokenLocator(String aInputSourceName,
       int aBegLineNumber,     int aBegColumnNumber,   int aBegPosition,
       int aEndLineNumber,     int aEndColumnNumber,   int aEndPosition) {

    iInputSourceName = aInputSourceName;
    iBegRadek = aBegLineNumber;
    iBegSloupec = aBegColumnNumber;
    iBegPozice = aBegPosition;
    iEndRadek = aEndLineNumber;
    iEndSloupec = aEndColumnNumber;
    iEndPozice = aEndPosition;

  }

  private String iInputSourceName;

  private int iBegRadek;
  private int iBegSloupec;
  private int iBegPozice;   // pozice znaku od počátku čtení z readeru (počítáno od 0)

  private int iEndRadek;
  private int iEndSloupec;
  private int iEndPozice;   // pozice znaku od počátku čtení z readeru (počítáno od 0)


    /** {@inheritDoc} */
  public int getBegLineNumber() {
    return iBegRadek;

  }

    /** {@inheritDoc} */
  public int getBegColumnNumber() {
    return iBegSloupec;
  }

    /** {@inheritDoc} */
  public int getBegPosition() {
    return iBegPozice;
  }

    /** {@inheritDoc} */
  public int getEndLineNumber() {
    return iEndRadek;
  }

    /** {@inheritDoc} */
  public int getEndColumnNumber() {
    return iEndSloupec;
  }

    /** {@inheritDoc} */
  public int getEndPosition() {
    return iEndPozice;
  }

    /** {@inheritDoc} */
  public String getInputSourceName() {
    return iInputSourceName;
  }
}
}
