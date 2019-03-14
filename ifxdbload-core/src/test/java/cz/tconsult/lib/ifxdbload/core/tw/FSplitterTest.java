package cz.tconsult.lib.ifxdbload.core.tw;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class FSplitterTest {

  private String join(final List<String> list) {
    return list.stream().collect(Collectors.joining("|"));
  }

  private void test(final String expected, final String s) {
    assertEquals(expected, join(FSplitter.splitByComma(s)));
  }

  @Test
  void test0() {
    test("aaa|bbb  ccc|ddd|eee|fff", ",  ,aaa ,  bbb  ccc  , , ddd,eee ,, fff , , ,, ");
  }

  @Test
  void test1() {
    test("a|b", "a,b");
  }

  @Test
  void test3() {
    assertThat(FSplitter.splitByComma(""), is(empty()));
  }


  @Test
  void test4() {
    assertThat(FSplitter.splitByComma(","), is(empty()));
  }

  @Test
  void test4a() {
    assertThat(FSplitter.splitByComma("     "), is(empty()));
  }

  @Test
  void test5() {
    assertThat(FSplitter.splitByComma("   ,     "), is(empty()));
  }

  @Test
  void test6() {
    assertThat(FSplitter.splitByComma(null), is(empty()));
  }

  @Test
  void test7() {
    test("ahoj jen  jedna hodnůtka", "  ahoj jen  jedna hodnůtka  ");
  }

  @Test
  void test8() {
    test("xx", "xx");
  }

  @Test
  void test9() {
    test("x", " x");
  }

  @Test
  void test10() {
    test("x", "x ");
  }

  @Test
  void test11() {
    test("x", " x ");
  }


}
