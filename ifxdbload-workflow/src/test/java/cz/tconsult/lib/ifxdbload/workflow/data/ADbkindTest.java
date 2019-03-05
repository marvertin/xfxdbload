package cz.tconsult.lib.ifxdbload.workflow.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


class ADbkindTest {

  @Test
  void test() {
    final ADbkind ajuj = new ADbkind("ajuj");
    assertEquals("ajuj", ajuj.toString());
  }

}
