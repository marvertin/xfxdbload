package cz.tconsult.lib.ifxdbload.core.once.datatypes;

import java.util.List;

import cz.tconsult.lib.ifxdbload.core.once.OnceScriptData;
import cz.tconsult.lib.ifxdbload.core.once.enums.EOncePhase;
import cz.tconsult.lib.ifxdbload.core.once.enums.EVariant;

public class OnceScriptInfo  {

  private final String scriptId;
  private final int scriptVersion;
  private final EOncePhase oncePhase;
  private final OnceScriptData sourceName;
  private final int scriptIdBeginRow;
  private final int scriptIdBeginCol;
  private final int scriptIdBeginPosition;
  private final boolean noTransFind;
  private final String[] dbKinds;
  private final List<String> loadUnlessLoaded;
  private final String loadIfLoaded;
  private final boolean ignoreChecksum;
  private final boolean reloadAlways;
  private final boolean loadOnce;
  private final String description;
  private final String[] dbms;
  private final String[] variant;
  private final String otherDirective;
  private final EVariant variantByDirectory;

  /**
   * @param aScriptId
   * @param aPhase
   * @param aSourceName
   * @param aScriptIdRow
   * @param aScriptIdCol
   * @param aScriptIdPosition
   * @param aNoTransFind
   */
  private OnceScriptInfo(final String aScriptId, final int aVersion, final EOncePhase aPhase
      , final OnceScriptData aSourceName, final int aScriptIdRow, final int aScriptIdCol, final int aScriptIdPosition
      , final boolean aNoTransFind, final String[] aDbKinds, final List<String> aLoadUnlessLoaded
      , final String aLoadIfLoaded, final boolean aIgnoreChecksum
      , final boolean aLoadOnce, final boolean aReloadAlways
      , final String aDescription, final String[] aDbms, final String[] aVariant
      , final String aOtherDirective, final EVariant aVariantByDirectory) {

    scriptId = aScriptId;
    scriptVersion = aVersion;
    oncePhase = aPhase;
    sourceName = aSourceName;
    scriptIdBeginRow = aScriptIdRow;
    scriptIdBeginCol = aScriptIdCol;
    scriptIdBeginPosition = aScriptIdPosition;
    noTransFind = aNoTransFind;
    dbKinds = aDbKinds;
    loadUnlessLoaded = aLoadUnlessLoaded;
    ignoreChecksum = aIgnoreChecksum;
    loadIfLoaded = aLoadIfLoaded;
    loadOnce = aLoadOnce;
    reloadAlways = aReloadAlways;
    description = aDescription;
    dbms = aDbms;
    variant = aVariant;
    otherDirective = aOtherDirective;
    variantByDirectory = aVariantByDirectory;
  }

  public static OnceScriptInfo create(final String aScriptId, final int aVersion, final EOncePhase aPhase
      , final OnceScriptData aSourceName, final int aScriptIdRow, final int aScriptIdCol, final int aScriptIdPosition
      , final boolean aNoTransFind, final String[] aDbKinds, final List<String> aLoadUnlessLoaded
      , final String loadIfLoaded, final boolean aIgnoreChecksum
      , final boolean aLoadOnce, final boolean aReloadAlways
      , final String aDescription, final String[] aDbms, final String[] aVariant
      , final String aOtherDirective, final EVariant aVariantByDirectory) {

    return new OnceScriptInfo(aScriptId, aVersion, aPhase
        , aSourceName, aScriptIdRow, aScriptIdCol, aScriptIdPosition
        , aNoTransFind, aDbKinds, aLoadUnlessLoaded
        , loadIfLoaded, aIgnoreChecksum
        , aLoadOnce, aReloadAlways
        , aDescription, aDbms, aVariant
        , aOtherDirective, aVariantByDirectory);
  }

  /**
   * @return the scriptId
   */
  public String getScriptId() {
    return scriptId;
  }

  /**
   * @return the phase
   */
  public EOncePhase getOncePhase() {
    return oncePhase;
  }

  /**
   * @return the sourceName
   */
  public OnceScriptData getSourceName() {
    return sourceName;
  }



  /**
   * @return the scriptIdBeginRow
   */
  public int getScriptIdBeginRow() {
    return scriptIdBeginRow;
  }

  /**
   * @return the scriptIdBeginCol
   */
  public int getScriptIdBeginCol() {
    return scriptIdBeginCol;
  }

  /**
   * @return the scriptIdBeginPosition
   */
  public int getScriptIdBeginPosition() {
    return scriptIdBeginPosition;
  }

  /**
   * @return the noTransFind
   */
  public boolean isNoTransFind() {
    return noTransFind;
  }

  public String[] getDbKinds() {
    return dbKinds;
  }

  public List<String> getLoadUnlessLoaded(){
    return loadUnlessLoaded;
  }

  public boolean isIgnoreChecksum() {
    return ignoreChecksum;
  }

  public String getLoadIfLoaded() {
    return loadIfLoaded;
  }

  public boolean isReloadAlways() {
    return reloadAlways;
  }

  public boolean isLoadOnce() {
    return loadOnce;
  }

  public String getDescription() {
    return description;
  }

  public String[] getDbms() {
    return dbms;
  }

  public String[] getVariant() {
    return variant;
  }

  public String getOtherDirective() {
    return otherDirective;
  }

  public int getScriptVersion() {
    return scriptVersion;
  }

  public EVariant getVariantByDirectory() {
    return variantByDirectory;
  }


}
