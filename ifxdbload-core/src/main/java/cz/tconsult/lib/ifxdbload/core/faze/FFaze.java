package cz.tconsult.lib.ifxdbload.core.faze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import cz.tconsult.dbloader.itf.EFileCategory;

public final class FFaze {

  public static FazeAnalyzeResult analyzeEntryName(final AEntryName aEntryName) {
    if (aEntryName.toString().endsWith(".auttrigs.xml")) {
      return new FazeAnalyzeResult(aEntryName, EFileCategory.XMLTRIGER, EnumSet.of(EFazeZavedeni.f255trgxml, EFazeZavedeni.f555trgxml));
    } else {
      final EFazeZavedeni faze = urciFaziDbObj(aEntryName);
      return new FazeAnalyzeResult(aEntryName, faze.getFileCategory(), EnumSet.of(faze));
    }
  }

  /**
   * @param aEntryName
   * @return
   */
  private static EFazeZavedeni urciFaziDbObj(final AEntryName aEntryName) {
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

  private static EFazeZavedeni urciFaziDbObjDleSlozky(final AEntryName aEntryName) {
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

  private static EFazeZavedeni urciFaziOnceDleSlozky(final AEntryName aEntryName) {
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

  private static EFazeZavedeni urciFaziDlePrimarnihoJmenaSkozky(final AEntryName aEntryName) {
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

  private static List<String> rozdelNaSlozkyBezPosledniho(final AEntryName aEntryName) {
    final List<String> list = new ArrayList<String>(Arrays.asList(aEntryName.toString().split("/")));
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
