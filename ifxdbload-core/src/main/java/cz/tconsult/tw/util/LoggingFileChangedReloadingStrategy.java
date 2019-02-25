package cz.tconsult.tw.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cz.tconsult.lib.exception.EExceptionSeverity;
import cz.tconsult.lib.exception.FThrowable;


public class LoggingFileChangedReloadingStrategy extends FileChangedReloadingStrategy {


  private static final Log log = LogFactory.getLog(LoggingFileChangedReloadingStrategy.class);

  Map<String, String> iOldConfiguration = new HashMap<String, String>();

  @Override
  public void init() {
    super.init();
    detectAndLogChanges();
  }


  @Override
  public synchronized void  reloadingPerformed() {
    super.reloadingPerformed();
    detectAndLogChanges();
  }


  private void detectAndLogChanges() {
    try {
      final StringBuffer sb = new StringBuffer();

      //    Set<String> nezpracovane = new HashSet<String>(iOldConfiguration);
      final List<String> newKeys = new ArrayList<String>();
      CollectionUtils.addAll(newKeys, configuration.getKeys());

      final Map<String, String> prac = iOldConfiguration; // starou konfiguraci budeme rozebírat
      iOldConfiguration = new HashMap<String, String>(); // a novou od začátku skládat
      for (final String key : newKeys) { // procházíme nový stav
        final String newValue = configuration.getString(key, null);  // default je tam, kdyby náhodou byla změna, aby to nespadlo
        if (newValue == null) {
          continue;
        }
        final String oldValue = prac.remove(key);  // vyber hodnotu a zároveň odstraň, co je tam
        if (oldValue == null) { // došlo k přidání, dříve nebylo
          zapisZmenu(sb, Smer.ADD, key, null, newValue);
        } else {
          if (! oldValue.equals(newValue)) { // došlo ke změně
            zapisZmenu(sb, Smer.CHANGE, key, oldValue, newValue);
          }
        }
        iOldConfiguration.put(key, newValue);
      }
      for (final String key : prac.keySet()) {
        final String oldValue = prac.get(key);
        zapisZmenu(sb, Smer.REMOVE, key, oldValue, null);
      }
      log.debug("The configuration " + getFile() + " has bean realoaded!" + sb.toString());
    } catch (final RuntimeException e) {
      // Pokud padne výjimka během logování zhměny konfigurace, tak kvůli tomu nespadne apliakce
      FThrowable.formatter(e).withSeverity(EExceptionSeverity.WORKARROUND).withCircumstance("Exception while logging configuration change").dump();
    }
  }


  private void zapisZmenu (final StringBuffer sb, final Smer aSmer, final String aKey, final String aOld, final String aNew) {
    sb.append(IOUtils.LINE_SEPARATOR);
    sb.append("   " + aSmer + ": " + aKey + " = ");
    if (aSmer == Smer.ADD) {
      sb.append(aNew);
    }
    if (aSmer == Smer.REMOVE) {
      sb.append(aOld);
    }
    if (aSmer == Smer.CHANGE) {
      sb.append(aNew + " <-- " + aOld);
    }

  }

  private static enum Smer {ADD, REMOVE, CHANGE};



}
