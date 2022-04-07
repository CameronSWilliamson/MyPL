/* 
 * File: ASTParser.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: MyPL Language parser that builds an abstract syntax tree on its way down.
 */

import java.util.ArrayList;
import java.util.List;

public class ASTParser {

  private Lexer lexer = null;
  private Token currToken = null;
  private final boolean DEBUG = false;
  // private final boolean DEBUG = true;

  /** 
   */
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   */
  public Program parse() throws MyPLException {
    // <program> ::= (<tdecl> | <fdecl>)*
    Program progNode = new Program();
    advance();
    while (!match(TokenType.EOS)) {
      debug("parse");
      if (match(TokenType.TYPE))
        tdecl(progNode);
      else
        fdecl(progNode);
    }
    advance(); // eat the EOS token
    return progNode;
  }

  // ------------------------------------------------------------
  // Helper Functions
  // ------------------------------------------------------------

  // get next token
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // advance if current token is of given type, otherwise error
  private void eat(TokenType t, String msg) throws MyPLException {
    if (match(t))
      advance();
    else
      error(msg);
  }

  // true if current token is of type t
  private boolean match(TokenType t) {
    return currToken.type() == t;
  }

  // throw a formatted parser error
  private void error(String msg) throws MyPLException {
    String s = msg + ", found '" + currToken.lexeme() + "' ";
    s += "at line " + currToken.line();
    s += ", column " + currToken.column();
    throw MyPLException.ParseError(s);
  }

  // output a debug message (if DEBUG is set)
  private void debug(String msg) {
    if (DEBUG)
      System.out.println("[debug]: " + msg);
  }

  // return true if current token is a (non-id) primitive type
  private boolean isPrimitiveType() {
    return match(TokenType.INT_TYPE) || match(TokenType.DOUBLE_TYPE) ||
        match(TokenType.BOOL_TYPE) || match(TokenType.CHAR_TYPE) ||
        match(TokenType.STRING_TYPE);
  }

  // return true if current token is a (non-id) primitive value
  private boolean isPrimitiveValue() {
    return match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
        match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
        match(TokenType.STRING_VAL);
  }

  // return true if current token starts an expression
  private boolean isExpr() {
    return match(TokenType.NOT) || match(TokenType.LPAREN) ||
        match(TokenType.NIL) || match(TokenType.NEW) ||
        match(TokenType.ID) || match(TokenType.NEG) ||
        match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
        match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
        match(TokenType.STRING_VAL);
  }

  private boolean isOperator() {
    return match(TokenType.PLUS) || match(TokenType.MINUS) ||
        match(TokenType.DIVIDE) || match(TokenType.MULTIPLY) ||
        match(TokenType.MODULO) || match(TokenType.AND) ||
        match(TokenType.OR) || match(TokenType.EQUAL) ||
        match(TokenType.LESS_THAN) || match(TokenType.GREATER_THAN) ||
        match(TokenType.LESS_THAN_EQUAL) || match(TokenType.GREATER_THAN_EQUAL) ||
        match(TokenType.NOT_EQUAL);
  }

  // ------------------------------------------------------------
  // Recursive Descent Functions
  // ------------------------------------------------------------

  // TODO: Add your recursive descent functions from HW-3
  // and extend them to build up the AST

  /**
   * <program> ::= (<tdecl> | <fdecl>)*
   * 
   * @param progNode
   * @throws MyPLException
   */
  private void tdecl(Program progNode) throws MyPLException {
    debug("tdecl");
    TypeDecl td = new TypeDecl();
    eat(TokenType.TYPE, "expecting 'type'");
    td.typeName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LBRACE, "expecting '{'");
    vdecls(td);
    progNode.tdecls.add(td);
    eat(TokenType.RBRACE, "expecting '}'");
  }

  /**
   * (<vdecl_stmt>)*
   * 
   * @param tdecl
   * @throws MyPLException
   */
  private void vdecls(TypeDecl tdecl) throws MyPLException {
    debug("vdecls");
    List<Stmt> tmpList = new ArrayList<>();
    while (!match(TokenType.RBRACE)) {
      vdecl_stmt(tmpList);
    }

    for (Stmt s : tmpList) {
      tdecl.vdecls.add((VarDeclStmt) s);
    }
  }

  /**
   * FUN (<dtype> | VOID) ID LPAREN <params> RPAREN LBRACE <stmts> RBRACE
   * 
   * @param progNode
   * @throws MyPLException
   */
  private void fdecl(Program progNode) throws MyPLException {
    debug("fdecl");
    FunDecl fd = new FunDecl();
    eat(TokenType.FUN, "expecting 'fun'");
    if (match(TokenType.VOID_TYPE)) {
      fd.returnType = currToken;
      advance();
    } else {
      fd.returnType = currToken;
      dtype();
    }
    fd.funName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LPAREN, "expecting '('");
    params(fd.params);
    eat(TokenType.RPAREN, "expecting ')'");
    eat(TokenType.LBRACE, "expecting '{'");
    stmts(fd.stmts);
    eat(TokenType.RBRACE, "expecting '}'");
    progNode.fdecls.add(fd);
  }

  /**
   * <dtype> ID (COMMA <dtype> ID)* | e
   * 
   * @param funParams
   * @throws MyPLException
   */
  private void params(List<FunParam> funParams) throws MyPLException {
    if (match(TokenType.ID) || isPrimitiveType()) {
      debug("first param");
      FunParam fp = new FunParam();
      fp.paramType = currToken;
      dtype();
      fp.paramName = currToken;
      eat(TokenType.ID, "expecting identifier");
      funParams.add(fp);
      while (match(TokenType.COMMA)) {
        debug("more params");
        eat(TokenType.COMMA, "expecting ','");
        fp = new FunParam();
        fp.paramType = currToken;
        dtype();
        fp.paramName = currToken;
        eat(TokenType.ID, "expecting identifier");
        funParams.add(fp);
      }
    }
  }

  /**
   * INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
   * 
   * @throws MyPLException
   */
  private void dtype() throws MyPLException {
    if (!isPrimitiveType()) {
      eat(TokenType.ID, "expecting identifier");
    } else {
      advance();
    }
  }

  /**
   * (stmt)*
   * 
   * @param stmtList
   * @throws MyPLException
   */
  private void stmts(List<Stmt> stmtList) throws MyPLException {
    while (!match(TokenType.RBRACE)) {
      debug("stmts");
      if (match(TokenType.EOS)) {
        error("expecting '}'");
      }
      stmt(stmtList);
    }
  }

  /**
   * <vdecl_stmt> | <assign_stmt> | <cond_stmt> | <while_stmt> | <for_stmt> |
   * <call_expr> | <return_stmt> | <delete_stmt>
   * 
   * @param stmtList
   * @throws MyPLException
   */
  private void stmt(List<Stmt> stmtList) throws MyPLException {
    debug("stmt");
    if (match(TokenType.VAR)) {
      vdecl_stmt(stmtList);
    } else if (match(TokenType.ID)) {
      Token id = currToken;
      advance();
      if (match(TokenType.LPAREN)) {
        debug("call_expr");
        CallExpr ce = new CallExpr();
        ce.funName = id;
        advance();
        args(ce.args);
        eat(TokenType.RPAREN, "expecting ')'");
        stmtList.add(ce);
      } else {
        debug("assign_stmt");
        AssignStmt as = new AssignStmt();
        as.lvalue.add(id);
        assign_stmt(as);
        // while (match(TokenType.DOT)) {
        //   debug("dot");
        //   as.lvalue.add(currToken);
        //   advance();
        //   as.lvalue.add(currToken);
        //   eat(TokenType.ID, "expecting identifier");
        // }
        // assign_stmt(as);
        stmtList.add(as);
      }
    } else if (match(TokenType.IF)) {
      cond_stmt(stmtList);
    } else if (match(TokenType.WHILE)) {
      while_stmt(stmtList);
    } else if (match(TokenType.FOR)) {
      for_stmt(stmtList);
    } else if (match(TokenType.RETURN)) {
      ret_stmt(stmtList);
    } else if (match(TokenType.DELETE)) {
      delete_stmt(stmtList);
    } else {
      error("expecting statement");
    }
  }

  /**
   * VAR (<dtype> | e) ID ASSIGN <expr>
   * @param stmtList
   * @throws MyPLException
   */
  private void vdecl_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("vdecl_stmt");
    VarDeclStmt vds = new VarDeclStmt();
    eat(TokenType.VAR, "expecting 'var'");
    if (isPrimitiveType()) {
      vds.typeName = currToken;
      dtype();
      vds.varName = currToken;
      eat(TokenType.ID, "expecting identifier");
    } else if (match(TokenType.ID)) {
      vds.typeName = currToken;
      advance();
      if (match(TokenType.ID)) {
        vds.varName = currToken;
        advance();
      } else {
        vds.varName = vds.typeName;
        vds.typeName = null;
      }
    }
    eat(TokenType.ASSIGN, "expecting '='");
    vds.expr = new Expr();
    expr(vds.expr);
    stmtList.add(vds);
  }

  /**
   * <lvalue> ASSIGN <expr>
   * @param as
   * @throws MyPLException
   */
  private void assign_stmt(AssignStmt as) throws MyPLException {
    debug("assign_stmt");
    lvalue(as.lvalue);
    eat(TokenType.ASSIGN, "expecting '='");
    Expr ex = new Expr();
    expr(ex);
    as.expr = ex;
  }

  /**
   * ID (DOT ID)*
   * @param tokens
   * @throws MyPLException
   */
  private void lvalue(List<Token> tokens) throws MyPLException {
    debug("lvalue");
    while (match(TokenType.DOT)) {
      tokens.add(currToken);
      eat(TokenType.DOT, "expecting '.'");
      tokens.add(currToken);
      eat(TokenType.ID, "expecting identifier");
    }
  }

  /**
   * IF <expr> LBRACE <stmts> RBRACE <condt>
   * @param stmtList
   * @throws MyPLException
   */
  private void cond_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("cond_stmt");
    CondStmt cs = new CondStmt();
    BasicIf ifPart = new BasicIf();
    eat(TokenType.IF, "expecting 'if'");
    ifPart.cond = new Expr();
    expr(ifPart.cond);
    eat(TokenType.LBRACE, "expecting '{'");
    stmts(ifPart.stmts);
    eat(TokenType.RBRACE, "expecting '}'");
    condt(cs);
    cs.ifPart = ifPart;
    stmtList.add(cs);
  }

  /**
   * ELIF <expr> LBRACE <stmts> RBRACE <condt> | ELSE LBRACE <stmts> RBRACE | e
   * @param cs
   * @throws MyPLException
   */
  private void condt(CondStmt cs) throws MyPLException {
    debug("condt");
    if (match(TokenType.ELIF)) {
      BasicIf elifPart = new BasicIf();
      advance();
      elifPart.cond = new Expr();
      expr(elifPart.cond);
      eat(TokenType.LBRACE, "expecting '{'");
      stmts(elifPart.stmts);
      eat(TokenType.RBRACE, "expecting '}'");
      cs.elifs.add(elifPart);
      condt(cs);
    } else if (match(TokenType.ELSE)) {
      advance();
      eat(TokenType.LBRACE, "expecting '{'");
      cs.elseStmts = new ArrayList<>();
      stmts(cs.elseStmts);
      eat(TokenType.RBRACE, "expecting '}'");
    }
  }

  /**
   * WHILE <expr> LBRACE <stmts> RBRACE
   * @param stmtList
   * @throws MyPLException
   */
  private void while_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("while_stmt");
    WhileStmt ws = new WhileStmt();
    eat(TokenType.WHILE, "expecting 'while'");
    ws.cond = new Expr();
    expr(ws.cond);
    eat(TokenType.LBRACE, "expecting '{'");
    stmts(ws.stmts);
    eat(TokenType.RBRACE, "expecting '}'");
    stmtList.add(ws);
  }

  /**
   * FOR ID FROM <expr> (UPTO | DOWNTO) <expr> LBRACE <stmts> RBRACE
   * @param stmtList
   * @throws MyPLException
   */
  private void for_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("for_stmt");
    ForStmt fs = new ForStmt();
    eat(TokenType.FOR, "expecting 'for'");
    fs.varName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.FROM, "expecting 'from'");
    fs.start = new Expr();
    expr(fs.start);
    if (match(TokenType.UPTO)) {
      fs.upto = true;
      eat(TokenType.UPTO, "expecting 'upto'");
    } else if (match(TokenType.DOWNTO)) {
      fs.upto = false;
      eat(TokenType.DOWNTO, "expecting 'downto'");
    } else {
      error("expecting 'upto' or 'downto'");
    }
    fs.end = new Expr();
    expr(fs.end);
    eat(TokenType.LBRACE, "expecting '{'");
    stmts(fs.stmts);
    eat(TokenType.RBRACE, "expecting '}'");
    stmtList.add(fs);
  }

  /**
   * <expr> (COMMA <expr>)* | e
   * @param args
   * @throws MyPLException
   */
  private void args(List<Expr> argList) throws MyPLException {
    debug("args");
    if (!match(TokenType.RPAREN)) {
      Expr arg = new Expr();
      expr(arg);
      argList.add(arg);
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "expecting ','");
        arg = new Expr();
        expr(arg);
        argList.add(arg);
      }
    }
  }

  /**
   * RETURN (<expr> | e)
   * @param stmtList
   * @throws MyPLException
   */
  private void ret_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("ret_stmt");
    ReturnStmt rs = new ReturnStmt();
    eat(TokenType.RETURN, "expecting 'return'");
    if (isExpr()) {
      rs.expr = new Expr();
      expr(rs.expr);
    }
    stmtList.add(rs);
  }

  /**
   * DELETE ID
   * @param stmtList
   * @throws MyPLException
   */
  private void delete_stmt(List<Stmt> stmtList) throws MyPLException {
    debug("delete_stmt");
    DeleteStmt ds = new DeleteStmt();
    eat(TokenType.DELETE, "expecting 'delete'");
    ds.varName = currToken;
    eat(TokenType.ID, "expecting identifier");
    stmtList.add(ds);
  }

  /**
   * (<rvalue> | NOT <expr> | LPAREN <expr> RPAREN) (<operator> <expr> | e)
   * @param ex
   * @throws MyPLException
   */
  private void expr(Expr ex) throws MyPLException {
    debug("expr");
    if (match(TokenType.NOT)) {
      advance();
      ex.logicallyNegated = !ex.logicallyNegated;
      ComplexTerm ct = new ComplexTerm();
      ct.expr = new Expr();
      expr(ct.expr);
      ex.first = ct;
    } else if (match(TokenType.LPAREN)) {
      advance();
      ComplexTerm ct = new ComplexTerm();
      ct.expr = new Expr();
      expr(ct.expr);
      ex.first = ct;
      eat(TokenType.RPAREN, "expecting ')'");
    } else {
      if (ex == null)
        System.out.println("null");
      ex.first = new SimpleTerm();
      rvalue((SimpleTerm) ex.first);
    }
    if (isOperator()) {
      ex.op = currToken;
      operator();
      ex.rest = new Expr();
      expr(ex.rest);
    }
  }

  private void operator() throws MyPLException {
    debug("operator");
    if (isOperator())
      advance();
    else
      error("expecting operator");
  }

  private void rvalue(SimpleTerm simpleTerm) throws MyPLException {
    debug("rvalue");
    if (match(TokenType.NEG)) {
      NegatedRValue nrv = new NegatedRValue();
      advance();
      nrv.expr = new Expr();
      expr(nrv.expr);
      simpleTerm.rvalue = nrv;
    } else if (match(TokenType.NEW)) {
      NewRValue nrv = new NewRValue();
      advance();
      nrv.typeName = currToken;
      eat(TokenType.ID, "expecting identifier");
      simpleTerm.rvalue = nrv;
    } else if (match(TokenType.NIL)) {
      SimpleRValue srv = new SimpleRValue();
      srv.value = currToken;
      advance();
      simpleTerm.rvalue = srv;
    } else if (isPrimitiveValue()) {
      SimpleRValue srv = new SimpleRValue();
      pval(srv);
      simpleTerm.rvalue = srv;
    } else if (match(TokenType.ID)) {
      Token id = currToken;
      advance();
      if (match(TokenType.LPAREN)) {
        CallExpr ce = new CallExpr();
        ce.funName = id;
        advance();
        args(ce.args);
        eat(TokenType.RPAREN, "expecting ')'");
        simpleTerm.rvalue = ce;
      } else {
        IDRValue idrv = new IDRValue();
        idrv.path.add(id);
        while (match(TokenType.DOT)) {
          idrv.path.add(currToken);
          advance();
          idrv.path.add(currToken);
          eat(TokenType.ID, "expecting identifier");
        }
        simpleTerm.rvalue = idrv;
      }
    }
  }

  private void pval(SimpleRValue srv) throws MyPLException {
    debug("pval");
    if (isPrimitiveValue()) {
      srv.value = currToken;
      advance();
    } else {
      error("expecting primitive value");
    }
  }
}
