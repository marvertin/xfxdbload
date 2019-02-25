/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import cz.tconsult.dbloader.itf.EFileCategory;
import cz.tconsult.tw.util.FileManager;
import cz.tconsult.tw.util.FileManager.EUtfBom;

/**
 * @author veverka
 *
 */
public class Zavadenec {

  /** Načtená data ze souboru v podobě bytů */
  private final byte[] dataBytes;

  /** Načtená data ze souboru v podobě stringu */
  private final String dataStr;

  /** Kořen, ze kterého se začalo číst, tedy jméno dbpacku nebo složky */
  private final File iRoot;

  /** Entry name, je to důležité protože se z toho mnoho může poznávat */
  private final String iEntryName;

  /** Schéma do kterého zavádět, momentálně se nepoužívá */
  private String schema;

  /** Kategorie souboru, ve svém důsledku se zní odvodí zavaděč */
  private final EFileCategory iFileCategory;

  /** Kódování, ve tkerém jsou předaná data, je to důležité,
   * položka se nepoužije pro data, ze kterých je kódování přímo patrno,
   * to jsou XML data. Pokud je předáno null, bere se default kódování.
   */
  private final Charset iCharset;

  public Zavadenec(final File root, final String entryName, final byte[] data, final Charset aCharset) {
    iRoot = root;
    iEntryName = entryName;
    iFileCategory = analyzeFileCategory(entryName);
    iCharset = aCharset != null ? aCharset : Charset.defaultCharset();
    dataBytes = data;
    try {
      final EUtfBom utfBom = FileManager.getUtfBomInfo(this.dataBytes);
      final int bomSize = utfBom.getSize();
      dataStr = new String(dataBytes, bomSize, dataBytes.length - bomSize, iCharset.name());
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Predpokladat, ze kodovani je: " + iCharset, e);
    }

  }

  /**
   * Vhodné pro explicitní urční fáze
   */
  public Zavadenec(final File root, final String entryName, final String data, final Charset aCharset) {
    iRoot = root;
    iEntryName = entryName;
    iFileCategory = analyzeFileCategory(entryName);
    iCharset = aCharset != null ? aCharset : Charset.defaultCharset();
    this.dataStr = data;
    try {
      dataBytes = dataStr.getBytes(iCharset.name());
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Predpokladat, ze kodovani je: " + iCharset, e);
    }

  }



  private static EFileCategory analyzeFileCategory(final String entryName) {
    final FazeAnalyzeResult analyzeResult = FFaze.analyzeEntryName(entryName);
    if (analyzeResult == null) {
      return null;
    }
    return analyzeResult.getFileCategory();

  }


  public String getLocator() {
    return iRoot + "!" + iEntryName;
  }


  /**
   * @return the data
   */
  public synchronized byte[] getDataAsBytes() {
    return dataBytes;
  }

  /**
   * @return
   */
  public synchronized String getDataAsString() {
    return dataStr;
  }


  /**
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }


  /**
   * @param aSchema the schema to set
   */
  public void setSchema(final String aSchema) {
    schema = aSchema;
  }


  /**
   * @return the fileCategory
   */
  public EFileCategory getFileCategory() {
    return iFileCategory;
  }


  /**
   * @return the charset
   */
  public Charset getCharset() {
    return iCharset;
  }



}
