package cz.tconsult.lib.ifxdbload.workflow.scan;

import java.util.function.Supplier;

import cz.tconsult.lib.ifxdbload.core.faze.AEntryName;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;

/**
 * Rozhraní reaceiveru obsahu souboru. Receiver je zavolán pro každý naskenovaný soubor.
 * Implementace rozhodne, zda vůbec číst tento soubor.
 * Stream implementace neotevřela, tak ho nezavírá, smí ho ale vyčíst.
 * @author veverka
 *
 */
public interface FileContentReceiver {

  /**
   *
   * @param dbprops Property dbpacku, odkud se čte.
   * @param contentSupplier Dodavatel contentu, teprve jeho zavoláním jsou data orpavdu načtena.
   * @param aEntryName Jmé no entry relativně k dbpacku, oddělovače jsou lomítka. Zařazuje se tím do fází.
   */
  void add(final DbpackProperties dbprops, final Supplier<byte[]> contentSupplier, final AEntryName aEntryName);

}
