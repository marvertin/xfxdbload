package cz.tconsult.lib.ifxdbload.core.tw;

import lombok.Data;

/**
 * Pojmenovaný bytestream. Vhodné například pro uchování dat přečtených z nějakého soboru či jiného pojmenovaného zdroje.
 * @author veverka
 *
 */
@Data
public class NamedString {

  private final ASourceName name;
  private final String data;

}
