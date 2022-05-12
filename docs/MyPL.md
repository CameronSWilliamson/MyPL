<!-- Specifications written by Dr. Bowers at Gonzaga University -->
# Language Features

## Comments

```
# This is a single line comment
```

## Primitive Data Types

```
int         # 4 bytes
double      # 8 bytes double precision
bool        # either true or false
char        # single character
string      # sequence of characters
void        # for procedures
```

## Variable Declarations

```
var x1 = 5
var int x2 = x1 + 1
var pi = 3.14
var e = 2.72

var string my_string = nil  # type required with nil
```

## Loops
```
var x = 0

for i from 1 upto 5 {
    print(i)
}

while x < 10 {
    print(x)
    x = x + 1
}
```

## Conditionals
```
if (x == 1) or (x == 2) {

}

if x == 1 {
    print(x)
} elif x == 2 {
    print(x)
} else {
    print(x)
}
```

## Functions

All functions are required to have explicit return and parameter types. Recursion *is* allowed.

```
fun int fib(int i) {
    if i < 0 {
        return nil
    }
    if i == 0 or i == 1 {
        return i
    }
    return fib(n-1) + fib(n-2)
}
```

## Structs

You can use structs in MyPL similarly to structs in C++ or Go. MyPL is able to dynamically infer types within a struct as long as the value is initialized. This does *not* work when you are converting from MyPL to Go.

```
type Node {
    var val = 0
    var Node next = nil 
}

var node = new Node
node.val = 100
node.next = new Node
node.next.val = 200
node.next.next = new Node
node.next.next.val = 300

delete node.next.next
delete node.next
delete node
```

## Built In Conversion Functions

```
# string to int and double (can lead to runtime error )
var int x1 = stoi ("4") # string to int
var double x2 = stod (" 3.14 ") # string to double
# int to string and double
var string y1 = itos (4) # int to string
var double y2 = itod (3) # int to double
# double to string and int
var string z1 = dtos (3.14) # double to string
var int z2 = dtoi (3.14) # double to int ( truncates )
```

## Additional Built-In Functions

```
var string s = read () # read string from standard input
var int x = length ("foo") # number of string chars
var char c = get (0 , "foo") # get i-th string char
```