package cz.tconsult.lib.ifxdbload.core.once.clientsupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Třída podporující jednu stranu rozhraní OnceLoader-volaný Java program, a to stranu volající.
 * Pomocník pro tvorbu parametrů pro spuštění klienta programu OnceLoader.
 *
 */
public class FOnceLoaderClientCallHelper {

  /**
   * Seznam parametrů pro ostré spuštění klienta. Pokud je aDbUser null, pak je DbPwd ignorován.
   * @param aDbUrl Nesmí být null.
   * @param aDbUser Může být null.
   * @param aDbPwd Může být null.
   * @param aWorkingDir Nesmí být null.
   * @return Parametry pro spuštění
   */
  public static List<String> getLaunchParameters(final String aDbUrl, final String aDbUser, final String aDbPwd, final File aWorkingDir){
    final List<String> result = new ArrayList<String>();

    if (StringUtils.isBlank(aDbUrl)) {

      throw new IllegalArgumentException("DB URL is blank!");
    }
    result.add("--"+ArgsConstants.ARG_DB_URL);
    result.add(aDbUrl);
    if (!StringUtils.isBlank(aDbUser)) {
      result.add("--"+ArgsConstants.ARG_DB_USER);
      result.add(aDbUser);
      result.add("--"+ArgsConstants.ARG_DB_PSWD);
      result.add(aDbPwd);
    }
    if (aWorkingDir == null) {

      throw new IllegalArgumentException("WorkingDir is blank!");
    }
    result.add("--"+ArgsConstants.ARG_WRKDIR);
    result.add(aWorkingDir.getPath());
    result.add("--");
    return result;
  }

  /**
   * Seznam parametrů pro zalogování parametrů spuštění klienta.
   * @param aDbUrl Nesmí být null.
   * @param aDbUser Může být null.
   * @param aWorkingDir Nesmí být null.
   * @return Parametry pro spuštění
   */
  public static List<String> getLaunchParametersDebug(final String aDbUrl, final String aDbUser, final File aWorkingDir){
    return getLaunchParameters(aDbUrl, aDbUser, "*****", aWorkingDir);
  }
}
