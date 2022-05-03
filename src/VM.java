/*
 * File: VM.java
 * Date: Spring 2022
 * Auth: 
 * Desc: A bare-bones MyPL Virtual Machine. The architecture is based
 *       loosely on the architecture of the Java Virtual Machine
 *       (JVM).  Minimal error checking is done except for runtime
 *       program errors, which include: out of bound indexes,
 *       dereferencing a nil reference, and invalid value conversion
 *       (to int and double).
 */

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Scanner;

/*----------------------------------------------------------------------

        Your main job for HW-6 is to finish the VM implementation
        below by finishing the handling of each instruction.

        Note that PUSH, NOT, JMP, READ, FREE, and NOP (trivially) are
        completed already to help get you started. 

        Be sure to look through OpCode.java to get a basic idea of
        what each instruction should do as well as the unit tests for
        additional details regarding the instructions.

        Note that you only need to perform error checking if the
        result would lead to a MyPL runtime error (where all
        compile-time errors are assumed to be found already). This
        includes things like bad indexes (in GETCHR), dereferencing
        and/or using a NIL_OBJ (see the ensureNotNil() helper
        function), and converting from strings to ints and doubles. An
        error() function is provided to help generate a MyPLException
        for such cases.

----------------------------------------------------------------------*/

class VM {

  // set to true to print debugging information
  private boolean DEBUG = false;

  // the VM's heap (free store) accessible via object-id
  private Map<Integer, Map<String, Object>> heap = new HashMap<>();

  // next available object-id
  private int objectId = 1111;

  // the frames for the program (one frame per function)
  private Map<String, VMFrame> frames = new HashMap<>();

  // the VM call stack
  private Deque<VMFrame> frameStack = new ArrayDeque<>();

  /**
   * For representing "nil" as a value
   */
  public static String NIL_OBJ = new String("nil");

  /**
   * Add a frame to the VM's list of known frames
   * 
   * @param frame the frame to add
   */
  public void add(VMFrame frame) {
    frames.put(frame.functionName(), frame);
  }

  /**
   * Turn on/off debugging, which prints out the state of the VM prior
   * to each instruction.
   * 
   * @param debug set to true to turn on debugging (by default false)
   */
  public void setDebug(boolean debug) {
    DEBUG = debug;
  }

  /**
   * Run the virtual machine
   */
  public void run() throws MyPLException {

    // grab the main stack frame
    if (!frames.containsKey("main"))
      throw MyPLException.VMError("No 'main' function");
    VMFrame frame = frames.get("main").instantiate();
    frameStack.push(frame);

    // run loop (keep going until we run out of frames or
    // instructions) note that we assume each function returns a
    // value, and so the second check below should never occur (but is
    // useful for testing, etc).
    while (frame != null && frame.pc < frame.instructions.size()) {
      // get next instruction
      VMInstr instr = frame.instructions.get(frame.pc);
      // increment instruction pointer
      ++frame.pc;

      // For debugging: to turn on the following, call setDebug(true)
      // on the VM.
      if (DEBUG) {
        System.out.println();
        System.out.println("\t FRAME........: " + frame.functionName());
        System.out.println("\t PC...........: " + (frame.pc - 1));
        System.out.println("\t INSTRUCTION..: " + instr);
        System.out.println("\t OPERAND STACK: " + frame.operandStack);
        System.out.println("\t HEAP ........: " + heap);
      }

      // ------------------------------------------------------------
      // Consts/Vars
      // ------------------------------------------------------------

      if (instr.opcode() == OpCode.PUSH) {
        frame.operandStack.push(instr.operand());
      }

      else if (instr.opcode() == OpCode.POP) {
        frame.operandStack.pop();
      }

      else if (instr.opcode() == OpCode.LOAD) {
        frame.operandStack.push(frame.variables.get((Integer) instr.operand()));
      }

      else if (instr.opcode() == OpCode.STORE) {
        if ((Integer) instr.operand() >= frame.variables.size())
          frame.variables.add(frame.operandStack.pop());
        else
          frame.variables.set((Integer) instr.operand(), frame.operandStack.pop());
      }

      // ------------------------------------------------------------
      // Ops
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.ADD) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand + (Integer) operand2);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand + (Double) operand2);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push((String) operand2 + (String) operand);
        } else {
          // throw MyPLException.VMError("Invalid operands for ADD");
        }
      }

      else if (instr.opcode() == OpCode.SUB) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 - (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 - (Double) operand);
        } else {
          error("Invalid operands for SUB", frame);
        }
      }

      else if (instr.opcode() == OpCode.MUL) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand * (Integer) operand2);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand * (Double) operand2);
        } else {
          error("Invalid operands for MUL", frame);
        }
      }

      else if (instr.opcode() == OpCode.DIV) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 / (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 / (Double) operand);
        } else {
          error("Invalid operands for DIV", frame);
        }
      }

      else if (instr.opcode() == OpCode.MOD) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        frame.operandStack.push((Integer) operand2 % (Integer) operand);
      }

      else if (instr.opcode() == OpCode.AND) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        frame.operandStack.push((Boolean) operand && (Boolean) operand2);
      }

      else if (instr.opcode() == OpCode.OR) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        frame.operandStack.push((Boolean) operand || (Boolean) operand2);
      }

      else if (instr.opcode() == OpCode.NOT) {
        Object operand = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        frame.operandStack.push(!(Boolean) operand);
      }

      else if (instr.opcode() == OpCode.CMPLT) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 < (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 < (Double) operand);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) < 0);
        } else {
          error("Invalid operands for CMPLT", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPLE) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 <= (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 <= (Double) operand);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) <= 0);
        } else {
          error("Invalid operands for CMPLE", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPGT) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 > (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 > (Double) operand);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) > 0);
        } else {
          error("Invalid operands for CMPGT", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPGE) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        ensureNotNil(frame, operand2);
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 >= (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push((Double) operand2 >= (Double) operand);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) >= 0);
        } else {
          error("Invalid operands for CMPGE", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPEQ) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 == (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          boolean result = Double.compare((Double) operand, (Double) operand2) == 0;
          frame.operandStack.push(result);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) == 0);
        } else {
          frame.operandStack.push((operand == operand2));
        }
      }

      else if (instr.opcode() == OpCode.CMPNE) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        if (operand instanceof Integer && operand2 instanceof Integer) {
          frame.operandStack.push((Integer) operand2 != (Integer) operand);
        } else if (operand instanceof Double && operand2 instanceof Double) {
          frame.operandStack.push(Double.compare((Double) operand2, (Double) operand) != 0);
        } else if (operand instanceof String && operand2 instanceof String) {
          frame.operandStack.push(((String) operand2).compareTo((String) operand) != 0);
        } else {
          frame.operandStack.push(operand != operand2);
        }
      }

      else if (instr.opcode() == OpCode.NEG) {
        Object operand = frame.operandStack.pop();
        if (operand instanceof Integer)
          frame.operandStack.push(-((Integer) operand));
        else if (operand instanceof Double)
          frame.operandStack.push(-((Double) operand));
        else
          error("Expecting integer or double for neg", frame);
      }

      // ------------------------------------------------------------
      // Jumps
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.JMP) {
        frame.pc = (int) instr.operand();
      }

      else if (instr.opcode() == OpCode.JMPF) {
        Object operand = instr.operand();
        Object stackItem = frame.operandStack.pop();
        if (((Boolean) stackItem).booleanValue() == false)
          frame.pc = (int) operand;
        else
          frame.operandStack.push(stackItem);
      }

      // ------------------------------------------------------------
      // Functions
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.CALL) {
        // (1) get frame and instantiate a new copy
        // (2) Pop argument values off stack and push into the newFrame
        // (3) Push the new frame onto frame stack
        // (4) Set the new frame as the current frame
        VMFrame newFrame = frames.get(instr.operand()).instantiate();
        for (int i = 0; i < newFrame.argCount(); i++) {
          newFrame.operandStack.push(frame.operandStack.pop());
        }
        frameStack.push(newFrame);
        frame = newFrame;
      }

      else if (instr.opcode() == OpCode.VRET) {
        // (1) pop return value off of stack
        // (2) remove the frame from the current frameStack
        // (3) set frame to the frame on the top of the stack
        // (4) push the return value onto the operand stack of the frame
        Object returnValue = frame.operandStack.pop();
        frameStack.pop();
        frame = frameStack.peek();
        if (frame != null)
          frame.operandStack.push(returnValue);
      }

      // ------------------------------------------------------------
      // Built-ins
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.WRITE) {
        System.out.print(String.valueOf(frame.operandStack.pop()));
      }

      else if (instr.opcode() == OpCode.READ) {
        Scanner s = new Scanner(System.in);
        frame.operandStack.push(s.nextLine());
      }

      else if (instr.opcode() == OpCode.LEN) {
        String str = (String) frame.operandStack.pop();
        frame.operandStack.push(str.length());
      }

      else if (instr.opcode() == OpCode.GETCHR) {
        String operand = (String) frame.operandStack.pop();
        Integer operand2 = (Integer) frame.operandStack.pop();
        if (operand2.intValue() < 0 || operand2.intValue() >= operand.length()) {
          error("Index out of bounds", frame);
        }

        frame.operandStack.push(String.valueOf((operand).charAt(operand2)));
      }

      else if (instr.opcode() == OpCode.TOINT) {
        Object operand = frame.operandStack.pop();
        if (operand instanceof String)
          try {
            frame.operandStack.push(Integer.parseInt((String) operand));
          } catch (NumberFormatException e) {
            error("Invalid string to int conversion", frame);
          }
        else if (operand instanceof Double)
          frame.operandStack.push(((Double) operand).intValue());
        else if (operand instanceof Integer)
          frame.operandStack.push((Integer) operand);
        else
          error("Invalid operand for TOINT", frame);
      }

      else if (instr.opcode() == OpCode.TODBL) {
        Object operand = frame.operandStack.pop();
        if (operand instanceof String)
          try {
            frame.operandStack.push(Double.parseDouble((String) operand));
          } catch (NumberFormatException e) {
            error("Invalid string to double conversion", frame);
          }
        else if (operand instanceof Double)
          frame.operandStack.push(((Double) operand));
        else if (operand instanceof Integer)
          frame.operandStack.push(((Integer) operand).doubleValue());
      }

      else if (instr.opcode() == OpCode.TOSTR) {
        Object operand = frame.operandStack.pop();
        frame.operandStack.push(operand.toString());
      }

      // Saves the start time in MS inside the stackframe.
      else if (instr.opcode() == OpCode.TIMESTART) {
        frame.startTime();
      }

      // Saves the end time in MS inside the stackframe.
      else if (instr.opcode() == OpCode.TIMEEND) {
        frame.endTime();
      }

      // Pushes the change in time to the stack
      else if (instr.opcode() == OpCode.TIMEDELTA) {
        frame.operandStack.push(frame.deltaTime());
      }

      // ------------------------------------------------------------
      // Heap related
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.ALLOC) {
        heap.put(objectId, new HashMap<>());
        frame.operandStack.push(objectId++);
      }

      else if (instr.opcode() == OpCode.FREE) {
        // pop the oid to
        Object oid = frame.operandStack.pop();
        ensureNotNil(frame, oid);
        // remove the object with oid from the heap
        heap.remove((int) oid);
      }

      else if (instr.opcode() == OpCode.SETFLD) {
        Object toAdd = frame.operandStack.pop();
        Object oid = frame.operandStack.pop();
        Map<String, Object> fieldMap = heap.get((Integer) oid);
        fieldMap.put((String) instr.operand(), toAdd);
      }

      else if (instr.opcode() == OpCode.GETFLD) {
        Integer oid = (Integer) frame.operandStack.pop();
        if (!heap.containsKey(oid))
          error("Invalid object reference", frame);
        heap.get(oid).get((String) instr.operand());
        frame.operandStack.push(heap.get(oid).get((String) instr.operand()));
      }

      // ------------------------------------------------------------
      // Special instructions
      // ------------------------------------------------------------

      else if (instr.opcode() == OpCode.DUP) {
        Object operand = frame.operandStack.pop();
        frame.operandStack.push(operand);
        frame.operandStack.push(operand);
      }

      else if (instr.opcode() == OpCode.SWAP) {
        Object operand = frame.operandStack.pop();
        Object operand2 = frame.operandStack.pop();
        frame.operandStack.push(operand);
        frame.operandStack.push(operand2);
      }

      else if (instr.opcode() == OpCode.NOP) {
        // NOTHING HERE
      }

    }
  }

  // to print the lists of instructions for each VM Frame
  @Override
  public String toString() {
    String s = "";
    for (Map.Entry<String, VMFrame> e : frames.entrySet()) {
      String funName = e.getKey();
      s += "Frame '" + funName + "'\n";
      List<VMInstr> instructions = e.getValue().instructions;
      for (int i = 0; i < instructions.size(); ++i) {
        VMInstr instr = instructions.get(i);
        s += "  " + i + ": " + instr + "\n";
      }
      // s += "\n";
    }
    return s;
  }

  // ----------------------------------------------------------------------
  // HELPER FUNCTIONS
  // ----------------------------------------------------------------------

  // error
  private void error(String m, VMFrame f) throws MyPLException {
    int pc = f.pc - 1;
    VMInstr i = f.instructions.get(pc);
    String name = f.functionName();
    m += " (in " + name + " at " + pc + ": " + i + ")";
    throw MyPLException.VMError(m);
  }

  // error if given value is nil
  private void ensureNotNil(VMFrame f, Object v) throws MyPLException {
    if (v == NIL_OBJ)
      error("Nil reference", f);
  }
}
