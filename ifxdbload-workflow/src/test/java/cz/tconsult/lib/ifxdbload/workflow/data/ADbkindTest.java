package cz.tconsult.lib.ifxdbload.workflow.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


class ADbkindTest {

  @Test
  void test() {
    final ADbkind ajuj = new ADbkind("ajuj");
    assertEquals("ajuj", ajuj.toString());
  }

}
