/*
 * File: GoVisitor.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Converting to GoLang
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GoVisitor implements Visitor {

    // output stream for printing
    private PrintStream out;
    private ByteArrayOutputStream baos;
    private int indent = 0;
    private final int INDENT_AMT = 1;
    private boolean fmt = false;

    public GoVisitor() {
        baos = new ByteArrayOutputStream();
        try {
            out = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        return imports() + baos.toString();
    }

    private String imports() {
        // TODO: Write the logic for thsi function
        StringBuilder importString = new StringBuilder("package main\n");
        if (fmt) {
            importString.append("import (");
            importString.append("\n\t\"fmt\"");
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
    }

    @Override
    public void visit(TypeDecl node) throws MyPLException {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(FunDecl node) throws MyPLException {
        // TODO Auto-generated method stub
        print(getIndent() + "func");
        print(" " + node.funName.lexeme() + "(");
        if (node.params.size() > 0) {
            for (FunParam param : node.params.subList(0, node.params.size() - 1)) {
                print(param.paramName.lexeme() + " " + param.paramType.lexeme() + ", ");
            }
            FunParam lastParam = node.params.get(node.params.size() - 1);
            print(lastParam.paramName.lexeme() + " " + lastParam.paramType.lexeme());
        }
        print(")");
        if (!node.returnType.lexeme().equals("void"))
            print(" " + node.returnType.lexeme());
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
    }

    @Override
    public void visit(AssignStmt node) throws MyPLException {
        for (Token token : node.lvalue.subList(0, node.lvalue.size() - 1)) {
            print(token.lexeme() + ".");
        }
        print(node.lvalue.get(node.lvalue.size() - 1).lexeme() + " = ");
        node.expr.accept(this);
    }

    @Override
    public void visit(CondStmt node) throws MyPLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(WhileStmt node) throws MyPLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ForStmt node) throws MyPLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReturnStmt node) throws MyPLException {
        // TODO Auto-generated method stub
        print("return");
        if (node.expr != null) {
            print(" ");
            node.expr.accept(this);
        }
    }

    @Override
    public void visit(DeleteStmt node) throws MyPLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CallExpr node) throws MyPLException {
        // TODO Auto-generated method stub
        if (node.funName.lexeme().equals("print")) {
            fmt = true;
            print("fmt.Print(");
        } else
            print(node.funName.lexeme() + "(");
        if (node.args.size() > 0) {
            for (Expr expr : node.args.subList(0, node.args.size() - 1)) {
                expr.accept(this);
                print(", ");
            }
            node.args.get(node.args.size() - 1).accept(this);
        }
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
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IDRValue node) throws MyPLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NegatedRValue node) throws MyPLException {
        out.print("-");
        node.expr.accept(this);
    }

    @Override
    public void visit(Expr node) throws MyPLException {
        // TODO Auto-generated method stub
        node.first.accept(this);
        if (node.op != null) {
            out.print(" " + node.op.lexeme() + " ");
            node.rest.accept(this);
        }
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