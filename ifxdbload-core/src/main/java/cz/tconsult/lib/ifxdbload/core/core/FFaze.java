package cz.tconsult.lib.ifxdbload.core.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.tconsult.dbloader.itf.EFileCategory;
import cz.tconsult.tw.lang.AString0;

public final class FFaze extends AString0 {

  private static final long serialVersionUID = -6767408765694497348L;

  //  private static final List<EFazeZavedeni> allFazes = new ArrayList<EFazeZavedeni>();

  //  private final EFileCategory iCategory;

  /*
  // důležité fáze ,na které nástro se odkazuje jsou zde

  private static final AFaze f050before =      AFaze.fromx(EFazeZavedeni.f050before.titleToLoad(), EFileCategory.ONCE);
  //  private static final AFaze f050before =      AFaze.fromx("f050before", EFileCategory.ONCE);
  private static final AFaze f100alter =       AFaze.fromx("f100alter", EFileCategory.ONCE);
  private static final AFaze f210tmp =         AFaze.fromx("f210tmp", EFileCategory.DBOBJ);
  private static final AFaze f220pkg =         AFaze.fromx("f220pkg", EFileCategory.DBOBJ);
  private static final AFaze f230prc =         AFaze.fromx("f230prc", EFileCategory.DBOBJ);
  private static final AFaze f240vue =         AFaze.fromx("f240vue", EFileCategory.DBOBJ);
  private static final AFaze f250trg =         AFaze.fromx("f250trg", EFileCategory.DBOBJ);
  private static final AFaze f255trgxml =      AFaze.fromx("f255trgxml", EFileCategory.XMLTRIGER);
  private static final AFaze f260afterobj =    AFaze.fromx("f260afterobj", EFileCategory.ALWAYS);
  private static final AFaze f270reg =         AFaze.fromx("f270reg", EFileCategory.ALWAYS);
  private static final AFaze f300settings =    AFaze.fromx("f300settings", EFileCategory.ONCE);
  private static final AFaze f400migration =   AFaze.fromx("f400migration", EFileCategory.ONCE);
  private static final AFaze f500alter =       AFaze.fromx("f500alter", EFileCategory.ONCE);
  private static final AFaze f555trgxml =      AFaze.fromx("f555trgxml", EFileCategory.XMLTRIGER);
  private static final AFaze f600tidy =        AFaze.fromx("f600tidy", EFileCategory.ONCE);
  private static final AFaze f700finish =      AFaze.fromx("f700finish", EFileCategory.ALWAYS);




  static {
    allFazes.add(f050before);
    allFazes.add(f100alter);
    allFazes.add(f210tmp);
    allFazes.add(f220pkg);
    allFazes.add(f230prc);
    allFazes.add(f240vue);
    allFazes.add(f250trg);
    allFazes.add(f255trgxml);
    allFazes.add(f260afterobj);
    allFazes.add(f270reg);
    allFazes.add(f300settings);
    allFazes.add(f400migration);
    allFazes.add(f500alter);
    allFazes.add(f555trgxml);
    allFazes.add(f600tidy);
    allFazes.add(f700finish);
  }
   */

  private FFaze(final String aValue/*, final EFileCategory category*/) {
    super(aValue);
    //    iCategory = category;
  }
  /*
  private static AFaze fromx(final String aValue, final EFileCategory category) {
    return aValue == null ? null : new AFaze(aValue, category);
  }
   */
  public static FazeAnalyzeResult analyzeEntryName(final String aEntryName) {
    final FazeAnalyzeResult result = new FazeAnalyzeResult();
    result.setAnalyzedEntryName(aEntryName);
    if (aEntryName.endsWith(".auttrigs.xml")) {
      result.setFazes(new EFazeZavedeni[]{EFazeZavedeni.f255trgxml, EFazeZavedeni.f555trgxml});
      result.setFileCategory(EFileCategory.XMLTRIGER);
    } else {
      final EFazeZavedeni faze = urciFaziDbObj(aEntryName);
      if (faze != null) {
        result.setFazes(new EFazeZavedeni[]{faze});
        result.setFileCategory(faze.getFileCategory());
      }
    }
    return result;
  }

  /**
   * @param aEntryName
   * @return
   */
  private static EFazeZavedeni urciFaziDbObj(final String aEntryName) {
    EFazeZavedeni faze;
    faze = urciFaziDbObjDleSlozky(aEntryName);
    if (faze != null) {
      return faze;
    }
    faze = urciFaziOnceDleSlozky(aEntryName);
    if (faze != null) {
      return faze;
    }
    faze = urciFaziDlePrimarnihoJmenaSkozky(aEntryName);
    if (faze != null) {
      return faze;
    }

    return null;
  }

  private static EFazeZavedeni urciFaziDbObjDleSlozky(final String aEntryName) {
    final List<String> xx = rozdelNaSlozkyBezPosledniho(aEntryName);
    for (final String slozka : xx) {
      if (slozka.equals("seq")) {
        return EFazeZavedeni.f205seq;
      }
      if (slozka.equals("tmp")) {
        return EFazeZavedeni.f210tmp;
      }
      if (slozka.equals("pkg")) {
        return EFazeZavedeni.f220pkg;
      }
      if (slozka.equals("prc")) {
        return EFazeZavedeni.f230prc;
      }
      if (slozka.equals("vue")) {
        return EFazeZavedeni.f240vue;
      }
      if (slozka.equals("trg")) {
        return EFazeZavedeni.f250trg;
      }
      if (slozka.equals("mview")) {
        return EFazeZavedeni.f265materview;
      }
      if (slozka.equals("reg")) {
        return EFazeZavedeni.f270reg;
      }
    }
    return null;
  }

  private static EFazeZavedeni urciFaziOnceDleSlozky(final String aEntryName) {
    final List<String> xx = rozdelNaSlozkyBezPosledniho(aEntryName);
    for (final String slozka : xx) {
      if (slozka.equals("0before")) {
        return EFazeZavedeni.f050before;
      }
      if (slozka.equals("1alter")) {
        return EFazeZavedeni.f100alter;
      }
      if (slozka.equals("3settings")) {
        return EFazeZavedeni.f300settings;
      }
      if (slozka.equals("4migration")) {
        return EFazeZavedeni.f400migration;
      }
      if (slozka.equals("5alter")) {
        return EFazeZavedeni.f500alter;
      }
      if (slozka.equals("6tidy")) {
        return EFazeZavedeni.f600tidy;
      }
    }
    return null;
  }

  private static EFazeZavedeni urciFaziDlePrimarnihoJmenaSkozky(final String aEntryName) {
    final List<String> xx = rozdelNaSlozkyBezPosledniho(aEntryName);
    for (final String slozka : xx) {
      for (final EFazeZavedeni faze : EFazeZavedeni.values()) {
        if (faze.name().equalsIgnoreCase(slozka)) {
          return faze;
        }
      }
    }
    return null;
  }

  private static List<String> rozdelNaSlozkyBezPosledniho(final String aEntryName) {
    final List<String> list = new ArrayList<String>(Arrays.asList(aEntryName.split("/")));
    if (list.size() == 0) {
      return list;
    }
    list.remove(list.size() - 1);
    return list;
  }

  /**
   * @return the allfazes
   */
  public static List<EFazeZavedeni> getAllfazes() {
    return Arrays.asList(EFazeZavedeni.values());
  }
}
