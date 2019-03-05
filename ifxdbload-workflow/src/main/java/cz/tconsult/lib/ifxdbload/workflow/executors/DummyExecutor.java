package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.util.List;
import java.util.Map;

import cz.tconsult.lib.ifxdbload.workflow.data.ASchema;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.ExecutionContext;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;
import cz.tconsult.lib.parser.SplParser;
import cz.tconsult.lib.parser.SplStatement;

public class DummyExecutor implements FazeExecutor {

  private LoFaze lofaze;

  @Override
  public void init(final LoFaze lofaze, final InterFazeBoard board) {
    this.lofaze = lofaze;
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]

  }

  @Override
  public boolean isLoadIfEmty() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    return false;
  }

  @Override
  public boolean shouldLoad() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    return true;
  }

  @Override
  public void prepare() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]

  }

  @Override
  public String execute(final ExecutionContext ctx) {
    System.out.println(lofaze.getSoubors().size());
    final List<Map<String, Object>> xxx = ctx.jt(ASchema.of("aris")).queryForList("SELECT USER,* FROM ap_status");
    System.out.println(xxx);

    for (final LoSoubor loso : lofaze.getSoubors()) {

      final String data = loso.getDataAsString();
      //      System.out.println("****************************************************************************************************************");
      //      System.out.println(data);
      //      System.out.println("----------------------------------------------------------------------------------------------------------------");

      //      final CSplLexer lexer = new CSplLexer();
      //      lexer.setIgnoreWhiteSpacesAndComments(true);
      //      lexer.setEndToken("KONECNIK");
      //      final List<LexerToken> tokens = lexer.lex(data, "blb");
      //
      //      for (final LexerToken token : tokens) {
      //        System.out.println("     " + token.getType().getClass() + "          " + token.getType() +  "                                   " + token.getText() + " ||| " + token.getValue());
      //      }
      final SplParser splParser = new SplParser();
      final List<SplStatement> result = splParser.parse(data, loso.getRoot() + "|" + loso.getEntryName().toString());
      for (final SplStatement stm: result) {
        System.out.println("********************** " + stm.getName() + " : " + stm.getStmType());
        System.out.println(stm.getText());
      }
    }


    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("jedu");
    return null;
  }

  @Override
  public void skip() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 13:06:02]
    //System.out.println("skipuju");

  }

}
