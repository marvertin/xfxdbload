package cz.tconsult.lib.ifxdbload.core.once.enums;

public enum EErrorType {
  BAD_DIR("Dir name isn't valid name of phase."),
  NOT_IN_TRANS("Commands not in BEGIN WORK/ROLLBACK block."),
  ID_NOT_EQUALS("Id in file and file name not equals."),
  ID_MISSING("File doesn't contain id."),
  SYNTAX_ERROR("Syntax error in once script file."),
  UNEXPECTED_PARSE_ERROR("Unexpected parsing error."),
  CONTAINS_COMMIT("File cannot contains commit."),
  UNEXPECTED_IO_ERROR("Unexpected IO error."), 
  
  // chyby pro metodiku V2
  
  LOAD_UNLESS_IF_LOADED("Directives LOAD_IF_LOADED and LOAD_UNLESS_LOADED cannot appear in the same once script with same value."),
  LOAD_ONCE_ALWAYS("Directives LOAD_ONCE and RELOAD_ALWAYS cannot appear in the same once script."),
  
  VARIANT_CONFLICT("Variant directory and variant directive conflict."),
  
  UNSUPPORTED_DIRECTIVE("Directive is not supported."),
  DESCRIPTION_MISSING("Directive DESCRIPTION is mandatory."),
  
  
  UNSUPPORTED_DIRECTIVE_FORMAT("Unsupported directive format."), 
  SQL_ERROR ("SQL error ocured."),
  LOADED_BEFORE_BUT_CHANGED("Skript uz byl jednou spusten, ale obsah souboru byl zmenen.", true),
  BAD_HEADER("Bad header of once script."),
  SCRIPT_DOS_NOT_EXISTS("Script doeas not exists."),
  ;
  
  
  private final String iMessage;
  private final boolean iOnlyWarning;
  
  /**
   * @return the isOnlyWarning
   */
  public boolean isOnlyWarning() {
    return iOnlyWarning;
  }

  EErrorType(String aMessage){
    iMessage=aMessage;
    iOnlyWarning = false;
  }

  EErrorType(String aMessage, boolean aIsOnlyWarning){
    iMessage=aMessage;
    iOnlyWarning = aIsOnlyWarning;
  }

  public String getMessage(){
    return iMessage;  
  }
}
