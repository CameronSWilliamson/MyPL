import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.ExcludeCategories;

public class GoVisitorTest {
    private PrintStream stdout = System.out;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void changeSystemOut() {
        System.setOut(new PrintStream(output));
    }

    @After
    public void restoreSystemOut() {
        System.setOut(stdout);
    }

    /**
     * Puts newlines between each argument passed.
     * 
     * @param args Strings for a MyPL program.
     * @return A string with newlines between each argument.
     */
    private static String buildString(String... args) {
        String str = "";
        for (String s : args)
            str += s + "\n";
        return str;
    }

    private static String buildVisitor(String myPLProgram) throws MyPLException {
        InputStream in = new ByteArrayInputStream(myPLProgram.getBytes());
        ASTParser parser = new ASTParser(new Lexer(in));
        Program program = parser.parse();
        GoVisitor visitor = new GoVisitor();
        return visitor.parse(program);
    }

    // --------------------
    // Basics
    // --------------------
    @Test
    public void emptyProgram() throws Exception {
        String s = buildString("fun void main() {", "}");
        String expected = buildString("package main\nfunc main() {\n}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void emptyProgramWithReturn() throws Exception {
        String s = buildString("fun int main() {", "return 1", "}");
        String expected = buildString("package main\nfunc main() int {\n\treturn 1\n}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // Variables and Assignment
    // --------------------
    @Test
    public void simpleAssignmentDecl() throws Exception {
        String s = buildString("fun void main() {", "var x = 1", "x = 2", "}");
        String expected = buildString("package main", "func main() {", "\tx := 1", "\tx = 2", "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void simpleAdd() throws Exception {
        String s = buildString("fun void main() {",
                "var x = 1",
                "var y = \"hi\"",
                "var a = 1 + 2",
                "var b = 2 - 3",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tx := 1",
                "\ty := \"hi\"",
                "\ta := 1 + 2",
                "\tb := 2 - 3\n}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void negExpr() throws Exception {
        String s = buildString("fun void main() {",
                "var x = neg 1",
                "var a = neg 1 + 2",
                "var b = neg 2 - 3",
                "}");
        String expected = buildString("package main", "func main() {",
                "\tx := -1",
                "\ta := -1 + 2",
                "\tb := -2 - 3\n}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // Arithmetic Expressions
    // --------------------

    // --------------------
    // Function Calls
    // --------------------
    @Test
    public void basicFunctionCall() throws Exception {
        String s = buildString("fun void x() {}",
                "fun void main() {",
                "x()",
                "}");
        String expected = buildString("package main",
                "func x() {",
                "}",
                "func main() {",
                "\tx()",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void functionCallWithParams() throws Exception {
        String s = buildString("fun void x(int a, int b) {}",
                "fun void main() {",
                "x(1, 2)",
                "}");
        String expected = buildString("package main",
                "func x(a int, b int) {",
                "}",
                "func main() {",
                "\tx(1, 2)",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // Built in Functions
    // --------------------
    @Test
    public void printFunction() throws Exception {
        String s = buildString("fun void main() {",
                "  print(\"Hello World!\")",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"fmt\"",
                ")",
                "func main() {",
                "\tfmt.Print(\"Hello World!\")",
                "}");
        assertEquals(expected, buildVisitor(s));
    }
    // --------------------
    // If Statements
    // --------------------

    // --------------------
    // While Loops
    // --------------------

    // --------------------
    // For Loops
    // --------------------

    // --------------------
    // User Defined Types
    // --------------------
}