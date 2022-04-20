import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect.Type;

/*
 * File: GoVisitor.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: Converting to GoLang
 */

public class GoVisitor implements Visitor {
    
    private PrintStream out;

    private int indent = 0;

    private final int INDENT_AMT = 2;

    public GoVisitor(PrintStream out) {
        this.out = out;
    }

    private String getIndent() {
        return " ".repeat(indent);
    }

    private void incIndent() {
        indent += INDENT_AMT;
    }

    private void decIndent() {
        indent -= INDENT_AMT;
    }

    public void visit(Program node) throws MyPLException {
        for (TypeDecl d : node.tdecls)
            d.accept(this);
        for (FunDecl d : node.fdecls)
            d.accept(this);
    }

    public void visit(TypeDecl node) throws MyPLException {
        out.print(getIndent() + "type " + node.typeName.lexeme() + " struct {\n");

        incIndent();
        for (VarDeclStmt d : node.vdecls)
            d.accept(this);
        decIndent();

        out.print(getIndent() + "}");
    }

    public void visit(FunDecl node) throws MyPLException {
        out.print(getIndent() + "func " + node.funName.lexeme() + "(");

        incIndent();
        for (FunParam param : node.params) {
            out.print(param.paramName + " " + param.paramType);
            if (node.params.indexOf(param) != node.params.size() - 1)
                out.print(", ");
        }

        out.print(") ");

        if (node.returnType.lexeme() != "void")
            out.print(node.returnType.lexeme());

        out.print(" {\n");

        incIndent();
        for (Stmt stmt : node.stmts) {
            stmt.accept(this);
            out.print("\n");
        }
        decIndent();

        out.print("}\n");
    }

    public void visit(VarDeclStmt node) throws MyPLException {
        out.print(node.varName.lexeme() + " := ");
        node.expr.accept(this);
    }

    public void visit(AssignStmt node) throws MyPLException {
        for (Token token : node.lvalue)
            out.print(token.lexeme());
        out.print(" = ");
        node.expr.accept(this);
    }

    public void visit(CondStmt node) throws MyPLException {
        out.print("if ");
        basicElif(node.ifPart, node.elseStmts != null);
        if (node.elifs != null) {
            for (BasicIf elif : node.elifs) {
                out.println(getIndent() + "elif ");
                basicElif(elif, node.elseStmts != null);
            }
        }
        if (node.elseStmts != null) {
            out.print(getIndent() + "else {\n");
            incIndent();
            for (Stmt elseStmts: node.elseStmts) {
                out.print(getIndent());
                elseStmts.accept(this);
                out.print("\n");
            }
            decIndent();
            out.print(getIndent() + "}");
        }
    }

    private void basicElif(BasicIf node, boolean elseStmts) throws MyPLException {
        node.cond.accept(this);
        out.print(" {\n");
        incIndent();
        for (Stmt stmt: node.stmts) {
            out.print(getIndent());
            stmt.accept(this);
            out.print("\n");
        }
        decIndent();
        if(elseStmts) {
            out.print(getIndent() + "}\n");
        } else {
            out.print(getIndent() + "}");
        }
    }

    public void visit(WhileStmt node) throws MyPLException {
        out.print("for ");
        node.cond.accept(this);
        out.print(" {\n");
        incIndent();
        for (Stmt stmt: node.stmts) {
            out.print(getIndent());
            stmt.accept(this);
            out.print("\n");
        }
        decIndent();
        out.print(getIndent() + "}");
    }
    public void visit(ForStmt node) throws MyPLException {
    }
    public void visit(ReturnStmt node) throws MyPLException {
        out.print("return");
        if (node.expr != null) {
            out.print(" ");
            visit(node.expr);
        }
    }
    public void visit(DeleteStmt node) throws MyPLException {
        // Golang is garbage collected so this function does nothing.
    }

    // statement and rvalue node
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
    public void visit(SimpleRValue node) throws MyPLException {}
    public void visit(NewRValue node) throws MyPLException {
        out.print(node.typeName.lexeme() + "{}");
    }
    public void visit(IDRValue node) throws MyPLException {}
    public void visit(NegatedRValue node) throws MyPLException {
        out.print("-");
        visit(node.expr);
    }

    // expression node
    public void visit(Expr node) throws MyPLException {}

    // terms
    public void visit(SimpleTerm node) throws MyPLException {}
    public void visit(ComplexTerm node) throws MyPLException {}
}
