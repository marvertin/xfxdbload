package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.Set;

/**
 * Výjimka onceskriptu.
 * @author veverka
 *
 */
public class XErrors extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private final Set<String> errors;

  public XErrors(final Set<String> errors) {
    this.errors = errors;
  }

  public Set<String> getErrors() {
    return errors;
  }



}
