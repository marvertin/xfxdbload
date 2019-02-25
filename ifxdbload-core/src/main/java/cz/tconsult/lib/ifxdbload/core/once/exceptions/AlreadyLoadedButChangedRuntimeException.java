/**
 * 
 */
package cz.tconsult.lib.ifxdbload.core.once.exceptions;

/**
 * @author polakm
 *
 */
public class AlreadyLoadedButChangedRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -1327475671765928361L;

  /**
   * 
   */
  public AlreadyLoadedButChangedRuntimeException() {
    super();
    
  }

  /**
   * @param aMessage
   * @param aCause
   */
  public AlreadyLoadedButChangedRuntimeException(String aMessage, Throwable aCause) {
    super(aMessage, aCause);
    
  }

  /**
   * @param aMessage
   */
  public AlreadyLoadedButChangedRuntimeException(String aMessage) {
    super(aMessage);
    
  }

  /**
   * @param aCause
   */
  public AlreadyLoadedButChangedRuntimeException(Throwable aCause) {
    super(aCause);
    
  }

}
