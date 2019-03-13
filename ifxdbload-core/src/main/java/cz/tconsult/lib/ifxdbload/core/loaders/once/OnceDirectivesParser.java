package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import cz.tconsult.lib.spllexer.SplDirective;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OnceDirectivesParser {

  private final Set<SplDirective> dires;

  // zpracované direktivy

  //  private static final Pattern idPattern = Pattern.compile("--\\s*V1\\s*,\\s*ID:[\\s]*([\\w|\\d|\\p{Punct}]+)",Pattern.CASE_INSENSITIVE);
  //  private static final Pattern noTransPatternV1 = Pattern.compile("(?i)^[\\s]*--[\\s]*notran[s]{0,1}[\\s]*$",Pattern.CASE_INSENSITIVE);
  //  private static final Pattern noTransPatternV2 = Pattern.compile("--\\s*@TC:ONCE:\\s*NO_TRANSACTION_CONTROL\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern idPattern2 = Pattern.compile("--\\s*@TC:ONCE:\\s*ID\\s*=\\s*V2\\s*,\\s*(.*)",Pattern.CASE_INSENSITIVE); // pole dvojprvkové V2, cokoli
  //  private static final Pattern descriptionPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DESCRIPTION\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota

  // direktivy ke zpracování
  //  private static final Pattern dbKindPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBKIND\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE);  //pole idů
  //  private static final Pattern loadUnlessLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_UNLESS_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota
  //  private static final Pattern loadIfLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_IF_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota
  //  private static final Pattern ignoreChecksumPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*IGNORE_CHECKSUM\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern loadOncePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_ONCE\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern reloadAlwaysPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*RELOAD_ALWAYS\\s*",Pattern.CASE_INSENSITIVE); // příznak
  //  private static final Pattern dbmsPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBMS\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern variantPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*VARIANT\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern otherDirectivePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*(\\w+\\s*=\\s*[\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern invalidDirective = Pattern.compile("--\\s*@TC:(?!ONCE:)\\s*", Pattern.CASE_INSENSITIVE);

  private Map<String, String> dirValues;

  private boolean parseFlag(final String directiveName) {
    return findValue(directiveName)
        .map(__ -> true)
        .orElse(false);
  }

  private Optional<String> parseValue(final String directiveName) {
    return findValue(directiveName);
  }

  private String parseValue(final String directiveName, final String defaltValue) {
    return findValue(directiveName)
        .orElse(defaltValue);
  }

  private Optional<String> findValue(final String directiveName) {
    return dires.stream()
        .filter(d -> d.getKey().equals(directiveName))
        .map(SplDirective::getValue)
        .map(x -> x == null ? "" : x)
        .map(String::trim)
        .findFirst();
  }

  public OnceDirectivesGlobal parse() {
    return new OnceDirectivesGlobal(
        parseValue("ID", "ss"),
        parseValue("DESCRIPTION", ""),
        parseFlag("NO_TRANSACTION_CONTROL"),
        parseFlag("IGNORE_CHECKSUM")

        );
  }
}
