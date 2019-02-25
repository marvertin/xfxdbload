/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.tconsult.dbloader.itf.UniversalResultMessage;


/**
 * @author veverka
 *
 */
public class UniversalDbLoaderResult {

  private final List<UniversalResultMessage> messages;

  private UniversalDbLoaderResult(final List<UniversalResultMessage> aMsgs) {

    if (aMsgs == null) {

      messages = Collections.unmodifiableList(new ArrayList<UniversalResultMessage>());
    }
    else {

      messages = Collections.unmodifiableList(new ArrayList<UniversalResultMessage>(aMsgs));
    }
  }

  static UniversalDbLoaderResult create(final List<UniversalResultMessage> aMsgs) {

    return new UniversalDbLoaderResult(aMsgs);
  }

  public List<UniversalResultMessage> getMessages() {
    return messages;
  }


}
