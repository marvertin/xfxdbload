package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.tconsult.lib.ifxdbload.core.loaders.once.legacy.FLegacyChecksumVerifier;
import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.ParseredSource;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.spllexer.SplDirective;
import lombok.Data;

/**
 * Reprezentuje jeden onceskript.
 * @author veverka
 *
 */
@Data
public class OnceScript implements Comparable<OnceScript> {

  private final ParseredSource ps;
  private final OnceDirectivesGlobal globalDirectives;

  public boolean verify(final long checksum) {

    if (checksum == computeChecksum()) {
      return true; // to je ponovu flexibilně vypočtené checksum
    }

    return FLegacyChecksumVerifier.verify(this, checksum);
  }

  public OnceScript(final ParseredSource ps) {
    this.ps = ps;
    this.globalDirectives = globalDirectives();
  }


  private OnceDirectivesLocal localDirectives(final SplStatement stm) {
    final OnceDirectivesParser odp = new OnceDirectivesParser(stm.getDirectives());
    return odp.parseLocal();
  }

  private OnceDirectivesGlobal globalDirectives() {
    final OnceDirectivesParser odp = new OnceDirectivesParser(firstCommandDirectives());
    odp.parseLocal(); // využíváme jen vedleší efekt, který vyžere lokální direktivy, jenž legitimně moho být u prvního příkazu, ale nás teď nezajímají
    return odp.parseGlobal();
  }

  private Set<SplDirective> firstCommandDirectives() {
    final Set<SplDirective> globalDirectives =
        ps.getStatements().stream()
        .map(x -> x.getDirectives())  // jen direktivy
        .findFirst()                  // z prvního příkazu
        .orElse(Collections.emptySet()); // a když není tak prázdné
    return globalDirectives;
  }


  /**
   * spočte checksum onceskriptu
   * @return
   */
  public long computeChecksum() {
    long suma = 1;
    final List<Long> sumy = ps.getStatements().stream()
        .map(SplStatement::getLongHash)
        .collect(Collectors.toList());
    // nelze použít reduce, protože reduce vyžaduje monoid a já neznám žádný nekomutativní monoid nad čísly (jednoduše realizovatelný). [veverka;2019-03-13 15:17:24]
    for (final long n : sumy) {
      suma = suma * 13 + n;
    }
    return suma;
  }

  public String getScriptIdFromFileName() {
    return extractujSkriptIdZeJmena(ps.getFileNameOnly());
  }

  public String getScriptIdFromDirective() {
    return globalDirectives.getScriptid();
  }

  private static String extractujSkriptIdZeJmena(final String path) {
    //odstranim z nazvu souboru priponu a a usporadavac (A12312-......)
    final String pureName = new File(path).getName();
    final String nameWithoutExt=pureName.substring(0,pureName.lastIndexOf("."));
    final int indexOfPom=nameWithoutExt.indexOf("-");
    final int indexOfRn=nameWithoutExt.toLowerCase().indexOf("rn");
    String nameWithoutUsporadavac=nameWithoutExt;
    if(indexOfPom<indexOfRn){
      nameWithoutUsporadavac=indexOfPom > -1?nameWithoutExt.substring(indexOfPom+1,nameWithoutExt.length()):nameWithoutExt;
    }
    return nameWithoutUsporadavac;
  }

  public boolean isNoTransactionControl() {
    return getGlobalDirectives().isNoTransactionControl();
  }


  /**
   * Vrátí příkazy, které mají být vykonat a to podle transakčního atributu.
   * Pokud se mají řídit transakce, tak všechna transakční řízení ze skriptu jsou vyhozeny.
   * @return
   */
  public List<SplStatement> getStatementsToRun() {
    final List<SplStatement> stms = getPs().getStatements();
    if (! isNoTransactionControl()) {
      return stms.stream()
          .filter(stm -> ! stm.getStmType().isTransactionControl())
          .collect(Collectors.toList());
    } else {
      return stms;
    }
  }

  @Override
  public int compareTo(final OnceScript o) {
    return ps.getFileNameOnly().compareTo(o.getPs().getFileNameOnly());
  }


  ////////////////////////// Blok kontroly syntaxe Once skriptu bez vnitřku příkazů //////////////////////////////

  /**
   * Vrací syntaktické chyby. Pouze chyby detekovatelné na once skriptu jako takovém. Tedy:
   *  - chyby direktiv
   *  - nesouhlas script id
   *  -špatné řízení transakcí
   * @return
   */
  public Set<String> checkSyntaxErrors() {
    final List<SplStatement> stms = getPs().getStatements();
    final Set<String> err = new TreeSet<>();
    if (! getScriptIdFromDirective().equalsIgnoreCase(getScriptIdFromFileName())) {
      err.add(String.format("Id '%s' from file name is not same as id from directive '%s'", getScriptIdFromFileName(), getScriptIdFromDirective()));
    }
    if (! isNoTransactionControl()) {
      if (stms.size() < 2) {
        err.add(String.format("Once hase only %d statements than it cannot starts with BEGIN WORK and cannot ends with ROLLBACK.", stms.size()));
      } else {
        final EStmType firstStmType = stms.get(0).getStmType();
        final EStmType lastStmType = stms.get(stms.size()-1).getStmType();
        if (firstStmType != EStmType.BEGINWORK) {
          err.add("Once does not starts witch BEGIN WORK");
        }
        if (lastStmType != EStmType.ROLLBACK) {
          err.add("Once does not ends witch ROLLBACK WORK");
        }
        err.addAll(
            stms.subList(1, stms.size()-2).stream()
            .filter(stm -> stm.getStmType().isTransactionControl())
            .map(stm -> "Once contains transaction control command inside it on line: " + (stm.getFirstTokenLocator().getBegLineNumber() + 1))
            .collect(Collectors.toList()));
      }
    }

    // kontrola globálních direktiv
    err.addAll(globalDirectivesError());

    // kontrola lokálních direktiv
    err.addAll(
        stms.stream()
        .skip(1) // první příkaz mžůže mít i globální direktivy a je zkontolován
        .flatMap(stm -> localDirectivesError(stm)
            .stream()
            .map(x -> x + " for command on line " + (stm.getFirstTokenLocator().getBegLineNumber() + 1) ))
        .collect(Collectors.toSet()));

    // kontrola chybějících migračních direktiv
    err.addAll(stms.stream()
        .filter(stm -> localDirectives(stm).getMigrationTable().isPresent())
        .flatMap(stm -> mssingMigrationDirectiveNames(stm).stream()
            .map(x -> "Missing directives " + x + " for command on line " + stm.getFirstTokenLocator().getBegLineNumber()))
        .collect(Collectors.toSet())
        );

    return err;
  }



  private Set<String> localDirectivesError(final SplStatement stm) {
    final OnceDirectivesParser odp = new OnceDirectivesParser(stm.getDirectives());
    odp.parseLocal();
    return odp.errors();
  }

  private Set<String> globalDirectivesError() {
    final OnceDirectivesParser odp = new OnceDirectivesParser(firstCommandDirectives());
    odp.parseGlobal();
    odp.parseLocal(); // využíváme jen vedleší efekt, který vyžere lokální direktivy, jenž legitimně moho být u prvního příkazu, ale nás teď nezajímají
    return odp.errors();
  }

  /**
   * Vrátí chybějící direktivy, které hjsou povinné, když už se má migrovat
   * @param stm
   * @return
   */
  private Set<String> mssingMigrationDirectiveNames(final SplStatement stm) {
    final OnceDirectivesLocal dires = localDirectives(stm);
    return Stream.of(

        dires.getMigrationTable().map(x->"").orElse("MIGRATION_TABLE"),   // pokud tam hodnota je, bude nahrazena "", pokdu tam není, půjde tam jméno hodnoty
        dires.getMigrationColumn().map(x->"").orElse("MIGRATION_COLUMN"),   // pokud tam hodnota je, bude nahrazena "", pokdu tam není, půjde tam jméno hodnoty
        dires.getMigrationStatusPrepared().map(x->"").orElse("MIGRATION_STATUS_PREPARED"),
        dires.getMigrationStatusOk().map(x->"").orElse("MIGRATION_STATUS_OK"),
        dires.getMigrationStatusError().map(x->"").orElse("MIGRATION_STATUS_ERROR ")
        )
        .filter(x -> ! x.isEmpty()) // prázdné hodnoty vyhodit
        .collect(Collectors.toSet()); // a zůstanou jen jména chybějících direktiv
  }



}
