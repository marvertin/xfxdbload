package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceError;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.SqlCommand;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.ECommand;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EErrorType;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.EOncePhase;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.exceptions.OnceScriptException;
import cz.tconsult.CORE_REVIDOVAT.spl.parser.SplBase0;
import cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlAlterTableClause;
import cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandCreate;

public class OnceScriptUtils {
  
  private static final String TRANS_CMD_PATTERN_R="(?i)(ROLLBACK)[\\s]*(WORK){0,1}[\\w]*;";
  private static final String TRANS_CMD_PATTERN_C="(?i)(COMMIT)[\\s]*(WORK){0,1}[\\w]*;";
  private static final String TRANS_CMD_PATTERN_B="(?i)(BEGIN)[\\s]*(WORK){0,1}[\\w]*;";
  
  
  /**
   * Provede striktni kontrolu prikazu, jestli spada do povolene
   * ddl nebo ddm
   * @param base
   */
  public static void testStrict(SplBase0 base,EOncePhase aOncePhase)
  throws OnceScriptException
  {
    switch(aOncePhase){
      case ALTER:
        if(!SqlCommand.isDDL(base)) throw new OnceScriptException("Script type " + aOncePhase + " must contains only DLL commands");
        if(SqlCommand.isDestructive(base)) throw new OnceScriptException("Alter je destruktivni");
        break;
      case BEFORE:
      case SETTINGS:
      case MIGRATION:
      case TIDY:
        if(!SqlCommand.isDML(base)) throw new OnceScriptException("Script type " + aOncePhase + " must contains only DML commands");
        break;
      case ALTERD:
        if(!SqlCommand.isDDL(base)) throw new OnceScriptException("Script type " + aOncePhase + " must contains only DLL commands");              
    }
  }
  
  public boolean isAllowedCommand(SplBase0 base){
    if(base instanceof SplSqlAlterTableClause) return true;
    if(base instanceof SplSqlCommandCreate) return true;
    return false;
  }
  
  /**
   * Dodelano z duvodu spadnuti parsrovani spl parsru kdy se pokusim spustit cely 
   * skript najednou ale musim odstranit begin work a rollback work.
   * Tim ze padne Spl padnou i veskere syn.,sem. kontroly
   */
  public static String tryHealNoTranScript(String aToHeal){
    Matcher m;
    String result=aToHeal;
    if((m=Pattern.compile(TRANS_CMD_PATTERN_R).matcher(result)).find()){
      result=m.replaceAll("");
    }
    else throw new RuntimeException("Nenalezen spravny rollback work");
    if((m=Pattern.compile(TRANS_CMD_PATTERN_B).matcher(result)).find()){
      result=m.replaceAll("");
    }
    else throw new RuntimeException("Nenalezen spravny begin work");
    if((m=Pattern.compile(TRANS_CMD_PATTERN_C).matcher(result)).find()){
      throw new RuntimeException("Skript nesmi obsahovat commit work"); 
    }
    result=result.trim();
    return result;
  }
  
  /**
   * 
   */
  public boolean testEnd(boolean aIsTransactionClosed,SplBase0 aCommand,List<OnceError> aErrors){
    boolean closed=false;
    if(aCommand instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandRollbackWork){
      if(aIsTransactionClosed){
        aErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS, (String)null));
      }
      closed =true;
    }
    return closed;
  }
  
  /**
   * 
   */
  public boolean testStart(boolean aIsTransactionOpened,SplBase0 aCommand,List<OnceError> aErrors){
    boolean opened=false;
    if(aCommand instanceof cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandBeginWork){
      if(aIsTransactionOpened){
        aErrors.add(OnceError.create(EErrorType.NOT_IN_TRANS, (String)null));
      }
      opened =true;
    }
    return opened;
  }
  
  
  public static Class<? extends SplBase0> commandToClass(ECommand aECommand){
    switch(aECommand){
      case INDEX:return cz.tconsult.CORE_REVIDOVAT.spl.parser.SplSqlCommandCreateIndex.class;
    }
    return null;
  }
}
