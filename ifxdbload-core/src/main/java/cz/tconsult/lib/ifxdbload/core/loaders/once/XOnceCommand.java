package cz.tconsult.lib.ifxdbload.core.loaders.once;

/**
 * Výjimka onceskriptu.
 * @author veverka
 *
 */
public class XOnceCommand extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public XOnceCommand(final Throwable cause) {
    super(cause);
  }



}
