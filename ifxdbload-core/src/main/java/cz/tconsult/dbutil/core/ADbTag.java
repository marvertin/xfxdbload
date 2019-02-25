/**
 *
 */
package cz.tconsult.dbutil.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Označuje v TC vymyšlený databázový jednoduchý tag
 * pro všechny možné dartabázové stroje.
 *
 * Připojení se dá specifikovat:
 *
 * [informix:]DATABÁZE@INFORMIX-SERVER
 * [oracle]:DATABÁZE@SERVER[:PORT]
 * [mysql]:DATABÁZE@SERVER[:PORT]
 *
 * Pokud úvodní rozlišovač neuveden, připojuje se k Informixu (zpětná kompatibilita).¨
 * Tag normálně obsahuje to, co zadal uživatel, může obsahovat i celé
 * jdbc URL.
 *
 * @author veverka
 *
 */
public class ADbTag  {

  static final long serialVersionUID = 158610270010909770L;

  /**
   * @param aValue
   */
  protected ADbTag(final String aValue) {
    //  super(aValue);
  }


  public static ADbTag from(final String s) {
    return StringUtils.isBlank(s) ? null : new ADbTag(s);
  }



}
