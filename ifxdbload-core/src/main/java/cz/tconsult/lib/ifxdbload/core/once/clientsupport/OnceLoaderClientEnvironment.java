package cz.tconsult.lib.ifxdbload.core.once.clientsupport;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import cz.tconsult.tw.cmdline.OptString;
import cz.tconsult.tw.cmdline.OptionProvider;
import cz.tconsult.tw.cmdline.YBadOptions;

public class OnceLoaderClientEnvironment {
  
  
  private final File iWorkingDir;
  private final List<String> iCmdLineArguments = new ArrayList<String>();
  private final String iDbUser;
  private final String iDbPassword;
  private final String iDbUrl;

  private java.sql.Connection iConnection;
  
  private OnceLoaderClientEnvironment(String aArgs[]) {
    
    List<String> args = Arrays.asList(aArgs);
    OptionProvider op = OptionProvider.getStandardProvider(args);
    OptString optDbUrl = op.defineString(ArgsConstants.ARG_DB_URL);
    OptString optDbUser = op.defineString(ArgsConstants.ARG_DB_USER);
    OptString optDbUserPwd = op.defineString(ArgsConstants.ARG_DB_PSWD);
    OptString optWrkDir = op.defineString(ArgsConstants.ARG_WRKDIR);
    OptString optPositionals = op.defineString(null);

    try {
      op.fill();
    } catch (YBadOptions ex) {

      throw new IllegalArgumentException("Bad cmd-line arguments! Args size: " + args.size(), ex);
    };
    
    iDbUrl = optDbUrl.value();
    if (StringUtils.isBlank(iDbUrl)) {
      
      throw new IllegalArgumentException("Argument '" + ArgsConstants.ARG_DB_URL + "' required!");
    }
    iDbUser = optDbUser.value();
    iDbPassword = optDbUserPwd.value();
    String workingDir = optWrkDir.value();
    if (StringUtils.isBlank(workingDir)) {
      
      throw new IllegalArgumentException("Argument '" + ArgsConstants.ARG_WRKDIR + "' required!");
    }
    iWorkingDir = new File(workingDir);
    iCmdLineArguments.addAll(Arrays.asList(optPositionals.values()));
  }
  
  public static OnceLoaderClientEnvironment create(String aArgs[]) {
    
    return new OnceLoaderClientEnvironment(aArgs);
  }
  
  /**
   * Vrátí connection na DB. Pokud user nebyl zadán, stvoří connection jednoparametrickou metodou.
   * @return Vrátí již navázanou connection k DB, nikdy nevrátí null.
   */
  public java.sql.Connection getConnection()
  throws ClassNotFoundException,SQLException{
    if(iConnection==null){

      if (StringUtils.isBlank(iDbUser)) {
        iConnection = DriverManager.getConnection(iDbUrl);
      }
      else {
        iConnection = DriverManager.getConnection(iDbUrl,iDbUser.trim(),iDbPassword.trim());
      }
    }
    return iConnection;
  }
  
  /**
   * Vrátí argumenty příkazového řádku (odstraní parametry potřebné k dodržení rozhraní na OnceLoader.
   * @return Nikdy nevrátí null, nejvýše tak prázdnou kolekci.
   */
  public List<String> getCmdLineArguments(){
    return iCmdLineArguments;
  }
  
  private boolean workingdirExistenceTested = false;
  /**
   * Vrátí pracovní adresář, kam zapisovat soubory (logy, výsledky).
   * Pokud neexistuje, tak je prvním voláním této metody vytvořen.
   * @return Existující adresář, nikdy nevrátí null.
   * @throws RuntimeException Pokud se nepodaří vytvořit adresář
   */
  public File getWorkingDir() {
    
    if (!workingdirExistenceTested) {
      
      workingdirExistenceTested = true;
      if (!iWorkingDir.isDirectory()) {
        
        boolean b = iWorkingDir.mkdirs();
        if (!b) {throw new RuntimeException("Unable to created dir: " + iWorkingDir.getAbsolutePath());}
      }
    }
    return iWorkingDir;
  }
  
}

