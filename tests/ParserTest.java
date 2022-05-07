/*
 * File: ParserTest.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Basic unit tests for the MyPL parser class.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static Parser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Parser parser = new Parser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }


  //------------------------------------------------------------
  // POSITIVE TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    Parser parser = buildParser("");
    parser.parse();
  }

  @Test
  public void implicitVariableDecls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var v1 = 0",
       "  var v2 = 0.0",
       "  var v3 = false",
       "  var v4 = 'a'",
       "  var v5 = \"abc\"",
       "  var v6 = new Node",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void explicitVariableDecls() throws Exception {
    String s = buildString("fun void main() {",
        "  var int v1 = 0",
        "  var double v2 = 0.0",
        "  var bool v3 = false",
        "  var char v4 = 'a'",
        "  var string v5 = \"abc\"",
        "  var Node v5 = new Node",
        "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void testExpr() throws Exception {
    String s = buildString("fun void main() {",
        "  var v1 = not true",
        "  var v2 = not false",
        "  var v3 = 1 + 1",
        "  var int v4 = 1 - 1",
        "  var int v5 = 1 / 1",
        "  var int v6 = 1 * 1",
        "  var int v7 = 1 % 1",
        "  var v8 = true and false",
        "  var v9 = true or false",
        "  var v10 = true == false",
        "  var v11 = true != false",
        "  var v12 = 1 < 2",
        "  var v13 = 1 <= 2",
        "  var v14 = 1 > 2",
        "  var v15 = 1 >= 2",
        "  var v16 = (1 + 2) * 3 - 4",
        "  var v17 = nil",
        "  var v18 = new Node",
        "  var v19 = node.next",
        "  var v20 = node.next.next",
        "  var v21 = clean()",
        "  var v22 = clean(1)",
        "  var v22 = clean(1, 2, 3)",
        "  var v23 = neg 10",
        "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void testStmt() throws Exception {
    String s = buildString("fun void main() {",
        "  var v1 = 1",
        "  v1 = 10",
        "  if (1 + 2 == 3) {",
        "    var v2 = 1",
        "  } elif (1 + 2 == 4) {",
        "    var v3 = 1",
        "  } elif (1 + 10 == 11) {",
        "    var v4 = 1",
        "  } else {",
        "    var v5 = 1",
        "  }",
        "  while (1 + 2 == 3) {",
        "    var v6 = 1",
        "  }",
        "  for i from 10 upto 20 {",
        "    var v7 = 1",
        "  }",
        "  for i from 10 downto 20 {",
        "    var v8 = 1",
        "  }",
        "  newNode()",
        "  newNode(1)",
        "  var node = newNode(1, 2, 3)",
        "  node.next = newNode(1, 2, 3)",
        "  return",
        "  return 10 + 100  % 10",
        "  delete v1",
        "  delete v2",
        "}");
    Parser parser = buildParser(s);
    parser.parse();
  }
  
  @Test
  public void testfDecl() throws Exception {
    String s = buildString("fun void main() {",
        "}",
        "fun void f1() {",
        "}",
        "fun int f2(int x) {",
        "}",
        "fun bool f3(bool x, int y) {",
        "}",
        "fun string f4(string x, bool y, int z) {",
        "}",
        "fun char f5(char x, string y, bool z, char w) {",
        "}",
        "fun Node f6(Node x, char y, string z, bool w, int v) {",
        "}");
    Parser parser = buildParser(s);
    parser.parse();
  }


  @Test
  public void testtDecl() throws Exception {
    String s = buildString("fun void main() {",
        "}",
        "type Node {",
        "  var int value = 0",
        "  var Node next = nil",
        "}",
        "type stack {",
        "  var Node top = nil",
        "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  
  //------------------------------------------------------------
  // NEGATIVE TEST CASES
  //------------------------------------------------------------
  
  @Test
  public void statementOutsideOfFunction() throws Exception {
    String s = "var v1 = 0";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
      System.out.println(e.getMessage());
      assertEquals("PARSE_ERROR: expecting 'fun', found 'var' at line 1, column 1", e.getMessage());
    }
  }

  @Test
  public void functionWithoutReturnType() throws Exception {
    String s = "fun main() {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
      assertEquals("PARSE_ERROR: expecting identifier, found '(' at line 1, column 9", e.getMessage());
    }
  }

  @Test
  public void functionWithoutClosingBrace() throws Exception {
    String s = "fun void main() {";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
      assertEquals("PARSE_ERROR: expecting '}', found 'EOF' at line 1, column 18", e.getMessage());
    }
  }
  
  /* add additional negative test cases here */ 
  @Test
  public void typeWithoutClosingBrace() throws Exception {
    String s = "type Node {";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch (MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
      assertEquals("PARSE_ERROR: expecting 'var', found 'EOF' at line 1, column 12", e.getMessage());
    }
  }
  
  @Test
  public void typeInsideFunc() throws Exception {
    String s = buildString("fun main() ",
        "type Node {",
        "  var int value = 0",
        "  var Node next = nil",
        "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch (MyPLException e) {
      assertEquals("PARSE_ERROR: expecting identifier, found '(' at line 1, column 9", e.getMessage());
    }
  }

  @Test
  public void typeWithoutName() throws Exception {
    String s = buildString("type {",
        "  var int value = 0",
        "  var Node next = nil",
        "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch (MyPLException e) {
      assertEquals("PARSE_ERROR: expecting identifier, found '{' at line 1, column 6", e.getMessage());
    }
  }

  @Test
  public void noAssignmentVdecl() throws Exception {
    String s = buildString("fun void main() {",
        "  var v1",
        "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch (MyPLException e) {
      assertEquals("PARSE_ERROR: expecting '=', found '}' at line 3, column 1", e.getMessage());
    }
  }
}
