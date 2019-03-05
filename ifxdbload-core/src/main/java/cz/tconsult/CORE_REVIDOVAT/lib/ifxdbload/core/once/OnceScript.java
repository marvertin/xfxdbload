package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.ExecuteElement;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceError;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceScriptInfo;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.SqlCommand;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.ECommand;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EErrorType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EExecuteType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EOncePhase;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EOnceType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EVariant;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.exceptions.OnceScriptException;
import cz.tconsult.CORE_REVIDOVAT.parser.lexer.LexerTokenLocator;
import cz.tconsult.CORE_REVIDOVAT.parser.util.SyntaxErrorException;
import cz.tconsult.CORE_REVIDOVAT.spl.IfxSqlCompiler;
import cz.tconsult.CORE_REVIDOVAT.spl.parser.SplBase0;
import cz.tconsult.CORE_REVIDOVAT.spl.parser.SplDocument;
import cz.tconsult.CORE_REVIDOVAT.tw.util.FileManager;
import cz.tconsult.lib.lexer.PositionTrackPushbackReader;

public class OnceScript  {

  private static final Logger log = LoggerFactory.getLogger(OnceScript.class);


  /** Znacky komentaru*/
  protected final static String COMMENTS[]={"//","#","--"};
  /** Fomát řetězce, kterým začíná identifikace verze skriptu**/
  protected final static String VERSION_FORMAT ="V1";

  /** Verze Once skriptu**/
  protected byte iVersion;
  /** SQL obsah skriptu orezany o begin w. , commit w. a rollback*/
  protected String iParsedScript;
  /** Soubor skriptu*/
  final protected OnceScriptData iFile;
  /** Byl-li skript parsovan*/
  protected boolean iParsed=false;
  /** Zapne striktni parsovani obsahu */
  protected boolean iStrictMode;
  /** Jedna-li se o externi spoustec */
  final protected EOnceType iOnceType;
  /** Externi elementy ke spusteni*/
  final protected List<ExecuteElement> iExecutes;
  /** Rozparsovane prikazy parsrem */
  protected List<StatementText> iSqlStatements=new ArrayList<StatementText>();
  /** Je mozne predat ke kontrole i samotny SplDocument */
  protected SplDocument iSplDocument;
  /** TODO Priznak vazne chyby v parsru */
  /** Udava ze soubor nemohl byt zparsrovan kvuli jine chybe nez syntakticke */
  /** skript se pak pokusi ulozit jako jeden prikaz a spustit */
  protected boolean iParsingCrashed=false;
  /** Poctiva zasoba chyb */
  protected List<OnceError> iErrors=new ArrayList<OnceError>();
  /** Informace o skriptu */
  final protected OnceScriptInfo iScriptInfo;
  /** Specifikace dbspace*/
  protected Class<? extends SplBase0> iDBSCommandType; //typ prikazu
  protected String iDbSpace;//dbspace
  protected boolean iUseBtree;//pouzit btree

  protected final static IfxSqlCompiler COMPILER=new IfxSqlCompiler();
  private final OnceFileTypeDispatcher typeDispatcher = new OnceFileTypeDispatcher();

  public OnceScript(){
    iScriptInfo = null;
    iFile = null;
    iOnceType = null;
    iExecutes = null;

  }

  protected OnceScript(final OnceScriptData aFile,final boolean aStrictMode){

    if(aFile == null) {

      throw new IllegalArgumentException("aFile has to be specified!");
    }

    iFile = aFile;

    final EOnceType onceType = ziskejTypSkriptuZeJmena(aFile);
    if (onceType==EOnceType.EXTERNAL_EXECUTE){
      iExecutes = new ArrayList<ExecuteElement>();
    }else {
      iExecutes = null;
    }
    iOnceType = onceType;
    iStrictMode=aStrictMode;
    try {
      iScriptInfo = fillScriptInfo();
    } catch (final IOException e) {
      throw new RuntimeException("Problem!", e);
    }

  }

  public static OnceScript getInstance(final OnceScriptData aFile,final boolean aStrictMode){
    return new OnceScript(aFile,aStrictMode);
  }

  //  public static OnceScript getInstance(SplDocument aDocument,boolean aStrictMode){
  //    return new OnceScript(aDocument,aStrictMode);
  //  }


  /**
   * @param aFile
   * @return
   */
  private EOnceType ziskejTypSkriptuZeJmena(final OnceScriptData aFile) {
    return new OncePathDeriver(aFile.getPath()).ziskejTypSkriptuZeJmena();
  }

  /**
   * Kontroluje obsah souboru
   * @throws SyntaxErrorException pri parsovaci chybe
   * @throws FileNotFoundException kdyz skript nebyl nalezeny
   */

  protected void _checkContent()
      throws IOException,SyntaxErrorException
  {
    List<SplBase0> children;
    String commandString;

    children = iSplDocument.getChildrenList();

    final String sourceName = iSplDocument.getLocator().getInputSourceName();

    boolean transactionOpen=false,transactionClose=false;
    for(final SplBase0 child : children){

      final LexerTokenLocator loc = child.getParseredTextPosition();

      if(child instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandBeginWork){
        if(!iScriptInfo.isNoTransFind()){
          if(transactionOpen) {
            iErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS,sourceName, loc));
          }
        }
        else {
          iSqlStatements.add(new StatementText(loc.getBegLineNumber()+1, child.getOriginText()));
        }
        transactionOpen=true;
        continue;
      }
      if(child instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandRollbackWork ||
          child instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandCommitWork){
        if(!iScriptInfo.isNoTransFind()){
          if(child instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandCommitWork){
            iErrors.add(OnceError.create(EErrorType.CONTAINS_COMMIT,sourceName, loc));
          }
          if(transactionClose) {
            iErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS,sourceName, loc));
          }
        }
        else{
          iSqlStatements.add(new StatementText(loc.getBegLineNumber()+1, child.getOriginText()));
        }
        transactionClose=true;
        continue;
      }

      if(iStrictMode) {
        //throws OnceScriptException
        OnceScriptUtils.testStrict(child,iScriptInfo.getOncePhase());
      }

      commandString=SqlCommand.doSplChild(child);
      if(commandString==null){
        iErrors.add(OnceError.create(EErrorType.UNEXPECTED_PARSE_ERROR,sourceName,loc));
      }
      if(child.getClass().equals(iDBSCommandType)){
        if(iUseBtree){
          commandString+=" USING BTREE ";
        }
        commandString+=" IN "+ iDbSpace;
      }
      iSqlStatements.add(new StatementText(0, commandString));

      if((transactionOpen==false || transactionClose==true) && !iScriptInfo.isNoTransFind()){
        iErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS,sourceName,loc));
      }

    }//end of for(SplBase0 child : children){
    if(!iScriptInfo.isNoTransFind()){
      if(transactionClose==false) {
        iErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS,sourceName));
      }
    }
  }

  private static final Pattern idPattern = Pattern.compile("--\\s*V1\\s*,\\s*ID:[\\s]*([\\w|\\d|\\p{Punct}]+)",Pattern.CASE_INSENSITIVE);
  private static final Pattern noTransPatternV1 = Pattern.compile("(?i)^[\\s]*--[\\s]*notran[s]{0,1}[\\s]*$",Pattern.CASE_INSENSITIVE);
  private static final Pattern noTransPatternV2 = Pattern.compile("--\\s*@TC:ONCE:\\s*NO_TRANSACTION_CONTROL\\s*",Pattern.CASE_INSENSITIVE);
  private static final Pattern idPattern2 = Pattern.compile("--\\s*@TC:ONCE:\\s*ID\\s*=\\s*V2\\s*,\\s*(.*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern dbKindPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBKIND\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern loadUnlessLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_UNLESS_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern loadIfLoadedPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_IF_LOADED\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern ignoreChecksumPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*IGNORE_CHECKSUM\\s*",Pattern.CASE_INSENSITIVE);
  private static final Pattern loadOncePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*LOAD_ONCE\\s*",Pattern.CASE_INSENSITIVE);
  private static final Pattern reloadAlwaysPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*RELOAD_ALWAYS\\s*",Pattern.CASE_INSENSITIVE);
  private static final Pattern descriptionPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DESCRIPTION\\s*=\\s*(.*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern dbmsPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*DBMS\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern variantPattern = Pattern.compile("--\\s*@TC:ONCE:\\s*VARIANT\\s*=\\s*([\\w,\\s]*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern otherDirectivePattern = Pattern.compile("--\\s*@TC:ONCE:\\s*(\\w+\\s*=\\s*[\\w,\\s]*)",Pattern.CASE_INSENSITIVE);
  private static final Pattern invalidDirective = Pattern.compile("--\\s*@TC:(?!ONCE:)\\s*", Pattern.CASE_INSENSITIVE);

  /**
   * @return
   * @throws IOException
   */
  private OnceScriptInfo fillScriptInfo()
      throws IOException{

    OnceScriptData file;
    String scriptId = null;
    int version = 1;
    int scriptIdRow = -1, scriptIdCol = -1, scriptIdPos = -1;
    boolean noTransFind = false;
    // implicitně je nastaveno na MAIN, pokud se vyskytne direktiva DBKIND, bude přepsáno
    String[] dbKinds = {"MAIN"};
    final List<String> loadUnlessLoaded = new LinkedList<String>();
    String loadIfLoaded = "";
    boolean ignorechecksum = false;
    boolean reloadAlways = false;
    boolean loadOnce = false;
    String description = "";
    String[] dbms = {};
    String[] variant = {};
    String otherDirective = null;

    if (iSplDocument != null) {

      file = iFile;
      final Set<LexerTokenLocator> comments = iSplDocument.getComments();
      if (comments != null) {

        final Iterator<LexerTokenLocator> i = comments.iterator();

        String comment1, comment2;
        LexerTokenLocator loc;
        if (i.hasNext()) {

          loc = i.next();
          final String s = iSplDocument.getParseredText(loc);
          final String sa[] = s.split("[\\n\\r]+");
          final List<String> list = new ArrayList<String>();
          for (final String item : sa) {

            if (!StringUtils.isBlank(item)) {

              list.add(item.trim());
            }
          }
          final int size = list.size();
          if (size == 0) {

            comment1 = "";
            comment2 = null;
          }
          else {

            comment1 = list.get(0);
            comment2 = size == 1 ? null : list.get(1);
          }
        }
        else {

          loc = null;
          comment1 = "";
          comment2 = null;
        }

        final Matcher mId=idPattern.matcher(comment1);
        final Matcher mId2 = idPattern2.matcher(comment1);
        final Boolean m1found = mId.find();
        final Boolean m2found = mId2.find();

        if(!m1found && !m2found) {

          iErrors.add(OnceError.create(EErrorType.ID_MISSING,file, loc));
        }
        else{
          if (m1found) {
            scriptId = mId.group(1).trim();
            scriptIdCol = mId.start();
            version = 1;

          } else {
            scriptId = mId2.group(1).trim();
            scriptIdCol = mId2.start();
            version = 2;

          }

          scriptIdRow = loc.getBegLineNumber();
          scriptIdPos = comment1.indexOf(scriptId) + loc.getBegPosition();
        }

        if (comment2 != null) {

          final Matcher mNoTrans=noTransPatternV1.matcher(comment2);
          if(mNoTrans.find()){
            noTransFind = true;
          }
        }

      }//end of if (comments != null) {
    }//end of if (iSplDocument != null) {
    else {

      file = iFile;
      final PositionTrackPushbackReader br=new PositionTrackPushbackReader(
          new PushbackReader(createReader()), iFile.getPath());
      try {
        String line;
        // pokusu se najit id
        while((line = br.readLine()) != null){

          if(line.trim().equals("")) {
            continue;
          }

          final Matcher mId=idPattern.matcher(line);
          final Matcher mId2=idPattern2.matcher(line);
          final Boolean m1found = mId.find();
          final Boolean m2found = mId2.find();

          if(!m1found && !m2found) {

            iErrors.add(OnceError.create(EErrorType.ID_MISSING,iFile

                ,br.getLineNumber(), 0, br.getPosition() - br.getColumnNumber()
                , br.getLineNumber(), br.getColumnNumber(), br.getPosition()));
          }
          else{
            if (m1found) {
              scriptId = mId.group(1).trim();
              scriptIdCol = mId.start();
              version = 1;

            } else {
              scriptId = mId2.group(1).trim();
              scriptIdCol = mId2.start();
              version = 2;
            }

            scriptIdRow = br.getLineNumber();
            scriptIdPos = br.getPosition();
            break;
          }
        }

        //pokusime se najít řídící direktivy
        Matcher mNoTransV1;
        Matcher mNoTransV2;
        Matcher mDbKind;
        Matcher mLoadUnlessLoaded;
        Matcher mIgnoreChecksum;
        Matcher mLoadIfLoaded;
        Matcher mLoadOnce;
        Matcher mReloadAlways;
        Matcher mDescription;
        Matcher mDbms;
        Matcher mVariant;
        Matcher mOther;
        Matcher minvalidDirective;

        while((line = br.readLine()) != null){

          mNoTransV1=noTransPatternV1.matcher(line);
          mNoTransV2=noTransPatternV2.matcher(line);
          mDbKind=dbKindPattern.matcher(line);
          mLoadUnlessLoaded=loadUnlessLoadedPattern.matcher(line);
          mIgnoreChecksum = ignoreChecksumPattern.matcher(line);
          mLoadIfLoaded = loadIfLoadedPattern.matcher(line);
          mLoadOnce = loadOncePattern.matcher(line);
          mReloadAlways = reloadAlwaysPattern.matcher(line);
          mDescription = descriptionPattern.matcher(line);
          mDbms = dbmsPattern.matcher(line);
          mVariant = variantPattern.matcher(line);
          mOther = otherDirectivePattern.matcher(line);
          minvalidDirective = invalidDirective.matcher(line);

          if(!line.trim().equals("")){

            if(mNoTransV1.find() || mNoTransV2.find()){
              noTransFind = true;
            }
            else if(mDbKind.find()){
              dbKinds = mDbKind.group(1).trim().split(",");
            }
            else if(mLoadUnlessLoaded.find()){
              loadUnlessLoaded.add(mLoadUnlessLoaded.group(1).trim());
            }
            else if(mLoadIfLoaded.find()){
              loadIfLoaded = mLoadIfLoaded.group(1).trim();
            }
            else if(mIgnoreChecksum.find()){
              ignorechecksum = true;
            }
            else if(mLoadOnce.find()){
              loadOnce = true;
            }
            else if(mReloadAlways.find()){
              reloadAlways = true;
            }
            else if(mDescription.find()){
              description = mDescription.group(1).trim();
            }
            else if (mDbms.find()){
              dbms = mDbms.group(1).trim().split(",");
            }
            else if (mVariant.find()){
              variant = mVariant.group(1).trim().split(",");
            }
            else if (mOther.find()) {
              otherDirective = mOther.group(1).trim();
            }
            else if (minvalidDirective.find()) {
              final OnceError error = OnceError.create(EErrorType.UNSUPPORTED_DIRECTIVE_FORMAT, "File: "+file.getPath()+". Error at: "+line+ ". Error type:");
              iErrors.add(error);
            }
          }
        }
      } finally {
        br.close();
      }
    } // else SPLDocuments != null

    final OncePathDeriver oncePathDeriver = new OncePathDeriver(file.getPath());
    final OnceScriptInfo scriptInfo= OnceScriptInfo.create(scriptId, version, oncePathDeriver.getPhase()
        , file, scriptIdRow, scriptIdCol, scriptIdPos
        , noTransFind, dbKinds, loadUnlessLoaded, loadIfLoaded, ignorechecksum
        , loadOnce, reloadAlways, description, dbms, variant, otherDirective, oncePathDeriver.getVariant());

    return scriptInfo;
  }

  /**
   * Zkontroluje syntax cesty k souboru a id obsazene v souboru.
   * dal vyhleda komentar --notrans
   * @throws IOException,OnceScriptException pri parsovaci chybe
   */

  private void _checkScriptInfo(final OnceScriptInfo aScriptInfo)
      throws IOException {
    final OnceScriptData lFile = aScriptInfo.getSourceName();
    final String nameWithoutUsporadavac = extractujSkriptIdZeJmena(lFile);
    //porovnam
    final String scriptId = aScriptInfo.getScriptId();
    if (scriptId != null) {
      final int len = scriptId.length();
      if(!nameWithoutUsporadavac.equalsIgnoreCase(scriptId)){ //ID skriptu
        iErrors.add(OnceError.create(EErrorType.ID_NOT_EQUALS
            ,lFile,aScriptInfo.getScriptIdBeginRow(), aScriptInfo.getScriptIdBeginCol()
            , aScriptInfo.getScriptIdBeginPosition()
            , aScriptInfo.getScriptIdBeginRow(), aScriptInfo.getScriptIdBeginCol() + len
            , aScriptInfo.getScriptIdBeginPosition() + len));
      }
    }
    if(aScriptInfo.getOncePhase() == null){ //onceFaze
      iErrors.add(OnceError.create(EErrorType.BAD_DIR,lFile));
    }
  }

  /**
   * @param lFile
   * @return
   */
  private String extractujSkriptIdZeJmena(final OnceScriptData lFile) {
    return new OncePathDeriver(lFile.getPath()).extractujSkriptIdZeJmena();
  }


  /**
   * Nacte obsah souboru
   * @throws IOException
   */
  protected void _loadContentOfScript() throws IOException{

    if (iFile != null) {
      final BufferedReader br= createReader();
      iParsedScript="";
      String line;
      if(iOnceType==EOnceType.EXTERNAL_EXECUTE){
        while((line=br.readLine())!=null){
          if(startsWithComment(line)) {
            continue;
          }
          parseExecuteLine(line);
        }
      }
      else{
        final StringBuffer buffScript = new StringBuffer();

        while((line=br.readLine())!=null){
          buffScript.append(line).append("\n");
        }
        iParsedScript = buffScript.toString();

      }
      br.close();
    }
  }

  protected static boolean startsWithComment(final String line){
    for(final String c:COMMENTS) {
      if(line.trim().startsWith(c)) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isEmptyLine(final String line){
    if(line.trim().equals("")) {
      return true;
    }
    return false;
  }

  /**
   *
   * @param line
   */

  private void parseExecuteLine(final String line){
    final EExecuteType executeType=EExecuteType.getExecuteTypeFrom(line);
    final int indexOfQueMrk=line.indexOf("?");
    String args=null;
    File command=null;
    String zacatek = line;
    if(indexOfQueMrk > 0){
      args=line.substring(indexOfQueMrk + 1, line.length());
      zacatek=line.substring(0, indexOfQueMrk);
    }
    //prevedeni na absolutni cestu
    command= new OncePathDeriver(iFile.getPath()).odvodExtenniSpoustec(zacatek);
    if(executeType != EExecuteType.UNKNOWN){
      iExecutes.add(new ExecuteElement(executeType,command,args));
    }
  }

  /**
   * Naplni kolekci iCommand SQL statementy Oracle skriptu.
   */
  private void _parseOracleStatements() throws IOException {
    if (iFile != null) {
      final BufferedReader br= createReader();
      final StringBuffer statement = new StringBuffer();
      int lineNo = 0;
      int statementStartLine = 0;

      for (String line = br.readLine(); line != null; line = br.readLine()) {
        lineNo++;
        if(startsWithComment(line) || isEmptyLine(line)) {
          continue;
        }
        if (statementStartLine == 0) {
          statementStartLine = lineNo;
        }
        // ukončení statementu
        if(line.trim().equals("/")){
          iSqlStatements.add(new StatementText(statementStartLine, statement.toString()));
          statementStartLine = 0;
          // vyprázdnění bufferu
          statement.setLength(0);
        }
        else{
          statement.append(" ").append(line).append(" \n");
        }
      }

      if (statement.length()>0) {
        iSqlStatements.add(new StatementText(statementStartLine, statement.toString()));
      }

      br.close();
    }

  }

  /** Odstraní prázdné příkazy. */
  private void _cleanseStatements() {
    for (final Iterator<StatementText> i = iSqlStatements.iterator(); i.hasNext(); ) {
      final StatementText st = i.next();
      if (st.isEmpty()) {
        i.remove();
        log.warn(String.format("IGNORUJI: Prázdný příkaz na ř. %s [%s]", st.getLineInScript(), this.iFile.getPath()));
      }
    }
  }

  public boolean acceptScriptFile(final OnceScriptData aFile){

    boolean accepted = false;
    if (aFile != null) {
      String line;
      try {
        final BufferedReader br= new BufferedReader(new StringReader(aFile.getData()));
        if((line=br.readLine())!=null){
          final Pattern pat_directiveV1 = Pattern.compile("--\\s*V1\\s*,\\s*ID:\\s*\\w*",Pattern.CASE_INSENSITIVE);
          final Matcher mat_directiveV1 =  pat_directiveV1.matcher(line);

          final Pattern pat_directiveV2 = Pattern.compile("--\\s*@TC:ONCE:\\s*ID\\s*=\\s*V2\\s*,.*",Pattern.CASE_INSENSITIVE);
          final Matcher mat_directiveV2 =  pat_directiveV2.matcher(line);

          if(mat_directiveV1.find() || mat_directiveV2.find() ) { // zkusíme nalézt hlavičku na prvním řádku
            accepted = true;
          }
        }

        br.close();


      }
      catch (final IOException e)
      {
        e.printStackTrace();
      }
    }

    return accepted;
  }

  public List<StatementText> getCommandStrings(){
    return iSqlStatements;
  }

  public String[] getDbKinds(){
    return iScriptInfo.getDbKinds();
  }

  public String[] getDbms(){
    return iScriptInfo.getDbms();
  }

  public String getDescription(){
    return iScriptInfo.getDescription();
  }

  public List<ExecuteElement> getExecutes(){
    return iExecutes;
  }

  public OnceScriptData getFile(){
    return iFile;
  }

  public OnceScriptInfo getInfo() {

    return iScriptInfo;
  }

  public String getLoadIfLoaded(){
    return iScriptInfo.getLoadIfLoaded();
  }

  public List<String> getLoadUnlessLoaded(){
    return iScriptInfo.getLoadUnlessLoaded();
  }

  public EOncePhase getOncePhase(){
    return iScriptInfo.getOncePhase();
  }

  public EOnceType getOnceType(){
    return iOnceType;
  }

  public String getOtherDirective(){
    return iScriptInfo.getOtherDirective();
  }

  public String getParsedScript(){
    return iParsedScript;
  }

  public String getScriptId(){
    return iScriptInfo.getScriptId();
  }

  public int getScriptVersion(){
    return iScriptInfo.getScriptVersion();
  }

  public String[] getVariant(){
    return iScriptInfo.getVariant();
  }

  public EVariant getVariantByDirectory(){
    return iScriptInfo.getVariantByDirectory();
  }

  public Boolean isIgnoreChecksum(){
    return iScriptInfo.isIgnoreChecksum();
  }

  public boolean isLoadOnce(){
    return iScriptInfo.isLoadOnce();
  }

  public boolean isNoTransaction(){
    return iScriptInfo.isNoTransFind();
  }

  public boolean isParsed(){
    return iParsed;
  }

  public boolean isReloadAlways(){
    return iScriptInfo.isReloadAlways();
  }

  public boolean isUseBtree(){
    return iUseBtree;
  }

  /**
   * Zkontroluje syntax cesty once skriptu a take jeho obsah
   * @throws OnceScriptException muze vyhodit syntax error a chyby zpusebene kpontrolami
   */
  public void parse() throws OnceScriptException {

    try {

      _checkScriptInfo(iScriptInfo);

      if (iSplDocument == null && !iOnceType.equals(EOnceType.ORA_SCRIPT) && !iOnceType.equals(EOnceType.MYSQL_SCRIPT)) {
        try {
          iSplDocument = COMPILER.getSplDocument(createReader(), iFile.getPath());

        }
        catch(final SyntaxErrorException e){

          iErrors.add(OnceError.create(EErrorType.SYNTAX_ERROR,iFile,e.getTokenLocator()));
        }
        catch(final Exception e){
          iParsingCrashed=true;
          iErrors.add(OnceError.create(EErrorType.UNEXPECTED_PARSE_ERROR,iFile));
        }
      }

      if(iOnceType!=EOnceType.EXTERNAL_EXECUTE && iOnceType!=EOnceType.ORA_SCRIPT && !iOnceType.equals(EOnceType.MYSQL_SCRIPT)){
        _checkContent();
      }

      if(iOnceType==EOnceType.ORA_SCRIPT || iOnceType.equals(EOnceType.MYSQL_SCRIPT)){
        _parseOracleStatements();
        _cleanseStatements();
      }

      _loadContentOfScript();

      if (iOnceType==EOnceType.IFX_SCRIPT && iFile != null) {
        if(iScriptInfo.isNoTransFind()==false){
          iParsedScript=OnceScriptUtils.tryHealNoTranScript(iParsedScript);
        }
      }
      if(iParsingCrashed) {
        //iLogger.info("Spl parsrování se nepovedlo, zkouším skript nasadit jako celek.");
        iErrors.add(OnceError.create(EErrorType.UNEXPECTED_PARSE_ERROR,iFile));
        iSqlStatements.add(new StatementText(1, iParsedScript));
      }
      /*System.out.println("Testovaci vypis parsringu:");
        for(String command:iCommands){
          System.out.println("cmd::"+command);
        }*/
      iParsed=true;
    }
    catch (final Exception e) {

      final OnceError oe = OnceError.create(EErrorType.UNEXPECTED_IO_ERROR, iFile, e);
      iErrors.add(oe);
    }

    if (!iErrors.isEmpty()) {

      throw new OnceScriptException(iErrors);
    }
  }

  /**
   * @return
   * @throws FileNotFoundException
   */
  private BufferedReader createReader() throws FileNotFoundException {
    final BufferedReader br = new BufferedReader(new StringReader(iFile.getData()));
    return br;
  }

  public void specifyDbSpaceByClass(final Class<? extends SplBase0> aClass,final String aDbSpace){
    iDBSCommandType=aClass;
    iDbSpace=aDbSpace;
  }

  public void specifyDbSpaceByCmd(final ECommand aECommand,final String aDbSpace){
    iDBSCommandType=OnceScriptUtils.commandToClass(aECommand);
    iDbSpace=aDbSpace;
  }

  public void setUseBtree(final boolean aUseBtree){
    iUseBtree=aUseBtree;
  }

  public static class StatementText {
    private final int iLineInScript;
    private final String iText;

    public StatementText(final int aLineNo, final CharSequence aText) {
      iLineInScript = aLineNo;
      iText = aText == null ? null : aText instanceof String ? (String)aText : aText.toString();
    }

    public int getLineInScript() {
      return iLineInScript;
    }

    public String getText() {
      return iText;
    }

    public boolean isEmpty() {
      return StringUtils.isBlank(iText);
    }

    @Override
    public String toString() {
      return iLineInScript + ": " + iText;
    }
  }

  public static void main(final String argv[])
      throws IOException{
    //TESTER

    final File aFile = new File("c:/.pracovni/oncetesting/A18511_RN00127397.isql ");
    final String data = FileManager.getInstance(1024 * 64).readWholeFileAsString(aFile);
    final String path = aFile.getAbsolutePath();
    final OnceScript once = OnceScript.getInstance(new OnceScriptData(data, path), false);
    System.out.println("dbobj:"+ !once.typeDispatcher.isOnceScript(new File("c:/.pracovni/tt/mm/getCurrentTime.isql ")));
    try{
      once.specifyDbSpaceByCmd(ECommand.INDEX,"dbindexes");
      once.setUseBtree(true);
      once.parse();

    } catch (final OnceScriptException exc) {

      System.out.println(once.isNoTransaction());
      for(final OnceError err: exc.getErrors()){
        System.out.println(err.getMessage() + " r:" + err.getBeginRow()+ " c:"+ err.getBeginCol());
      }
    }
    catch(final Exception e){
      e.printStackTrace();
    }
    System.out.println("--COMMANDS:--");
    for(final StatementText command : once.getCommandStrings()){
      System.out.println("cmd::"+command);
    }
    System.out.println(once.getOncePhase());
  }

}
