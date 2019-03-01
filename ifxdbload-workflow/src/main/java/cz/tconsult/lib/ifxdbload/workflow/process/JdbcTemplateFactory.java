package cz.tconsult.lib.ifxdbload.workflow.process;

import org.springframework.jdbc.core.JdbcTemplate;

import cz.tconsult.lib.ifxdbload.workflow.data.ADbkind;
import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;

/**
 * Továrna na template. Implementace musí poskytnout.
 * @author veverka
 *
 */
public interface JdbcTemplateFactory {

  /**
   * Pokud je pro daný druh a schema definováno připjení k databáz, vrátí template,
   * který dokáže vykonávat příkazy.
   * @param dbkind
   * @param schema
   * @return
   */
  public JdbcTemplate jt(ADbkind dbkind, ASchema schema);

  /**
   * Zjistí, zda je možné se připojit k databázi tohoto druhu, tedy že
   * má nástroj databázi nakonfigurovanou a to nakonfigurovanou správně.
   * @param dbkind
   * @return
   */
  public boolean canCreate(ADbkind dbkind);
}
