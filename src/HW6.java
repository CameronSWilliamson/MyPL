/*
 * File: HW6.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Example program to test the MyPL VM
 */


/*----------------------------------------------------------------------
   Your job for this part of the assignment is to imlement the
   following as a set of MyPL VM instructions and the VM. Note that
   you must implement the is_prime function and generally folow the
   approach laid out below. You can view the following as pseudocode
   (which could have been written in any procedural programming
   language). Note that since we don't have a square root function in
   MyPL, our naive primality tester is not very efficient.

    fun bool is_prime(int n) {
      var m = n / 2
      var v = 2
      while v <= m {
        var r = n / v
        var p = r * v
        if p == n {
          return false
        }
        v = v + 1
      }
      return true
    }

    fun void main() {
      print("Please enter integer values to sum (prime number to quit)\n")
      var sum = 0
      while true {
        print(">> Enter an int: ")
        var val = stoi(read())
        if is_prime(val) {
          print("The sum is: " + itos(sum) + "\n")
          print("Goodbye!\n")
          return
        }
        sum = sum + val
      }
    }
----------------------------------------------------------------------*/  

public class HW6 {

  public static void main(String[] args) throws Exception {
    VM vm = new VM();
    VMFrame f = new VMFrame("is_prime", 1);
    vm.add(f);
    f.instructions.add(VMInstr.STORE(0));
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.PUSH(2));
    f.instructions.add(VMInstr.DIV());
    f.instructions.add(VMInstr.STORE(1));
    f.instructions.add(VMInstr.PUSH(2));
    f.instructions.add(VMInstr.STORE(2));
    f.instructions.add(VMInstr.LOAD(2));
    f.instructions.add(VMInstr.LOAD(1));
    f.instructions.add(VMInstr.CMPLE());
    f.instructions.add(VMInstr.JMPF(32)); 
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.LOAD(2));
    f.instructions.add(VMInstr.DIV());
    f.instructions.add(VMInstr.STORE(3));
    f.instructions.add(VMInstr.LOAD(3));
    f.instructions.add(VMInstr.LOAD(2));
    f.instructions.add(VMInstr.MUL());
    f.instructions.add(VMInstr.STORE(4));
    f.instructions.add(VMInstr.LOAD(4));
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.CMPEQ());
    f.instructions.add(VMInstr.JMPF(27)); 
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.LOAD(4));
    f.instructions.add(VMInstr.CMPNE());
    f.instructions.add(VMInstr.VRET());
    f.instructions.add(VMInstr.NOP());
    f.instructions.add(VMInstr.LOAD(2));
    f.instructions.add(VMInstr.PUSH(1));
    f.instructions.add(VMInstr.ADD());
    f.instructions.add(VMInstr.STORE(2));
    f.instructions.add(VMInstr.NOP());
    f.instructions.add(VMInstr.PUSH(0));
    f.instructions.add(VMInstr.PUSH(0));
    f.instructions.add(VMInstr.CMPEQ());
    f.instructions.add(VMInstr.VRET());

    f = new VMFrame("main", 0);
    vm.add(f);
    f.instructions.add(VMInstr.PUSH("Please enter integer values to sum (prime number to quit)\\n"));
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.PUSH(0));
    f.instructions.add(VMInstr.STORE(0));
    f.instructions.add(VMInstr.NOP());
    f.instructions.add(VMInstr.PUSH(">> Enter an int: "));
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.READ());
    f.instructions.add(VMInstr.TOINT());
    f.instructions.add(VMInstr.STORE(1));
    f.instructions.add(VMInstr.LOAD(1));
    f.instructions.add(VMInstr.CALL("is_prime"));
    f.instructions.add(VMInstr.JMPF(22));
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.TOSTR());
    f.instructions.add(VMInstr.ADD());
    f.instructions.add(VMInstr.PUSH("\\n"));
    f.instructions.add(VMInstr.ADD());
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.PUSH("Goodbye!\\n"));
    f.instructions.add(VMInstr.WRITE());
    f.instructions.add(VMInstr.JMP(25));
    f.instructions.add(VMInstr.NOP());
    f.instructions.add(VMInstr.LOAD(0));
    f.instructions.add(VMInstr.LOAD(1));
    f.instructions.add(VMInstr.ADD());
    f.instructions.add(VMInstr.JMP(4));
    f.instructions.add(VMInstr.STORE(1));
    f.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    f.instructions.add(VMInstr.VRET());



    // vm.setDebug(true);   
    vm.run();
  }
}
