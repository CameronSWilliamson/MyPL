/*
 * File: VMInstrTest.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Basic unit tests for MyPL VM
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;


public class VMTest {

  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream(); 

  
  @Before
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @After
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }

  
  //------------------------------------------------------------
  // Simple getting started test
  //------------------------------------------------------------
  
  @Test
  public void singleNOPTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.NOP());
    vm.run();
  }

  @Test
  public void singleWriteTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("blue", output.toString());
  }

  // //------------------------------------------------------------
  // // Consts/Vars
  // //------------------------------------------------------------

  @Test
  public void singlePopTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.PUSH("green"));    
    main.instructions.add(VMInstr.POP());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  public void storeAndLoadTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.STORE(0));
    main.instructions.add(VMInstr.LOAD(0));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("blue", output.toString());
  }

  // //------------------------------------------------------------
  // // Special instructions
  // //------------------------------------------------------------

  @Test
  public void dupTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("blueblue", output.toString());
  }

  @Test
  public void swapTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.PUSH("green"));    
    main.instructions.add(VMInstr.SWAP());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("bluegreen", output.toString());
  }
  
  // //------------------------------------------------------------
  // // Ops
  // //------------------------------------------------------------
  
  @Test
  public void addIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(42));
    main.instructions.add(VMInstr.PUSH(43));
    main.instructions.add(VMInstr.ADD());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("85", output.toString());
  }

  @Test
  public void addDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(3.50));
    main.instructions.add(VMInstr.PUSH(2.25));
    main.instructions.add(VMInstr.ADD());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("5.75", output.toString());
  }

  @Test
  public void addStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.PUSH("green"));
    main.instructions.add(VMInstr.ADD());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("bluegreen", output.toString());
  }
  
  @Test
  public void subIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(15));
    main.instructions.add(VMInstr.PUSH(9));
    main.instructions.add(VMInstr.SUB());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("6", output.toString());
  }
  
  @Test
  public void subDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(3.75));
    main.instructions.add(VMInstr.PUSH(2.50));
    main.instructions.add(VMInstr.SUB());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("1.25", output.toString());
  }

  @Test
  public void mulIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(15));
    main.instructions.add(VMInstr.PUSH(3));
    main.instructions.add(VMInstr.MUL());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("45", output.toString());
  }

  @Test
  public void mulDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.25));
    main.instructions.add(VMInstr.PUSH(3.0));
    main.instructions.add(VMInstr.MUL());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("3.75", output.toString());
  }
  
  @Test
  public void divIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(15));
    main.instructions.add(VMInstr.PUSH(3));
    main.instructions.add(VMInstr.DIV());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("5", output.toString());
  }

  @Test
  public void divDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.5));
    main.instructions.add(VMInstr.PUSH(2.0));
    main.instructions.add(VMInstr.DIV());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("0.75", output.toString());
  }

  
  @Test
  public void modTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(16));
    main.instructions.add(VMInstr.PUSH(3));
    main.instructions.add(VMInstr.MOD());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("1", output.toString());
  }

  @Test
  public void andTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(true));
    main.instructions.add(VMInstr.PUSH(true));
    main.instructions.add(VMInstr.AND());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true", output.toString());
  }

  @Test
  public void orTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(false));
    main.instructions.add(VMInstr.PUSH(true));
    main.instructions.add(VMInstr.OR());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true", output.toString());
  }

  @Test
  public void notTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(false));
    main.instructions.add(VMInstr.NOT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true", output.toString());
  }
  
  @Test
  public void cmpltIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpltDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.25));
    main.instructions.add(VMInstr.PUSH(1.50));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpltStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPLT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpleIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }

  @Test
  public void cmpleDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.PUSH(1.25));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }

  @Test
  public void cmpleStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPLE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }
  
  @Test
  public void cmpgtIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpgtDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.50));
    main.instructions.add(VMInstr.PUSH(1.25));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpgtStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPGT());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true false", output.toString());
  }

  @Test
  public void cmpgeIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }

  @Test
  public void cmpgeDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(1.25));
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }

  @Test
  public void cmpgeStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPGE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true true", output.toString());
  }

  @Test
  public void cmpeqIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("true false false", output.toString());
  }

  @Test
  public void cmpeqDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.250));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("true false false", output.toString());
  }

  @Test
  public void cmpeqStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("true false false", output.toString());
  }

  @Test
  public void cmpeqNilTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPEQ());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("true", output.toString());
  }

  // cmpne
  @Test
  public void cmpneIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("false true true", output.toString());
  }

  @Test
  public void cmpneDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(2.125));
    main.instructions.add(VMInstr.PUSH(2.250));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(1.125));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("false true true", output.toString());
  }

  @Test
  public void cmpneStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH("abd"));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("abc"));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());    
    vm.run();
    assertEquals("false true true", output.toString());
  }

  @Test
  public void cmpneNilTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    main.instructions.add(VMInstr.CMPNE());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("false", output.toString());
  }
  
  @Test
  public void negIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(42));
    main.instructions.add(VMInstr.NEG());
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));    
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.NEG());
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("-42 42", output.toString());
  }

  
  //------------------------------------------------------------
  // Jumps
  //------------------------------------------------------------

  @Test
  public void jmpForwardTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.JMP(3)); // instruction 0
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH("green"));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("green", output.toString());
  }

  @Test
  public void jmpfForwardTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(false));   // 0
    main.instructions.add(VMInstr.JMPF(4));       // 1
    main.instructions.add(VMInstr.PUSH("blue"));  // 2
    main.instructions.add(VMInstr.WRITE());       // 3
    main.instructions.add(VMInstr.PUSH("green")); // 4
    main.instructions.add(VMInstr.WRITE());       // 5
    vm.run();
    assertEquals("green", output.toString());
  }

  @Test
  public void jmpfFailingForwardTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(true));    // 0
    main.instructions.add(VMInstr.JMPF(4));       // 1
    main.instructions.add(VMInstr.PUSH("blue"));  // 2
    main.instructions.add(VMInstr.WRITE());       // 3
    main.instructions.add(VMInstr.PUSH("green")); // 4
    main.instructions.add(VMInstr.WRITE());       // 5
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  public void jmpBackwardTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(0));       // 0
    main.instructions.add(VMInstr.STORE(0));      // 1
    main.instructions.add(VMInstr.LOAD(0));       // 2
    main.instructions.add(VMInstr.PUSH(2));       // 3
    main.instructions.add(VMInstr.CMPLT());       // 4
    main.instructions.add(VMInstr.JMPF(15));      // 5 
    main.instructions.add(VMInstr.PUSH("blue"));  // 6
    main.instructions.add(VMInstr.WRITE());       // 7
    main.instructions.add(VMInstr.PUSH("green")); // 8
    main.instructions.add(VMInstr.WRITE());       // 9
    main.instructions.add(VMInstr.LOAD(0));       // 10
    main.instructions.add(VMInstr.PUSH(1));       // 11
    main.instructions.add(VMInstr.ADD());         // 12
    main.instructions.add(VMInstr.STORE(0));      // 13
    main.instructions.add(VMInstr.JMP(2));        // 14
    main.instructions.add(VMInstr.PUSH("red"));   // 15
    main.instructions.add(VMInstr.WRITE());       // 16
    vm.run();
    assertEquals("bluegreenbluegreenred", output.toString());
  }

  //------------------------------------------------------------
  // Functions
  //------------------------------------------------------------

  @Test
  public void funReturnConstTest() throws Exception {
    VM vm = new VM();
    VMFrame f = new VMFrame("f", 0);
    vm.add(f);
    f.instructions.add(VMInstr.PUSH("blue"));
    f.instructions.add(VMInstr.VRET());
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.CALL("f"));
    main.instructions.add(VMInstr.WRITE());       
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  public void funReturnModifiedParamTest() throws Exception {
    VM vm = new VM();
    VMFrame f = new VMFrame("f", 1);
    vm.add(f);
    f.instructions.add(VMInstr.PUSH("green"));
    f.instructions.add(VMInstr.ADD());
    f.instructions.add(VMInstr.VRET());
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.CALL("f"));
    main.instructions.add(VMInstr.WRITE());       
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  public void funTwoParamTest() throws Exception {
    VM vm = new VM();
    VMFrame f = new VMFrame("f", 2);
    vm.add(f);
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    f.instructions.add(VMInstr.VRET());
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.PUSH("green"));    
    main.instructions.add(VMInstr.CALL("f"));
    main.instructions.add(VMInstr.POP()); // return value
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  public void funRecursiveSumTest() throws Exception {
    VM vm = new VM();
    VMFrame f = new VMFrame("sum", 1);
    vm.add(f);
    f.instructions.add(VMInstr.STORE(0));    // 0
    f.instructions.add(VMInstr.LOAD(0));     // 1
    f.instructions.add(VMInstr.PUSH(0));     // 2
    f.instructions.add(VMInstr.CMPLE());     // 3
    f.instructions.add(VMInstr.JMPF(7));     // 4
    f.instructions.add(VMInstr.PUSH(0));     // 5
    f.instructions.add(VMInstr.VRET());      // 6
    f.instructions.add(VMInstr.LOAD(0));     // 7
    f.instructions.add(VMInstr.PUSH(1));     // 8
    f.instructions.add(VMInstr.SUB());       // 9
    f.instructions.add(VMInstr.CALL("sum")); // 10
    f.instructions.add(VMInstr.LOAD(0));     // 11
    f.instructions.add(VMInstr.ADD());       // 12
    f.instructions.add(VMInstr.VRET());      // 13
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(3));
    main.instructions.add(VMInstr.CALL("sum"));
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("6", output.toString());
  }
  
  // //------------------------------------------------------------
  // // Built-ins
  // //------------------------------------------------------------

  @Test
  public void lenTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.LEN());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("4", output.toString());
  }

  @Test
  public void getCharTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(2));
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.GETCHR());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("u", output.toString());
  }

  @Test
  public void exceedsBoundsGetCharTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(4));
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.GETCHR());
    main.instructions.add(VMInstr.WRITE()); // return value
    try {
      vm.run();
      fail("no error reported in TOINT");
    }
    catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("VM_ERROR:"));
    }
  }

  @Test
  public void preceedsBoundsGetCharTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(-1));
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.GETCHR());
    main.instructions.add(VMInstr.WRITE()); // return value
    try {
      vm.run();
      fail("no error reported in TOINT");
    }
    catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("VM_ERROR:"));
    }
  }
  
  @Test
  public void stringToIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("42"));
    main.instructions.add(VMInstr.TOINT());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("42", output.toString());
  }
  
  @Test
  public void nonIntStringToIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.TOINT());
    main.instructions.add(VMInstr.WRITE()); // return value
    try {
      vm.run();
      fail("no error reported in TOINT");
    }
    catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("VM_ERROR:"));
    }
  }

  @Test
  public void doubleToIntTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(3.125));
    main.instructions.add(VMInstr.TOINT());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("3", output.toString());
  }
  
  @Test
  public void stringToDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("3.125"));
    main.instructions.add(VMInstr.TODBL());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("3.125", output.toString());
  }
  
  @Test
  public void nonDoubleStringToDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.TODBL());
    main.instructions.add(VMInstr.WRITE()); // return value
    try {
      vm.run();
      fail("no error reported in TODBL");
    }
    catch(MyPLException ex) {
      assertTrue(ex.getMessage().startsWith("VM_ERROR:"));
    }
  }

  @Test
  public void intToDoubleTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(42));
    main.instructions.add(VMInstr.TODBL());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("42.0", output.toString());
  }

  @Test
  public void intToStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(42));
    main.instructions.add(VMInstr.TOSTR());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("42", output.toString());
  }

  @Test
  public void doubleToStringTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH(3.125));
    main.instructions.add(VMInstr.TOSTR());
    main.instructions.add(VMInstr.WRITE()); // return value
    vm.run();
    assertEquals("3.125", output.toString());
  }
  
  //------------------------------------------------------------
  // Heap related
  //------------------------------------------------------------

  @Test
  public void basicAllocTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    List<String> fields = new ArrayList<>();
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.PUSH(" "));
    main.instructions.add(VMInstr.WRITE());                          
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("1111 1112", output.toString());
  }

  @Test
  public void basicSetAndGetOneFieldTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    List<String> fields = new ArrayList<>();
    fields.add("x");
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.SETFLD("x"));
    main.instructions.add(VMInstr.GETFLD("x"));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  public void basicSetAndGetTwoFieldsTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    List<String> fields = new ArrayList<>();
    fields.add("x");
    fields.add("y");    
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.SETFLD("x"));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.PUSH("green"));    
    main.instructions.add(VMInstr.SETFLD("y"));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.GETFLD("x"));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.GETFLD("y"));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  public void basicSetAndGetTwoAllocsTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    List<String> fields = new ArrayList<>();
    fields.add("x");
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.PUSH("blue"));
    main.instructions.add(VMInstr.SETFLD("x"));
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.PUSH("green"));
    main.instructions.add(VMInstr.SETFLD("x"));
    main.instructions.add(VMInstr.GETFLD("x"));
    main.instructions.add(VMInstr.WRITE());
    main.instructions.add(VMInstr.GETFLD("x"));
    main.instructions.add(VMInstr.WRITE());
    vm.run();
    assertEquals("greenblue", output.toString());
  }
  
  @Test
  public void basicFreeTest() throws Exception {
    VM vm = new VM();
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    List<String> fields = new ArrayList<>();
    fields.add("x");
    main.instructions.add(VMInstr.ALLOC(fields));
    main.instructions.add(VMInstr.DUP());
    main.instructions.add(VMInstr.FREE());
    main.instructions.add(VMInstr.GETFLD("x"));
    try {
      vm.run();
      fail("no error reported in GETFLD");
    }
    catch(Exception ex) {
      // should be a NPE
    }
  }
}
