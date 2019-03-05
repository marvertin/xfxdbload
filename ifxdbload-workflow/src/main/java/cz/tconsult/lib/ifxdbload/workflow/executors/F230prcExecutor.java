package cz.tconsult.lib.ifxdbload.workflow.executors;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import cz.tconsult.lib.ifxdbload.core.tw.NamedString;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;
import cz.tconsult.lib.ifxdbload.workflow.data.LoSoubor;
import cz.tconsult.lib.ifxdbload.workflow.process.ExecutionContext;
import cz.tconsult.lib.ifxdbload.workflow.process.FazeExecutor;
import cz.tconsult.lib.ifxdbload.workflow.process.InterFazeBoard;
import cz.tconsult.lib.parser.ParseredSource;
import cz.tconsult.lib.parser.SplParser;
import cz.tconsult.lib.parser.SplStatement;

public class F230prcExecutor implements FazeExecutor {

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
    //    lofaze.getSoubors()
    //    .spliterator()
    final SplParser splParser = new SplParser();

    final Instant str = Instant.now();

    final List<SplStatement> stms = lofaze.getSoubors().stream()
        .map(LoSoubor::getDataAsString)
        .map(splParser::parse)
        .flatMap(ps -> ps.getStatements().stream())
        .collect(Collectors.toList());

    for (final SplStatement stm : stms) {

      System.out.println("********************** " + stm.getName() + " : " + stm.getStmType());
      //System.out.println(stm.getText());
    }

    //stms.stream().filter(predicate);



    for (final LoSoubor loso : lofaze.getSoubors()) {

      final NamedString data = loso.getDataAsString();
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
      final ParseredSource result = splParser.parse(data);
      for (final SplStatement stm: result.getStatements()) {
        System.out.println("********************** " + stm.getName() + " : " + stm.getStmType());
        //System.out.println(stm.getText());
      }
    }
    System.out.println(Duration.between(str, Instant.now()));




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
