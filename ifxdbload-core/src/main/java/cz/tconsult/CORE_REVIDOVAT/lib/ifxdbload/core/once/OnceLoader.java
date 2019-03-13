package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.OnceScript.StatementText;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.DirectiveCheck;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceError;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceScriptInfo;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.ECommand;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EDbKind;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EDirectiveCheckType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EErrorType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.ELoadResult;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EOnceType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EStatementStatus;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EVariant;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.exceptions.AlreadyLoadedButChangedRuntimeException;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.exceptions.OnceScriptException;
import cz.tconsult.lib.exception.EExceptionSeverity;
import cz.tconsult.lib.exception.FThrowable;
import cz.tconsult.lib.ifxdbload.core.loaders.once.legacy.ChecksumComputer;
import cz.tconsult.lib.ifxdbload.core.loaders.once.legacy.Kontrolnici;
import cz.tconsult.lib.tuples.Tuple2;

/*
 * @author brzoza
 */

public class OnceLoader {


  private static final Logger log = LoggerFactory.getLogger(OnceLoader.class);

  private static final Pattern INFORMIX_CHECK= Pattern.compile("ADD\\s+CONSTRAINT\\s+\\(CHECK\\s+", Pattern.CASE_INSENSITIVE);

  private FrmInfoDto frmInfo;
  private EVariant iForceVariant; // vynucená varianta zadaná při spuštění onceloaderu jako parametr

  private final OnceLoaderParams iLoadingParams;

  private OnceLoader(final OnceLoaderParams aOnceLoaderParams)  {
    iLoadingParams = aOnceLoaderParams;
  }


  public static OnceLoader getInstance(final OnceLoaderParams aOnceLoaderParams) {
    return new OnceLoader(aOnceLoaderParams);
  }


  /**
   * Zjisti ze zadaného db spojení druh a variantu databáze.
   */
  private FrmInfoDto getFrmInfo(final Connection aConn) throws SQLException{
    Statement stm = null;
    ResultSet rs = null;
    EDbKind typ;
    String varianta;
    FrmInfoDto result;
    try {
      final String SQL="select dbkind, variantcode from frm_info";
      stm=aConn.createStatement();
      rs=stm.executeQuery(SQL);
      if(rs.next()) {
        String dbkind=rs.getString("dbkind");

        if (dbkind==null) {
          throw new RuntimeException("DBkind in table frm_info is not set!");
        }
        dbkind=dbkind.trim();
        varianta=rs.getString("variantcode");

        if (varianta==null) {
          throw new RuntimeException("variantcode in table frm_info not set!");
        }
        varianta=varianta.trim();

        if (EDbKind.MAIN.getTextValue().equals(dbkind)) {
          typ = EDbKind.MAIN;
        } else if (EDbKind.ARCHIV.getTextValue().equals(dbkind)) {
          typ = EDbKind.ARCHIV;
        } else if (EDbKind.STAT.getTextValue().equals(dbkind)) {
          typ = EDbKind.STAT;
        } else {
          throw new RuntimeException("Value dbkind in table frm_info is not set!");
        }

      } else {
        throw new RuntimeException("No row in table frm_info!");
      }
    }
    catch(final SQLException e){
      throw e;
    }
    finally{
      if(stm!=null) {
        stm.close();
      }
      if(rs!=null) {
        rs.close();
      }
    }

    result = new FrmInfoDto(typ, varianta);

    return result;

  }

  /**
   * Nacte zprasovany/nezparsovany skript do databaze
   * Vyhazuje vyjimku jen pri chybe u z parsovani a
   * a pokud se nepodarilo dostat udaje z evidence
   */
  public ELoadResult load(final OnceScript aOnceScript) throws IOException, OnceScriptException, SQLException {
    final List<OnceError> errs = new ArrayList<OnceError>();

    // zásobní zjištěných chyb v once skriptu, které se mají projevit
    if(aOnceScript==null) {
      throw new RuntimeException("Script is null");
    }
    if(!aOnceScript.isParsed()) {
      aOnceScript.parse();
    }

    // pokud se ve skriptu nacházejí direktivy loadonce a reaload always, je to chyba
    if (aOnceScript.getInfo().isReloadAlways() && aOnceScript.getInfo().isLoadOnce()) {
      final OnceError err = OnceError.create(EErrorType.LOAD_ONCE_ALWAYS, aOnceScript.getInfo().getScriptId());
      errs.add(err);
    }

    // pokud byla ve skriptu nalezena neznámá direktiva, je to chyba
    if (aOnceScript.getInfo().getOtherDirective()!=null) {
      final OnceError err = OnceError.create(EErrorType.UNSUPPORTED_DIRECTIVE, aOnceScript.getInfo().getOtherDirective());
      errs.add(err);
    }

    // pokud se jedná verzi skriptu 2 a není přítomna direktiva descritption, je to chyba
    if (aOnceScript.getInfo().getScriptVersion()==2 && "".equals(aOnceScript.getInfo().getDescription())) {
      final OnceError err = OnceError.create(EErrorType.DESCRIPTION_MISSING, aOnceScript.getInfo().getScriptId());
      errs.add(err);
    }

    // pokud byla zjištěna alespoň jedna chyba, projeví se
    if (errs.size() > 0) {
      throw new OnceScriptException(errs);
    }

    if (iLoadingParams.isForceReload() || aOnceScript.getInfo().isReloadAlways()) {
      // Pokud se má skript zavádět vždy, tak ho jednoduše odstraním z evidence, jako by nikdy zaveden ještě nebyl
      removeFromEvidence(aOnceScript);
    }

    final boolean isRecovery = shouldBeRecovery(aOnceScript.getScriptId());

    final EEvidenceState evidenceState = checkEvidence(aOnceScript); // zjistit stav, jaký je v evidenci
    final boolean maSeZavest = evidenceState == EEvidenceState.NOT_LOADED_YET
        || aOnceScript.isReloadAlways()
        || isRecovery;
    // pokud se nejedná o zotavení a není přítomna direktiva RELOAD_ALWAYS
    ELoadResult result;
    if (maSeZavest) {

      final EOnceType type = aOnceScript.getOnceType();
      switch(type) {
      case IFX_SCRIPT:
        final boolean autoCommit=getConnection().getAutoCommit();
        result = processSQLOnceScript(aOnceScript);
        getConnection().setAutoCommit(autoCommit);
        break;
      case ORA_SCRIPT:
      case MYSQL_SCRIPT:
        result = processSQLOnceScript(aOnceScript);
        break;
      case EXTERNAL_EXECUTE:
      default: throw new RuntimeException("Unsupported type of script: " + type);
      }
    } else {
      switch (evidenceState) {
      case ALREADY_LOADED: result = ELoadResult.ALREADY_LOADED_BEFORE; break;
      case LOADED_BUT_CHANGED:  throw new AlreadyLoadedButChangedRuntimeException("Skript " + aOnceScript.getScriptId() + " byl zmenen!!");
      default: throw new RuntimeException("Jiný stav není možný: " + evidenceState);
      }
    }

    return result;
  }

  protected String listToString(final List<String> l){
    String result="";
    for(final String item:l) {
      result+=item + " ";
    }
    return result;
  }

  /**
   * Zpracuje jednorazove skripty tvorene pouze SQL prikazy
   */
  protected ELoadResult processSQLOnceScript(final OnceScript aOnceScript)
      throws SQLException, OnceScriptException{
    final Statement stm=getConnection().createStatement();
    Boolean zavestSkript=false;

    final OnceScriptInfo info = aOnceScript.getInfo();
    final String[] druhyDb = info.getDbKinds();
    final String[] dbStroje = info.getDbms();
    final String[] varianty = info.getVariant(); // hodnota direktivy VARIANT v hlavičce skriptu
    final EVariant variantByDirectory = info.getVariantByDirectory(); // varianta specifikovaná v názvu adresáře s OnceSkripty

    ELoadResult result;
    final boolean oracleScript = EOnceType.ORA_SCRIPT.equals(aOnceScript.getOnceType());
    final boolean mysqlScript = EOnceType.MYSQL_SCRIPT.equals(aOnceScript.getOnceType());

    // kolekce, do které přidáváme záznamy v případě porušení kontroly direktiv
    final List<DirectiveCheck> checkDirectives = new ArrayList<DirectiveCheck>();

    //kontrola, zda se má tento skript závést do aktuální DB (podle druhu)
    for (final String element : druhyDb) {
      if(element.equalsIgnoreCase(getDbKind().getTextValue())) {
        zavestSkript=true;
        break;
      }
    }
    if (!zavestSkript) {
      checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.DBKIND, getDbKind().getTextValue()));
    }

    //kontrola, zda se má tento skript závést na aktuální DB stroj
    zavestSkript=false;
    if (dbStroje.length>0){
      for (final String element : dbStroje) {
        // pokud je součástí connection URL jméno stroje v direktivě DBMS, je možné skript zavést
        if (getConnection().getMetaData().getURL().toLowerCase().indexOf(element.toLowerCase())!=-1) {
          zavestSkript = true;
          break;
        }
      }
    } else {
      zavestSkript = true;
    }

    if (!zavestSkript) {
      checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.DBMS, getConnection().getMetaData().getURL()));
    }

    //kontrola, zda se má tento skript závést na aktuální DB variantu
    zavestSkript=false;
    if (varianty.length>0) {
      for (final String varianta:varianty) {
        if(varianta.equalsIgnoreCase(getVariant())) {
          zavestSkript = true;
          break;
        }
      }
    } else {
      // pokud ve skriptu nebyla zadána direktiva VARIANT
      // otestujeme zda nebyla při spuštění onceloaderu vynucena jazyková varianta argumentem -forceVariant
      if (iForceVariant!=null && !iForceVariant.getTextValue().equalsIgnoreCase(getVariant())) {
        zavestSkript=false;
      } else {
        zavestSkript = true;
      }
    }

    if (!zavestSkript) {
      checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.VARIANT, getVariant()));
    }

    // pokud jev OnceSkript v adresáři shodném s názvem varianty, musíme provést kontroly
    if (variantByDirectory != null) {
      zavestSkript=false;

      // varianty adresáře s hodnotou varianty v DB
      if (variantByDirectory.getTextValue().equalsIgnoreCase(getVariant())) {
        zavestSkript=true;
      }

      if (!zavestSkript) {
        checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.VARIANT_DIRECTORY, getVariant()));
      }

      // kontrola, zda se shoduje varianta adresáře s variantami v hlavičce OnceSkriptu
      if (varianty.length>0) {

        zavestSkript=false;
        String conflictVariant="[";
        for (final String varianta:varianty) {
          if(varianta.equalsIgnoreCase(variantByDirectory.getTextValue())) {
            zavestSkript = true;

            break;
          }
          conflictVariant = conflictVariant + " " + varianta;
        }

        if (!zavestSkript) {
          final List<OnceError> errs = new ArrayList<OnceError>();
          errs.add(OnceError.create(EErrorType.VARIANT_CONFLICT, aOnceScript.getFile()));

          throw new OnceScriptException(errs);
          // checkDirectives.add(DirectiveCheck.create(EErrorType.VARIANT_CONFLICT, variantByDirectory.getTextValue() + " VS " + conflictVariant));
        }
      }
    }


    //kontrola, zda může tento skript být zaveden do Db (podle direktivy LOAD_UNLESS_LOADED)
    if (!info.getLoadUnlessLoaded().isEmpty()) {
      final ResultSet rs = getXonceTableRecord(info.getLoadUnlessLoaded(),"_exec");
      // pokud byl odkazovaný skript na této databázi zaveden, aktuální onceskript nemůže být zpracován
      if(rs.next()){
        checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.LOAD_UNLESS_LOADED, info.getLoadUnlessLoaded().toString()));
      }
    }


    //kontrola, zda může tento skript být zaveden do DB (podle direktivy LOAD_IF_LOADED)
    if (info.getLoadIfLoaded()!="") {
      final ResultSet rs = getXonceTableRecord(Arrays.asList(info.getLoadIfLoaded()),"_exec");
      // pokud nebyl odkazovaný skript na této databázi zaveden, aktuální onceskript nemůže být zpracován
      if(!rs.next()){
        checkDirectives.add(DirectiveCheck.create(EDirectiveCheckType.LOAD_IF_LOADED, info.getLoadIfLoaded()));
      }
    }

    //Zde se vykonava samotny skript, je v samostatnem try/catch s osetrenim
    //na rollback
    try {
      if(checkDirectives.isEmpty()){

        // pokud je třeba zotavení skriptu (může nastat pouze u Oracle)
        final boolean isRecovery = shouldBeRecovery(aOnceScript.getScriptId());
        if(isRecovery){
          // zotavení z předchozího pádu skriptu
          recoverCommand(stm,aOnceScript);
        } else {

          // pokud je v IFX scriptu nastavena direktiva noTrans nebo pokud jde o ORA script,
          // řídí si script sám transakce
          if(info.isNoTransFind() || oracleScript || mysqlScript){
            executeNoTransaction(stm,aOnceScript);
          }
          else {
            executeWithTransaction(stm,aOnceScript);
          }

          // pokud se nejedná o ORA script, zapíšeme stav do xonce_tabulky (ORA zapisuje průběžně po každém statementu)
          if (!oracleScript && !mysqlScript) {
            insertToEvidence(aOnceScript,false);
          }
        }

        result = ELoadResult.LOADED_NOW;

      } else {
        // evidujeme přeskočení tohoto skriptu
        insertToEvidence(aOnceScript, true, checkDirectives);
        result = ELoadResult.SKIPPED_NOW;
      }
    }
    catch(final SQLException e){
      if(!info.isNoTransFind()) {
        getConnection().rollback();
      }
      //log.error(aOnceScript.getFile().getName()+":"+e.getLocalizedMessage()+" SQL CODE:"+e.getErrorCode());
      throw e;
    }
    finally{
      if(stm!=null) {stm.close();}
    }

    return result;
  }

  /**
   * Zpracuje jednorázový Oracle skript tvořený pouze SQL příkazy.
   * Provádí evidenci úspěšnosti zpracování SQL příkazů v xonce_scripts tabulce
   */
  protected ELoadResult processSQLOracleOnceScript(final OnceScript aOnceScript, final boolean aIsRecovery) throws SQLException{
    final Statement stm=getConnection().createStatement();

    ELoadResult result;

    try {

      if(aIsRecovery){
        // zotavení z předchozího pádu skriptu
        recoverCommand(stm,aOnceScript);

      }else {
        // postupné provádění příkazů, evidence v xonce tabulce
        executeCommands(stm,aOnceScript);
      }

      result = ELoadResult.LOADED_NOW;
    }
    catch(final SQLException e){
      log.error(aOnceScript.getFile().getPath()+":"+e.getMessage()+" SQL CODE:"+e.getErrorCode());
      throw e;
    }
    finally{
      if(stm!=null) {stm.close();}
    }

    return result;
  }

  protected void executeNoTransaction(final Statement aStm,final OnceScript aOnceScript)
      throws SQLException{
    executeCommands(aStm,aOnceScript);
  }

  protected void executeWithTransaction(final Statement aStm,final OnceScript aOnceScript)
      throws SQLException{
    getConnection().setAutoCommit(false);
    executeCommands(aStm,aOnceScript);
    getConnection().commit();
  }


  /**
   * Vykonává SQL příkazy a v případě Oracle onceSkriptu je eviduje v tabulce xonce_scripts
   * @param stm
   * @param aOnceScript
   * @throws SQLException
   */
  protected void executeCommands(final Statement stm, final OnceScript aOnceScript)
      throws SQLException{
    int commandNum=0;
    final StringBuffer commandsForChecksum = new StringBuffer();
    final boolean oracleScript = EOnceType.ORA_SCRIPT.equals(aOnceScript.getOnceType());
    final boolean informixScript = EOnceType.IFX_SCRIPT.equals(aOnceScript.getOnceType());
    final boolean mysqlScript = EOnceType.MYSQL_SCRIPT.equals(aOnceScript.getOnceType());
    if(oracleScript || mysqlScript){
      // evidence once scriptu v tabulce xonce
      insertToEvidence(aOnceScript,false);
    }

    for(final StatementText text : aOnceScript.getCommandStrings()){
      final String command = text.getText();
      String logText = new String(command);

      Kontrolnici kontrolnici = null; // může to zlstat null, ale pak by to mělo být testováno dole. Ať to zhučí na NullPointerException, když už je to tak zbastelno. [veverka;2015-01-02 11:28:41]
      if(oracleScript || mysqlScript){
        kontrolnici = getCheckSumOfString(commandsForChecksum.toString());
        commandNum++;
      }

      final int i=command.indexOf("\n");
      if(i>0){
        logText=command.substring(0, i-1) + " ... ";
        logText.replace("\r", "");
      }
      log.info(">>> "+logText);

      try{
        stm.execute(command);

        if(oracleScript || mysqlScript){
          commandsForChecksum.append(command);

          // update dat v tabulce xonce po úspěšném provedení statementu
          // TODO kk - do tabulky neukládám číslo řádku, ale číslo příkazu - nutná větší předělávka
          updateEvidence(aOnceScript, kontrolnici, commandNum, EStatementStatus.EXECUTING);
        }
      }
      catch (final SQLException ex){
        // RN00325838 - pokud příkaz selže, chceme jej znát celý
        if (i > 0) {
          log.info("Full text of failed command:\n" + command);
        }

        if(oracleScript || mysqlScript){
          updateEvidence(aOnceScript, kontrolnici, commandNum, EStatementStatus.FAILED);
        }
        if (informixScript) {
          // RN00325838 - ošetření chyby databáze, která pokud selže CHECK omezení, nevypíše nic bližšího
          // což je nepříjemné, pokud je v příkazu takových CHECK omezení více
          if (ex.getErrorCode() == -530 && "Check constraint (%s) failed.".equals(ex.getMessage())) {
            // Zjištění, jestli v příkazu není více CHECK
            int checkNumber = 0;
            final Matcher matcher = INFORMIX_CHECK.matcher(command);
            while (matcher.find()) {
              checkNumber++;
            }

            if (checkNumber > 1) {
              log.info("Informix cannot distinguish, what check was violated by existing data, because it "
                  + "dont belong to system catalog yet. (RN00325838)");
              log.info("HINT: Please split next time the command with checks into more ones sequentially executed.");
            }
          }
        }

        throw ex;
      }
    }
    if(oracleScript || mysqlScript){
      // checksum celého souboru
      final Kontrolnici kontrolnici = getCheckSumOfFile(aOnceScript);
      // byly úspěšně provedeny všechny příkazy
      updateEvidence(aOnceScript, kontrolnici, 0, EStatementStatus.DONE);
    }
  }

  /**
   * Provede příkazy once skriptu a pokusí se o zotavení posledního neúspěšného příkazu.
   * @return
   */
  protected boolean recoverCommand(final Statement stm, final OnceScript aOnceScript) throws SQLException{
    int commandNum=0;
    int dbCommandNum=0;
    long dbChecksum=0;
    final StringBuffer commandsForChecksum = new StringBuffer();
    boolean executeCommand = false;

    // kontrola checksumu podle db a našeho checksumu
    final ResultSet rs = getXonceTableRecord(aOnceScript.getInfo().getScriptId());
    if(rs.next()){
      dbChecksum = rs.getLong("checksum");
      dbCommandNum = rs.getInt("rownumber");
    }

    for(final StatementText text : aOnceScript.getCommandStrings()){
      final String command = text.getText();
      String logText = new String(command);

      final Kontrolnici kontrolnici = getCheckSumOfString(commandsForChecksum.toString());
      commandNum++;
      commandsForChecksum.append(command);
      // iterujeme až k problémovému SQL příkazu
      if(commandNum==dbCommandNum){
        // kontrola checksumu
        if(kontrolnici.verifyChecksum(dbChecksum)){
          executeCommand = true;
        }
        else { // došlo k editaci příkazů před problémovým SQL příkazem
          throw new RuntimeException("Checksum nesouhlasí, před příkazem číslo " + commandNum + " byla provedena změna" );
        }
      }

      if(executeCommand){


        final int i=command.indexOf("\n");
        if(i>0){
          logText=command.substring(0, i-1) + " ... ";
          logText.replace("\r", "");
        }
        log.info(">>> "+logText);

        try{
          stm.execute(command);

          // update dat v tabulce xonce po úspěšném provedení statementu
          updateEvidence(aOnceScript, kontrolnici, commandNum, EStatementStatus.EXECUTING);
        }
        catch (final SQLException ex){
          updateEvidence(aOnceScript, kontrolnici, commandNum, EStatementStatus.FAILED);
          throw ex;

        }
      }

    }

    // checksum celého souboru
    final Kontrolnici kontrolnici = getCheckSumOfFile(aOnceScript);
    // byly úspěšně provedeny všechny příkazy
    updateEvidence(aOnceScript, kontrolnici, 0, EStatementStatus.DONE);


    return true;
  }


  private enum EEvidenceState {NOT_LOADED_YET, LOADED_BUT_CHANGED, ALREADY_LOADED};


  /**
   * Zkontroluje v tabulce jestli uz nahodou dany skript nebyl spousten
   * nebo jeho obsah nebyl zmenen
   */
  protected void removeFromEvidence(final OnceScript aOnceScript) throws SQLException {
    final PreparedStatement pstm = getConnection().prepareStatement("DELETE FROM xonce_scripts WHERE scriptid=?");
    try {
      pstm.setString(0 +1, aOnceScript.getScriptId());
      pstm.execute();
    } finally {
      pstm.close();
    }
  }

  /**
   * Zkontroluje v tabulce jestli uz nahodou dany skript nebyl spousten
   * nebo jeho obsah nebyl zmenen
   * @return
   * @throws SQLException
   */
  protected EEvidenceState checkEvidence(final OnceScript aOnceScript) throws SQLException{

    //Pokud je tabulka prázdná nebo neexistuje, tak záměrně zhuč na výjimku
    //Chystáme se na přechod, kdy se předstane používat špatně pojmenovaná tabulka xonce_scripts
    //[polakm;2010-02-11 15:37:57]
    {
      long count = -1;
      final String sql = "select count(*) from xonce_scripts";
      final Statement stm = getConnection().createStatement();
      try {
        final ResultSet rs = stm.executeQuery(sql);
        try {
          if (rs.next()) {
            count = rs.getLong(1);
          }
        }
        finally {
          rs.close();
        }
      }
      finally {
        stm.close();
      }
      if (count < 0) {
        throw new RuntimeException("Table xonce_scripts is empty or even dosn't exist!");
      }
    }

    final String SQL="select * from xonce_scripts where scriptid='"+aOnceScript.getInfo().getScriptId()+"'";
    final Statement stm = getConnection().createStatement();
    try {
      final ResultSet rs = stm.executeQuery(SQL);
      try {
        if(rs.next()) {
          final long checksum=rs.getLong("checksum");
          // kontrola direktivy IGNORE_CHECKSUM
          if(!aOnceScript.getInfo().isIgnoreChecksum()){
            final Kontrolnici checkSumOfFile = getCheckSumOfFile(aOnceScript, checksum);
            if(!checkSumOfFile.verifyChecksum(checksum)){
              // Nasledujici hlasky obsahuji hodne citlive informace,
              // zobraz je pouze pri nejvyssim levelu trasovani chyb.#RN00225703
              log.info("Skript " + aOnceScript.getFile().getPath() + " byl zmenen, checksum souboru=" + checkSumOfFile + " checksum z xonce_script=" + checksum);
              if (log.isTraceEnabled()) {
                final String prikaz = "UPDATE xonce_scripts SET checksum = " + checkSumOfFile + " WHERE scriptid = '" + aOnceScript.getInfo().getScriptId() + "'";
                log.trace(prikaz);
              }
              return EEvidenceState.LOADED_BUT_CHANGED;
            }
          }
          return EEvidenceState.ALREADY_LOADED;
        }
      }
      finally {

        rs.close();
      }
    }
    finally{
      stm.close();
    }
    return EEvidenceState.NOT_LOADED_YET;
  }

  /**
   * Vlozi do evidence zaznam o spuštění skriptu s komentářem.
   * @param aOnceScript
   * @param preskocit
   * @throws SQLException
   */

  protected void insertToEvidence(final OnceScript aOnceScript, final Boolean preskocit) throws SQLException{
    insertToEvidence(aOnceScript,preskocit,null);
  }

  /**
   * Vlozi do evidence zaznam o spusteno skriptu.
   */
  protected void insertToEvidence(final OnceScript aOnceScript, final Boolean aPreskocit, final List<DirectiveCheck> aComments) throws SQLException{
    final long checksum=getCheckSumOfFile(aOnceScript).getMainChecksum();
    final OnceScriptInfo info = aOnceScript.getInfo();
    final String scriptid=info.getScriptId();
    final String description = info.getDescription();
    String SQL="";
    String comments = "";
    Boolean isCheckSum=true;

    if (aComments!=null) {
      comments = aComments.toString();
    }

    // pokud nemá být skript zaváděn na databázi aktuálního druhu
    EStatementStatus status=null;
    if (aPreskocit) {
      status=EStatementStatus.SKIPPED;
    } else if(EOnceType.ORA_SCRIPT.equals(aOnceScript.getOnceType()) || EOnceType.MYSQL_SCRIPT.equals(aOnceScript.getOnceType())){
      status=EStatementStatus.EXECUTING;
      isCheckSum=false;

    } else {
      status=EStatementStatus.DONE;
    }

    PreparedStatement stm=null;

    SQL = "insert into xonce_scripts(checksum,scriptid,rownumber,status,comments,description) values(?,?,?,?,?,?)";
    stm=getConnection().prepareStatement(SQL);
    if(isCheckSum){
      stm.setLong(1, checksum);
    }
    else{
      stm.setLong(1, 0);
    }
    stm.setString(2,scriptid);
    stm.setInt(3,0);
    stm.setString(4,status.name());
    stm.setString(5,comments);
    stm.setString(6, description);

    try{
      System.out.println(stm);
      getConnection().setAutoCommit(false);
      stm.execute();
      getConnection().commit();
    }
    catch(final SQLException e){
      throw e;
    }
    finally{
      if(stm !=null) {
        stm.close();
      }
    }

  }

  /**
   * Vlozi do evidence zaznam o spusteno skriptu.
   * @param aOnceScript
   * @throws SQLException
   */

  protected void updateEvidence(final OnceScript aOnceScript, final Kontrolnici kontrolnici, final int aRowNumber, final EStatementStatus aStatus) throws SQLException{
    final String scriptid=aOnceScript.getInfo().getScriptId();
    final String SQL="UPDATE xonce_scripts SET checksum="+kontrolnici.getMainChecksum()+", rownumber="+aRowNumber+", status='"+aStatus.getTextValue()+"' WHERE scriptid='"+scriptid+"'";
    Statement stm=null;
    try{
      stm = getConnection().createStatement();
      getConnection().setAutoCommit(false);
      stm.execute(SQL);
      getConnection().commit();
    }
    catch(final SQLException e){
      throw e;
    }
    finally{
      if(stm !=null) {
        stm.close();
      }
    }
  }

  /**
   * Vraci zaznam z tabulky xonce_scripts podle scriptId.
   */
  protected ResultSet getXonceTableRecord(final String scriptid) throws SQLException{
    return getXonceTableRecord(Arrays.asList(scriptid),"");
  }

  /**
   * Vraci zaznam z tabulky xonce_scripts podle scriptId.
   * Někdy můžeme kontrolovat i existenci generovaných procedur se suffixem _exec.
   */
  protected ResultSet getXonceTableRecord(final List<String> aScriptIds, final String suffix) throws SQLException{
    final StringBuffer sb = new StringBuffer("SELECT * FROM xonce_scripts WHERE ");
    if (aScriptIds.isEmpty()) {
      sb.append("1=0");
    } else {
      sb
      .append("scriptId IN (")
      .append(StringUtils.repeat("?, ?, ", aScriptIds.size()))
      ;
      sb.setLength(sb.length()-2);
      sb.append(")");
    }

    PreparedStatement stm = null;
    ResultSet rs= null;
    try{
      stm = getConnection().prepareStatement(sb.toString());
      for (final java.util.ListIterator<String> i = aScriptIds.listIterator(); i.hasNext(); ) {
        final int pozice = i.nextIndex()*2;
        final String scriptId = i.next();
        stm.setString(pozice +1, scriptId);
        stm.setString(pozice +2, scriptId+suffix);
      }
      rs = stm.executeQuery();
    }
    catch(final SQLException e){
      throw e;
    }


    return rs;
  }

  /**
   * Vrati CRC32 kontrolni soucet souboru.
   */
  protected Kontrolnici getCheckSumOfFile(final OnceScript  onceScript, final Long aExpectedChecksum){

    final ChecksumComputer cscompPoCastech = new ChecksumComputer();
    final ChecksumComputer cscompVCelku = new ChecksumComputer();

    cscompVCelku.update(onceScript.getFile().getData(), aExpectedChecksum);

    for (final StatementText st: onceScript.getCommandStrings()) {
      //System.err.println("XX: " + st.getText());
      st.getText();
      cscompPoCastech.update(st.getText(), aExpectedChecksum);
    }

    final Kontrolnici kontrolnici = new Kontrolnici();
    cscompVCelku.apllyTo(kontrolnici);
    cscompPoCastech.apllyTo(kontrolnici);

    return kontrolnici;
  }

  protected Kontrolnici getCheckSumOfFile(final OnceScript  onceScript){

    final Kontrolnici result = getCheckSumOfFile(onceScript, null);
    return result;
  }

  /**
   * Vrati CRC32 kontrolni soucet retezce.
   */
  protected Kontrolnici getCheckSumOfString(final String aChain){
    final ChecksumComputer cscomp = new ChecksumComputer();
    cscomp.update(aChain);
    return cscomp.apllyTo(null);
  }




  public void checkDbKind() throws SQLException {

  }

  private void loadFrmInfo() {
    if (getConnection() != null) {
      try {
        frmInfo = getFrmInfo(getConnection());
      } catch (final SQLException e) {
        throw new RuntimeException("Cannot load frm info", e);
      }
    } else {
      throw new RuntimeException("Connection to DB is not set!");
    }
  }

  /**
   * Třída pro přenos
   * @author kovarikj
   *
   */

  private class FrmInfoDto{
    private final EDbKind typ;
    private final String varianta;

    public FrmInfoDto(final EDbKind aTyp, final String aVarianta) {
      typ = aTyp;
      varianta = aVarianta;
    }

    public EDbKind getTyp() {
      return typ;
    }
    public String getVarianta() {
      return varianta;
    }
  }

  public EVariant getForceVariant() {
    return iForceVariant;
  }

  public void setForceVariant(final EVariant aForceVariant) {
    iForceVariant = aForceVariant;
  }

  /**
   * @param id
   * @return
   * @throws SQLException
   */
  private boolean shouldBeRecovery(final String id) throws SQLException {
    boolean isRecoveryScript = false;
    {
      final String sql = "SELECT count(*) FROM xonce_scripts WHERE status = 'FAILED' and scriptid=?";
      final PreparedStatement stm = getConnection().prepareStatement(sql);
      try {

        stm.setString(1, id);
        final ResultSet rs = stm.executeQuery();
        try {

          if (rs.next()) {

            final int count = rs.getInt(1);
            if (count > 0) {

              isRecoveryScript = true;
            }
          }
        }
        finally {

          rs.close();
        }
      }
      finally {
        stm.close();
      }
    }
    return isRecoveryScript;
  }

  private Tuple2<OnceLoaderResult, OnceScript>  _data2Script (final OnceScriptData aPath)  {

    final OnceLoaderResult result = new OnceLoaderResult();
    result.setResult(null); // poud zůstane null, tak došlo k chybě
    if (! new OnceScript().acceptScriptFile(aPath)){
      naplneResultInformaciOVyjimce(result, EErrorType.BAD_HEADER, aPath, null);
      log.error("Skript "+ aPath.getPath()+ " nelze zpracovat - nepodporovaná verze skriptu (špatná hlavička). ");
      return new Tuple2<OnceLoaderResult, OnceScript>(result, null);
    }
    final OnceScript onceScript =  OnceScript.getInstance(aPath, iLoadingParams.isStrictMode());

    return new Tuple2<OnceLoaderResult, OnceScript>(result, onceScript);
  }
  /**
   * Metoda spusti nasazeni once skriptu do databaze. Metoda nevyhazuje
   * zadne vyjimky.Sama si osetruje pomoci knihoven pripadne vyjimky a
   * loguje chyby.
   * @param aPath cesta k souboru
   */
  public OnceLoaderResult load(final OnceScriptData aPath)  {

    final Tuple2<OnceLoaderResult, OnceScript> t = _data2Script(aPath);
    final OnceLoaderResult result = t.get1();
    final OnceScript onceScript = t.get2();
    if (onceScript == null) {

      return result;
    }

    try{
      //Osetrime prikaz na specifikaci dbspacu indexu
      if(iLoadingParams.getIndexesDbSpace()!=null ){
        onceScript.setUseBtree(true);
        onceScript.specifyDbSpaceByCmd(ECommand.INDEX, iLoadingParams.getIndexesDbSpace());
      }

      final ELoadResult eresult = load(onceScript);
      result.setResult(eresult);
      //**************//
      //      String msg=result.getResult().getMessage().replace("%s", getPathForLog(aPath));
      //      msg+="(faze:"+ onceScript.getOncePhase()+")";
      //      if(result.getResult() != ELoadResult.LOADED_NOW) {
      //        msg+="("+getCatalogUser()+","+getCatalogDatetime()+")";
      //      }
      //      log.info("<<"+msg+">>");
    }
    catch(final OnceScriptException e){
      result.setErrs(e.getErrors());
    }
    catch(final AlreadyLoadedButChangedRuntimeException e) {
      naplneResultInformaciOVyjimce(result, EErrorType.LOADED_BEFORE_BUT_CHANGED, aPath, e);
    }
    catch(final IOException e) {
      naplneResultInformaciOVyjimce(result, EErrorType.UNEXPECTED_IO_ERROR, aPath, e);
    }
    catch(final SQLException e) {
      naplneResultInformaciOVyjimce(result, EErrorType.SQL_ERROR, aPath, e);
    }
    catch(final Exception e) {
      //      naplneResultInformaciOVyjimce(result, EErrorType.IO, aPath, e);
      FThrowable.formatter(e).withSeverity(EExceptionSeverity.DISPLAY)
      .withCircumstance( "Nasazeni skriptu " + aPath)
      .withPrefix(getPathForLog(aPath))
      .dump();
    }
    return result;
  }

  private void naplneResultInformaciOVyjimce(final OnceLoaderResult result, final EErrorType erorrType, final OnceScriptData osd, final Throwable thr) {
    //    FExceptionDumper.dump(thr, EExceptionSeverity.DISPLAY, "Nasazeni skriptu " + osd);
    final OnceError err = OnceError.create(erorrType, getPathForLog(osd), thr);
    //    FThrowable.printStackTrace(thr, System.err, getPathForLog(osd));
    result.getErrs().add(err);
  }

  private String getPathForLog(final OnceScriptData data) {
    return new File(data.getPath()).getName();
  }

  private static List<OnceErrorData> _exportErrorData(final List<OnceError> aErrors) {

    final List<OnceErrorData> result = new ArrayList<OnceErrorData>();
    if (aErrors != null) {

      for (final OnceError e : aErrors) {

        final EErrorType type = e.getErrorType();
        final OnceErrorData d = OnceErrorData.create(
            type == null ? null : type.toString(), e.getSourceName()
                , e.getBeginRow(), e.getBeginCol(), e.getBeginPosition()
                , e.getEndRow(), e.getEndCol(), e.getEndPosition()
                , e.getCause()
            );
        result.add(d);
      }
    }
    return result;
  }
  /* (non-Javadoc)
   * @see cz.tconsult.tw.oncescript.IEclipseOnceLoader#loadFromEclipse(cz.tconsult.tw.oncescript.OnceScriptData)
   */
  public Map<String, Object> loadByEclipse(final OnceScriptData aData) throws  SQLException,  IOException {

    final OnceLoaderResult result = load(aData);
    final Map<String, Object> resultMap = new HashMap<String, Object>();

    resultMap.put("result", result.getResult());

    final List<OnceErrorData> l = _exportErrorData(result.getErrs());
    resultMap.put("errorList", l);
    return resultMap;
  }

  public List<OnceError> checkByEclipse(final OnceScriptData aData) {

    final Tuple2<OnceLoaderResult, OnceScript> t = _data2Script(aData);
    final OnceLoaderResult result = t.get1();
    final OnceScript onceScript = t.get2();
    final List<OnceError> errs = result.getErrs();
    if (onceScript != null) {

      try {
        onceScript.parse();
      } catch(final OnceScriptException e) {

        errs.addAll(e.getErrors());
      }
    }
    return errs;
  }


  public Map<String, Object> getOnceScriptInfo(final String aFullFileName) {

    return getOnceScriptInfoStatic(aFullFileName);
  }

  public static Map<String, Object> getOnceScriptInfoStatic(final String aFullFileName) {

    final File fullFile = new File(aFullFileName);
    boolean isInPrcDir = false;
    boolean isAtPrcPath = false;
    boolean isOncePhase = false;
    String oncePhaseName = null;

    final File parent = fullFile.getParentFile();
    if (parent != null) {

      File dir = fullFile;
      while ((dir = dir.getParentFile()) != null) {

        final String name = dir.getName();
        if (name.equalsIgnoreCase("prc")) {

          isAtPrcPath = true;
          if (parent.equals(dir)) {
            isInPrcDir = true;
          }
        }
        else if (
            name.equalsIgnoreCase("0before")
            || name.equalsIgnoreCase("1alter")
            || name.equalsIgnoreCase("3settings")
            || name.equalsIgnoreCase("4migration")
            || name.equalsIgnoreCase("5alter")
            || name.equalsIgnoreCase("6tidy")
            ) {

          isOncePhase = true;
          oncePhaseName = name.toLowerCase();
          break;
        }
      }
    }
    final Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("isInPrcDir", isInPrcDir);
    resultMap.put("isAtPrcPath", isAtPrcPath);
    resultMap.put("isOncePhase", isOncePhase);
    resultMap.put("oncePhaseName", oncePhaseName);

    return resultMap;
  }




  /**
   * @return the dbKind
   */
  protected EDbKind getDbKind() {
    if (frmInfo == null) {
      loadFrmInfo();
    }
    return frmInfo.getTyp();
  }



  /**
   * @return the variant
   */
  protected String getVariant() {
    if (frmInfo == null) {
      loadFrmInfo();
    }
    return frmInfo.getVarianta();
  }


  /**
   * @return the connection
   */
  private Connection getConnection() {
    return iLoadingParams.getConnection();
  }

}