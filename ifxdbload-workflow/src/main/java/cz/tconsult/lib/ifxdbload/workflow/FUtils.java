package cz.tconsult.lib.ifxdbload.workflow;

import java.time.Duration;


public class FUtils {

  /**
   * Vrátí hezky formátovaný časový úsek.
   * @param aMs
   * @param showLeftZeros
   * @param showMilliseconds
   * @return
   */
  public static String prettyDuration(final Duration aDuration, final boolean showLeftZeros, final boolean showMilliseconds) {

    long l = aDuration.toMillis();
    final int miliSeconds = (int)(l % 1000);
    l /= 1000;
    final int seconds = (int)(l % 60);
    l /= 60;
    final int minutes = (int)(l % 60);
    l /= 60;
    final int hours = (int )(l % 24);
    l /= 24;
    final int days = (int)l;

    final StringBuffer result = new StringBuffer();

    boolean usedDays = false;
    if (days > 0) {

      result.append(days);
      result.append("d ");
      usedDays = true;
    }

    boolean usedHours = false;
    if (showLeftZeros || usedDays || hours > 0) {

      result.append( showLeftZeros || usedDays ? String.format("%02d", hours) : ""+hours);
      result.append(":");
      usedHours = true;
    }

    boolean usedMinutes = false;
    if (showLeftZeros || usedHours || minutes > 0) {

      result.append( showLeftZeros || usedHours ? String.format("%02d", minutes) : ""+minutes);
      result.append(":");
      usedMinutes = true;
    }
    if (showLeftZeros || usedMinutes) {

      result.append(String.format("%02d", seconds));
    }
    else {

      result.append(seconds);
    }

    if (showMilliseconds) {

      result.append(String.format(".%03d", miliSeconds));
    }

    return result.toString();

  }


}
