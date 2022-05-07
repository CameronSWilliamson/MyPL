/*
 * File: GoVisitor.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Converting to GoLang
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoVisitor implements Visitor {
    class Tuple<T, V> {
        public T first;
        public V second;

        Tuple(T first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    // output stream for printing
    private PrintStream out;
    private ByteArrayOutputStream baos;
    private StringBuilder bonusFunctions;
    private int indent = 0;
    private final int INDENT_AMT = 1;
    private boolean fmt = false;
    private boolean bufio = false;
    private boolean stdconv = false;
    private boolean os = false;
    private boolean time = false;
    private boolean read = false;
    private boolean stoi = false;
    private boolean stod = false;
    private boolean startCurFunc = false;
    private boolean endCurFunc = false;
    private List<String> references = new ArrayList<>();
    private List<String> objectVars = new ArrayList<>();
    private boolean isObj = false;
    private boolean isReturning = false;
    private boolean isFunctionCall = false;

    private List<Tuple<List<VarDeclStmt>, String>> necessaryDeclarations;
    private List<String> builtInTypes = Arrays.asList("int", "double", "bool", "char", "string", "void");

    public GoVisitor() {
        baos = new ByteArrayOutputStream();
        bonusFunctions = new StringBuilder();
        try {
            out = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        necessaryDeclarations = new ArrayList<>();
    }

    private String getIndent() {
        return "\t".repeat(indent);
    }

    private void incIndent() {
        indent += INDENT_AMT;
    }

    private void decIndent() {
        indent -= INDENT_AMT;
    }

    private void print(String... s) {
        for (String str : s)
            out.print(str);
    }

    public String parse(Program node) throws MyPLException {
        visit(node);
        return (imports() + baos.toString() + bonusFunctions.toString()).replaceAll("double", "float64");
    }

    public void parse(Program node, OutputStream out) throws MyPLException {
        try {
            out.write(parse(node).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String imports() {
        StringBuilder importString = new StringBuilder("package main\n");
        if (fmt || bufio || stdconv || time) {
            importString.append("import (");
            if (fmt)
                importString.append("\n\t\"fmt\"");
            if (bufio)
                importString.append("\n\t\"bufio\"");
            if (stdconv)
                importString.append("\n\t\"strconv\"");
            if (time)
                importString.append("\n\t\"time\"");
            if (os)
                importString.append("\n\t\"os\"");
            importString.append("\n)\n");
        }

        return importString.toString();
    }

    @Override
    public void visit(Program node) throws MyPLException {
        for (TypeDecl d : node.tdecls)
            d.accept(this);
        for (FunDecl d : node.fdecls)
            d.accept(this);
        for (Tuple<List<VarDeclStmt>, String> tuple : necessaryDeclarations) {
            typeDeclVars(tuple.first, tuple.second);
        }
    }

    @Override
    public void visit(TypeDecl node) throws MyPLException {
        String typeName = node.typeName.lexeme();
        print("type " + typeName + " struct {\n");
        incIndent();
        for (VarDeclStmt v : node.vdecls) {
            if (v.typeName == null) {
                throw MyPLException
                        .GOError("Type name not set for variable " + v.varName.lexeme() + " in type " + typeName);
            }
            if (v.typeName.lexeme().equals(typeName)) {
                if (!references.contains(typeName))
                    references.add(typeName);
                print(getIndent(), v.varName.lexeme(), " *", v.typeName.lexeme(), "\n");
            } else if (references.contains(v.typeName.lexeme()))
                print(getIndent(), v.varName.lexeme(), " *", v.typeName.lexeme(), "\n");
            else {
                print(getIndent(), v.varName.lexeme(), " ", v.typeName.lexeme(), "\n");
            }
        }
        decIndent();
        print("}\n");
        necessaryDeclarations.add(new Tuple<>(node.vdecls, typeName));
    }

    /**
     * Creates a new function declaration for a specific type.
     * 
     * @return
     * @throws MyPLException
     */
    private void typeDeclVars(List<VarDeclStmt> vdecls, String typeName) throws MyPLException {
        print("func make", typeName, "() ");
        boolean inRef = references.contains(typeName);
        if (inRef)
            print("*", typeName);
        else
            print(typeName);
        print(" {\n");
        incIndent();
        print(getIndent(), "return ");
        if (inRef)
            print("&", typeName, "{\n");
        else
            print(typeName, "{\n");
        incIndent();
        for (VarDeclStmt vdecl : vdecls) {
            if (vdecl.expr != null) {
                print(getIndent() + vdecl.varName.lexeme() + ": ");
                vdecl.expr.accept(this);
                print(",\n");
            }
        }
        decIndent();
        print(getIndent(), "}\n");
        decIndent();
        print("}\n");
    }

    @Override
    public void visit(FunDecl node) throws MyPLException {
        print(getIndent() + "func");
        print(" " + node.funName.lexeme() + "(");
        objectVars.clear();
        if (node.params.size() > 0) {
            for (FunParam param : node.params.subList(0, node.params.size() - 1)) {
                print(param.paramName.lexeme(), " ");
                if (!builtInTypes.contains(param.paramType.lexeme()))
                    // if (references.contains(param.paramType.lexeme()))
                    print("*", param.paramType.lexeme(), ", ");
                else
                    print(param.paramType.lexeme(), ", ");
            }
            FunParam lastParam = node.params.get(node.params.size() - 1);
            print(lastParam.paramName.lexeme(), " ");
            if (!builtInTypes.contains(lastParam.paramType.lexeme()))
                print("*", lastParam.paramType.lexeme());
            else
                print(lastParam.paramType.lexeme());
        }
        print(")");
        if (!node.returnType.lexeme().equals("void")) {
            if (references.contains(node.returnType.lexeme()))
                print(" *", node.returnType.lexeme());
            else
                print(" " + node.returnType.lexeme());
        }
        print(" {\n");
        incIndent();
        for (Stmt stmt : node.stmts) {
            out.print(getIndent());
            stmt.accept(this);
            out.print("\n");
        }
        decIndent();
        out.print(getIndent() + "}\n");
    }

    @Override
    public void visit(VarDeclStmt node) throws MyPLException {
        print(node.varName.lexeme() + " := ");
        node.expr.accept(this);
        if (isObj) {
            isObj = false;
            objectVars.add(node.varName.lexeme());
        }
    }

    @Override
    public void visit(AssignStmt node) throws MyPLException {
        for (Token token : node.lvalue) {
            print(token.lexeme());
        }
        print(" = ");
        node.expr.accept(this);
    }

    @Override
    public void visit(CondStmt node) throws MyPLException {
        print("if ");
        basicIf(node.ifPart);
        if (node.elifs != null) {
            for (BasicIf elif : node.elifs) {
                print(" else if ");
                basicIf(elif);
            }
        }
        if (node.elseStmts != null) {
            print(" else {\n");
            incIndent();
            for (Stmt stmt : node.elseStmts) {
                out.print(getIndent());
                stmt.accept(this);
                out.print("\n");
            }
            decIndent();
            print(getIndent() + "}");
        }
    }

    private void basicIf(BasicIf node) throws MyPLException {
        node.cond.accept(this);
        print(" {\n");
        incIndent();
        for (Stmt stmt : node.stmts) {
            print(getIndent());
            stmt.accept(this);
            print("\n");
        }
        decIndent();
        print(getIndent() + "}");
    }

    @Override
    public void visit(WhileStmt node) throws MyPLException {
        print("for ");
        node.cond.accept(this);
        print(" {\n");
        incIndent();
        for (Stmt stmt : node.stmts) {
            print(getIndent());
            stmt.accept(this);
            print("\n");
        }
        decIndent();
        print(getIndent() + "}");
    }

    @Override
    public void visit(ForStmt node) throws MyPLException {
        String varName = node.varName.lexeme();
        print("for " + varName + " := ");
        node.start.accept(this);
        if (node.upto) {
            print("; " + varName + " < ");
            node.end.accept(this);
            print("; " + varName + "++");
        } else {
            print("; " + varName + " > ");
            node.end.accept(this);
            print("; " + varName + "--");
        }
        print(" {\n");
        incIndent();
        for (Stmt stmt : node.stmts) {
            print(getIndent());
            stmt.accept(this);
            print("\n");
        }
        decIndent();
        print(getIndent(), "}");
    }

    @Override
    public void visit(ReturnStmt node) throws MyPLException {
        print("return");
        if (node.expr != null) {
            print(" ");
            isReturning = true;
            node.expr.accept(this);
            isReturning = false;
        }
    }

    @Override
    public void visit(DeleteStmt node) throws MyPLException {
        // Left empty because go has a garbage collector
    }

    @Override
    public void visit(CallExpr node) throws MyPLException {
        if (node.funName.lexeme().equals("print")) {
            fmt = true;
            print("fmt.Print(");
        } else if (node.funName.lexeme().equals("read")) {
            bufio = true;
            os = true;
            print("read(");
            if (!read) {
                bonusFunctions.append("func read() string {\n");
                bonusFunctions.append("\treader := bufio.NewReader(os.Stdin)\n");
                bonusFunctions.append("\ttext, _ := reader.ReadString('\\n')\n");
                bonusFunctions.append("\treturn text\n");
                bonusFunctions.append("}\n");
            }
            read = true;
        } else if (node.funName.lexeme().equals("length"))
            print("len(");
        else if (node.funName.lexeme().equals("get")) {
            print("string(");
            node.args.get(0).accept(this);
            print("[");
            node.args.get(1).accept(this);
            print("]");
            print(")");
            return;
        } else if (node.funName.lexeme().equals("stoi")) {
            stdconv = true;
            print("stoi(");
            if (!stoi) {
                bonusFunctions.append("func stoi(s string) int {\n");
                bonusFunctions.append("\tnum, _ := strconv.Atoi(s)\n");
                bonusFunctions.append("\treturn num\n");
                bonusFunctions.append("}\n");
            }
            stoi = true;
        } else if (node.funName.lexeme().equals("stod")) {
            stdconv = true;
            print("stod(");
            if (!stod) {
                bonusFunctions.append("func stod(s string) float64 {\n");
                bonusFunctions.append("\tnum, _ := strconv.ParseFloat(s, 64)\n");
                bonusFunctions.append("\treturn num\n");
                bonusFunctions.append("}\n");
            }
            stod = true;
        } else if (node.funName.lexeme().equals("itos")) {
            stdconv = true;
            print("strconv.Itoa(");
        } else if (node.funName.lexeme().equals("itod"))
            print("float64(");
        else if (node.funName.lexeme().equals("dtos")) {
            stdconv = true;
            print("strconv.FormatFloat(");
            node.args.get(0).accept(this);
            print(", 'f', -1, 64)");
            return;
        } else if (node.funName.lexeme().equals("dtoi")) {
            stdconv = true;
            print("int(");
        } else if (node.funName.lexeme().equals("timestart")) {
            time = true;
            print("start ");
            if (startCurFunc)
                print("= time.Now()");
            else
                print(":= time.Now()");
            startCurFunc = true;
            return;
        } else if (node.funName.lexeme().equals("timeend")) {
            time = true;
            print("end ");
            if (endCurFunc)
                print("= time.Now()");
            else
                print(":= time.Now()");
            endCurFunc = true;
            return;
        } else if (node.funName.lexeme().equals("timedelta")) {
            time = true;
            print("end.Sub(start)");
            return;
        } else
            print(node.funName.lexeme() + "(");
        isFunctionCall = true;
        if (node.args.size() > 0) {
            for (Expr expr : node.args.subList(0, node.args.size() - 1)) {
                expr.accept(this);
                print(", ");
            }
            node.args.get(node.args.size() - 1).accept(this);
        }
        isFunctionCall = false;
        print(")");
    }

    @Override
    public void visit(SimpleRValue node) throws MyPLException {
        if (node.value.type() == TokenType.STRING_VAL)
            out.print("\"" + node.value.lexeme() + "\"");
        else if (node.value.type() == TokenType.CHAR_VAL)
            out.print("'" + node.value.lexeme() + "'");
        else
            out.print(node.value.lexeme());
    }

    @Override
    public void visit(NewRValue node) throws MyPLException {
        isObj = true;
        print("make" + node.typeName.lexeme() + "()");
    }

    @Override
    public void visit(IDRValue node) throws MyPLException {
        if (isFunctionCall && node.path.size() == 1) {
            if (objectVars.contains(node.path.get(0).lexeme()))
                print("&(", node.path.get(0).lexeme(), ")");
            else
                print(node.path.get(0).lexeme());
        } else {
            for (Token token : node.path) {
                print(token.lexeme());
            }
        }
    }

    @Override
    public void visit(NegatedRValue node) throws MyPLException {
        out.print("-");
        node.expr.accept(this);
    }

    @Override
    public void visit(Expr node) throws MyPLException {
        if (node.logicallyNegated)
            out.print("!(");
        node.first.accept(this);
        if (node.op != null) {
            if (node.op.lexeme().equals("and"))
                out.print(" && ");
            else if (node.op.lexeme().equals("or"))
                out.print(" || ");
            else
                out.print(" " + node.op.lexeme() + " ");
            node.rest.accept(this);
        }
        if (node.logicallyNegated)
            out.print(")");
    }

    @Override
    public void visit(SimpleTerm node) throws MyPLException {
        node.rvalue.accept(this);
    }

    @Override
    public void visit(ComplexTerm node) throws MyPLException {
        node.expr.accept(this);
    }

}