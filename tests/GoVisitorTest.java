import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void readFunction() throws Exception {
        String s = buildString("fun void main() {",
                "  var x = read()",
                "print(x)",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"fmt\"",
                "\t\"bufio\"",
                "\t\"os\"",
                ")",
                "func main() {",
                "\tx := read()",
                "\tfmt.Print(x)",
                "}",
                "func read() string {",
                "\treader := bufio.NewReader(os.Stdin)",
                "\ttext, _ := reader.ReadString('\\n')",
                "\treturn text",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void lengthFunction() throws Exception {
        String s = buildString("fun void main() {",
                "var x = \"hello\"",
                "print(len(x))",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"fmt\"",
                ")",
                "func main() {",
                "\tx := \"hello\"",
                "\tfmt.Print(len(x))",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void getFunction() throws Exception {
        String s = buildString("fun void main() {",
                "var x = \"hello\"",
                "print(get(x, 0))",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"fmt\"",
                ")",
                "func main() {",
                "\tx := \"hello\"",
                "\tfmt.Print(string(x[0]))",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void conversionFuncs() throws Exception {
        String s = buildString("fun void main() {",
                "var x = stoi(itos(1))",
                "var y = stod(dtos(2))",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"strconv\"",
                ")",
                "func main() {",
                "\tx := stoi(strconv.Itoa(1))",
                "\ty := stod(strconv.FormatFloat(2, 'f', -1, 64))",
                "}",
                "func stoi(s string) int {",
                "\tnum, _ := strconv.Atoi(s)",
                "\treturn num",
                "}",
                "func stod(s string) float64 {",
                "\tnum, _ := strconv.ParseFloat(s, 64)",
                "\treturn num",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void timerFuncs() throws Exception {
        String s = buildString("fun void main() {",
                "timestart()",
                "timeend()",
                "timedelta()",
                "}");
        String expected = buildString("package main",
                "import (",
                "\t\"time\"",
                ")",
                "func main() {",
                "\tstart := time.Now()",
                "\tend := time.Now()",
                "\tend.Sub(start)",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // If Statements
    // --------------------
    @Test
    public void basicIf() throws Exception {
        String s = buildString("fun void main() {",
                "if 1 == 1 {",
                "var x = 1",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tif 1 == 1 {",
                "\t\tx := 1",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void nestedIf() throws Exception {
        String s = buildString("fun void main() {",
                "if 1 == 1 {",
                "if 1 == 1 {",
                "var x = \"hi\"",
                "}",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tif 1 == 1 {",
                "\t\tif 1 == 1 {",
                "\t\t\tx := \"hi\"",
                "\t\t}",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void ifElif() throws Exception {
        String s = buildString("fun void main() {",
                "if 1 == 1 {",
                "var x = 1",
                "} elif 1 == 2 {",
                "var y = 2",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tif 1 == 1 {",
                "\t\tx := 1",
                "\t} else if 1 == 2 {",
                "\t\ty := 2",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void ifElseStatement() throws Exception {
        String s = buildString("fun void main() {",
                "if 1 == 1 {",
                "var  x = 1",
                "} else {",
                "var y = 1",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tif 1 == 1 {",
                "\t\tx := 1",
                "\t} else {",
                "\t\ty := 1",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void ifWithNot() throws Exception {
        String s = buildString("fun void main() {",
                "if not (1 == 1) {",
                "var x = 1",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tif !(1 == 1) {",
                "\t\tx := 1",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // While Loops
    // --------------------
    @Test
    public void whileLoop() throws Exception {
        String s = buildString("fun void main() {",
                "var x = 1",
                "while x < 10 {",
                "x = x + 1",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tx := 1",
                "\tfor x < 10 {",
                "\t\tx = x + 1",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // For Loops
    // --------------------
    @Test
    public void forLoop() throws Exception {
        String s = buildString("fun void main() {",
                "for x from 1 upto 10 {",
                "var y = x",
                "}",
                "for x from 10 downto 1 {",
                "var z = x",
                "}",
                "}");
        String expected = buildString("package main",
                "func main() {",
                "\tfor x := 1; x < 10; x++ {",
                "\t\ty := x",
                "\t}",
                "\tfor x := 10; x > 1; x-- {",
                "\t\tz := x",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    // --------------------
    // User Defined Types
    // --------------------
    @Test
    public void userDefinedTypeNoVals() throws Exception {
        String s = buildString("type MyType{}", "fun void main() {",
                "var x = new MyType",
                "}");
        String expected = buildString("package main",
                "type MyType struct {",
                "}",
                "func main() {",
                "\tx := makeMyType()",
                "}",
                "func makeMyType() MyType {",
                "\treturn MyType{",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void userDefinedTypeVals() throws Exception {
        String s = buildString("type MyType{",
                "var int x = 1",
                "var int y = 2",
                "}",
                "fun void main() {",
                "var x = new MyType",
                "}");
        String expected = buildString("package main",
                "type MyType struct {",
                "\tx int",
                "\ty int",
                "}",
                "func main() {",
                "\tx := makeMyType()",
                "}",
                "func makeMyType() MyType {",
                "\treturn MyType{",
                "\t\tx: 1,",
                "\t\ty: 2,",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }

    @Test
    public void badTypeDeclaration() throws Exception {
        String s = buildString("type T {",
                "var x = 1",
                "}");
        try {
            buildVisitor(s);
            fail("Expected exception");
        } catch (Exception e) {
            assertEquals("GO_ERROR: Type name not set for variable x in type T", e.getMessage());
        }
    }

    @Test
    public void selfReferenceType() throws Exception {
        String s = buildString("type T {",
                "var T x = nil",
                "}",
                "fun void main() {",
                "var x = new T",
                "}");
        String expected = buildString("package main",
                "type T struct {",
                "\tx *T",
                "}",
                "func main() {",
                "\tx := makeT()",
                "}",
                "func makeT() *T {",
                "\treturn &T{",
                "\t\tx: nil,",
                "\t}",
                "}");
        assertEquals(expected, buildVisitor(s));
    }
}