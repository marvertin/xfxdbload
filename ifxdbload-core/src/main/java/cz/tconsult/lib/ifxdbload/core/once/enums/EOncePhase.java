package cz.tconsult.lib.ifxdbload.core.once.enums;


public enum EOncePhase  {
  BEFORE("0before"),
  ALTER("1alter"),
  SETTINGS("3settings"),
  MIGRATION("4migration"),
  ALTERD("5alter"),
  TIDY("6tidy"),
  MANUAL("manual");

  public String iString;

  EOncePhase(final String aString){
    iString=aString;
  }

  @Override
  public String toString(){
    return iString;
  }

  public static EOncePhase getTypeByStringAndPrior(final String s,final int p){
    if(s.equalsIgnoreCase("before") && p==0) {
      return BEFORE;
    }
    if(s.equalsIgnoreCase("alter") && p==1) {
      return ALTER;
    }
    if(s.equalsIgnoreCase("settings") && p==3) {
      return SETTINGS;
    }
    if(s.equalsIgnoreCase("migration")&& p==4) {
      return MIGRATION;
    }
    if(s.equalsIgnoreCase("alter")&& p==5) {
      return ALTERD;
    }
    if(s.equalsIgnoreCase("tidy") && p==6) {
      return TIDY;
    }
    if(s.equalsIgnoreCase("manual")) {
      return MANUAL;
    }
    return null;
  }

  public static EOncePhase getTypeByString(final String s){
    if(s.equalsIgnoreCase("0before")) {
      return BEFORE;
    }
    if(s.equalsIgnoreCase("1alter")) {
      return ALTER;
    }
    if(s.equalsIgnoreCase("3settings")) {
      return SETTINGS;
    }
    if(s.equalsIgnoreCase("4migration")) {
      return MIGRATION;
    }
    if(s.equalsIgnoreCase("5alter")) {
      return ALTERD;
    }
    if(s.equalsIgnoreCase("6tidy")) {
      return TIDY;
    }
    if(s.equalsIgnoreCase("manual")) {
      return MANUAL;
    }
    return null;
  }

  public String getOncePhaseName() {
    return iString;
  }

  /*
  0before
   DML
   Příprava, která je nutná udělat před tím, než je možné alterovat. Mohou to být různé konsolidace a já nevím co ještě. Ve většině případů se asi fáze nepoužije, ale pro jistotu ji necháme.

  1alter
   DDL
   Nedestruktivní altery. Ty jsou taková DDL, která nezničí data ani neznemožní další činnosti. Patří sem například:

  Vytvoření tabulky
  Přidání sloupce, mterý není not null nebo má defaultní hodnotu.
  Přidání indexu umožňující zadávat duplicitní hodnoty.
  Ostranění nebo zablokování.

  3settings
   DML
   Jednorázová nastavení databázových registrů, číselníků a podobně. Jsou to nastavení taková, která se nesmí již nikdy zopakovat. Například založení databázového registru a nastavení implicitní hodnoty, kterou si zákazník později změní.
  Nepatří sem nastavení registrů a číselníků, které zákazník nikdy nesmí měnit. To patří do patřičného podmodulu a složky src/reg.

  4migration
   DML
   DML modifikující data.

  Executy všech jednorázových procedur kromě procedur pomocných (viz kapitola "Jednorázové procedury"), typicky executy migračních procedur (vkládáno automaticky při sestavení).

  5alter
   DDL
   Destruktivní altery jsou takové altery, které mohou zničit data nebo mohou znemožnit migrace či jiná zpracování. Patří sem například:

  Rušení tabulek.
  Rušení sloupců.
  Rušení indexů.
  Zavádění nebo odblokování,  včetně překlopení sloupce na not null.

  6tidy
   DML
   Úklid. Promazání dat z různých tabulek, které nejsou dropovány jako celek. Odstranění údajů z dbregistrů a číselníků, které již nebudou potřeba, ale byly potřeba ještě během migrace atd.

  Dropy všech procedur, které prováděly jednorázovou činnost, včetně migračních procedur (vkládáno automaticky při sestavení).

   */
}
