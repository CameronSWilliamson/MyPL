/*
 * File: StaticChecker.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Static checker for the MyPL language.
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


// NOTE: Some of the following are filled in, some partly filled in,
// and most left for you to fill in. The helper functions are provided
// for you to use as needed. 


public class StaticChecker implements Visitor {

  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();
  // the current expression type
  private String currType = null;
  // the program's user-defined (record) types and function signatures
  private TypeInfo typeInfo = null;

  //--------------------------------------------------------------------
  // helper functions:
  //--------------------------------------------------------------------
  
  // generate an error
  private void error(String msg, Token token) throws MyPLException {
    String s = msg;
    if (token != null)
      s += " near line " + token.line() + ", column " + token.column();
    throw MyPLException.StaticError(s);
  }

  // return all valid types
  // assumes user-defined types already added to symbol table
  private List<String> getValidTypes() {
    List<String> types = new ArrayList<>();
    types.addAll(Arrays.asList("int", "double", "bool", "char", "string",
                               "void"));
    for (String type : typeInfo.types())
      if (symbolTable.get(type).equals("type"))
        types.add(type);
    return types;
  }

  // return the build in function names
  private List<String> getBuiltinFunctions() {
    return Arrays.asList("print", "read", "length", "get", "stoi", "stod",
      "itos", "itod", "dtos", "dtoi", "timestart", "timeend", "timedelta");
  }

  // check if given token is a valid function signature return type
  private void checkReturnType(Token typeToken) throws MyPLException {
    if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  // helper to check if the given token is a valid parameter type
  private void checkParamType(Token typeToken) throws MyPLException {
    if (typeToken.equals("void"))
      error("'void' is an invalid parameter type", typeToken);
    else if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  
  // helpers to get first token from an expression for calls to error
  
  private Token getFirstToken(Expr expr) {
    return getFirstToken(expr.first);
  }

  private Token getFirstToken(ExprTerm term) {
    if (term instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)term).rvalue);
    else
      return getFirstToken(((ComplexTerm)term).expr);
  }

  private Token getFirstToken(RValue rvalue) {
    if (rvalue instanceof SimpleRValue)
      return ((SimpleRValue)rvalue).value;
    else if (rvalue instanceof NewRValue)
      return ((NewRValue)rvalue).typeName;
    else if (rvalue instanceof IDRValue)
      return ((IDRValue)rvalue).path.get(0);
    else if (rvalue instanceof CallExpr)
      return ((CallExpr)rvalue).funName;
    else 
      return getFirstToken(((NegatedRValue)rvalue).expr);
  }

  
  //---------------------------------------------------------------------
  // constructor
  //--------------------------------------------------------------------
  
  public StaticChecker(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
  

  //--------------------------------------------------------------------
  // top-level nodes
  //--------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {
    // push the "global" environment
    symbolTable.pushEnvironment();

    // (1) add each user-defined type name to the symbol table and to
    // the list of rec types, check for duplicate names
    for (TypeDecl tdecl : node.tdecls) {
      String t = tdecl.typeName.lexeme();
      if (symbolTable.nameExists(t))
        error("type '" + t + "' already defined", tdecl.typeName);
      // add as a record type to the symbol table
      symbolTable.add(t, "type");
      // add initial type info (rest added by TypeDecl visit function)
      typeInfo.add(t);
    }
    
    // table check for duplicate names
    for (FunDecl fdecl : node.fdecls) {
      String funName = fdecl.funName.lexeme();
      // make sure not redefining built-in functions
      if (getBuiltinFunctions().contains(funName)) {
        String m = "cannot redefine built in function " + funName;
        error(m, fdecl.funName);
      }
      // check if function already exists
      if (symbolTable.nameExists(funName))
        error("function '" + funName + "' already defined", fdecl.funName);

      // ...
      

      // make sure the return type is a valid type
      checkReturnType(fdecl.returnType);
      // add to the symbol table as a function
      symbolTable.add(funName, "fun");
      // add to typeInfo
      typeInfo.add(funName);

      // ...
      List<String> paramNames = new ArrayList<>();
      for (FunParam param : fdecl.params) {
        checkParamType(param.paramType);
        typeInfo.add(funName, param.paramName.lexeme(), param.paramType.lexeme());
        if (paramNames.contains(param.paramName.lexeme()))
          error("Parameter already defined", param.paramName);
        paramNames.add(param.paramName.lexeme());
      }

      // add the return type
      typeInfo.add(funName, "return", fdecl.returnType.lexeme());
    }

    // signature
    // ...
    FunDecl main = null;
    List<String> builtInFxns = getBuiltinFunctions();
    for (FunDecl fdecl : node.fdecls) {
      if (builtInFxns.contains(fdecl.funName.lexeme()))
        error("Cannot override a built in type.", fdecl.funName);
      if (fdecl.funName.lexeme().equals("main")) {
        main = fdecl;
      }
    }
    if (main == null)
      error("Must define a function named 'main'", null);
    if (main.params.size() != 0)
      error("'main' must have no parameters", main.funName);
    if (!main.returnType.lexeme().equals("void"))
      error("'main' must have return type 'void'", main.returnType);
    
    // check each type and function
    for (TypeDecl tdecl : node.tdecls) 
      tdecl.accept(this);
    for (FunDecl fdecl : node.fdecls) 
      fdecl.accept(this);

    // all done, pop the global table
    symbolTable.popEnvironment();
  }
  

  public void visit(TypeDecl node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (VarDeclStmt varDeclStmt : node.vdecls) {
      varDeclStmt.accept(this);
      if (currType == "void")
        typeInfo.add(node.typeName.lexeme(), varDeclStmt.varName.lexeme(), varDeclStmt.typeName.lexeme());
      else
        typeInfo.add(node.typeName.lexeme(), varDeclStmt.varName.lexeme(), currType);
    }
    symbolTable.popEnvironment();
  }

  
  public void visit(FunDecl node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (String component : typeInfo.components(node.funName.lexeme()))
      symbolTable.add(component, typeInfo.get(node.funName.lexeme(), component));
    for (Stmt stmt : node.stmts) 
      stmt.accept(this);
    symbolTable.popEnvironment();
  }


  //--------------------------------------------------------------------
  // statement nodes
  //--------------------------------------------------------------------
  
  public void visit(VarDeclStmt node) throws MyPLException {
    String varName = node.varName.lexeme();

    if (symbolTable.nameExistsInCurrEnv(varName))
      error("Variable already defined", node.varName);
    
    node.expr.accept(this);
    String rhsType = currType;
  
    if (node.typeName == null) {
      // no type specified, use rhs type
      if (rhsType.equals("void"))
        error("Cannot infer type of 'nil'", node.varName);
      symbolTable.add(varName, rhsType);
    } else {
      String typeName = node.typeName.lexeme();
      if (!rhsType.equals(typeName) && !rhsType.equals("void"))
        error("Type mismatch, cannot mix " + typeName + " with " + rhsType, node.typeName);
      symbolTable.add(varName, node.typeName.lexeme());
    }
  }
  

  public void visit(AssignStmt node) throws MyPLException {
    node.expr.accept(this);
    String rhsType = currType;
    String varName = node.lvalue.get(0).lexeme();

    if (!symbolTable.nameExists(varName))
      error("\"" + varName + "\" not defined", node.lvalue.get(0));


    complexPaths(node.lvalue);
    String lhsType = currType;

    if (!rhsType.equals("void") && !lhsType.equals(rhsType))
      error("expecting " + lhsType + ", found " + rhsType, node.lvalue.get(0));

  }
  
  private void complexPaths(List<Token> path) throws MyPLException {
    if (!symbolTable.nameExists(path.get(0).lexeme())) 
      error("\"" + path.get(0).lexeme() + "\" not defined", path.get(0));
    if (symbolTable.get(path.get(0).lexeme()).equals("type"))
      error("\"" + path.get(0).lexeme() + "\" is a type", path.get(0));
    if (symbolTable.get(path.get(0).lexeme()).equals("fun"))
      error("\"" + path.get(0).lexeme() + "\" is a function", path.get(0));
    String typeName = symbolTable.get(path.get(0).lexeme());
    currType = typeName;

    for (int i = 2; i < path.size(); i += 2) {
      String name = path.get(i).lexeme();
      String componentType = typeInfo.get(typeName, name);
      if (componentType == null)
        // System.out.println(name);
        error("\"" + typeName + "\" does not have a component named \"" + name + "\"", path.get(i));
      currType = componentType;
      typeName = componentType;
    }
  }
  
  
  public void visit(CondStmt node) throws MyPLException {
    basicElif(node.ifPart);
    for (BasicIf elif : node.elifs)
      basicElif(elif);

    if (node.elseStmts != null) {
      symbolTable.pushEnvironment();
      for (Stmt stmt : node.elseStmts)
        stmt.accept(this);
      symbolTable.popEnvironment();
    }
  }
  
  private void basicElif(BasicIf node) throws MyPLException {
    node.cond.accept(this);
    if (!currType.equals("bool"))
      error("condition must be boolean", node.cond.op);
    symbolTable.pushEnvironment();
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }
  

  public void visit(WhileStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    node.cond.accept(this);
    if (!currType.equals("bool"))
      error("condition must be boolean", node.cond.op);
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }
  

  public void visit(ForStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    node.start.accept(this);
    if (!currType.equals("int"))
      error("start must be integer", node.start.op);
    node.end.accept(this);
    if (!currType.equals("int"))
      error("end must be integer", node.end.op);
    
    symbolTable.add(node.varName.lexeme(), "int");
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }
  
  
  public void visit(ReturnStmt node) throws MyPLException {
    if (node.expr != null)
      node.expr.accept(this);
    else {
      currType = "void";
    }
    String retType = symbolTable.get("return");
    if (!currType.equals(retType) && !currType.equals("void"))
      error("return type mismatch, " + currType + " != " + retType, null);
  }
  
  public void visit(DeleteStmt node) throws MyPLException {
    String varName = node.varName.lexeme();
    String varType = symbolTable.get(varName);
    if (Arrays.asList("int", "double", "bool", "char", "string", "void").contains(varType))
      error("Cannot delete a variable of type " + varType, node.varName);
    else if (symbolTable.get(varName).equals("fun") || symbolTable.get(varName).equals("type"))
      error("Cannot delete a function or type", node.varName);
  }
  

  //----------------------------------------------------------------------
  // statement and rvalue node
  //----------------------------------------------------------------------

  private void checkBuiltIn(CallExpr node) throws MyPLException {
    String funName = node.funName.lexeme();
    if (funName.equals("print")) {
      // has to have one argument, any type is allowed
      if (node.args.size() != 1)
        error("print expects one argument", node.funName);
      currType = "void";
    }
    else if (funName.equals("read")) {
      // no arguments allowed
      if (node.args.size() != 0)
        error("read takes no arguments", node.funName);
      currType = "string";
    }
    else if (funName.equals("length")) {
      // one string argument
      if (node.args.size() != 1)
        error("length expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in length", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("get")) {
      if (node.args.size() != 2)
        error("get expects two arguments", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int")) 
        error("expecting int in get", getFirstToken(e));
      e = node.args.get(1);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in get", getFirstToken(e));
      currType = "char";
    }
    else if (funName.equals("stoi")) {
      if (node.args.size() != 1)
        error("stoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in stoi", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("stod")) {
      if (node.args.size() != 1)
        error("stod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in stod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("itos")) {
      if (node.args.size() != 1)
        error("itos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int"))
        error("expecting int in itos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("itod")) {
      if (node.args.size() != 1)
        error("itod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int"))
        error("expecting int in itod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("dtos")) {
      if (node.args.size() != 1)
        error("dtos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double"))
        error("expecting double in dtos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("dtoi")) {
      if (node.args.size() != 1)
        error("dtoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double"))
        error("expecting double in dtoi", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("timestart")) {
      if (node.args.size() != 0)
        error("timestart takes no arguments", node.funName);
      currType = "void";
    }
    else if (funName.equals("timeend")) {
      if (node.args.size() != 0)
        error("timeend takes no arguments", node.funName);
      currType = "void";
    }
    else if (funName.equals("timedelta")) {
      if (node.args.size() != 0)
        error("timedelta takes no arguments", node.funName);
      currType = "double";
    }
  }

  
  public void visit(CallExpr node) throws MyPLException {
    String funcName = node.funName.lexeme();
    if (getBuiltinFunctions().contains(funcName)) {
      checkBuiltIn(node);
      return;
    }
    if (!symbolTable.nameExists(funcName))
      error("function " + funcName + " is not defined", node.funName);
    ArrayList<String> components = new ArrayList<>(typeInfo.components(funcName));
    if (components.size() - 1 != node.args.size())
      error("function " + funcName + " expects " + components.size() + " arguments", node.funName);
    for (int i = 0; i < node.args.size(); i++) {
      Expr currExpr = node.args.get(i);
      currExpr.accept(this);
      if (!currType.equals(typeInfo.get(funcName, components.get(i))) && !currType.equals("void"))
        error("expecting " + typeInfo.get(funcName, components.get(i)) + " in argument " + (i + 1) + " got " + currType, node.funName);
    }
    currType = typeInfo.get(funcName, "return");
  }
  

  //----------------------------------------------------------------------
  // rvalue nodes
  //----------------------------------------------------------------------
  
  public void visit(SimpleRValue node) throws MyPLException {
    TokenType tokenType = node.value.type();
    if (tokenType == TokenType.INT_VAL)
      currType = "int";
    else if (tokenType == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (tokenType == TokenType.BOOL_VAL)
      currType = "bool";
    else if (tokenType == TokenType.CHAR_VAL)    
      currType = "char";
    else if (tokenType == TokenType.STRING_VAL)
      currType = "string";
    else if (tokenType == TokenType.NIL)
      currType = "void";
  }
  
    
  public void visit(NewRValue node) throws MyPLException {
    String typeName = node.typeName.lexeme();
    if (!symbolTable.nameExists(typeName))
      error("\"" + node.typeName.lexeme() + "\" not defined", node.typeName);
    if (!symbolTable.get(typeName).equals("type"))
      error("\"" + node.typeName.lexeme() + "\" is not a type", node.typeName);
    currType = node.typeName.lexeme();
  }
  
      
  public void visit(IDRValue node) throws MyPLException {
    complexPaths(node.path);
  }
  
      
  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    if (!currType.equals("int") && !currType.equals("double"))
      error("expecting int or double in negation", getFirstToken(node.expr));
  }
  

  //----------------------------------------------------------------------
  // expression node
  //----------------------------------------------------------------------
  
  public void visit(Expr node) throws MyPLException {
    if (node == null) {
      currType = "void";
      return;
    }
    node.first.accept(this);
    String lhsType = currType;
    if (node.rest != null) {
      TokenType operator = node.op.type();
      node.rest.accept(this);
      String rhsType = currType;

      if (operator == TokenType.PLUS) {
        if (lhsType.equals("int") && rhsType.equals("int"))
          currType = "int";
        else if (lhsType.equals("double") && rhsType.equals("double"))
          currType = "double";
        else if ((lhsType.equals("string") && (rhsType.equals("string") || rhsType.equals("char"))) ||
                 (lhsType.equals("char") && rhsType.equals("string")))
          currType = "string";
        else
          error("invalid operands to binary +", node.rest.op);
      } else if (operator == TokenType.MINUS) {
        if (lhsType.equals("int") && rhsType.equals("int"))
          currType = "int";
        else if (lhsType.equals("double") && rhsType.equals("double"))
          currType = "double";
        else
          error("invalid operands to binary -", node.rest.op);
      } else if (operator == TokenType.MULTIPLY) {

      } else if (operator == TokenType.DIVIDE) {

      } else if (operator == TokenType.MODULO) {
        if (lhsType.equals("int") && rhsType.equals("int"))
          currType = "int";
        else
          error("invalid operands to binary %", node.rest.op);
      } else if (operator == TokenType.LESS_THAN || operator == TokenType.LESS_THAN_EQUAL ||
                 operator == TokenType.GREATER_THAN || operator == TokenType.GREATER_THAN_EQUAL) {
        if (!lhsType.equals(rhsType)) {
          error("invalid operands to binary comparison", node.rest.op);
        } else if ((lhsType.equals("int") || lhsType.equals("double") || lhsType.equals("char") || lhsType.equals("string"))) {
          currType = "bool";
        } else {
          error("invalid operands to binary comparison", node.rest.op);
        }
        currType = "bool";
      } else if (operator == TokenType.EQUAL || operator == TokenType.NOT_EQUAL) {
        if (!lhsType.equals(rhsType) && !lhsType.equals("void") && !rhsType.equals("void")) {
          error("unmatched types for binary equality comparison", node.op);
        }
        else
          currType = "bool";
      }
    }

    if (node.logicallyNegated && !currType.equals("bool"))
      error("boolean required for negation statement", node.op);
  }


  //----------------------------------------------------------------------
  // terms
  //----------------------------------------------------------------------
  
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }
  

  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }
}
