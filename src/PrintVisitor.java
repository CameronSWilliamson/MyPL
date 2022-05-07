/*
 * File: PrintVisitor.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Pretty-printing visitor for MyPL Programs
 */

import java.io.PrintStream;


public class PrintVisitor implements Visitor {

  // output stream for printing
  private PrintStream out;
  // current indent level (number of spaces)
  private int indent = 0;
  // indentation amount
  private final int INDENT_AMT = 2;
  
  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private String getIndent() {
    return " ".repeat(indent);
  }

  private void incIndent() {
    indent += INDENT_AMT;
  }

  private void decIndent() {
    indent -= INDENT_AMT;
  }

  //------------------------------------------------------------
  // VISITOR FUNCTIONS
  //------------------------------------------------------------

  // Hint: To help deal with call expressions, which can be statements
  // or expressions, statements should not indent themselves and add
  // newlines. Instead, the function asking statements to print
  // themselves should add the indent and newlines.
  

  // constructor
  public PrintVisitor(PrintStream printStream) {
    out = printStream;
  }

  
  // top-level nodes

  @Override
  public void visit(Program node) throws MyPLException {
    // print type decls first
    for (TypeDecl d : node.tdecls)
      d.accept(this);
    // print function decls second
    for (FunDecl d : node.fdecls)
      d.accept(this);
  }

  // type Employee {
  // }
  public void visit(TypeDecl node) throws MyPLException {
    out.print(getIndent() +"type " + node.typeName.lexeme() + " {\n");
    incIndent();
    for (VarDeclStmt v : node.vdecls) {
      out.print(getIndent());
      v.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print(getIndent() + "}\n\n");
  }

  // fun int add (int x, int y) {
  // }
  public void visit(FunDecl node) throws MyPLException {
    out.print(getIndent() + "fun " + node.returnType.lexeme() + " " + node.funName.lexeme() + "(");
    if (node.params.size() > 0) {
      for (FunParam param : node.params.subList(0, node.params.size() - 1)) {
        out.print("" + param.paramType.lexeme() + " " + param.paramName.lexeme());
        out.print(", ");
      }
      FunParam lastParam = node.params.get(node.params.size() - 1);
      out.print("" + lastParam.paramType.lexeme() + " " + lastParam.paramName.lexeme());
    }
    out.print(") {\n");
    incIndent();
    for (Stmt stmt : node.stmts) {
      out.print(getIndent());
      stmt.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print(getIndent() + "}\n\n");
  }

  // statement nodes

  // var int x  = 5
  public void visit(VarDeclStmt node) throws MyPLException {
    out.print("var ");
    if (node.typeName != null) {
      out.print(node.typeName.lexeme() + " ");
    }
    out.print(node.varName.lexeme() + " = ");
    visit(node.expr);
  }

  // x = 5
  public void visit(AssignStmt node) throws MyPLException {
    for (Token token : node.lvalue) {
      out.print(token.lexeme());
    }
    out.print(" = ");
    visit(node.expr);
  }

  // if (x > 5) {
  // }
  // else {
  // 
  public void visit(CondStmt node) throws MyPLException {
    out.print("if ");
    basicElif(node.ifPart, node.elseStmts != null);
    if (node.elifs != null) {
      for (BasicIf elif : node.elifs) {
        out.print(getIndent() + "elif ");
        basicElif(elif, node.elseStmts != null);
      }
    }
    if (node.elseStmts != null) {
      out.print(getIndent() + "else {\n");
      incIndent();
      for (Stmt elseStmts : node.elseStmts) {
        out.print(getIndent());
        elseStmts.accept(this);
        out.print("\n");
      }
      decIndent();
      out.print(getIndent() + "}");
    }
  }

  /**
   * Helper function for CondStmt that handles basicIf statements. This 
   * function is to be called after the "if" / "elif" keyword has been printed.
   * @param node the BasicIf node to be printed
   * @param elseStmts true if there are else statements to be printed
   * @throws MyPLException
   */
  private void basicElif(BasicIf node, boolean elseStmts) throws MyPLException {
    node.cond.accept(this);
    out.print(" {\n");
    incIndent();
    for (Stmt stmt : node.stmts) {
      out.print(getIndent());
      stmt.accept(this);
      out.print("\n");
    }
    decIndent();
    if (elseStmts) {
      out.print(getIndent() + "}\n");
    } else {
      out.print(getIndent() + "}");
    }
  }

  // while flag {
  // }
  public void visit(WhileStmt node) throws MyPLException {
    out.print("while ");
    visit(node.cond);
    out.print(" {\n");
    incIndent();
    for (Stmt stmt : node.stmts) {
      out.print(getIndent());
      stmt.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print(getIndent() + "}");
  }

  // for i from 1 upto n {
  // }
  public void visit(ForStmt node) throws MyPLException {
    out.print("for " + node.varName.lexeme() + " from ");
    visit(node.start);
    if (node.upto) {
      out.print(" upto ");
    }
    else 
      out.print(" downto ");
    visit(node.end);
    out.print(" {\n");
    incIndent();
    for (Stmt stmt : node.stmts) {
      out.print(getIndent());
      stmt.accept(this);
      out.print("\n");
    }
    decIndent();
    out.print(getIndent() + "}");
  }

  // return 5
  public void visit(ReturnStmt node) throws MyPLException {
    out.print("return ");
    if (node.expr != null) {
      visit(node.expr);
    }
  }

  // delete Node
  public void visit(DeleteStmt node) throws MyPLException {
    out.print("delete " + node.varName.lexeme());
  }

  // statement and rvalue node

  // exp(5, 2)
  public void visit(CallExpr node) throws MyPLException {
    out.print(node.funName.lexeme());
    out.print("(");
    if (node.args.size() > 0) {
      for (Expr arg : node.args.subList(0, node.args.size() - 1)) {
        visit(arg);
        out.print(", ");
      }
      visit(node.args.get(node.args.size() - 1));
    }
    out.print(")");
  }

  // rvalue nodes

  // "hello world"
  public void visit(SimpleRValue node) throws MyPLException {
    if (node.value.type() == TokenType.STRING_VAL) {
      out.print("\"" + node.value.lexeme() + "\"");
    } else if (node.value.type() == TokenType.CHAR_VAL) {
      out.print("'" + node.value.lexeme() + "'");
    } else
      out.print(node.value.lexeme());
  }

  // new Node
  public void visit(NewRValue node) throws MyPLException {
    out.print("new " + node.typeName.lexeme());
  }

  // node.next.value
  public void visit(IDRValue node) throws MyPLException {
    for (Token token : node.path) {
      out.print(token.lexeme());
    }
  }

  // neg 5
  public void visit(NegatedRValue node) throws MyPLException {
    out.print("neg ");
    visit(node.expr);
  }

  // expression node

  // (5 + (6 + (8 * 9)))
  public void visit(Expr node) throws MyPLException {
    boolean hasRest = node.rest != null ? true : false;
    if (hasRest)
      out.print("(");
    if (node.logicallyNegated) {
      out.print("(");
      out.print("not ");
    }
    node.first.accept(this);
    if (node.op != null) 
      out.print(" " + node.op.lexeme() + " ");
    if (node.rest != null) {
      visit(node.rest);
    }
    if (hasRest)
      out.print(")");
    if (node.logicallyNegated) 
      out.print(")");
  }

  // terms

  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  public void visit(ComplexTerm node) throws MyPLException {
    visit(node.expr);
  }
}
