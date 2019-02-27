package cz.tconsult.CORE_REVIDOVAT.dbutil.informix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.CORE_REVIDOVAT.dbutil.core.AColumnName;
import cz.tconsult.CORE_REVIDOVAT.dbutil.core.ATableName;
import cz.tconsult.CORE_REVIDOVAT.dbutil.core.ATriggerName;
import cz.tconsult.CORE_REVIDOVAT.dbutil.informix.atom.ETriggerEvent;
import cz.tconsult.CORE_REVIDOVAT.dbutil.informix.atom.ETriggerType;
import cz.tconsult.CORE_REVIDOVAT.dbutil.informix.catalog.CatalogColumnsHelper;

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

public class IfxAutomaticTriggersCreator {

  private static final AColumnName REFUSER_COLUMN = AColumnName.from("refuser");
  private static final AColumnName REFTIME_COLUMN = AColumnName.from("reftime");
  private static final AColumnName REFDATPROV_COLUMN = AColumnName.from("refdatprov");
  private static final AColumnName NAVTIME_COLUMN = AColumnName.from("navtime");
  private static final AColumnName NAVUSER_COLUMN = AColumnName.from("navuser");
  private static final AColumnName PORTIME_COLUMN = AColumnName.from("portime");
  private static final AColumnName PORUSER_COLUMN = AColumnName.from("poruser");

  private static final ATableName SERNO_EVIDENCE_TABLE = ATableName.from("tw_serno");
  private static final AColumnName SERNO_COLUMN = AColumnName.from("serno");
  private static final AColumnName TABLECOLUMN_SERNOEVIDENCETABLE = AColumnName.from("tabulka");
  private static final String SERNO_SQLTYPENAME_INFORMIX = "bigint";
  private static final String SERNO_SQLTYPENAME_ORACLE = "n/a";

  public enum EColumnsHandleMode {

    NOTHING, INCLUDE, EXCLUDE
  }

  // **************************************************************************************** //
  // **************************************************************************************** //


  private Set<ATableName> iJmenaTabulkeProNejzSenecoDeloSTriggery;
  private int pocetDropnutychTriggeru;
  private int pocetVytvorenychInsertTriggeru;
  private int pocetVytvorenychUpdateTriggeru;
  private int pocetVytvorenychDeleteTriggeru;


  private static final Logger log = LoggerFactory.getLogger(IfxAutomaticTriggersCreator.class);

  protected Connection conn;

  private Map<ATableName, Set<AColumnName>> iTablesToColumns;
  private Map<ATableName, Set<ATriggerName>> iTablesToTriggers;
  private boolean iForOracle;

  /**
   * Dřív jsem měl místo Set<AColumnName> celou strukturu Set<ColumnDef> ve které byl i datový typ.
   * Ale to se neukázalo jako dobré řešení, protože existuje nástroj, který čte XML, ve kterém jsou
   * pouze názvy sloupců a ne jejich datové typy. A pak jsem měl tedy problém, protože se seznam sloupce
   * tahá tam i zpět z této sdílené knihovny.
   * Takže si datové typy vytvářím pouze pro účely této knihovny.[polakm;2014-06-25 16:47:30]
   */
  private Map<ATableName, Map<AColumnName, String>> iTable2Column2DataType;

  //[polakm;2009-08-26 17:41:52] Zda nad danou tabulkou promazat všechny triggery. V opačném případě jen ten, který bude zaváděn
  private boolean iDropAllTriggers;

  private void _addColumnType(final Map<ATableName, Map<AColumnName, String>> aStorage
      , final ATableName aATableName, final AColumnName aAColumnName, final String aDataType) {

    Map<AColumnName, String> m = aStorage.get(aATableName);
    if (m == null) {

      m = new LinkedHashMap<>();
      aStorage.put(aATableName, m);
    }
    m.put(aAColumnName, aDataType);
  }

  private void loadTablesColumns() throws SQLException {
    // Načti seznam
    final Map<ATableName, Set<AColumnName>> tablesToColumns = new HashMap<ATableName, Set<AColumnName>>();
    final Map<ATableName, Map<AColumnName, String>> t2c2t = new LinkedHashMap<>();
    PreparedStatement stmt=null;
    if(iForOracle){
      //LATER Pozor pro oracle se neda dat do updateof klauzule [C|B atd.]LOB sloupecky
      //proto je to osetreno tady v tomto sloupecku. Pozor na to.
      stmt = conn.prepareStatement(

          //LATER Pro sqlDataType typu DATE asi závorky nechceme vůbec.....[polakm;2014-06-23 18:31:39]
          "select distinct USER, a.TABLE_NAME, a.OWNER, a.COLUMN_NAME"
          + ", a.DATA_TYPE || '(' || NVL2(a.DATA_PRECISION, a.DATA_PRECISION || ',' || a.DATA_SCALE, NVL2(a.CHAR_USED, a.CHAR_LENGTH, a.DATA_LENGTH)) || ')' sqlDataType"
          + " from dba_tab_columns a"
          + " where a.owner in ('ELBOS','ARIS','STAT','TEST','STATREKONS')"
          + " and a.DATA_TYPE not in ('BLOB','CLOB','NCLOB','BFILE')"
          );
    }
    else{
      stmt = conn.prepareStatement(
          "select USER, t.tabname, t.owner, c.colname, c.coltype, c.collength"
              + " from systables t"
              + " join syscolumns c on c.tabid = t.tabid"
              + " where t.tabid > 99");
    }
    final ResultSet rset = stmt.executeQuery();
    // projdi vrácený ResultSet a vlož hodnoty do listu

    int n = 0;
    while (rset.next()) {

      final String prihlasenec = rset.getString(1).toLowerCase();
      final String tabName = rset.getString(2).toLowerCase();
      final String owner = rset.getString(3).toLowerCase();
      final AColumnName columnName = AColumnName.from(rset.getString(4));
      String sqlColumnType;
      if(iForOracle){

        sqlColumnType = rset.getString(5).toLowerCase().trim();
      }
      else {

        final int coltype = rset.getInt(5);
        final int collength  = rset.getInt(6);
        try {
          sqlColumnType = CatalogColumnsHelper.getColumnSqlType(coltype, collength);
        }
        catch (final Exception e) {

          throw new RuntimeException("Sloupec " + tabName + "." + columnName, e);
        }
      }

      {
        final ATableName tabname = ATableName.from(owner + "." + tabName);
        addDvojKlicovecDoStruktury(tablesToColumns, tabname, columnName);
        _addColumnType(t2c2t, tabname, columnName, sqlColumnType);
      }
      if (prihlasenec.equals(owner)) { // dat to sem nekvalifikovane
        final ATableName tabname = ATableName.from(tabName);
        addDvojKlicovecDoStruktury(tablesToColumns, tabname, columnName);
        _addColumnType(t2c2t, tabname, columnName, sqlColumnType);
      }
      n++;
    }
    rset.close();
    stmt.close();
    log.info("From database was loaded " + n + " columns from " + tablesToColumns.size() + " tables");

    iTablesToColumns = tablesToColumns;
    iTable2Column2DataType = t2c2t;
  }


  private void loadTablesTriggers() throws SQLException {
    // Načti seznam
    final Map<ATableName, Set<ATriggerName>> tablesToTriggers = new HashMap<ATableName, Set<ATriggerName>>();
    PreparedStatement stmt=null;
    if(iForOracle) {
      stmt = conn.prepareStatement(
          "select distinct a.table_name,b.trigger_name"
              + " from DBA_TABLES a inner join dba_triggers b on a.table_name=b.table_name"
          );
    }
    else{
      stmt = conn.prepareStatement(
          " select tabname, trigname "
              + " from systables, systriggers "
              + " where systables.tabid = systriggers.tabid ");
    }

    final ResultSet rset = stmt.executeQuery();
    // projdi vrácený ResultSet a vlož hodnoty do listu

    int n=0;
    while (rset.next()) {
      final ATableName tabname = ATableName.from(rset.getString(1).toLowerCase());
      final ATriggerName trigname = ATriggerName.from(rset.getString(2).toLowerCase());
      addDvojKlicovecDoStruktury(tablesToTriggers, tabname, trigname);
      n++;
    }
    rset.close();
    stmt.close();
    log.info("From database was loaded " + n + " triggers from " + tablesToTriggers.size() + " tables");
    iTablesToTriggers = tablesToTriggers;
  }

  /**
   * Inicializace objektu
   * @param aConnection Spojení na databázi
   * pouzijte metodu se specifikovanim cilove databaze
   */
  @Deprecated
  public void init(final Connection aConnection) {
    init(aConnection,false,true);
  }

  public void init(final Connection aConnection, final boolean aForOracle, final boolean aDropAllTriggers){
    iForOracle=aForOracle;
    conn = aConnection;
    iDropAllTriggers = aDropAllTriggers;
    reinit();
  }

  /**
   *
   */
  public void reinit() {
    pocetDropnutychTriggeru = 0;
    pocetVytvorenychDeleteTriggeru = 0;
    pocetVytvorenychInsertTriggeru = 0;
    pocetVytvorenychUpdateTriggeru = 0;
    iJmenaTabulkeProNejzSenecoDeloSTriggery = new HashSet<ATableName>();
    iTablesToColumns = null;
    iTablesToTriggers = null;
  }

  /** @deprecated Použijte makeTrigger(String astrTableName, int aintTriggerType, Object aLokace)
   *  @param astrTableName
   *  			Jméno tabulky bez tečky a bez schématu.
   *  @param aTriggerType
   *  			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   *  @param aTriggerEvent
   *  			Event triggeru
   *  @throws SQLException
   */
  @Deprecated
  public void makeTrigger(final ATableName astrTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent) throws SQLException {
    makeTrigger(astrTableName, aTriggerType, aTriggerEvent, (Object)null);
  }
  public void makeTrigger(final ATableName astrTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Object aLokace) throws SQLException {
    makeTrigger(astrTableName,aTriggerType, aTriggerEvent,null,EColumnsHandleMode.NOTHING, aLokace);
  } // public boolean makeTrigger

  /** @deprecated makeTrigger(String astrTableName, String astrArchivTableName, int aintTriggerType, Object aLokace)
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
  @Deprecated
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent) throws SQLException {
    makeTrigger(astrTableName, astrArchivTableName, aTriggerType, aTriggerEvent, (Object)null);
  }
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Object aLokace) throws SQLException {
    makeTrigger(astrTableName,astrArchivTableName,aTriggerType, aTriggerEvent,null, aLokace);
  } // public boolean makeTrigger

  /** @deprecated makeTrigger(String, int, Set,int, Object)
   * @param astrTableName
   * 			Jméno tabulky bez tečky a bez schématu.
   * @param aTriggerType
   * 			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent
   * 			Event triggeru
   * @param columnsA
   * 			Množina sloupců tabulky, při jejichž změně se má/nemá trigger aktivovat
   * @param aMode
   * 			Jde výčet dvou hodnot ("INCLUDE" a "EXCLUDE")
   *           - říká, zda-li se mají sloupce "columnsA" uplatnit, nebo naopak nemají
   * @throws SQLException
   */
  @Deprecated
  public void makeTrigger(final ATableName astrTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsA,final EColumnsHandleMode aMode) throws SQLException {
    makeTrigger(astrTableName, aTriggerType, aTriggerEvent, columnsA,aMode, (Object)null);
  }

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


  /** @deprecated použijte makeTrigger(String, String, int, Set, int, Object)
   * @param astrTableName
   * 			Jméno tabulky bez tečky a bez schématu.
   * @param astrArchivTableName
   * 			Jméno archivní tabulky bez tečky a bez schématu.
   * @param aTriggerType
   * 			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent
   * 			Event triggeru
   * @param columnsA
   * 			 Množina sloupců tabulky, při jejichž změně se má/nemá trigger aktivovat
   * @param aMode
   * 			Jde výčet dvou hodnot ("INCLUDE" a "EXCLUDE")
   *           - říká, zda-li se mají sloupce "columnsA" uplatnit, nebo naopak nemají
   * @throws SQLException
   */
  @Deprecated
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType,final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsA,final EColumnsHandleMode aMode) throws SQLException {
    makeTrigger(astrTableName, astrArchivTableName, aTriggerType, aTriggerEvent, columnsA,aMode, null);
  }

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
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent,
      Set<AColumnName> columnsA,final EColumnsHandleMode aMode, final Object aLokace) throws SQLException {
    checkTableExistence(astrTableName, aLokace);

    iJmenaTabulkeProNejzSenecoDeloSTriggery.add(astrTableName);
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
      makeTrigger(astrTableName, strArchivTableName, aTriggerType, aTriggerEvent, columnsA, aLokace);
    } else {
      makeTrigger(astrTableName, null, aTriggerType, aTriggerEvent, columnsA, aLokace);
    }
  }

  /**
   * @deprecated Použijte makeTrigger(String, String, int, Set, Object )
   * @param astrTableName
   * 			Jméno tabulky bez tečky a bez schématu.
   * @param astrArchivTableName
   * 			Jméno archivní tabulky bez tečky a bez schématu.
   * @param aTriggerType
   * 			Typ triggeru, jedna z hodnot [NOUPDATE, NODELETE, CURREF, ARCHIVE]
   * @param aTriggerEvent
   * 			Event triggeru
   * @param columnsToIncludeA
   * 			Množina sloupců tabulky, při jejichž změně se má trigger aktivovat
   * @throws SQLException
   */
  @Deprecated
  public void makeTrigger(final ATableName astrTableName, final ATableName astrArchivTableName, final ETriggerType aTriggerType, final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsToIncludeA) throws SQLException {
    makeTrigger(astrTableName, astrArchivTableName, aTriggerType, aTriggerEvent, columnsToIncludeA, null);
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

  public void makeTrigger(final ATableName aStrTableName, final ATableName aStrArchivTableName,
      final ETriggerType aTriggerType,final ETriggerEvent aTriggerEvent, final Set<AColumnName> columnsToIncludeA, final Object aLokace)
          throws SQLException {
    checkTableExistence(aStrTableName, aLokace);
    if (aStrArchivTableName != null) {
      checkTableExistence(aStrArchivTableName, aLokace);
    }
    iJmenaTabulkeProNejzSenecoDeloSTriggery.add(aStrTableName);

    // zruš všechny triggery, pokud ještě nebyly zrušeny
    if(iDropAllTriggers && !iForOracle) {
      dropTriggersIfNotDropedYet(aStrTableName);
    }

    // switch podle typu triggeru, který má být vytvořen
    if(aTriggerEvent==ETriggerEvent.ONDELETE){
      if(aTriggerType==ETriggerType.NODELETE){
        createNoDeleteTrigger(aStrTableName);
      }
      else{
        throw new RuntimeException("Neni podporovana kombinace aut. triggeru event "+aTriggerEvent+" a typ "+aTriggerType);
      }
    }
    else if (aTriggerEvent==ETriggerEvent.ONUPDATE){
      if(aTriggerType==ETriggerType.NOUPDATE){
        createNoUpdateTrigger(aStrTableName,columnsToIncludeA);
      }
      else if(aTriggerType==ETriggerType.ARCH){
        createArchiveUpdateTrigger(aStrTableName, aStrArchivTableName);
      }
      else if(aTriggerType==ETriggerType.CURREF){
        createCurRefUpdateTrigger(aStrTableName);
      }
      else{
        throw new RuntimeException("Neni podporovana kombinace aut. triggeru event "+aTriggerEvent+" a typ "+aTriggerType);
      }

    }
    else if (aTriggerEvent==ETriggerEvent.ONINSERT){
      if(aTriggerType==ETriggerType.CURREF){
        createCurRefInsertTrigger(aStrTableName);
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
      final ATableName aTableName) throws SQLException {

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

  private void _dropTriggerIfRequired(final ATableName aTableName, final ATriggerName aTriggerName) throws SQLException {

    if (aTableName == null) {return;}
    if (aTriggerName == null) {return;}
    if (!iDropAllTriggers) {

      final Set<ATriggerName> triggerNames = getTablesToTriggersx().get(aTableName);
      if (triggerNames != null) {
        if (triggerNames.contains(aTriggerName)) {

          dropTrigger(aTableName.getSchemaWithDot() +aTriggerName);
        }
      }
    }
  }

  /**
   * Vytvoří trigger zabraňující aktualizaci tabulky
   *
   * @param astrTableName Jméno tabulky
   * @param columnsToIncludeA Množina sloupců tabulky, při jejichž změně se má trigger aktivovat
   * @throws SQLException
   *
   * @author Roman Přichystal
   */
  protected void createNoUpdateTrigger(final ATableName astrTableName,
      final Set<AColumnName> columnsToIncludeA) throws SQLException {
    String strSQL,strSQLora,toExecute;
    String strColumnsToIncludeA;

    strColumnsToIncludeA = convColumnsToIncludeToString(columnsToIncludeA);

    final ATriggerName triggerName = createTriggerName(astrTableName, ETriggerEvent.ONUPDATE);
    _dropTriggerIfRequired(astrTableName, triggerName);

    strSQL = " CREATE TRIGGER "  + triggerName
        + " UPDATE " + strColumnsToIncludeA + "ON " + astrTableName
        + " for each row (execute procedure ap_noupdate())";

    strSQLora = " CREATE OR REPLACE TRIGGER "  + createTriggerName(astrTableName,  ETriggerEvent.ONUPDATE)
    + " BEFORE UPDATE " + strColumnsToIncludeA + "ON " + astrTableName
    + " for each row BEGIN ap_noupdate(); END;";
    toExecute=iForOracle?strSQLora:strSQL;
    //System.out.println(strSQLora);
    executeSqlCommand(toExecute);
    pocetVytvorenychUpdateTriggeru++;
    return;
  }


  /**
   * Vytvoří trigger zabraňující mazání záznamů z tabulky
   *
   * @param astrTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected void createNoDeleteTrigger(final ATableName astrTableName) throws SQLException {
    String strSQL,strSQLora;
    String toExecute;

    final ATriggerName triggerName = createTriggerName(astrTableName,  ETriggerEvent.ONDELETE);
    _dropTriggerIfRequired(astrTableName, triggerName);

    strSQL = " CREATE TRIGGER " + triggerName
        + " DELETE ON " + astrTableName
        + " for each row (execute procedure \"aris\".ap_nodelete())";

    strSQLora = " CREATE OR REPLACE TRIGGER " + createTriggerName(astrTableName, ETriggerEvent.ONDELETE)
    + " BEFORE DELETE ON " + astrTableName
    + " for each row BEGIN ap_nodelete(); END;";
    //System.out.println(strSQLora);
    toExecute=iForOracle?strSQLora:strSQL;
    executeSqlCommand(toExecute);
    pocetVytvorenychDeleteTriggeru++;
    return;
  }


  /**
   * Vytvoří trigger aktualizující položky refuser a reftime při aktualizaci tabulky
   *
   * @param aStrTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Roman Přichystal
   */

  protected void createCurRefUpdateTrigger(final ATableName aStrTableName) throws SQLException {

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
      _dropTriggerIfRequired(aStrTableName, triggerName);

      String toExecute;
      if (iForOracle) {

        final String triggerSpec = useRefDatProv
            ? "datProvCurrRefForTrigOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser, tc$out2 => :new.refdatprov)"
                : "currentRefForTriggerOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser)";
        toExecute = String.format(
            " CREATE OR REPLACE TRIGGER %s BEFORE UPDATE OF %s ON %s"
                + " for each row BEGIN tw_userident.%s; END;"
                , createTriggerName(aStrTableName, ETriggerEvent.ONUPDATE)
                , strColumnsList
                , aStrTableName
                , triggerSpec
            );
      }
      else {

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
      }

      executeSqlCommand(toExecute);
      //System.err.println(toExecute);
      pocetVytvorenychUpdateTriggeru++;
    }
  }

  private boolean _shouldBeSerno64bitInsertedToEvidenceTable(final ATableName aTableName, final Set<AColumnName> aColumns) {

    boolean result;
    if (SERNO_EVIDENCE_TABLE.getPureTableName().equals(aTableName.getPureTableName())) {

      result = false;
    }
    else {

      result = false;
      for (final AColumnName cd : aColumns) {

        if (SERNO_COLUMN.equals(cd)) {

          final Map<AColumnName, String> m = iTable2Column2DataType.get(aTableName);
          if (m != null) {
            final String colType = m.get(cd);
            if (!StringUtils.isBlank(colType)) {

              final String s = iForOracle ? SERNO_SQLTYPENAME_ORACLE : SERNO_SQLTYPENAME_INFORMIX;
              if (colType.equalsIgnoreCase(s)) {

                result = true;
                break;
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Vytvoří trigger aktualizující položky refuser a reftime při insertu tabulky
   *
   * @param aTableName Jméno tabulky
   * @throws SQLException
   *
   * @author Přemysl Cimbálek
   */
  protected void createCurRefInsertTrigger(final ATableName aTableName) throws SQLException {

    // načti seznam sloupců tabulky
    final Set<AColumnName> columns = getTableColumns(aTableName);
    final boolean useRefDatProv = columns.contains(REFDATPROV_COLUMN);

    final boolean shouldBeSerno64bitInsertedToEvidenceTable
    = _shouldBeSerno64bitInsertedToEvidenceTable(aTableName, columns);

    // odstraň sloupce reftime a refuser, tyto sloupce se do triggeru nezahrnují
    //columns.remove("reftime");
    //columns.remove("refuser");
    // sestav string obsahující seznam sloupců tabulky oddělených čárkami
    //strColumnsList = getOneStringFromListOfStrings(columns);

    final ATriggerName triggerName = createTriggerName(aTableName, ETriggerEvent.ONINSERT);
    _dropTriggerIfRequired(aTableName, triggerName);

    String toExecute;
    if (iForOracle) {

      final String triggerSpec = useRefDatProv
          ? "datProvCurrRefForTrigOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser, tc$out2 => :new.refdatprov)"
              : "currentRefForTriggerOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser)";
      toExecute = String.format(
          " CREATE OR REPLACE TRIGGER %s BEFORE INSERT ON %s for each row BEGIN tw_userident.%s;"
          , createTriggerName(aTableName, ETriggerEvent.ONINSERT)
          , aTableName
          , triggerSpec
          );
      if(columns.contains(PORUSER_COLUMN) && columns.contains(PORTIME_COLUMN)){

        final String porSpec = useRefDatProv
            ? "datProvCurrRefForTrigOnly(tc$out0 => :new.portime, tc$out1 => :new.poruser, tc$out2 => :new.pordatprov)"
                : "currentRefForTriggerOnly(tc$out0 => :new.portime, tc$out1 => :new.poruser)";
        toExecute += " tw_userident." + porSpec + ";";
      }
      if(columns.contains(NAVUSER_COLUMN) && columns.contains(NAVTIME_COLUMN)){

        final String navSpec = useRefDatProv
            ? "datProvCurrRefForTrigOnly(tc$out0 => :new.navtime, tc$out1 => :new.navuser, tc$out2 => :new.navdatprov)"
                : "currentRefForTriggerOnly(tc$out0 => :new.navtime, tc$out1 => :new.navuser)";
        toExecute += " tw_userident." + navSpec + ";";
      }
      if (shouldBeSerno64bitInsertedToEvidenceTable) {

        toExecute += String.format(" INSERT INTO %s(%s, %s) VALUES (:new.%s, '%s')"
            , SERNO_EVIDENCE_TABLE, SERNO_COLUMN, TABLECOLUMN_SERNOEVIDENCETABLE, SERNO_COLUMN, aTableName);
      }
      toExecute += " END;";
    }
    else {

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
    }

    //System.err.println("DBG: " + toExecute);
    executeSqlCommand(toExecute);
    pocetVytvorenychInsertTriggeru++;
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

  protected void createArchiveUpdateTrigger(
      final ATableName aStrTableName, final ATableName aStrArchivTableName) throws SQLException {

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
      if(iForOracle) {
        strColumnsListWithPrefixes = getOneStringFromListOfStrings(tableColumns, ":pre");
      } else {
        strColumnsListWithPrefixes = getOneStringFromListOfStrings(tableColumns, "pre");
      }
      // v Oracle ve "when" části se ":" u prefixů nepoužívá, jinak ano
      strEqualsConditionInWhen = getEqualsCondition(tableColumnsUpdateOfClause, "pre", "new");
      strEqualsConditionInIf = getEqualsCondition(tableColumnsUpdateOfClause, ":pre", ":new");

      final ATriggerName triggerName = createTriggerName(aStrTableName, ETriggerEvent.ONUPDATE);
      _dropTriggerIfRequired(aStrTableName, triggerName);

      String toExecute;
      if (iForOracle) {

        final String triggerSpec = useRefDatProv
            ? "datProvCurrRefForTrigOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser, tc$out2 => :new.refdatprov)"
                : "currentRefForTriggerOnly(tc$out0 => :new.reftime, tc$out1 => :new.refuser)"
                  ;
        toExecute = String.format(
            " CREATE OR REPLACE TRIGGER %s BEFORE UPDATE OF %s ON %s referencing new as new old as pre for each row"
                + " \nBEGIN \nIF (%s) \n THEN \ninsert into %s(%s) values(%s);\n"

          , createTriggerName(aStrTableName, ETriggerEvent.ONUPDATE)
          , strColumnsListUpdateOfClause
          , aStrTableName
          , strEqualsConditionInIf
          , aStrArchivTableName
          , strColumnsList
          , strColumnsListWithPrefixes
            );

        if (isThereRefTimeAndRefUser(aStrTableName, useRefDatProv)) {

          toExecute += " tw_userident." + triggerSpec + ";";
        }
        toExecute += " END IF;\nEND;";
      }
      else {

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
        if (isThereRefTimeAndRefUser(aStrTableName, useRefDatProv)) {

          toExecute += ", execute function " + triggerSpec;
        }
        toExecute += ")";
      }

      executeSqlCommand(toExecute);
      //System.err.println(toExecute);
      pocetVytvorenychUpdateTriggeru++;
    }
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
    if(iForOracle) {

      columnsToSearch.add(REFUSER_COLUMN.toString().toUpperCase());
      columnsToSearch.add(REFTIME_COLUMN.toString().toUpperCase());
      if (aUseRefDatProv) {

        columnsToSearch.add(REFDATPROV_COLUMN.toString().toUpperCase());
      }

      sql = "select * from dba_tab_columns a inner join DBA_TABLES b on a.table_name = b.table_name and a.owner = b.owner"
          + " where b.table_name='"+tableName.toUpperCase()
          + "' and b.owner = " +  aStrTableName.getSchemaForSql().toUpperCase()
          + " and a.column_name=?";

    }
    else {

      columnsToSearch.add(REFUSER_COLUMN.toString().toLowerCase());
      columnsToSearch.add(REFTIME_COLUMN.toString().toLowerCase());
      if (aUseRefDatProv) {

        columnsToSearch.add(REFDATPROV_COLUMN.toString().toLowerCase());
      }

      sql = "SELECT * FROM syscolumns a,systables b" +
          "  WHERE b.tabname = '" + tableName.toLowerCase() +
          "' AND a.tabid = b.tabid" +
          "  AND a.colname = ?";
    }

    try (PreparedStatement pstm = conn.prepareStatement(sql)) {

      for (final String columnName : columnsToSearch) {

        pstm.setString(1, columnName);
        final ResultSet rs = pstm.executeQuery();
        if (!rs.next()) { return false; }
      }

      return true;
    } // try

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

  protected Set<AColumnName> getTableColumns(final ATableName astrTableName) throws SQLException {
    final Set<AColumnName> columns = getTableColumnsForTable(astrTableName);

    if (columns.isEmpty()) {
      throw new RuntimeException("Table '" + astrTableName + "' does not exists or has no columns");
    }
    return columns;
  }

  /*
    private boolean existTrigger(String astrTableName, String astrTriggerName) throws SQLException {
      PreparedStatement stmt;
      ResultSet rset;
      boolean blnTriggerExist = false;

      stmt =
        conn.prepareStatement(
          " select trigname "
            + " from systables, systriggers "
            + " where systables.tabid = systriggers.tabid "
            + "and systables.tabname = ?"
            + "and systriggers.trigname = ?");

      stmt.setString(1, astrTableName);
      stmt.setString(2, astrTriggerName);
      rset = stmt.executeQuery();
      // z vráceného ResultSetu přečti jméno triggeru
      if (rset != null && rset.next()) {
        rset.getString("trigname");
        blnTriggerExist = true;
      }
      rset.close();
      stmt.close();
      return blnTriggerExist;
    }
   */

  /*
   * Pokud existuje trigger zadaného typu, vrátí jeho jméno. V jíném případě vrací null. Trigger načte ze
   * ze systémové tabulky systriggers.
   * @param astrTableName Jméno tabulky
   * @param astrTriggerType Typ triggeru. Jedna z hodnot INSERT_TRIGGER, DELETE_TRIGGER, UPDATE_TRIGGER
   * @return Jméno trigeru. Null pokud trigger neexistuje.
   * @throws SQLException
   * @author Roman Přichystal
   */

  /*    protected String getExistingTriggerName(String astrTableName, String astrTriggerType) throws SQLException {
        String strTriggerName;
        PreparedStatement stmt;
        ResultSet rset;

        strTriggerName = null;
        stmt = conn.prepareStatement(
                " select trigname "
                    + " from systables, systriggers "
                    + " where systables.tabid = systriggers.tabid "
                    +       "and systables.tabname = ?"
                    +       "and systriggers.event = ?");

        stmt.setString(1, astrTableName);
        stmt.setString(2, astrTriggerType);
        rset = stmt.executeQuery();
        // z vráceného ResultSetu přečti jméno triggeru
        if (rset != null && rset.next()) {
            strTriggerName = rset.getString("trigname");
        }
        rset.close();
        stmt.close();
        return strTriggerName;
    }
   */

  /**
   * Zruší všechny triggery zadaného typu.
   *
   * @param astrTableName jméno tabulky
   * @param astrTriggerType typ triggeru
   *
   * @throws SQLException
   *
   * @author Roman Přichystal
   */
  private void dropTriggersIfNotDropedYet(final ATableName astrTableName) throws SQLException {
    final Set<ATriggerName> triggernames = getTablesToTriggersx().remove(astrTableName); // vyhodit z mapy, aby se příště triggery pro danozu tabulku nerušily
    if (triggernames == null)
    {
      return; // triggery pro takovou tabulku neexistují nebo jižř byly zrušeny
    }
    for (final ATriggerName strTriggerName : triggernames) {
      dropTrigger(astrTableName.getSchemaWithDot()+strTriggerName);
    }
  }


  /**
   * Zruší trigger zadaného jména
   *
   * @param astrTriggerName Jméno triggeru
   *
   * @author Roman Přichystal
   */

  private void dropTrigger(final String astrTriggerName) throws SQLException {
    String      strSQL;

    strSQL = " drop trigger " + astrTriggerName;
    executeSqlCommand(strSQL);
    pocetDropnutychTriggeru++;
  }


  private void executeSqlCommand(final String aSqlCommand) throws SQLException {
    //log.debug("----------------------------------" + System.getProperty("line.separator") + aSqlCommand);
    log.debug(aSqlCommand);
    final Statement stmt = conn.createStatement();
    try {
      //System.out.println("Executed: " + aSqlCommand);
      stmt.execute(aSqlCommand);
    } catch (final SQLException e) {
      throw new IfxSqlException(e, conn, aSqlCommand);
    }
    stmt.close();

  }

  public void done() throws SQLException  {
    conn = null;
  }

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
   * @return počet dropnutych triggerů
   */
  public int getPocetDropnutychTriggeru() {
    return pocetDropnutychTriggeru;
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

  public int getPocetTabulekProNezSeZpracovavalyTriggery() {
    return iJmenaTabulkeProNejzSenecoDeloSTriggery.size();
  }


  /**
   * Přidá dvojici hodnot do mapy seznamů.
   */
  private <K1, K2> void addDvojKlicovecDoStruktury(final Map<K1, Set<K2>> aMapa, final K1 aKey, final K2 aValue) {
    Set<K2> values = aMapa.get(aKey);
    if (values == null) {
      values = new LinkedHashSet<K2>();
      aMapa.put(aKey, values);
    }
    values.add(aValue);
  }

  private void checkTableExistence(final ATableName astrTableName, final Object aLokace) throws SQLException {
    final Set<AColumnName> columns = getTableColumnsForTable(astrTableName);
    if (columns == null) {
      throw new RuntimeException("Table '" + astrTableName + "' does not exists" + (aLokace == null ? "" : " in \"" + aLokace + "\""));
    }
  }

  /**
   * @return the tablesToColumns
   * @throws SQLException
   */
  private Map<ATableName, Set<AColumnName>> getTablesToColumns() throws SQLException {
    if (iTablesToColumns == null) {
      loadTablesColumns();
    }
    return iTablesToColumns;
  }

  private Set<AColumnName> getTableColumnsForTable(final ATableName aQualifiedTableName) throws SQLException {
    final Set<AColumnName> cols = getTablesToColumns().get(aQualifiedTableName);

    @SuppressWarnings("unchecked")
    final Set<AColumnName> result = cols == null ? Collections.EMPTY_SET : new LinkedHashSet<>(cols);
    return result;

  }


  /**
   * @return the tablesToTriggers
   * @throws SQLException
   */
  private Map<ATableName, Set<ATriggerName>> getTablesToTriggersx() throws SQLException {
    if (iTablesToTriggers == null) {
      loadTablesTriggers();
    }
    return iTablesToTriggers;
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
