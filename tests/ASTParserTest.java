/*
 * File: ASTParserTest.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Basic unit tests for the MyPL ast-based parser class.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ASTParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    ASTParser parser = buildParser("");
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }

  @Test
  public void oneTypeDeclInProgram() throws Exception {
    String s = buildString
      ("type Node {",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }
  
  @Test
  public void oneFunDeclInProgram() throws Exception {
    String s = buildString
      ("fun void main() {",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void multipleTypeAndFunDeclsInProgram() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void F1() {}",
       "type T2 {}",
       "fun void F2() {}",
       "fun void main() {}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(2, p.tdecls.size());
    assertEquals(3, p.fdecls.size());
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
        "  var v23 = clean(1, 2, 3)",
        "  var v24 = neg 10",
        "}");
    ASTParser parser = buildParser(s);
    Program prog = parser.parse();
    assertEquals(1, prog.fdecls.size());
    assertEquals(0, prog.tdecls.size());
    FunDecl mainFxn = prog.fdecls.get(0);
    assertEquals(24, mainFxn.stmts.size());
    VarDeclStmt v8 = (VarDeclStmt) mainFxn.stmts.get(7);
    assertEquals(null, v8.typeName);
    VarDeclStmt v7 = (VarDeclStmt) mainFxn.stmts.get(6);
    assertEquals("int", v7.typeName.lexeme());
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
        "  for i from 10 + 100 downto 30 {",
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
    ASTParser parser = buildParser(s);
    Program prog = parser.parse();
    assertEquals(14, prog.fdecls.get(0).stmts.size());

    FunDecl mainFunc = prog.fdecls.get(0);
    CondStmt firstCond = (CondStmt) mainFunc.stmts.get(2);
    assertEquals("1", ((SimpleRValue) ((SimpleTerm)((ComplexTerm) firstCond.ifPart.cond.first).expr.first).rvalue).value.lexeme());
    assertEquals("+", ((ComplexTerm) firstCond.ifPart.cond.first).expr.op.lexeme());
    WhileStmt whileStmt = (WhileStmt) mainFunc.stmts.get(3);
    assertEquals("1", ((SimpleRValue) ((SimpleTerm)((ComplexTerm) whileStmt.cond.first).expr.first).rvalue).value.lexeme());
    assertEquals("+", ((ComplexTerm) whileStmt.cond.first).expr.op.lexeme());
    ForStmt forStmt = (ForStmt) mainFunc.stmts.get(5);
    assertEquals("10", ((SimpleRValue) ((SimpleTerm) forStmt.start.first).rvalue).value.lexeme());
    assertEquals("+", forStmt.start.op.lexeme());
    assertEquals("30", ((SimpleRValue) ((SimpleTerm) forStmt.end.first).rvalue).value.lexeme());
    AssignStmt assignStmt = (AssignStmt) mainFunc.stmts.get(9);
    assertEquals("node", assignStmt.lvalue.get(0).lexeme());
    assertEquals(".", assignStmt.lvalue.get(1).lexeme());
    assertEquals("next", assignStmt.lvalue.get(2).lexeme());
    CallExpr callExpr = (CallExpr) ((SimpleTerm) assignStmt.expr.first).rvalue;
    assertEquals("newNode", callExpr.funName.lexeme());
    assertEquals("1", ((SimpleRValue)((SimpleTerm) callExpr.args.get(0).first).rvalue).value.lexeme());
    assertEquals("2", ((SimpleRValue)((SimpleTerm) callExpr.args.get(1).first).rvalue).value.lexeme());
    assertEquals("3", ((SimpleRValue)((SimpleTerm) callExpr.args.get(2).first).rvalue).value.lexeme());
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
    ASTParser parser = buildParser(s);
    Program prog = parser.parse();
    assertEquals(7, prog.fdecls.size());
    assertEquals("void", prog.fdecls.get(0).returnType.lexeme());
    assertEquals("main", prog.fdecls.get(0).funName.lexeme());
    assertEquals("void", prog.fdecls.get(1).returnType.lexeme());
    assertEquals("f1", prog.fdecls.get(1).funName.lexeme());
    assertEquals("int", prog.fdecls.get(2).returnType.lexeme());
    assertEquals("f2", prog.fdecls.get(2).funName.lexeme());
    assertEquals("int", prog.fdecls.get(2).params.get(0).paramType.lexeme());
    assertEquals("x", prog.fdecls.get(2).params.get(0).paramName.lexeme());
    assertEquals("bool", prog.fdecls.get(3).returnType.lexeme());
    assertEquals("f3", prog.fdecls.get(3).funName.lexeme());
    assertEquals("bool", prog.fdecls.get(3).params.get(0).paramType.lexeme());
    assertEquals("x", prog.fdecls.get(3).params.get(0).paramName.lexeme());
    assertEquals("int", prog.fdecls.get(3).params.get(1).paramType.lexeme());
    assertEquals("y", prog.fdecls.get(3).params.get(1).paramName.lexeme());
    assertEquals("string", prog.fdecls.get(4).returnType.lexeme());
    assertEquals("f4", prog.fdecls.get(4).funName.lexeme());
    assertEquals("string", prog.fdecls.get(4).params.get(0).paramType.lexeme());
    assertEquals("x", prog.fdecls.get(4).params.get(0).paramName.lexeme());
    assertEquals("bool", prog.fdecls.get(4).params.get(1).paramType.lexeme());
    assertEquals("y", prog.fdecls.get(4).params.get(1).paramName.lexeme());
    assertEquals("int", prog.fdecls.get(4).params.get(2).paramType.lexeme());
    assertEquals("z", prog.fdecls.get(4).params.get(2).paramName.lexeme());
    assertEquals("char", prog.fdecls.get(5).returnType.lexeme());
    assertEquals("f5", prog.fdecls.get(5).funName.lexeme());
    assertEquals("char", prog.fdecls.get(5).params.get(0).paramType.lexeme());
    assertEquals("x", prog.fdecls.get(5).params.get(0).paramName.lexeme());
    assertEquals("string", prog.fdecls.get(5).params.get(1).paramType.lexeme());
    assertEquals("y", prog.fdecls.get(5).params.get(1).paramName.lexeme());
    assertEquals("bool", prog.fdecls.get(5).params.get(2).paramType.lexeme());
    assertEquals("z", prog.fdecls.get(5).params.get(2).paramName.lexeme());
    assertEquals("char", prog.fdecls.get(5).params.get(3).paramType.lexeme());
    assertEquals("w", prog.fdecls.get(5).params.get(3).paramName.lexeme());
    assertEquals("Node", prog.fdecls.get(6).returnType.lexeme());
    assertEquals("f6", prog.fdecls.get(6).funName.lexeme());
    assertEquals("Node", prog.fdecls.get(6).params.get(0).paramType.lexeme());
    assertEquals("x", prog.fdecls.get(6).params.get(0).paramName.lexeme());
    assertEquals("char", prog.fdecls.get(6).params.get(1).paramType.lexeme());
    assertEquals("y", prog.fdecls.get(6).params.get(1).paramName.lexeme());
    assertEquals("string", prog.fdecls.get(6).params.get(2).paramType.lexeme());
    assertEquals("z", prog.fdecls.get(6).params.get(2).paramName.lexeme());
    assertEquals("bool", prog.fdecls.get(6).params.get(3).paramType.lexeme());
    assertEquals("w", prog.fdecls.get(6).params.get(3).paramName.lexeme());
    assertEquals("int", prog.fdecls.get(6).params.get(4).paramType.lexeme());
    assertEquals("v", prog.fdecls.get(6).params.get(4).paramName.lexeme());
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
    ASTParser parser = buildParser(s);
    Program program = parser.parse();
    assertEquals(1, program.fdecls.size());
    assertEquals(2, program.tdecls.size());
    TypeDecl tdecl = program.tdecls.get(0);
    assertEquals("Node", tdecl.typeName.lexeme());
    assertEquals(2, tdecl.vdecls.size());
    assertEquals("int", tdecl.vdecls.get(0).typeName.lexeme());
    assertEquals("value", tdecl.vdecls.get(0).varName.lexeme());
    assertEquals("Node", tdecl.vdecls.get(1).typeName.lexeme());
    assertEquals("next", tdecl.vdecls.get(1).varName.lexeme());
    tdecl = program.tdecls.get(1);
    assertEquals("stack", tdecl.typeName.lexeme());
    assertEquals(1, tdecl.vdecls.size());
    assertEquals("Node", tdecl.vdecls.get(0).typeName.lexeme());
    assertEquals("top", tdecl.vdecls.get(0).varName.lexeme());
  }

  
  //------------------------------------------------------------
  // NEGATIVE TEST CASES
  //------------------------------------------------------------
  
  @Test
  public void statementOutsideOfFunction() throws Exception {
    String s = "var v1 = 0";
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
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
    ASTParser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch (MyPLException e) {
      assertEquals("PARSE_ERROR: expecting '=', found '}' at line 3, column 1", e.getMessage());
    }
  } 
}
