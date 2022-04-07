/* 
 * File: Parser.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Parser for MyPL programs.
 */


public class Parser {

  private Lexer lexer = null; 
  private Token currToken = null;
  private boolean DEBUG = true;

  
  // constructor
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  // do the parse
  public void parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    advance();
    while (!match(TokenType.EOS)) {
      if (match(TokenType.TYPE))
        tdecl();
      else 
        fdecl();
    }
    advance(); // eat the EOS token
  }

  
  //------------------------------------------------------------ 
  // Helper Functions
  //------------------------------------------------------------

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

  
  //------------------------------------------------------------
  // Recursive Descent Functions 
  //------------------------------------------------------------


  /* TODO: Add the recursive descent functions below */
  private void tdecl() throws MyPLException {
    // <tdecl> ::= TYPE ID LBRACE <vdecls> RBRACE
    eat(TokenType.TYPE, "expecting 'type'");
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LBRACE, "expecting '{'");
    vdecls();
    eat(TokenType.RBRACE, "expecting '}'");    
  }

  private void vdecls() throws MyPLException {
    // <vdecls> ::= (<vdecl_stmt>)*
    while (!match(TokenType.RBRACE)) {
      vdeclStmt();
    }
  }
  
  private void fdecl() throws MyPLException {
    // <fdecl> ::= FUN (<dtype> | VOID) ID LPAREN <params> RPAREN LBRACE <stmts> RBRACE
    eat(TokenType.FUN, "expecting 'fun'");
    if (match(TokenType.VOID_TYPE)) {
      eat(TokenType.VOID_TYPE, "expecting 'void'");
    } else {
      dtype();
    }
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LPAREN, "expecting '('");
    params();
    eat(TokenType.RPAREN, "expecting ')'");
    eat(TokenType.LBRACE, "expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "expecting '}'");
  }

  private void params() throws MyPLException {
    // <params> ::= <dtype> ID (COMMA <dtype> ID)* | $\epsilon$
    if (match(TokenType.ID) || isPrimitiveType()) {
      dtype();
      eat(TokenType.ID, "expecting identifier");
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "expecting ','");
        dtype();
        eat(TokenType.ID, "expecting identifier");
      }
    }
  }

  private void dtype() throws MyPLException {
    // <dtype> ::= INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
    if (!isPrimitiveType()) {
      eat(TokenType.ID, "expecting primitive type");
    } else {
      advance();
    }
  }

  private void stmts() throws MyPLException {
    // <stmts> ::= (<stmt>)*
    while (!match(TokenType.RBRACE)) {
      if (match(TokenType.EOS)) {
        error("expecting '}'");
      }
      stmt();
    }
  }

  private void stmt() throws MyPLException {
    // <stmt> ::= <vdecl_stmt> | <assign_stmt> | <cond_stmt> | <while_stmt> | <for_stmt> | <call_expr> | <ret_stmt> | <delete_stmt>
    if (match(TokenType.VAR)) 
      vdeclStmt();
    else if (match(TokenType.ID)) {
      idrval();
      if (match(TokenType.ASSIGN))
        assignStmt();
      else if (match(TokenType.LPAREN))
        callExpr();
      else
        error("expecting '=' or '('");
    } else if (match(TokenType.IF))
      condStmt();
    else if (match(TokenType.WHILE))
      whileStmt();
    else if (match(TokenType.FOR))
      forStmt();
    else if (match(TokenType.RETURN))
      retStmt();
    else if (match(TokenType.DELETE))
      deleteStmt();
  }

  private void vdeclStmt() throws MyPLException {
    // <vdecl_stmt> ::= VAR (<dtype> | $\epsilon$) ID ASSIGN <expr>
    eat(TokenType.VAR, "expecting 'var'");
    if (isPrimitiveType()) {
      dtype();
      eat(TokenType.ID, "expecting identifier");
    } else if (match(TokenType.ID)) {
      advance();
      if (match(TokenType.ID)) {
        advance();
      }
    }

    eat(TokenType.ASSIGN, "expecting '='");
    expr();
  }

  private void assignStmt() throws MyPLException {
    // <assign_stmt> ::= <lvalue> ASSIGN <expr>
    lValue();
    eat(TokenType.ASSIGN, "expecting '='");
    expr();
  }

  private void lValue() throws MyPLException {
    // <lvalue> ::= ID (DOT ID)*
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "expecting '.'");
      eat(TokenType.ID, "expecting identifier");
    }
  }

  private void condStmt() throws MyPLException {
    // <cond_stmt> ::= IF <expr> LBRACE <stmts> RBRACE <condt>
    eat(TokenType.IF, "expecting 'if'");
    expr();
    eat(TokenType.LBRACE, "expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "expecting '}'");
    condt();
  }

  private void condt() throws MyPLException {
    // <condt> ::= ELIF <expr> LBRACE <stmts> RBRACE <condt> | ELSE LBRACE <stmts> RBRACE | $\epsilon$
    if (match(TokenType.ELIF)) {
      advance();
      expr();
      eat(TokenType.LBRACE, "expecting '{'");
      stmts();
      eat(TokenType.RBRACE, "expecting '}'");
      condt();
    } else if (match(TokenType.ELSE)) {
      advance();
      eat(TokenType.LBRACE, "expecting '{'");
      stmts();
      eat(TokenType.RBRACE, "expecting '}'");
    }
  }

  private void whileStmt() throws MyPLException {
    // <while_stmt> ::= WHILE <expr> LBRACE <stmts> RBRACE
    eat(TokenType.WHILE, "expecting 'while'");
    expr();
    eat(TokenType.LBRACE, "expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "expecting '}'");
  }

  private void forStmt() throws MyPLException {
    // <for_stmt> ::= FOR ID FROM <expr> (UPTO | DOWNTO) <expr> LBRACE <stmts> RBRACE
    eat(TokenType.FOR, "expecting 'for'");
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.FROM, "expecting 'from'");
    expr();
    if (match(TokenType.UPTO)) {
      eat(TokenType.UPTO, "expecting 'upto'");
    } else if (match(TokenType.DOWNTO)) {
      eat(TokenType.DOWNTO, "expecting 'downto'");
    } else {
      error("expecting 'upto' or 'downto'");
    }
    expr();
    eat(TokenType.LBRACE, "expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "expecting '}'");
  }

  private void callExpr() throws MyPLException {
    // <call_expr> ::= ID LPAREN <args> RPAREN 
    // eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LPAREN, "expecting '('");
    args();
    eat(TokenType.RPAREN, "expecting ')'");
  }

  private void args() throws MyPLException {
    // <args> ::= <expr> (COMMA <expr>)* | $\epsilon$
    if (!match(TokenType.RPAREN)) {
      expr();
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "expecting ','");
        expr();
      }
    }
  }

  private void retStmt() throws MyPLException {
    // <ret_stmt> ::= RETURN (<expr> | $\epsilon$)
    eat(TokenType.RETURN, "expecting 'return'");
    if (isExpr()) {
      expr();
    }
  }

  private void deleteStmt() throws MyPLException {
    // <delete_stmt> ::= DELETE ID
    eat(TokenType.DELETE, "expecting 'delete'");
    eat(TokenType.ID, "expecting identifier");
  }

  private void expr() throws MyPLException {
    // <expr> ::= (<rvalue> | NOT <expr> | LPAREN <expr> RPAREN) (<operator> <expr> | $\epsilon$)
    if (match(TokenType.NOT)) {
      eat(TokenType.NOT, "expecting 'not'");
      expr();
    } else if (match(TokenType.LPAREN)) {
      eat(TokenType.LPAREN, "expecting '('");
      expr();
      eat(TokenType.RPAREN, "expecting ')'");
    } else {
      rValue();
    }
    if (isOperator()) {
      operator();
      expr();
    }
  }

  private void operator() throws MyPLException {
    // <operator> ::= PLUS | MINUS| DIVIDE| MULTIPLY | MODULO | AND | OR | EQUAL | LESS_THAN | GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
    if (isOperator()) {
      advance();
    } else {
      error("expecting operator");
    }
  }

  private void rValue() throws MyPLException {
    // <rvalue> ::= <pval> | NIL | NEW ID | <idrval> | <call_expr> | NEG <expr>
    if (isPrimitiveValue()) {
      pval();
    } else if (match(TokenType.NIL)) {
      eat(TokenType.NIL, "expecting 'nil'");
    } else if (match(TokenType.NEW)) {
      eat(TokenType.NEW, "expecting 'new'");
      eat(TokenType.ID, "expecting identifier");
    } else if (match(TokenType.NEG)) {
      eat(TokenType.NEG, "expecting '-'");
      expr();
    } else if (match(TokenType.ID)) {
      idrval();
      if (match(TokenType.LPAREN)) {
        eat(TokenType.LPAREN, "expecting '('");
        args();
        eat(TokenType.RPAREN, "expecting ')'");
      }
    } else {
      debug(currToken.toString());
      error("expecting primitive value, 'nil', 'new', identifier, or '('");
    }
  }

  private void pval() throws MyPLException {
    // <pval> ::= INT_VAL | DOUBLE_VAL | BOOL_VAL | CHAR_VAL | STRING_VAL
    if (isPrimitiveValue()) {
      advance();
    } else {
      error("expecting primitive value");
    }
  }

  private void idrval() throws MyPLException {
    // <idrval> ::= ID (DOT ID)*
    eat(TokenType.ID, "expecting identifier");
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "expecting '.'");
      eat(TokenType.ID, "expecting identifier");
    }
  }
}
