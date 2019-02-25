package cz.tconsult.lib.ifxdbload.core.once.datatypes;

import cz.tconsult.spl.parser.SplBase0;

public class SqlCommand {
  
  public static String doSplChild(SplBase0 child) {
    String commandString=null;
    commandString=child.getParseredText();
    if(commandString.startsWith(";")){
      commandString=commandString.substring(1,commandString.length());
      commandString=commandString.trim();
    }
    return commandString;
  }
  
  /**
   * Vrati jestli dany prikaz je destruktivni na databazi
   * @param cmd
   * @return
   */
  
  public static boolean isDestructive(SplBase0 cmd){
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandDrop) return true;
    //TODO rozparsovat alter
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlAlterTableClause) return true;
    return false;
  }
  
  /**
   * 
   * @param cmd
   * @return Vrati zda-li je sql prikaz typu DML(data modification language)
   */
  public static boolean isDML(SplBase0 cmd){
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandUpdate) return true;
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandDelete) return true;
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandInsertBase) return true;
    return false;
  }
  
  /**
   * 
   * @param cmd
   * @return Vrati zdali je sql prikaz typu DDL (data definition language)
   */
  public static boolean isDDL(SplBase0 cmd){
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlAlterTableClause) return true;
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandDrop) return true;
    if(cmd instanceof cz.tconsult.spl.parser.SplSqlCommandCreate) return true;
    return false;
  }
}
