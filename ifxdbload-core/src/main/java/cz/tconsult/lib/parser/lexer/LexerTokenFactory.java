package cz.tconsult.lib.parser.lexer;

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
    if (sFactory == null) {
      sFactory = new LexerTokenFactory();
    }
    return sFactory;
  }

  public LexerToken createToken(final Object aTokenTyp, final Object aTokenValue, final String aTokenText, final LexerTokenLocator aLocator) {
    return new CLexerToken(aTokenTyp, aTokenValue, aTokenText, aLocator);

  }

  public LexerTokenLocator createLocator(final String aInputSourceName,
      final int aBegLineNumber,     final int aBegColumnNumber,   final int aBegPosition,
      final int aEndLineNumber,     final int aEndColumnNumber,   final int aEndPosition) {
    return new CLexerTokenLocator(aInputSourceName, aBegLineNumber, aBegColumnNumber, aBegPosition,
        aEndLineNumber, aEndColumnNumber, aEndPosition);

  }

  public LexerTokenLocator createLocator(final String aInputSourceName,
      final LexerTokenLocator aBegin, final LexerTokenLocator aEnd) {

    return new CLexerTokenLocator(aInputSourceName
        , aBegin.getBegLineNumber(), aBegin.getBegColumnNumber(), aBegin.getBegPosition()
        , aEnd.getEndLineNumber(), aEnd.getEndColumnNumber(), aEnd.getEndPosition());
  }

  public LexerTokenLocator createLocator(
      final LexerTokenLocator aBegin, final LexerTokenLocator aEnd) {

    return new CLexerTokenLocator(aBegin.getInputSourceName()
        , aBegin.getBegLineNumber(), aBegin.getBegColumnNumber(), aBegin.getBegPosition()
        , aEnd.getEndLineNumber(), aEnd.getEndColumnNumber(), aEnd.getEndPosition());
  }

  /////////////////////////////////////////////////////////////////////////
  private static class CLexerToken implements java.io.Serializable, LexerToken {

    private static final long serialVersionUID = 3351811220087986441L;

    private final String iTokenText;
    private final Object iTokenTyp;
    private final Object iTokenValue;
    private final LexerTokenLocator iLocator;

    CLexerToken(final Object aTokenTyp, final Object aTokenValue, final String aTokenText, final LexerTokenLocator aLocator) {
      iTokenTyp = aTokenTyp;
      iTokenValue = aTokenValue;
      iTokenText = aTokenText;
      iLocator = aLocator;
    }

    /**
     * Vrátí typ tokenu, je to hodnota definovaná metodu {@link QuickSimpleLexer#defineToken}
     * @return Typ tokenu.
     */
    @Override
    public Object getType() {
      return iTokenTyp;
    }

    /**
     * Vrátí hodnotu tokenu, hodnota může být přímo zadaný řetězec, nebo se jedná o konvertovanou hodnotu.
     * @return Hodnota tokenu.
     */
    @Override
    public Object getValue() {
      return iTokenValue;
    }

    /**
     * Vrátí infomraci o pozici.
     * @return Vrácená hodnota je typu {@link LexerTokenLocator}, nikdy nevrací null.
     */
    @Override
    public LexerTokenLocator getLocator() {
      return iLocator;
    }

    /**
     * Vrátí vždy původní nekonvertovaný text token, jenž byl přečten ze vstupního proudu.
     * @return Text tokenu.
     */
    @Override
    public String getText() {
      return iTokenText;
    }


    /**
     * Vrací řetězcovou reprezentaci tokenu obsahující typ, hodnotu a pozici, kde byl token přečten.
     * @return Řetězcová reprezentace tokenu.
     */
    @Override
    public String toString() {

      return  "tokentype=" +iTokenTyp + " value={" + getTokenValueWithoutNL() + "} " + getLocator();
    }

    /**
     * Vrátí hodnotu tokenu bez odřádkování.
     * @return
     */
    private String getTokenValueWithoutNL() {
      final String tokentext = iTokenText.trim();
      StringBuffer sb = null;
      int i;
      for (i = 0; i < tokentext.length(); i++) {
        final char c = tokentext.charAt(i);
        if (c == '\r' || c =='\n' || c == '\t' ) { // těchto znaků se chceme zbavit
          sb = new StringBuffer(tokentext.substring(0, i));
          break;
        }
      }
      if (sb == null)
      {
        return tokentext; // žádný pochybný znak nenalezen
      }
      for ( ; i < tokentext.length(); i++) {
        final char c = tokentext.charAt(i);
        if (c == '\r' || c =='\n' || c == '\t' ) { // těchto znaků se chceme zbavit
          sb.append(' ');
        } else {
          sb.append(c);
        }
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


    CLexerTokenLocator(final String aInputSourceName,
        final int aBegLineNumber,     final int aBegColumnNumber,   final int aBegPosition,
        final int aEndLineNumber,     final int aEndColumnNumber,   final int aEndPosition) {

      iInputSourceName = aInputSourceName;
      iBegRadek = aBegLineNumber;
      iBegSloupec = aBegColumnNumber;
      iBegPozice = aBegPosition;
      iEndRadek = aEndLineNumber;
      iEndSloupec = aEndColumnNumber;
      iEndPozice = aEndPosition;

    }

    private final String iInputSourceName;

    private final int iBegRadek;
    private final int iBegSloupec;
    private final int iBegPozice;   // pozice znaku od počátku čtení z readeru (počítáno od 0)

    private final int iEndRadek;
    private final int iEndSloupec;
    private final int iEndPozice;   // pozice znaku od počátku čtení z readeru (počítáno od 0)


    /** {@inheritDoc} */
    @Override
    public int getBegLineNumber() {
      return iBegRadek;

    }

    /** {@inheritDoc} */
    @Override
    public int getBegColumnNumber() {
      return iBegSloupec;
    }

    /** {@inheritDoc} */
    @Override
    public int getBegPosition() {
      return iBegPozice;
    }

    /** {@inheritDoc} */
    @Override
    public int getEndLineNumber() {
      return iEndRadek;
    }

    /** {@inheritDoc} */
    @Override
    public int getEndColumnNumber() {
      return iEndSloupec;
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPosition() {
      return iEndPozice;
    }

    /** {@inheritDoc} */
    @Override
    public String getInputSourceName() {
      return iInputSourceName;
    }
  }
}
