package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import cz.tconsult.lib.ifxdbload.core.tw.FSplitter;
import cz.tconsult.lib.spllexer.SplDirective;

/**
 * Jednorázový parser direktiv.
 * Instanci lze použít jen jednou pro zparsrování direktiv.
 *
 *   1. Vytvořit instanci a dát mu direktivy.
 *   2. Provést parsování lokálních, nebo lokálních a globálních.
 *   3. Získat chyby, mezi nimiž budou i neklatné direktivy.
 * @author veverka
 *
 */
public class OnceDirectivesParser {

  public OnceDirectivesParser(final Set<SplDirective> directives) {
    // kopie musí být vyrobena, protože během parsrování se z množiny
    // ubírá, aby se zjistilo, které direktivy jsou nepaltné

    this.directives = new HashSet<>(directives);
  }

  private final Set<SplDirective> directives;

  private final List<String> errors = new ArrayList<>();

  // zpracované direktivy

  //  private static final Pattern idPattern = Pattern.compile("--\\s*V1\\s*,\\s*ID:[\\s]*([\\w|\\d|\\p{Punct}]+)",Pattern.CASE_INSENSITIVE);
  //  private static final Pattern noTransPatternV1 = Pattern.compile("(?i)^[\\s]*--[\\s]*notran[s]{0,1}[\\s]*$",Pattern.CASE_INSENSITIVE);
  //  private static final Pattern noTransPatternV2 = Pattern.compile("--\\s*@TC:ONCE:\\s*NO_TRANSACTION_CONTROL\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern idPattern2 = Pattern.compile("--\\s*@TC:ONCE:\\s*ID\\s*=\\s*V2\\s*,\\s*(.*)",Pattern.CASE_INSENSITIVE); // pole dvojprvkové V2, cokoli
  //  private static final Pattern descriptionPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DESCRIPTION\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota
  //  private static final Pattern reloadAlwaysPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*RELOAD_ALWAYS\\s*",Pattern.CASE_INSENSITIVE); // příznak
  //  private static final Pattern loadUnlessLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_UNLESS_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota
  //  private static final Pattern loadIfLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_IF_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE); // hodnota
  //  private static final Pattern ignoreChecksumPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*IGNORE_CHECKSUM\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern dbKindPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBKIND\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE);  //pole idů

  // direktivy ke zpracování, které se nikde nevyskytly
  //  private static final Pattern loadOncePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_ONCE\\s*",Pattern.CASE_INSENSITIVE); //příznak
  //  private static final Pattern dbmsPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBMS\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern variantPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*VARIANT\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern otherDirectivePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*(\\w+\\s*=\\s*[\\w,\\s]*)",Pattern.CASE_INSENSITIVE); //pole idů
  //  private static final Pattern invalidDirective = Pattern.compile("--\\s*@TC:(?!ONCE:)\\s*", Pattern.CASE_INSENSITIVE);


  private boolean parseFlag(final String directiveName) {
    final List<String> values = findAndRemoveValues(directiveName);
    checkDuplicity(directiveName, values);
    return ! values.isEmpty(); // neprázná množina značí výskyt direktivy
  }


  private Optional<String> parseValue(final String directiveName) {
    final List<String> values = findAndRemoveValues(directiveName);
    checkDuplicity(directiveName, values);
    return values.stream().findFirst();
  }

  private String parseValue(final String directiveName, final String defaltValue) {
    final List<String> values = findAndRemoveValues(directiveName);
    checkDuplicity(directiveName, values);
    return values.stream().findFirst().orElse(defaltValue);
  }

  private List<String> parseValues(final String directiveName) {
    final List<String> values = findAndRemoveValues(directiveName);
    checkDuplicity(directiveName, values);

    return values.stream()
        .findFirst()
        .map(FSplitter::splitByComma)
        .orElse(Collections.emptyList());
  }

  private void checkDuplicity(final String directiveName, final List<String> values) {
    if (values.size() > 1) {
      errors.add(String.format("Duplicit directive \"%s\" with values %s.", directiveName, values));
    }
  }

  private List<String> findAndRemoveValues(final String directiveName) {
    final Set<SplDirective> dires = directives.stream()
        .filter(d -> "ONCE".equals(d.getScope()))  // jen našeho rozsahu
        .filter(d -> directiveName.equals(d.getKey()))
        .collect(Collectors.toSet());
    // to, co jsme našli odstranit, aby zůstaly jen direktivy, které jsoui neplatné
    directives.removeAll(dires);

    return dires.stream()
        .map(SplDirective::getValue)
        .map(x -> x == null ? "" : x)
        .map(String::trim)
        .collect(Collectors.toList());
  }

  public OnceDirectivesGlobal parseGlobal() {
    return new OnceDirectivesGlobal(
        parseValue("ID", "ss"),
        parseValue("DESCRIPTION", ""),
        parseFlag("NO_TRANSACTION_CONTROL"),
        parseFlag("IGNORE_CHECKSUM"),
        parseFlag("RELOAD_ALWAYS"),
        parseValue("LOAD_UNLESS_LOADED"),
        parseValue("LOAD_IF_LOADED"),
        parseValues("DBKIND")
        );
  }

  public OnceDirectivesLocal parseLocal() {
    final OnceDirectivesLocal dires = new OnceDirectivesLocal(
        parseValue("MIGRATION_TABLE"),
        parseFlag("MIGRATION_FAIL_ON_ERROR"),
        parseValue("MIGRATION_INTERVAL", "PT1M"),
        parseValue("MIGRATION_COLUMN"),
        parseValue("MIGRATION_STATUS_PREPARED"),
        parseValue("MIGRATION_STATUS_OK"),
        parseValue("MIGRATION_STATUS_ERROR"),
        parseValue("MIGRATION_STATUS_SKIPPED")

        //@TC:ONCE: MIGRATION_TABLE = jméno tabulky
        //@TC:ONCE: MIGRATION_FAIL_ON_ERROR
        //@TC:ONCE: MIGRATION_INTERVAL = počet vteřin pro výpis
        //@TC:ONCE: MIGRATION_COLUMN = jméno sloupce se stavem migrovaných záznamů
        //@TC:ONCE: MIGRATION_STATUS_PREPARED = název hodnoty pro doposud nezmigrované záznamy
        //@TC:ONCE: MIGRATION_STATUS_OK = název hodnoty pro úspěšně zmigrované záznamy
        //@TC:ONCE: MIGRATION_STATUS_ERROR = název hodnoty pro neúspěšně zmigrované záznamy
        //@TC:ONCE: MIGRATION_STATUS_SKIPPED = název hodnoty pro přeskočení nezmigrovatelných záznamů
        );
    try {
      dires.getMigrationInterval();
    } catch (final DateTimeParseException e) {
      errors.add(String.format("Wrong value \"%s\" of directive MIGRATION_INTERVAL, cannot convert to Duration",  dires.getMigrationIntervalIsoStr()));
    }
    return dires;

    // DateTimeParseException
  }

  public List<String> errors() {
    return ListUtils.union(errors,
        directives.stream()
        .map(dire -> "Unknown directive " + dire)
        .collect(Collectors.toList())
        );
  }

  public static void main(final String[] args) {

    System.out.println(Duration.parse("XPT20S"));
  }

}
