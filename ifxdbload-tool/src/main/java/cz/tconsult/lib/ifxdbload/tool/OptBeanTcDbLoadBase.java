package cz.tconsult.lib.ifxdbload.tool;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import cz.tconsult.dbutil.core.ADbTag;
import cz.tconsult.lib.ifxdbload.core.core.EFazeZavedeni;
import cz.tconsult.lib.ifxdbload.workflow.FazeIntervalParser;
import cz.tconsult.tcbase.clib.mpgmbase.opt.EHelpLevel;
import cz.tconsult.tcbase.clib.mpgmbase.opt.annotation.OptAlias;
import cz.tconsult.tcbase.clib.mpgmbase.opt.annotation.OptHelp;
import cz.tconsult.tcbase.clib.mpgmbase.opt.annotation.OptTransient;
import cz.tconsult.tcbase.clib.mpgmbase.opt.annotation.OptVariableNameNotUsed;
import cz.tconsult.tw.annotation.TwText;
import cz.tconsult.tw.atom.APasswordHolder;
import cz.tconsult.tw.lang.FEnvVar;

public class OptBeanTcDbLoadBase {

  public OptBeanTcDbLoadBase() {
    simulation = false;
    failOneError = true;
  }

  private ADbTag dbUrl;
  private String userName;
  private APasswordHolder userPassword;
  private List<Path> dir;
  private String processedFazesStr;
  private Set<EFazeZavedeni> processedFazes;
  private boolean simulation;
  private boolean failOneError;
  private String nlsLengthSemantics;
  private boolean clean;

  //Než všechny projekty například přejdou na UTF-8, tak musíme držet kompatibilitu s nepsaným win-1250
  private boolean backwardSourceCodeCompatibility;

  @OptAlias(values = {"database-url", "c"})
  @OptHelp(description=@TwText("124L941TCUI=specifikace databáze")
  , paramName=@TwText("125K954TCUI=conn"), level=EHelpLevel.COMMON)
  public ADbTag getDbUrl() {
    return dbUrl;
  }
  public void setDbUrl(final ADbTag aDbUrl) {
    dbUrl = aDbUrl;
  }

  @OptAlias(values = {"database-user", "u"})
  public String getUserName() {
    return userName;
  }
  public void setUserName(final String aUserName) {
    userName = aUserName;
  }

  @OptAlias(values = {"database-password", "p"})
  public APasswordHolder getUserPassword() {
    return userPassword;
  }
  public void setUserPassword(final APasswordHolder aUserPassword) {
    userPassword = aUserPassword;
  }

  @OptAlias(values={"dir", "d"})
  @OptHelp(description=@TwText("123J041TCUI=adresář souborů k vykonání. Bere jen soubory v tomto adresáři, nikoliv v podadresářích.")
  , paramName=@TwText("123K042TCUI=adresář"))
  @OptVariableNameNotUsed
  public List<Path> getDir() {
    return dir;
  }

  public void setDir(final List<Path> aDir) {
    dir = aDir;
  }

  @OptAlias(values={"fazes", "f"})
  @OptHelp(description=@TwText("123M044TCUI=které fáze provést, příklad: --fazes 2-550,3,500-600")
  , paramName=@TwText("123I040TCUI=fáze"))
  @OptVariableNameNotUsed
  public String getProcessedFazesStr() {
    return processedFazesStr;
  }

  public void setProcessedFazesStr(final String aProcessedFazesStr) {
    processedFazesStr = aProcessedFazesStr;
    processedFazes = null;
  }

  @OptTransient
  public Set<EFazeZavedeni> getProcessedFazes() {
    if (processedFazes == null) {
      if (processedFazesStr != null) {
        processedFazes = FazeIntervalParser.parse(processedFazesStr);
      }
    }
    return processedFazes;
  }

  @OptAlias(values={"clean"})
  @OptHelp(description=@TwText("118J855TCUI=Povolí fázi 020clean, jinak bude přeskočena."))
  @OptVariableNameNotUsed
  public boolean isClean() {
    return clean;
  }

  public void setClean(final boolean aClean) {
    clean = aClean;
  }

  @OptAlias(values={"nls-length-semantics"})
  @OptHelp(description=@TwText("123O046TCUI=nastavení NLS_LENGTH_SEMANTICS, možné hodnoty BYTE, CHAR, default nevyplněno."),
  paramName=@TwText("123N045TCUI=NLS_LENGTH_SEMANTICS"))
  @OptVariableNameNotUsed
  public String getNlsLengthSemantics() {
    return nlsLengthSemantics;
  }

  public void setNlsLengthSemantics(final String aNlsLengthSemantics) {
    nlsLengthSemantics = aNlsLengthSemantics;
  }


  @OptAlias(values={"simulation"})
  @OptHelp(description=@TwText("107D903TCUI=povolí čekání na vstup od uživatele."))
  @OptVariableNameNotUsed
  public boolean isSimulation() {
    return simulation;
  }

  public void setSimulation(final boolean aSimulation) {
    simulation = aSimulation;
  }


  @OptAlias(values={"failoneerror", "e"})
  @OptHelp(description=@TwText("107B901TCUI=při první chybě ve oncu končit, defaultně se KONČÍ pri chybě, příznak přebíjí proměnná prostředí %1"
      + FEnvVar.ONCELOADER_FAILONERROR))
  @OptVariableNameNotUsed
  public boolean isFailOneError() {
    return failOneError;
  }

  public void setFailOneError(final boolean aFailOneError) {
    failOneError = aFailOneError;
  }

  @OptAlias(values={"backward-compatibility"})
  @OptHelp(description=@TwText("120P629TCUI=Kódování pro načítání zdrojových textů bude zpátky win-1250"))
  @OptVariableNameNotUsed
  public boolean isBackwardSourceCodeCompatibility() {
    return backwardSourceCodeCompatibility;
  }

  public void setBackwardSourceCodeCompatibility(final boolean backwardSourceCodeCompatibility) {
    this.backwardSourceCodeCompatibility = backwardSourceCodeCompatibility;
  }


}
