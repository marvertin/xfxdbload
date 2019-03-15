package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

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
    // raději unmodifieble, v dřívejší verzi byla tendence direktivy odstraňovat
    this.directives = Collections.unmodifiableSet(directives);
  }

  private final Set<SplDirective> directives;
  private final Set<SplDirective> usedDirectives = new HashSet<>();

  private final Set<String> errors = new TreeSet<>();

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
    final Optional<String> valline = findOneValline(directiveName);
    if (valline.isPresent() && ! StringUtils.isEmpty(valline.get())) {
      errors.add(String.format("Directive \"%s\" is flag, but has a value \"%s\"", directiveName, valline.get()));
    }
    return valline.isPresent(); // neprázná množina značí výskyt direktivy
  }


  private Optional<String> parseValue(final String directiveName) {
    final Optional<String> valline = findOneValline(directiveName);
    if (valline.isPresent() && StringUtils.isBlank(valline.get())) { // jen pokud existuje, musí mít hodnotu, může ale neexistovat
      errors.add(String.format("Directive \"%s\" must have a value", directiveName));

    }
    return valline;
  }

  private String parseValue(final String directiveName, final String defaltValue) {
    return findOneValline(directiveName).orElse(defaltValue);
  }



  private String parseId(final String directiveName) {
    final List<String> values = parseValues(directiveName);
    if (values.isEmpty()) {
      errors.add(String.format("Missing ID directive or has no value."));
      return "<MISSING-ID>";
    }
    if (values.size() != 2) {
      errors.add(String.format("Not exactly 2 values in ID directive\"%s\"", values));
    }
    if (! values.get(0).equals("V2")) {
      errors.add(String.format("Unsupoorted version \"%s\", only V2 is supported", values.get(0)));
    }
    return values.get(values.size()-1);
  }


  private List<String> parseValues(final String directiveName) {
    return findOneValline(directiveName)
        .map(FSplitter::splitByComma)
        .orElse(Collections.emptyList());
  }

  private void checkDuplicityx(final String directiveName, final List<String> values) {
    if (values.size() > 1) {
      errors.add(String.format("Duplicit directive \"%s\" with values %s.", directiveName, values));
    }
  }

  /**
   * Pro danou direktivu najde seznam jejich použití a vrátí všechny řetězce hodnot direktuiv.
   * V kažzdé položce je celý řetězec za rovnítkem, není parsrováno čárkama.
   * @param directiveName
   * @return
   */
  private List<String> findVallines(final String directiveName) {
    final Set<SplDirective> dires = findDirectives(directiveName);

    return dires.stream()
        .map(SplDirective::getValue)
        .map(x -> x == null ? "" : x)
        .map(String::trim)
        .collect(Collectors.toList());
  }

  /**
   * Vrátí linu z porvní direktivy, pokud je mnoho direktiv, tak zapíše chybu
   * @param directiveName
   * @return
   */
  private Optional<String> findOneValline(final String directiveName) {
    final List<String> vallines = findVallines(directiveName);
    checkDuplicityx(directiveName, vallines);
    return vallines.stream().findFirst();
  }

  /**
   * Najde všechny direktivy daného jména a odstraní je, takže volat jen jednou.
   * @param directiveName
   * @return
   */
  private Set<SplDirective> findDirectives(final String directiveName) {
    final Set<SplDirective> dires = directives.stream()
        .filter(d -> "ONCE".equals(d.getScope()))  // jen našeho rozsahu
        .filter(d -> directiveName.equals(d.getKey()))
        .collect(Collectors.toSet());
    // to, co jsme našli odstranit, aby zůstaly jen direktivy, které jsoui neplatné
    usedDirectives.addAll(dires);
    return dires;
  }

  public OnceDirectivesGlobal parseGlobal() {
    return new OnceDirectivesGlobal(
        parseId("ID"),
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
        parseValue("MIGRATION_TABLE"),            //@TC:ONCE: MIGRATION_TABLE = jméno tabulky
        parseFlag("MIGRATION_FAIL_ON_ERROR"),     //@TC:ONCE: MIGRATION_FAIL_ON_ERROR
        parseValue("MIGRATION_INTERVAL", "PT1M"), //@TC:ONCE: MIGRATION_INTERVAL = počet vteřin pro výpis
        parseValue("MIGRATION_COLUMN"),           //@TC:ONCE: MIGRATION_COLUMN = jméno sloupce se stavem migrovaných záznamů
        parseValue("MIGRATION_STATUS_PREPARED"),  //@TC:ONCE: MIGRATION_STATUS_PREPARED = název hodnoty pro doposud nezmigrované záznamy
        parseValue("MIGRATION_STATUS_OK"),        //@TC:ONCE: MIGRATION_STATUS_OK = název hodnoty pro úspěšně zmigrované záznamy
        parseValue("MIGRATION_STATUS_ERROR"),     //@TC:ONCE: MIGRATION_STATUS_ERROR = název hodnoty pro neúspěšně zmigrované záznamy
        parseValue("MIGRATION_STATUS_SKIPPED")    //@TC:ONCE: MIGRATION_STATUS_SKIPPED = název hodnoty pro přeskočení nezmigrovatelných záznamů
        );
    try {
      dires.getMigrationInterval();
    } catch (final DateTimeParseException e) {
      errors.add(String.format("Wrong value \"%s\" of directive MIGRATION_INTERVAL, cannot convert to Duration",  dires.getMigrationIntervalIsoStr()));
    }
    return dires;
  }

  /**
   *  Vrátí všechny posbírané chyby.
   *  Také chyby neexistující direktivy, to znamená direktiv, která neprošla přes parsrování.
   */
  public Set<String> errors() {
    return Sets.union(errors,
        Sets.difference(directives, usedDirectives).stream()
        .map(dire -> "Unknown directive " + dire)
        .collect(Collectors.toSet())
        );
  }


}
