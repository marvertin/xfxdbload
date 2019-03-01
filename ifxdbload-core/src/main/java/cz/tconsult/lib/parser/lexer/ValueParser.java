package cz.tconsult.lib.parser.lexer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Tokenizer je zodpovědný pouze za rozsekání vstupního proudu znaků na jedntolivé řetězce, tzv. tokeny. Tyto tokeny
 * jsou však v řetězcovém formátu tak, jak byly ve vstupním textu.
 * Umožnuje parserování řetězcového tvaru tokenů mapování na jiné, pro další zpracování vhodnější hodnoty.
 * <p>Příklady použití:
 * <p>Zpracování čísel. Když je vytvořen token čísla, je vždy v řetězcovém formátu. Lepší je, aby token neobsahoval jako hodnotu
 * řetězec, ale přímo číslo v typu Integer, Long nebo BigInteger nebo BigDecimal atd. Konverzi provede právě třída implementující toto rozhraní
 * <p>Uvozovkování řetězců. Řetězce jsou běžně v jazycích uzavírány do uvozovek a nějakým způsobem jsou escapovány uvozovky uvnitř.
 * Pro další zpracování je však vhodné se od uvozovek odporstit. A právě toto odproštění od uvozovek a od vnitřích escapů může dělat ValueParser,
 * datový typ hodnoty v tomto případě nemění, je to String, ale mění reprezentaci.
 * <p>Implementace rozhraní bude často děláno jako ananymní třída při definici tokenu pomocí define token.
 * Například:
 * <pre>
 *   defineToken("[0-9]+\\.[0-9]+", "DOUBLE",
 *        new ValueParser() {
 *           public Object parseValue(String str) { return new Double(str); }
 *        }
 *   );
 * </pre>
 * Tento příklad je jen pro ilustraci, protože existuje předdefinované parsery pro základní javovské typy.
 * Stejného efektu lze tedy dosáhnout takto:
 * <pre>
 *   defineToken("[0-9]+\\.[0-9]+", "DOUBLE", ValueParser.DOUBLE);
 * </pre>
 *
 * @author Martin Veverka
 * @version 1.0
 */

@FunctionalInterface
public interface ValueParser {

  /**
   * Veškeré hodnoty parseruje na null. Hodí se v případech, kdy tokeny představují oddělovače,
   * operátory atd. a hodnota tak postrádá jakýkoli význam.
   */
  public static final ValueParser NULL = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return null; }
  };

  /**
   * Parserování je identita, čili vrátí řetězec, který dostane a neprovádí žádné parserování. Jen pro úplnost.
   * Stejné chování se dosáhne uvedením defineToken pez parametru parseru.
   */
  public static final ValueParser IDENTITY = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return aValue; }
  };
  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Double
   */
  public static final ValueParser DOUBLE = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Double(aValue); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Integer
   */
  public static final ValueParser INTEGER = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Integer(aValue); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Long
   */
  public static final ValueParser LONG = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Long(aValue); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Boolean
   */
  public static final ValueParser BOOLEAN = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Boolean(aValue); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Short
   */
  public static final ValueParser SHORT = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Short(aValue); }
  };


  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Byte
   */
  public static final ValueParser BYTE = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new Byte(aValue); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Byte
   */
  public static final ValueParser BIGINTEGER = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new BigInteger(aValue,10); }
  };

  /**
   * Parseruje hodnotu ve formátu javovského typu java.lang.Byte
   */
  public static final ValueParser BIGDECIMAL = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) { return new BigDecimal(aValue); }
  };


  /**
   * Parser hodnot ve formátu Javového řetězce. V této verzi nepodoruje unicode escape ani oknatlové hodnoty.
   * Podporuje však veškeré ostatní bekslešování.
   */
  public static final ValueParser STRING = new ValueParser() {
    @Override
    public Object parseValue(final String aValue) {

      assert aValue != null;
      assert aValue.length() > 2;
      assert aValue.charAt(0) == '"';
      assert aValue.charAt(aValue.length()-1) == '"';

      final StringBuffer sb = new StringBuffer(aValue.length());

      for (int i = 1; i < aValue.length() - 1; i++) {  // předpokládáme, že řetězec je uzavřen uvozovkami, takže koncové značky nebereme
        char c = aValue.charAt(i);  // znak řetězce
        if (c == '\\') { // je-li to escape
          c = aValue.charAt(++i); //posuneme se na další znak
          switch (c) {
          case 'b'  : sb.append('\b'); break;
          case 't'  : sb.append('\t'); break;
          case 'n'  : sb.append('\n'); break;
          case 'f'  : sb.append('\f'); break;
          case 'r'  : sb.append('\r'); break;
          case '"'  : sb.append('\"'); break;
          case '\'' : sb.append('\''); break;
          case '\\' : sb.append('\\'); break;
          default: assert false; // nesmí tam být paznak
          }
        }
        else {

          sb.append(c);
        }
      }
      return sb.toString();
    }
  };

  /**
   * Jediná metoda rozhraní, která provádí parserování hodnoty řetězce
   * na jiný vhodnější typ. Matoda zná pouze řetězcovou hodnotu a nic víc. Pouze z ním musí být schopna určit,
   * jak parserovat.
   * @exception RuntimeException Metodě je předán řetězec, jenž byl již "prohnán" regulárním výrazem. Takže by sem
   * neměla příjít hodnota, kterou nelze parserovat na odpovídající typ. Tedy veškeré výjimky vyhozené z implementace
   * mají fatální dopad.
   * @param aValue Řetězcová hodnota tokenu.
   * @return Parserovaná hodnota.
   */
  public Object parseValue(String aValue);



}
