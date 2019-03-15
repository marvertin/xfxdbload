package cz.tconsult.lib.ifxdbload.core.loaders.once;

import java.util.Set;

/**
 * Výjimka onceskriptu.
 * @author veverka
 *
 */
public class XOnceScript extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private final OnceScript once;
  private final Set<String> errors;

  public XOnceScript(final OnceScript once, final Throwable cause, final Set<String> errors) {
    super(cause);
    this.once = once;
    this.errors = errors;
  }

  public OnceScript getOnce() {
    return once;
  }

  public Set<String> getErrors() {
    return errors;
  }



}
