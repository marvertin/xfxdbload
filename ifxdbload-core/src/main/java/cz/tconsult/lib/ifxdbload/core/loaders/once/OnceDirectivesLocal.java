package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.time.Duration;
import java.util.Optional;

import lombok.Data;

@Data
public class OnceDirectivesLocal {

  private final Optional<String> migrationTable;
  private final boolean migrationFailOnError;
  private final String migrationIntervalIsoStr;
  private final Optional<String> migrationColumn;
  private final Optional<String> migrationStatusPrepared;
  private final Optional<String> migrationStatusOk;
  private final Optional<String> migrationStatusError;
  private final Optional<String> migrationStatusSkipped;

  /**
   * Migrační interval jako doba trvání.
   * @return
   */
  public Duration getMigrationInterval() {
    return Duration.parse(migrationIntervalIsoStr);
  }
}

//@TC:ONCE: MIGRATION_TABLE = jméno tabulky
//@TC:ONCE: MIGRATION_FAIL_ON_ERROR
//@TC:ONCE: MIGRATION_INTERVAL = počet vteřin pro výpis
//@TC:ONCE: MIGRATION_COLUMN = jméno sloupce se stavem migrovaných záznamů
//@TC:ONCE: MIGRATION_STATUS_PREPARED = název hodnoty pro doposud nezmigrované záznamy
//@TC:ONCE: MIGRATION_STATUS_OK = název hodnoty pro úspěšně zmigrované záznamy
//@TC:ONCE: MIGRATION_STATUS_ERROR = název hodnoty pro neúspěšně zmigrované záznamy
//@TC:ONCE: MIGRATION_STATUS_SKIPPED = název hodnoty pro přeskočení nezmigrovatelných záznamů
