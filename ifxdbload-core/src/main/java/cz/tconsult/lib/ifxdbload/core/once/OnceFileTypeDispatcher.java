package cz.tconsult.lib.ifxdbload.core.once;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cz.tconsult.spl.IfxSqlCompiler;
import cz.tconsult.spl.parser.SplBase0;
import cz.tconsult.spl.parser.SplDocument;
import cz.tconsult.spl.parser.SplFunction;
import cz.tconsult.spl.parser.SplProcedure;
import cz.tconsult.spl.parser.SplSqlCommandCreateView;
import cz.tconsult.spl.parser.SplTriggerBase0;

/**
 * Knihovna je urcena pro rozhodovaci fazi nacitani Once loader skriptu
 * a ostatnich dbobjektu.
 * @author brzoza
 *
 */
public class OnceFileTypeDispatcher {
  private final static IfxSqlCompiler COMPILER=new IfxSqlCompiler();
  //private OnceScript iOnceScript;

  OnceFileTypeDispatcher(){
  }


  public boolean isOnceScript(final SplDocument aDocument){
    final List<SplBase0> aChildren=aDocument.getChildrenList();
    for(final SplBase0 cmd:aChildren){
      if(cmd instanceof SplFunction){/*null*/}
      else if(cmd instanceof SplSqlCommandCreateView){/*null*/}
      else if(cmd instanceof SplTriggerBase0){/*null*/}
      else if(cmd instanceof SplProcedure){/*null*/} else {
        return true;
      }
    }
    return false;
  }

  public boolean isDbObject(final SplDocument aDocument){
    return !isOnceScript(aDocument);
  }

  public boolean isOnceScript(final File aFile) throws IOException{
    final SplDocument document=COMPILER.getSplDocument(aFile);
    return isOnceScript(document);
  }

  public boolean isDbObject(final File aFile) throws IOException{
    final SplDocument document=COMPILER.getSplDocument(aFile);
    return !isOnceScript(document);
  }


  public boolean isOnceScript(final String aDocument) {

    final SplDocument document=COMPILER.getSplDocument(aDocument, "memory hack");
    return isOnceScript(document);
  }


}
