package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.splparser.EStmType;
import cz.tconsult.lib.ifxdbload.core.splparser.SplStatement;
import cz.tconsult.lib.lexer.LexerTokenLocator;

/**
 * @author Veverka, Roman Přichystal
 *
 * Je to prasácky napsaný a možná i zcestně navržený
 * Oddělil bych od sebe tvorbu triggerů (zdrojáků) a zavádění do DB
 * Nejraději bych to deprekoval, ale nemám za co lepšího[polakm;2009-08-26 18:15:50]
 *
 * Třída automaticky aktualizující triggery. Výkonostně je navržena pro vytváření většího počtu triggerů.
 *
 * Příklad použití:
 * <pre>
 *   IfxAutomaticTriggersCreator trigCreateor = new IfxAutomaticTriggersCreator();
 *   Connection conn = DriverManager.getConnection("...");
 *   trigCreateor.init(conn);
 *   trigCreateor.makeTrigger("es_sml_mat", IfxAutomaticTriggersCreator.NODELETE);
 *   trigCreateor.makeTrigger("es_sml_mat", "es_sml_arch",   IfxAutomaticTriggersCreator.ARCHIVE);
 *   trigCreateor.done();
 * </pre>
 */

/**
 * @author jaksik
 *
 */
public class AutomaticTriggersSplStatementGenerator {

  private static final AColumnName REFUSER_COLUMN = AColumnName.from("refuser");
  private static final AColumnName REFTIME_COLUMN = AColumnName.from("reftime");
  private static final AColumnName REFDATPROV_COLUMN = AColumnName.from("refdatprov");
  private static final AColumnName PORTIME_COLUMN = AColumnName.from("portime");
  private static final AColumnName PORUSER_COLUMN = AColumnName.from("poruser");

  private static final ATableName SERNO_EVIDENCE_TABLE = ATableName.from("tw_serno");
  private static final AColumnName SERNO_COLUMN = AColumnName.from("serno");
  private static final AColumnName TABLECOLUMN_SERNOEVIDENCETABLE = AColumnName.from("tabulka");
  private static final String SERNO_SQLTYPENAME_INFORMIX = "bigint";

  public enum EColumnsHandleMode {

    NOTHING, INCLUDE, EXCLUDE
  }

  // **************************************************************************************** //
  // **************************************************************************************** //


  private int pocetVytvorenychInsertTriggeru;
  private int pocetVytvorenychUpdateTriggeru;
  private int pocetVytvorenychDeleteTriggeru;


  private static final Logger log = LoggerFactory.getLogger(AutomaticTriggersSplStatementGenerator.class);

  private final Map<ATableName, Set<AColumnName>> tables2columns;
  private final List<ATableName> bigsernoTables;


  public AutomaticTriggersSplStatementGenerator(final Map<ATableName, Set<AColumnName>> tables2Columns, final List<ATableName> bigsernoTables) {
    this.tables2columns = tables2Columns;
    this.bigsernoTables = bigsernoTables;
  }

  /**
   *  @param astrTableName
   *  			Jméno tabulky bez tečky a bez schématu.
   *  @param aTriggerType
   *  			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   *  @param aTriggerEvent
   *  			Event triggeru
   *  @throws SQLException
   */
  public void makeTrigger(final ATableName astrTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Object aLokace) throws SQLException {
    makeTrigger(astrTableName,aTriggerType, aTriggerEvent,null,EColumnsHandleMode.NOTHING, aLokace);
  } // public boolean makeTrigger

  /**
   * @param astrTableName
   * 			Jméno tabulky bez tečky a bez schématu.
   * @param astrArchivTableName
   * 			Jméno archivní tabulky bez tečky a bez schématu.
   * @param aTriggerType
   * 			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent
   * 			Event triggeru
   * @throws SQLException
   */
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Object aLokace) throws SQLException {
    makeTrigger(astrTableName,astrArchivTableName,aTriggerType, aTriggerEvent,null, aLokace);
  } // public boolean makeTrigger


  /**
   * Vytvoří na tabulce trigger daného typu. Pokud již nějaký trigger daného typu bez ohledu na jméno
   * existuje, trigger bude nejdříve zrušen. Pak bude vytvořen nový trigger.
   * Jméno nového triggeru bude odvozeno od jména tabulky přidáním "_d" pro delete trigger
   * a "_u" pro update triggery.
   *
   * @param astrTableName Jméno tabulky bez tečky a bez schématu.
   * @param aTriggerType Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent Event triggeru
   * @param columnsA Množina sloupců tabulky, při jejichž změně se má/nemá trigger aktivovat
   * @param aMode Jde výčet dvou hodnot ("INCLUDE" a "EXCLUDE")
   *           - říká, zda-li se mají sloupce "columnsA" uplatnit, nebo naopak nemají
   * @param aLokace Objekt, který převeden na String popíše místo definice vytvářeného triggeru.
   * @throws SQLException
   *
   * @author Jan Kalendovský
   */
  public void makeTrigger(final ATableName astrTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsA,final EColumnsHandleMode aMode, final Object aLokace) throws SQLException {
    makeTrigger(astrTableName,null,aTriggerType,aTriggerEvent,columnsA,aMode, aLokace);
  } // public boolean makeTrigger


  /**
   * Vytvoří na tabulce trigger daného typu. Pokud již nějaký trigger daného typu bez ohledu na jméno
   * existuje, trigger bude nejdříve zrušen. Pak bude vytvořen nový trigger.
   * Jméno nového triggeru bude odvozeno od jména tabulky přidáním "_d" pro delete trigger
   * a "_u" pro update triggery.
   *
   * @param astrTableName Jméno tabulky bez tečky a bez schématu.
   * @param astrArchivTableName Jméno archivní tabulky bez tečky a bez schématu.
   *                            Pokud je null, pak se jménoo archivní tabulky vygeneruje
   *                            z <code>astrTableName</code>.
   * @param aTriggerType Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent Event triggeru
   * @param columnsA Množina sloupců tabulky, při jejichž změně se má/nemá trigger aktivovat
   * @param aMode Jde výčet dvou hodnot ("INCLUDE" a "EXCLUDE")
   *           - říká, zda-li se mají sloupce "columnsA" uplatnit, nebo naopak nemají
   * @param aLokace Objekt, který převeden na String popíše místo definice vytvářeného triggeru.
   * @throws SQLException
   *
   * @author Roman Přichystal, Jan Kalendovský
   */
  public SplStatement makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent,
      Set<AColumnName> columnsA,final EColumnsHandleMode aMode, final Object aLokace) {
    checkTableExistence(astrTableName, aLokace);

    ATableName strArchivTableName;

    // Pokud byly zadány sloupce, které se mají exkludovat,
    // tak z nich nejdříve získej sloupce, které se mají inklůdovat.
    if (aMode == EColumnsHandleMode.EXCLUDE) {
      columnsA = getComplementColumns(columnsA,astrTableName);
    } // if (columnsTypeA == COLUMNS_EXCLUDE)

    if (aTriggerType == ETriggerType.ARCH ) {
      if (astrArchivTableName != null) {
        // Je explicitně zadáno jméno archivní tabulky - tak je použijeme.
        strArchivTableName = astrArchivTableName;
      } // if (astrArchivTableName != null)
      else {
        // Jméno archivní tabulky není zadáno - musíme je proto vytvořit.
        // Jméno archivní tabulky se vytvoří z původního jména nahrazením přípony _mat příponou _arch.
        // Záměrně není použit String.replace("_mat", "_arch"), protože pokud by v názvu nebyla přípona _mat,
        // zůstal by název beze změny a došlo by k vytvoření triggeru, který by kopíroval upravovaný
        // řádek tabulky do té stejné tabulky
        // takhle to v daném případě spadne
        strArchivTableName = astrTableName.jmenoArchivniTabulky();
      } // else (astrArchivTableName != null)
      return makeTrigger(astrTableName, strArchivTableName, aTriggerType, aTriggerEvent, columnsA, aLokace);
    } else {
      return makeTrigger(astrTableName, null, aTriggerType, aTriggerEvent, columnsA, aLokace);
    }
  }

  /**
   * Vytvoří na tabulce trigger daného typu. Pokud již nějaký trigger daného typu bez ohledu na jméno existuje, trigger bude
   * nejdříve zrušen. Pak bude vytvořen nový trigger. Jméno nového triggeru bude odvozeno od jména tabulky přidáním "_d" pro
   * delete trigger a "_u" pro update triggery.
   *
   * @param aStrTableName
   *          Jméno tabulky bez tečky a bez schématu.
   * @param aStrArchivTableName
   *          Jméno archivní tabulky bez tečky a bez schématu.
   * @param aTriggerType
   *          Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent
   * 		  Event triggeru
   * @param columnsToIncludeA
   *          Množina sloupců tabulky, při jejichž změně se má trigger aktivovat
   * @param aLokace Objekt, který převeden na String popíše místo definice vytvářeného triggeru.
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  public SplStatement makeTrigger(final ATableName aStrTableName, final ATableName aStrArchivTableName,
      final ETriggerType aTriggerType,final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsToIncludeA, final Object aLokace)
  {
    checkTableExistence(aStrTableName, aLokace);
    if (aStrArchivTableName != null) {
      checkTableExistence(aStrArchivTableName, aLokace);
    }

    // switch podle typu triggeru, který má být vytvořen
    if(aTriggerEvent==ETriggerEvent.ONDELETE){
      if(aTriggerType==ETriggerType.NODELETE){
        return createNoDeleteTrigger(aStrTableName);
      }
      else{
        throw new RuntimeException("Neni podporovana kombinace aut. triggeru event "+aTriggerEvent+" a typ "+aTriggerType);
      }
    }
    else if (aTriggerEvent==ETriggerEvent.ONUPDATE){
      if(aTriggerType==ETriggerType.NOUPDATE){
        return createNoUpdateTrigger(aStrTableName,columnsToIncludeA);
      }
      else if(aTriggerType==ETriggerType.ARCH){
        return createArchiveUpdateTrigger(aStrTableName, aStrArchivTableName);
      }
      else if(aTriggerType==ETriggerType.CURREF){
        return createCurRefUpdateTrigger(aStrTableName);
      }
      else{
        throw new RuntimeException("Neni podporovana kombinace aut. triggeru event "+aTriggerEvent+" a typ "+aTriggerType);
      }

    }
    else if (aTriggerEvent==ETriggerEvent.ONINSERT){
      if(aTriggerType==ETriggerType.CURREF){
        return createCurRefInsertTrigger(aStrTableName);
      }
      else{
        throw new RuntimeException("Neni podporovana kombinace aut. triggeru event "+aTriggerEvent+" a typ "+aTriggerType);
      }

    }
    else{
      throw new RuntimeException("Nevim, co mam delat, protoze event triggeru pro tabulku "+aStrTableName +" je "+aTriggerEvent);
    }
  }


  /*
   * Vrátí doplněk sloupců "columnsA" vůči tabulce "tableA".
   * Tj. Vrátí ty sloupce, které jsou v tabulce "tableA" a nejsou v seznamu "columnsA".
   *
   * @author Jan Kalendovský
   */
  public Set<AColumnName> getComplementColumns(final Set<AColumnName> aInputColumns,
      final ATableName aTableName)  {

    final Set<AColumnName> tableColumns = getTableColumns(aTableName);

    final Set<AColumnName> result = new LinkedHashSet<>();
    for (final AColumnName columnDefA : tableColumns) {
      if (!aInputColumns.contains(columnDefA)) {
        result.add(columnDefA);
      }
    }

    return result;
  }



  /*
   * Zadaný sezmam sloupců převede na string.
   * Výsledný string obsahuje i klíčové slovo "OF". Výsledn string
   * je chápán jako část sql-dotazu pro vytvoření triggeru.
   *
   * @author Jan Kalendovský
   */
  protected String convColumnsToIncludeToString(final Set<AColumnName> columnsToIncludeA) {
    String strColumnsToIncludeA;

    if (columnsToIncludeA == null) {
      strColumnsToIncludeA = "";
    } // if (columnsToIncludeA == null)
    else {
      strColumnsToIncludeA = "OF ";
      for (final AColumnName columnA : columnsToIncludeA) {
        strColumnsToIncludeA += columnA + ",";
      } // for (Iterator i = columnsToIncludeA.iterator();i.hasNext();)
      strColumnsToIncludeA = strColumnsToIncludeA.substring(0,strColumnsToIncludeA.length()-1);
      strColumnsToIncludeA += ' ';
    } // else (columnsToIncludeA == null)

    return strColumnsToIncludeA;
  } // protected String convColumnsToIncludeToString

  /**
   * Vytvoří trigger zabraňující aktualizaci tabulky
   *
   * @param astrTableName Jméno tabulky
   * @param columnsToIncludeA Množina sloupců tabulky, při jejichž změně se má trigger aktivovat
   * @throws SQLException
   *
   * @author Roman Přichystal
   */
  protected SplStatement createNoUpdateTrigger(final ATableName astrTableName,
      final Set<AColumnName> columnsToIncludeA) {
    String toExecute;
    String strColumnsToIncludeA;

    strColumnsToIncludeA = convColumnsToIncludeToString(columnsToIncludeA);

    final ATriggerName triggerName = createTriggerName(astrTableName, ETriggerEvent.ONUPDATE);

    toExecute = " CREATE TRIGGER "  + triggerName
        + " UPDATE " + strColumnsToIncludeA + "ON " + astrTableName
        + " for each row (execute procedure ap_noupdate())";

    //System.out.println(strSQLora);

    pocetVytvorenychUpdateTriggeru++;
    return createSplStatement(toExecute, triggerName);
  }


  /**
   * Vytvoří trigger zabraňující mazání záznamů z tabulky
   *
   * @param astrTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected SplStatement createNoDeleteTrigger(final ATableName astrTableName) {
    String toExecute;

    final ATriggerName triggerName = createTriggerName(astrTableName,  ETriggerEvent.ONDELETE);

    toExecute = " CREATE TRIGGER " + triggerName
        + " DELETE ON " + astrTableName
        + " for each row (execute procedure \"aris\".ap_nodelete())";

    pocetVytvorenychDeleteTriggeru++;

    return createSplStatement(toExecute,triggerName);


  }


  /**
   * Vytvoří trigger aktualizující položky refuser a reftime při aktualizaci tabulky
   *
   * @param aStrTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected SplStatement createCurRefUpdateTrigger(final ATableName aStrTableName) {

    // načti seznam sloupců tabulky
    final Set<AColumnName> columns = getTableColumns(aStrTableName);

    final boolean useRefDatProv = columns.contains(REFDATPROV_COLUMN);

    // odstraň sloupce reftime a refuser, tyto sloupce se do triggeru nezahrnují
    columns.remove(REFTIME_COLUMN);
    columns.remove(REFUSER_COLUMN);
    if (useRefDatProv) {

      columns.remove(REFDATPROV_COLUMN);
    }
    if (!columns.isEmpty()) {
      // sestav string obsahující seznam sloupců tabulky oddělených čárkami
      final String strColumnsList = getOneStringFromListOfStrings(columns);

      final ATriggerName triggerName = createTriggerName(aStrTableName, ETriggerEvent.ONUPDATE);

      String toExecute;

      final String triggerSpec = useRefDatProv
          ? "TW_DatProvCurrRefForTrigOnly() into reftime, refuser, refdatprov"
              : "TW_CurrentRefForTriggerOnly() into reftime, refuser";
      toExecute = String.format(
          " CREATE TRIGGER %s UPDATE OF %s ON %s"
              + " for each row (execute function %s)"
              , triggerName
              , strColumnsList
              , aStrTableName
              , triggerSpec
          );


      //System.err.println(toExecute);
      pocetVytvorenychUpdateTriggeru++;

      return createSplStatement(toExecute, triggerName);
    }

    return null;
  }


  /**
   * Vrací true pokud je tabulka v seznamu tabulek, které mají "bigserno"
   */
  private boolean shouldBeSerno64bitInsertedToEvidenceTable(final ATableName aTableName) {
    return !aTableName.getPureTableName().equals(SERNO_EVIDENCE_TABLE) && bigsernoTables.contains(aTableName);
  }

  /**
   * Vytvoří trigger aktualizující položky refuser a reftime při insertu tabulky
   *
   * @param aTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Přemysl Cimbálek
   */
  protected SplStatement createCurRefInsertTrigger(final ATableName aTableName)  {

    // načti seznam sloupců tabulky
    final Set<AColumnName> columns = getTableColumns(aTableName);
    final boolean useRefDatProv = columns.contains(REFDATPROV_COLUMN);

    final boolean shouldBeSerno64bitInsertedToEvidenceTable = shouldBeSerno64bitInsertedToEvidenceTable(aTableName);

    final ATriggerName triggerName = createTriggerName(aTableName, ETriggerEvent.ONINSERT);

    String toExecute;

    final String triggerSpec = useRefDatProv
        ? "TW_DatProvCurrRefForTrigOnly() into reftime, refuser, refdatprov"
            : "TW_CurrentRefForTriggerOnly() into reftime, refuser";

    toExecute = String.format(
        " CREATE TRIGGER %s INSERT ON %s REFERENCING new as new for each row (execute function %s"
        , triggerName
        , aTableName
        , triggerSpec
        );
    if(columns.contains(PORUSER_COLUMN) && columns.contains(PORTIME_COLUMN)){

      final String porSpec = useRefDatProv
          ? "TW_DatProvCurrRefForTrigOnly() into portime, poruser, pordatprov"
              : "TW_CurrentRefForTriggerOnly() into portime, poruser";

      toExecute += "), (execute function " + porSpec;
    }
    if (shouldBeSerno64bitInsertedToEvidenceTable) {

      toExecute += String.format("), (INSERT INTO %s(%s, %s) VALUES (new.%s, '%s')"
          , SERNO_EVIDENCE_TABLE, SERNO_COLUMN, TABLECOLUMN_SERNOEVIDENCETABLE, SERNO_COLUMN, aTableName);
    }

    toExecute += ")";

    //System.err.println("DBG: " + toExecute);

    pocetVytvorenychInsertTriggeru++;

    return createSplStatement(toExecute, triggerName);
  }


  /**
   * Vytvoří trigger, který při změně tabulky uloží původní hodnoty do archivní tabulky
   *
   * @param aStrTableName Jméno tabulky
   * @param aStrArchivTableName Jméno archivní tabulky
   *
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected SplStatement createArchiveUpdateTrigger(
      final ATableName aStrTableName, final ATableName aStrArchivTableName)  {

    String      strColumnsList, strColumnsListWithPrefixes, strEqualsConditionInWhen, strEqualsConditionInIf, strColumnsListUpdateOfClause;
    Set<AColumnName>   tableColumns, archiveTableColumns, tableColumnsUpdateOfClause;

    // načti seznam sloupců původní a archivní tabulky
    tableColumns = getTableColumns(aStrTableName);
    tableColumnsUpdateOfClause = getTableColumns(aStrTableName);
    archiveTableColumns = getTableColumns(aStrArchivTableName);

    // projdi seznam sloupců původní tabulky a pokud archivní tabulka neobsahuje odpovídající sloupec,
    // odstraň ho ze seznamu
    for (final Iterator<AColumnName> i = tableColumns.iterator(); i.hasNext();) {
      final AColumnName colName = i.next();
      if (!archiveTableColumns.contains(colName)) {
        i.remove();
      }
    }

    final boolean useRefDatProv = tableColumns.contains(REFDATPROV_COLUMN);

    // odstraň sloupce reftime a refuser
    // tyto sloupce se do ON klauzule triggeru nezahrnují
    tableColumnsUpdateOfClause.remove(REFTIME_COLUMN);
    tableColumnsUpdateOfClause.remove(REFUSER_COLUMN);
    if (useRefDatProv) {
      tableColumnsUpdateOfClause.remove(REFDATPROV_COLUMN);
    }

    if (!tableColumnsUpdateOfClause.isEmpty()) {
      // sestav string obsahující seznam sloupců tabulky pro klauzuli UPDATE OF oddělených čárkami
      strColumnsListUpdateOfClause = getOneStringFromListOfStrings(tableColumnsUpdateOfClause);
      // sestav string obsahující seznam sloupců tabulky oddělených čárkami
      strColumnsList = getOneStringFromListOfStrings(tableColumns);
      // sestav string obsahující seznam sloupců tabulky oddělených čárkami a s prefixem
      strColumnsListWithPrefixes = getOneStringFromListOfStrings(tableColumns, "pre");
      // v Oracle ve "when" části se ":" u prefixů nepoužívá, jinak ano
      strEqualsConditionInWhen = getEqualsCondition(tableColumnsUpdateOfClause, "pre", "new");
      strEqualsConditionInIf = getEqualsCondition(tableColumnsUpdateOfClause, ":pre", ":new");

      final ATriggerName triggerName = createTriggerName(aStrTableName, ETriggerEvent.ONUPDATE);

      String toExecute;


      final String triggerSpec = useRefDatProv
          ? "TW_DatProvCurrRefForTrigOnly() into reftime, refuser, refdatprov"
              : "TW_CurrentRefForTriggerOnly() into reftime, refuser"
                ;
      toExecute = String.format(
          " CREATE TRIGGER %s UPDATE OF %s ON %s referencing new as new old as pre for each row"
              + " WHEN (%s) (insert into %s(%s) values(%s)"
              , triggerName
              , strColumnsListUpdateOfClause
              , aStrTableName
              , strEqualsConditionInWhen
              , aStrArchivTableName
              , strColumnsList
              , strColumnsListWithPrefixes
          );

      /*
      if (isThereRefTimeAndRefUser(aStrTableName, useRefDatProv)) {

        toExecute += ", execute function " + triggerSpec;
      }
       */
      toExecute += ")";

      //System.err.println(toExecute);
      pocetVytvorenychUpdateTriggeru++;

      return createSplStatement(toExecute, triggerName);
    }

    return null;

  }


  /**
   * Zjistí, zda-li tabulka <code>astrTableName</code> obsahuje
   * sloupečky <code>reftime</code> a <code>refuser</code>.
   * Pokud je oba dva obsahuje, pak vrátí <code>true</code>,
   * jinak vrátí <code>false</code>.
   *
   * @author Jan Kalendovský
   */
  boolean isThereRefTimeAndRefUser(final ATableName aStrTableName, final boolean aUseRefDatProv) throws SQLException {

    String sql;
    final String tableName = aStrTableName.getPureTableName();

    final List<String> columnsToSearch = new ArrayList<>(3);
    columnsToSearch.add(REFUSER_COLUMN.toString().toLowerCase());
    columnsToSearch.add(REFTIME_COLUMN.toString().toLowerCase());
    if (aUseRefDatProv) {

      columnsToSearch.add(REFDATPROV_COLUMN.toString().toLowerCase());
    }

    sql = "SELECT * FROM syscolumns a,systables b" +
        "  WHERE b.tabname = '" + tableName.toLowerCase() +
        "' AND a.tabid = b.tabid" +
        "  AND a.colname = ?";


    /*
    try (PreparedStatement pstm = conn.prepareStatement(sql)) {

      for (final String columnName : columnsToSearch) {


        pstm.setString(1, columnName);
        final ResultSet rs = pstm.executeQuery();
        if (!rs.next()) { return false; }
      }

      return true;
    } // try

     */

    return false;

  } // boolean isThereRefTimeAndRefUser



  /**
   * Za zadaného seznamu sestaví řetězec, který obsahuje položky seznamu oddělené čárkami
   *
   * @param aList Seznam položek typu String
   * @return Řetězec sestavený z položek seznamu oddělených čárkami
   *
   * @author Roman Přichystal
   */

  protected String getOneStringFromListOfStrings(final Collection<AColumnName> aList) {
    return getOneStringFromListOfStrings(aList, null);
  }


  /**
   * Za zadaného seznamu sestaví řetězec, který obsahuje položky seznamu oddělené čárkami
   *
   * @param aList Seznam položek typu String
   * @param astrPrefix Prefix, který bude přidán před každou položku
   * @return Řetězec sestavený z položek seznamu oddělených čárkami
   *
   * @author Roman Přichystal
   */

  protected String getOneStringFromListOfStrings(final Collection<AColumnName> aList, final String astrPrefix) {
    String strList = "";
    // projdi zadaný seznam
    for (final Iterator<AColumnName> i = aList.iterator(); i.hasNext();) {
      // před každou položku vlož prefix, pokud je zadaný
      if (astrPrefix != null) {
        strList += astrPrefix + ".";
      }
      // vlož položku do řetězce
      strList = strList + i.next();
      // položky jsou odděleny čárkou, za poslední položkou čárka není
      if (i.hasNext()) {
        strList += ", ";
      }
    }
    return strList;
  }

  protected String getEqualsCondition(final Collection<AColumnName> aList, final String astrPrefix1, final String astrPrefix2) {
    final StringBuffer sb = new StringBuffer();

    // projdi zadaný seznam
    for (final Iterator<AColumnName> i = aList.iterator(); i.hasNext();) {
      final AColumnName polozka = i.next();

      sb.append("(");
      if (astrPrefix1 != null) {
        sb.append(astrPrefix1).append(".");
      }
      // vlož položku do řetězce
      sb.append(polozka);

      sb.append(" is null AND ");

      if (astrPrefix2 != null) {
        sb.append(astrPrefix2).append(".");
      }
      sb.append(polozka);

      sb.append(" is not null)");

      /////
      sb.append(" OR (");

      if (astrPrefix1 != null) {
        sb.append(astrPrefix1).append(".");
      }
      // vlož položku do řetězce
      sb.append(polozka);

      sb.append(" is not null AND ");

      if (astrPrefix2 != null) {
        sb.append(astrPrefix2).append(".");
      }
      sb.append(polozka);

      sb.append(" is null)");

      /////
      sb.append(" OR (");

      if (astrPrefix1 != null) {
        sb.append(astrPrefix1).append(".");
      }
      sb.append(polozka);

      sb.append(" != ");

      if (astrPrefix2 != null) {
        sb.append(astrPrefix2).append(".");
      }
      sb.append(polozka);
      sb.append(")");

      // položky jsou odděleny "AND", za poslední položkou AND není
      if (i.hasNext()) {
        sb.append(" OR ");
      }
    }

    return sb.toString();
  }

  /**
   * Vrátí seznam sloupců zadané tabulky. Seznam načte ze systémové tabulky syscolumns.
   *
   * @param astrTableName Jméno tabulky
   * @return Seznam sloupců zadané tabulky
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected Set<AColumnName> getTableColumns(final ATableName astrTableName)  {

    /*
    if (astrTableName == null) {
      return Collections.EMPTY_SET;
    }
     */

    final Set<AColumnName> columns = getColumnsForTable(astrTableName);

    if (columns.isEmpty()) {
      throw new RuntimeException("Table '" + astrTableName + "' does not exists or has no columns");
    }
    return columns;
  }

  private SplStatement createSplStatement(final String aSqlCommand, final ATriggerName aTriggerName){

    //log.debug("----------------------------------" + System.getProperty("line.separator") + aSqlCommand);
    log.debug(aSqlCommand);

    final SplStatement stmt = new SplStatement(null, EStmType.TRIGGER, aTriggerName.toString(), aSqlCommand, aSqlCommand, new LexerTokenLocator() {

      @Override
      public String getInputSourceName() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return "FIXME";
      }

      @Override
      public int getEndPosition() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }

      @Override
      public int getEndLineNumber() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }

      @Override
      public int getEndColumnNumber() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }

      @Override
      public int getBegPosition() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }

      @Override
      public int getBegLineNumber() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }

      @Override
      public int getBegColumnNumber() {
        //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 10:00:58]
        return 0;
      }
    });
    return stmt;

    /*
    final Statement stmt = conn.createStatement();
    try {
      //System.out.println("Executed: " + aSqlCommand);
      stmt.execute(aSqlCommand);
    } catch (final SQLException e) {
      throw new IfxSqlException(e, conn, aSqlCommand);
    }
    stmt.close();
     */

  }

  /*
  public void done() throws SQLException  {
    conn = null;
  }
   */

  /**
   * Vrátí jméno triggeru podle zadaného typu a jména tabulky.
   *
   * @param astrTableName jméno tabulky
   * @param aTriggerEvent jméno triggeru
   * @return Jméno triggeru
   *
   * @author Roman Přichystal
   */
  public static ATriggerName createTriggerName(final ATableName astrTableName, final ETriggerEvent aTriggerEvent) {
    final ATableName tableName=astrTableName;
    if(ETriggerEvent.ONDELETE==aTriggerEvent){
      return ATriggerName.from(tableName + "_d");
    }
    if(ETriggerEvent.ONINSERT==aTriggerEvent){
      return ATriggerName.from(tableName + "_i");
    }
    if(ETriggerEvent.ONUPDATE==aTriggerEvent){
      return ATriggerName.from(tableName + "_u");
    }
    return null;
  }




  /**
   * Vrátí typ triggeru podle zadaného jména. Pokud je zadané jméno chybné, vrátí 0;
   *
   * @param astrTriggerName Jméno triggeru
   * @return Typ triggeru
   *
   * @author Roman Přichystal
   */
  public static ETriggerType getTriggerTypeFromName (final String astrTriggerName) {
    if (astrTriggerName.equals("NOUPDATE")) {
      return ETriggerType.NOUPDATE;
    } else
      if (astrTriggerName.equals("NODELETE")) {
        return ETriggerType.NODELETE;
      } else
        if (astrTriggerName.equals("CURREF")) {
          return ETriggerType.CURREF;
        } else
          if (astrTriggerName.equals("ARCH")) {
            return ETriggerType.ARCH;
          } else
            if (astrTriggerName.equals("-") || astrTriggerName.equals("NONE")) {
              return ETriggerType.NONE;
            } else {
              return null;
            }
  }

  /**
   * @return počet vytvorenych delete triggerů
   */
  public int getPocetVytvorenychDeleteTriggeru() {
    return pocetVytvorenychDeleteTriggeru;
  }

  /**
   * @return počet vytvorenych insert triggerů
   */
  public int getPocetVytvorenychInsertTriggeru() {
    return pocetVytvorenychInsertTriggeru;
  }

  /**
   * @return počet vytvorenych update triggerů
   */
  public int getPocetVytvorenychUpdateTriggeru() {
    return pocetVytvorenychUpdateTriggeru;
  }

  private void checkTableExistence(final ATableName astrTableName, final Object aLokace) {
    final Set<AColumnName> columns = getColumnsForTable(astrTableName);
    if (columns == null) {
      throw new RuntimeException("Table '" + astrTableName + "' does not exists" + (aLokace == null ? "" : " in \"" + aLokace + "\""));
    }
  }


  private Set<AColumnName> getColumnsForTable(final ATableName aQualifiedTableName) {
    final Set<AColumnName> cols = tables2columns.get(aQualifiedTableName);

    @SuppressWarnings("unchecked")
    final Set<AColumnName> result = cols == null ? Collections.EMPTY_SET : new LinkedHashSet<>(cols);
    return result;

  }

  public SplStatement generate(final AutoTrigger trigger) {
    return makeTrigger(
        trigger.getTableNameA(),
        trigger.getArchTableNameA(),
        trigger.getTriggerType(),
        trigger.getTriggerEvent(),
        null,
        EColumnsHandleMode.NOTHING,
        null);
  }

}



/*

-- Priklady 4 typu generovaných triggerů


--NOUPDATE
-- Trigger zabraňuje aktualizaci tabulky
CREATE TRIGGER rz_real_arch_u UPDATE ON rz_real_arch
for each row (execute procedure ap_noupdate())


--NODELETE
-- Trigger zabraňuje zrušení řádků z tabulky
--
#TRIGGER rz_real_mat_d DELETE ON rz_real_mat
for each row(execute procedure "aris".ap_nodelete())
#END


-- CURREF
-- Trigger nastavuje sloupce reftime a refuser. Je vytvářen nad všemi sloupci,
-- kromě modifikovaných, tedy kromě reftime, refuser
--
CREATE TRIGGER rz_hrstorno_den_u UPDATE OF smlouva,tsml,prov,sqlcode,isamcode,sqltext
ON rz_hrstorno_den
for each row(execute function ap_currentref() into reftime, refuser)


--ARCH
-- Trigger zajišťuje při aktualizaci tabulky uložení původních hodnot
-- do archivní tabulky a také aktualizacu reftime a refuser
-- je spouštěn při aktualizaci kteréhokoli sloupce, kromě refuser, reftime
--
CREATE TRIGGER rz_real_mat_u UPDATE OF
akce,datakce,mp,rdz,davka,real,treal,kodakce,smlouva,tsml,stavreal,prosp,budsp,
datukoncsp,duvodukoncsp,serspstop,plrs,plru,plrp,plrv,reals,
realsu,realpu,realvu,dolozucel ON rz_real_mat
referencing old as pre for each row
(insert into rz_real_arch
  (real,treal,smlouva,tsml,davka,stavreal,akce,kodakce,datakce,rdz,mp,prosp,budsp,
   datukoncsp,duvodukoncsp,serspstop,plrs,plru,plrp,reals,
   realsu,realpu,dolozucel,refuser,reftime)values(pre.real,pre.treal,pre.smlouva,
   pre.tsml,pre.davka,pre.stavreal,pre.akce,pre.kodakce,pre.datakce,pre.rdz,
   pre.mp,pre.prosp,pre.budsp,pre.datukoncsp,pre.duvodukoncsp,pre.serspstop,pre.plrs,
   pre.plru,pre.plrp,pre.reals,pre.realsu,pre.realpu,
   pre.dolozucel,pre.refuser,pre.reftime),
   execute function ap_currentref() into reftime, refuser
)

-- ARCH s explicitním testem změny
-- po přechroustání informixem
create trigger "aris".oos_xskupinaoz_mat_u update of serno,refskupinaoz,datod,datdo,aktivita,refter,vykonyter,refsmlvztah,minhodnotabodu,platnost
  on "aris".oos_xskupinaoz_mat referencing old as pre new as nxt                                                    for each row
        when ((((((((((((((((((((((((((((((((pre.serno IS NULL ) AND (nxt.serno IS NOT NULL ) ) OR ((pre.serno IS NOT NULL ) AND (nxt.serno IS NULL ) ) ) OR (pre.serno != nxt.serno ) )
          OR ((pre.refskupinaoz IS NULL ) AND (nxt.refskupinaoz IS NOT NULL ) ) ) OR ((pre.refskupinaoz IS NOT NULL ) AND (nxt.refskupinaoz IS NULL ) ) ) OR (pre.refskupinaoz != nxt.refskupinaoz ) )
          OR ((pre.datod IS NULL ) AND (nxt.datod IS NOT NULL ) ) ) OR ((pre.datod IS NOT NULL ) AND (nxt.datod IS NULL ) ) ) OR (pre.datod != nxt.datod ) )
          OR ((pre.datdo IS NULL ) AND (nxt.datdo IS NOT NULL ) ) ) OR ((pre.datdo IS NOT NULL ) AND (nxt.datdo IS NULL ) ) ) OR (pre.datdo != nxt.datdo ) )
          OR ((pre.aktivita IS NULL ) AND (nxt.aktivita IS NOT NULL ) ) ) OR ((pre.aktivita IS NOT NULL ) AND (nxt.aktivita IS NULL ) ) ) OR (pre.aktivita != nxt.aktivita ) )
          OR ((pre.refter IS NULL ) AND (nxt.refter IS NOT NULL ) ) ) OR ((pre.refter IS NOT NULL ) AND (nxt.refter IS NULL ) ) ) OR (pre.refter != nxt.refter ) )
          OR ((pre.vykonyter IS NULL ) AND (nxt.vykonyter IS NOT NULL ) ) ) OR ((pre.vykonyter IS NOT NULL ) AND (nxt.vykonyter IS NULL ) ) ) OR (pre.vykonyter != nxt.vykonyter ) )
          OR ((pre.refsmlvztah IS NULL ) AND (nxt.refsmlvztah IS NOT NULL ) ) ) OR ((pre.refsmlvztah IS NOT NULL ) AND (nxt.refsmlvztah IS NULL ) ) ) OR (pre.refsmlvztah != nxt.refsmlvztah ) )
          OR ((pre.minhodnotabodu IS NULL ) AND (nxt.minhodnotabodu IS NOT NULL ) ) ) OR ((pre.minhodnotabodu IS NOT NULL ) AND (nxt.minhodnotabodu IS NULL ) ) ) OR (pre.minhodnotabodu != nxt.minhodnotabodu ) )
          OR ((pre.platnost IS NULL ) AND (nxt.platnost IS NOT NULL ) ) ) OR ((pre.platnost IS NOT NULL ) AND (nxt.platnost IS NULL ) ) ) OR (pre.platnost != nxt.platnost ) ) )
            (
            insert into "aris".oos_xskupinaoz_arch (serno,refskupinaoz,datod,datdo,aktivita,refter,vykonyter,refsmlvztah,minhodnotabodu,platnost,refuser,reftime)  values (pre.serno ,pre.refskupinaoz ,pre.datod ,pre.datdo ,pre.aktivita ,pre.refter ,pre.vykonyter ,pre.refsmlvztah ,pre.minhodnotabodu ,pre.platnost ,pre.refuser ,pre.reftime ),
            execute function "aris".tw_currentreffortriggeronly() into "aris".oos_xskupinaoz_mat.reftime,"aris".oos_xskupinaoz_mat.refuser);


 */
