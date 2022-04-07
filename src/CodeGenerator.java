/*
 * File: CodeGenerator.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Compiles a MyPl program into a VM Instructions.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CodeGenerator implements Visitor {

  // the user-defined type and function type information
  private TypeInfo typeInfo = null;

  // the virtual machine to add the code to
  private VM vm = null;

  // the current frame
  private VMFrame currFrame = null;

  // mapping from variables to their indices (in the frame)
  private Map<String, Integer> varMap = null;

  // the current variable index (in the frame)
  private int currVarIndex = 0;

  // to keep track of the typedecl objects for initialization
  Map<String, TypeDecl> typeDecls = new HashMap<>();

  // ----------------------------------------------------------------------
  // HELPER FUNCTIONS
  // ----------------------------------------------------------------------

  // helper function to clean up uneeded NOP instructions
  private void fixNoOp() {
    int nextIndex = currFrame.instructions.size();
    // check if there are any instructions
    if (nextIndex == 0)
      return;
    // get the last instuction added
    VMInstr instr = currFrame.instructions.get(nextIndex - 1);
    // check if it is a NOP
    if (instr.opcode() == OpCode.NOP)
      currFrame.instructions.remove(nextIndex - 1);
  }

  private void fixCallStmt(Stmt s) {
    // get the last instuction added
    if (s instanceof CallExpr) {
      VMInstr instr = VMInstr.POP();
      instr.addComment("clean up call return value");
      currFrame.instructions.add(instr);
    }

  }

  // ----------------------------------------------------------------------
  // Constructor
  // ----------------------------------------------------------------------

  public CodeGenerator(TypeInfo typeInfo, VM vm) {
    this.typeInfo = typeInfo;
    this.vm = vm;
  }

  // ----------------------------------------------------------------------
  // VISITOR FUNCTIONS
  // ----------------------------------------------------------------------

  public void visit(Program node) throws MyPLException {

    // store UDTs for later
    for (TypeDecl tdecl : node.tdecls) {
      // add a mapping from type name to the TypeDecl
      typeDecls.put(tdecl.typeName.lexeme(), tdecl);
    }
    // only need to translate the function declarations
    for (FunDecl fdecl : node.fdecls)
      fdecl.accept(this);
  }

  public void visit(TypeDecl node) throws MyPLException {
    // Intentionally left blank -- nothing to do here
  }

  public void visit(FunDecl node) throws MyPLException {
    // TODO:
    // 1. create a new frame for the function
    // 2. create a variable mapping for the frame
    // 3. store args
    // 4. visit statement nodes
    // 5. check to see if the last statement was a return (if not, add
    // return nil)
    VMFrame functionFrame = new VMFrame(node.funName.lexeme(), node.params.size());
    currFrame = functionFrame;
    varMap = new HashMap<>();
    currVarIndex = 0;

    // Adding function parameters
    for (FunParam param : node.params) {
      varMap.put(param.paramName.lexeme(), currVarIndex);
      currFrame.instructions.add(VMInstr.STORE(currVarIndex++));
    }

    // Adding function body
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }

    // Adding return statement if missing
    if (node.stmts.size() != 0) {
      if (!(node.stmts.get(node.stmts.size() - 1) instanceof ReturnStmt)) {
        currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
        currFrame.instructions.add(VMInstr.VRET());
        // System.out.println("Added return statement");
      }
    } else {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.instructions.add(VMInstr.VRET());
    }
    vm.add(currFrame);
  }

  public void visit(VarDeclStmt node) throws MyPLException {
    varMap.put(node.varName.lexeme(), currVarIndex);

    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.STORE(currVarIndex));
    currVarIndex++;
  }

  public void visit(AssignStmt node) throws MyPLException {
    String varName = node.lvalue.get(0).lexeme();
    // Case when we don't need to grab the type information
    if (node.lvalue.size() == 1) {
      node.expr.accept(this);
      currFrame.instructions.add(VMInstr.STORE(varMap.get(varName)));
      return;
    }
    // Case when we do need to grab the type information
    currFrame.instructions.add(VMInstr.LOAD(varMap.get(varName)));
    int i = 2;
    for (;i < node.lvalue.size() - 1; i += 2) {
      varName = node.lvalue.get(i).lexeme();
      currFrame.instructions.add(VMInstr.GETFLD(varName));
    }
    node.expr.accept(this);

    if (node.lvalue.size() > 2) {
      varName = node.lvalue.get(i).lexeme();
      currFrame.instructions.add(VMInstr.SETFLD(varName));
    }
  }

  public void visit(CondStmt node) throws MyPLException {
    // If
    List<VMInstr> jmpInstructions = new ArrayList<>();
    visit(node.ifPart, jmpInstructions);
    // Elif
    for (BasicIf elif : node.elifs)
      visit(elif, jmpInstructions);
    // Else
    if (node.elseStmts != null) {
      for (Stmt stmt : node.elseStmts) {
        stmt.accept(this);
      }
    }

    // Changing all the jumps to the end of the nop
    for (VMInstr instr : jmpInstructions) {
      instr.updateOperand(currFrame.instructions.size());
    }
    currFrame.instructions.add(VMInstr.NOP());
  }

  private void visit(BasicIf node, List<VMInstr> endJumps) throws MyPLException {
    node.cond.accept(this);
    VMInstr jmpf = VMInstr.JMPF(0);
    currFrame.instructions.add(jmpf);
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    VMInstr jmp = VMInstr.JMP(0);
    currFrame.instructions.add(jmp);
    endJumps.add(jmp);
    jmpf.updateOperand(currFrame.instructions.size());
    currFrame.instructions.add(VMInstr.NOP());
  }

  public void visit(WhileStmt node) throws MyPLException {
    int startingIndex = currFrame.instructions.size();
    node.cond.accept(this);
    VMInstr jmpf = VMInstr.JMPF(0);
    currFrame.instructions.add(jmpf);
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    currFrame.instructions.add(VMInstr.JMP(startingIndex));
    jmpf.updateOperand(currFrame.instructions.size());
    currFrame.instructions.add(VMInstr.NOP());
  }

  public void visit(ForStmt node) throws MyPLException {
    // Handling the for line
    node.start.accept(this);
    String varName = node.varName.lexeme();
    int tempVarIndex = currVarIndex++;
    varMap.put(varName, tempVarIndex);
    int startingIndex = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.STORE(tempVarIndex));
    currFrame.instructions.add(VMInstr.LOAD(tempVarIndex));
    node.end.accept(this);
    if (node.upto)
      currFrame.instructions.add(VMInstr.CMPLE());
    else
      currFrame.instructions.add(VMInstr.CMPGE());

    VMInstr jmpf = VMInstr.JMPF(0);
    // Handling statements
    currFrame.instructions.add(jmpf);
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    // Increment / Decrement
    currFrame.instructions.add(VMInstr.LOAD(tempVarIndex));
    currFrame.instructions.add(VMInstr.PUSH(1));
    if (node.upto)
      currFrame.instructions.add(VMInstr.ADD());
    else
      currFrame.instructions.add(VMInstr.SUB());
    currFrame.instructions.add(VMInstr.JMP(startingIndex));
    jmpf.updateOperand(currFrame.instructions.size());
    currFrame.instructions.add(VMInstr.NOP());
  }

  public void visit(ReturnStmt node) throws MyPLException {
    if (node.expr == null)
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    else
      node.expr.accept(this);
    currFrame.instructions.add(VMInstr.VRET());
  }

  public void visit(DeleteStmt node) throws MyPLException {
    String varName = node.varName.lexeme();
    currFrame.instructions.add(VMInstr.LOAD(varMap.get(varName)));
    currFrame.instructions.add(VMInstr.FREE());
  }

  public void visit(CallExpr node) throws MyPLException {
    // TODO: Finish the following (partially completed)

    // push args (in order)
    for (Expr arg : node.args)
      arg.accept(this);
    // built-in functions:
    if (node.funName.lexeme().equals("print"))
      currFrame.instructions.add(VMInstr.WRITE());
    else if (node.funName.lexeme().equals("read"))
      currFrame.instructions.add(VMInstr.READ());
    else if (node.funName.lexeme().equals("length"))
      currFrame.instructions.add(VMInstr.LEN());
    else if (node.funName.lexeme().equals("get"))
      currFrame.instructions.add(VMInstr.GETCHR());
    else if (node.funName.lexeme().equals("stoi"))
      currFrame.instructions.add(VMInstr.TOINT());
    else if (node.funName.lexeme().equals("stod"))
      currFrame.instructions.add(VMInstr.TODBL());
    else if (node.funName.lexeme().equals("itos"))
      currFrame.instructions.add(VMInstr.TOSTR());
    else if (node.funName.lexeme().equals("itod"))
      currFrame.instructions.add(VMInstr.TODBL());
    else if (node.funName.lexeme().equals("dtos"))
      currFrame.instructions.add(VMInstr.TOSTR());
    else if (node.funName.lexeme().equals("dtoi"))
      currFrame.instructions.add(VMInstr.TOINT());

    // user-defined functions
    else
      currFrame.instructions.add(VMInstr.CALL(node.funName.lexeme()));
  }

  public void visit(SimpleRValue node) throws MyPLException {
    if (node.value.type() == TokenType.INT_VAL) {
      int val = Integer.parseInt(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
    } else if (node.value.type() == TokenType.DOUBLE_VAL) {
      double val = Double.parseDouble(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
    } else if (node.value.type() == TokenType.BOOL_VAL) {
      if (node.value.lexeme().equals("true"))
        currFrame.instructions.add(VMInstr.PUSH(true));
      else
        currFrame.instructions.add(VMInstr.PUSH(false));
    } else if (node.value.type() == TokenType.CHAR_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
    } else if (node.value.type() == TokenType.STRING_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
    } else if (node.value.type() == TokenType.NIL) {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    }
  }

  public void visit(NewRValue node) throws MyPLException {
    // TODO
    // Set<String> components = typeInfo.components(node.typeName.toString());
    String typeName = node.typeName.lexeme();
    List<String> components = new ArrayList<>(typeInfo.components(typeName));
    TypeDecl currentType = typeDecls.get(typeName);

    currFrame.instructions.add(VMInstr.ALLOC(components));

    for (VarDeclStmt vdecl : currentType.vdecls) {
      currFrame.instructions.add(VMInstr.DUP());
      vdecl.expr.accept(this);
      currFrame.instructions.add(VMInstr.SETFLD(vdecl.varName.lexeme()));
    }
  }

  public void visit(IDRValue node) throws MyPLException {
    String varName = node.path.get(0).lexeme();
    currFrame.instructions.add(VMInstr.LOAD(varMap.get(varName)));
    for (int i = 2; i < node.path.size(); i += 2) {
      varName = node.path.get(i).lexeme();
      currFrame.instructions.add(VMInstr.GETFLD(varName));
    }
    // TODO: Handle pathing
  }

  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.NEG());
  }

  public void visit(Expr node) throws MyPLException {
    node.first.accept(this);
    if (node.op != null) {
      node.rest.accept(this);

      String operator = node.op.lexeme();
      if (operator.equals("+"))
        currFrame.instructions.add(VMInstr.ADD());
      else if (operator.equals("-"))
        currFrame.instructions.add(VMInstr.SUB());
      else if (operator.equals("*"))
        currFrame.instructions.add(VMInstr.MUL());
      else if (operator.equals("/"))
        currFrame.instructions.add(VMInstr.DIV());
      else if (operator.equals("%"))
        currFrame.instructions.add(VMInstr.MOD());
      else if (operator.equals("=="))
        currFrame.instructions.add(VMInstr.CMPEQ());
      else if (operator.equals("!="))
        currFrame.instructions.add(VMInstr.CMPNE());
      else if (operator.equals("<"))
        currFrame.instructions.add(VMInstr.CMPLT());
      else if (operator.equals(">"))
        currFrame.instructions.add(VMInstr.CMPGT());
      else if (operator.equals("<="))
        currFrame.instructions.add(VMInstr.CMPLE());
      else if (operator.equals(">="))
        currFrame.instructions.add(VMInstr.CMPGE());
      else if (operator.equals("and")) {
        currFrame.instructions.add(VMInstr.AND());
      } else if (operator.equals("or"))
        currFrame.instructions.add(VMInstr.OR());

    }
    if (node.logicallyNegated)
      currFrame.instructions.add(VMInstr.NOT());
  }

  public void visit(SimpleTerm node) throws MyPLException {
    // defer to contained rvalue
    node.rvalue.accept(this);
  }

  public void visit(ComplexTerm node) throws MyPLException {
    // defer to contained expression
    node.expr.accept(this);
  }

}
