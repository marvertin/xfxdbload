package cz.tconsult.lib.ifxdbload.workflow.process;

import cz.tconsult.lib.ifxdbload.core.db.DbContext;
import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;

/**
 * Továrna na template. Implementace musí poskytnout.
 * @author veverka
 *
 */
public interface DbContextFactory {

  /**
   * Pokud je pro daný druh a schema definováno připjení k databáz, vrátí template,
   * který dokáže vykonávat příkazy.
   * @param dbkind
   * @param schema
   * @return
   */
  public DbContext dc(ADbkind dbkind);

  /**
   * Zjistí, zda je možné se připojit k databázi tohoto druhu, tedy že
   * má nástroj databázi nakonfigurovanou a to nakonfigurovanou správně.
   * @param dbkind
   * @return
   */
  public boolean canCreate(ADbkind dbkind);
}
