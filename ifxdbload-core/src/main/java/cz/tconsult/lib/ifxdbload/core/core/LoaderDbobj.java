/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cz.tconsult.dbloader.itf.EMessageCategory;
import cz.tconsult.dbloader.itf.UniversalResultMessage;

/**
 * @author veverka
 *
 */
class LoaderDbobj extends Loader0 {

  //private static final Logf log = Logf.wrap(LogFactory.getLog(LoaderDbobj.class));
  private int citac;

  /**
   * @param zavadenec
   */
  @Override
  public UniversalDbLoaderResult load(final Connection conn, final Zavadenec zavadenec) {
    final SimpleParser simpleParser = new SimpleParser();
    final List<LoCommand> listCommands = simpleParser.parse(zavadenec.getDataAsString());
    final List<UniversalResultMessage> msgs = new ArrayList<UniversalResultMessage>();
    for (final LoCommand loCommand : listCommands) {
      final UniversalResultMessage m = loadOneDbObj(conn, loCommand, zavadenec);
      if (m != null) {
        msgs.add(m);
      }
    }

    if (msgs.isEmpty()) {

      final UniversalResultMessage m = UniversalResultMessage.create(
          EMessageCategory.NOTICE, "Loaded OK: " + zavadenec.getLocator()
          , null/*aBeginFilePos*/, null/*aEndFilePos*/
          , null/*aSqlCommand*/, null/*aBeginSqlPos*/, null/*aEndSqlPos*/, null/*aThr*/);
      msgs.add(m);
    }
    final UniversalDbLoaderResult uniresult = UniversalDbLoaderResult.create(msgs);
    return  uniresult;

  }



  /**
   * @param aString
   */
  private UniversalResultMessage loadOneDbObj(final Connection connection, final LoCommand loCommand, final Zavadenec loSoubor) {
    citac++;
    final SqlCommand command = new SqlCommand(loSoubor,
        loCommand.getCommand(), loCommand.getLineNumber(), citac);
    final UniversalResultMessage result = command.execute(connection);
    return result;
  }


}
