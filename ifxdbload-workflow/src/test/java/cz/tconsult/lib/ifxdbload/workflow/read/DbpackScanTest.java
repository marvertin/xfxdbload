package cz.tconsult.lib.ifxdbload.workflow.read;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import cz.tconsult.lib.ifxdbload.core.faze.AEntryName;
import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;
import cz.tconsult.lib.ifxdbload.workflow.scan.DbpackScaner;
import cz.tconsult.lib.ifxdbload.workflow.scan.FileContentReceiver;

class DbpackScanTest {

  @Test
  void testNacteni1() throws IOException {
    final Path cesta = Paths.get("./src/test/data/nacteni1");
    new DbpackScaner(new FileContentBuilderTestImpl()).read(cesta);

  }



  private class FileContentBuilderTestImpl implements FileContentReceiver {

    String[] vysledky = {
        "dub/list  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "dub/zalud  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "skala  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "smrk/jehlice  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "smrk/siska  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "voda  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "zeme  ---  DbpackProperties(root=./src/test/data/nacteni1/les, dbkind=lesodruh, dbschema=smiseny)",
        "namesti/cinzak/byt/jidelna/stul  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "namesti/cinzak/byt/loznice/postel/polstar  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "namesti/cinzak/byt/loznice/postel/prikryvka  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "namesti/cinzak/byt/loznice/postel/prosteradlo  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "namesti/cinzak/byt/loznice/skrin  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "namesti/cinzak/chodba  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty, dbkind=mestodruh, dbschema=malinkaty)",
        "domek/jidelna/stul  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
        "domek/loznice/postel/polstar  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
        "domek/loznice/postel/prikryvka  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
        "domek/loznice/postel/prosteradlo  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
        "domek/loznice/skrin  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
        "predzahradka  ---  DbpackProperties(root=./src/test/data/nacteni1/mestodruh-malinkaty/ulice/zabalenecDomovni1.zip, dbkind=ulicodruh, dbschema=kratky)",
    };

    private int index;
    @Override
    public void add(final DbpackProperties dbprops, final Supplier<byte[]> contentSupplier, final AEntryName aEntryName) {
      final String s = aEntryName + "  ---  " + dbprops.toString().replace('\\', '/');
      System.out.println(s);
      assertEquals(vysledky[index++], s);
      //System.out.println(new String(contentSupplier.get(), StandardCharsets.UTF_8));
    }
  }


}
