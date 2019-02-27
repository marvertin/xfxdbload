/**
 * 
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once;

import java.util.ArrayList;
import java.util.List;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.datatypes.OnceError;
import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.once.enums.ELoadResult;

/**
 * @author veverka
 *
 */
public class OnceLoaderResult {
  private ELoadResult result;

  private List<OnceError> errs = new ArrayList<OnceError>();

  /**
   * @param result the result to set
   */
  public void setResult(ELoadResult result) {
    this.result = result;
  }

  /**
   * @return the result
   */
  public ELoadResult getResult() {
    return result;
  }

  /**
   * @param errs the errs to set
   */
  public void setErrs(List<OnceError> errs) {
    this.errs = errs;
  }

  /**
   * @return the errs
   */
  public List<OnceError> getErrs() {
    return errs;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TOTO " + result;
  }
}
