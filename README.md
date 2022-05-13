<div id="top"></div>
<!-- PROJECT LOGO -->
<br />
<div align="center">
<h3 align="center">MyPL Language Compiler</h3>

  <p align="center">
    Programming language built using a JVM-like interpreter. This language also has the ability to be transpiled to go to improve performance.
    <br />
    <a href="https://github.com/CameronSWilliamson/MyPL">View Demo</a>
    ·
    <a href="https://github.com/CameronSWilliamson/MyPL/issues">Report Bug</a>
    ·
    <a href="https://github.com/CameronSWilliamson/MyPL/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
  </ol>
</details>

## Tests

This project was tested using both `GoVisitorTest.java` and using the files stored within `./examples`.

## Built With

* Java
* [Bazel](https://bazel.build/)

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## Usage

You can compile the compiler using the `bazel build //:mypl` command from the commandline from the project directory.


### MyPL

To execute MyPL code using the native compiler, run 

```
bazel-bin/mypl [filename]
```

from the project directory.

### Go Transpiler

To transpile MyPL code into go code run the following:

```
$ bazel-bin/mypl --go [filename]
```

This creates a new file called `[filename].go` that can be then run using

```
$ go run [filename].go
```

You are also able to use the Makefile provided by running the following command for generating and executing MyPL-GO code (leave off the extension for filename):

```
$ make gorun FILE=[filename]
```

```
$ make run FILE=[filename]
``` 

Will run the source code file using the MyPL interpreter.


<!-- ROADMAP -->
## Roadmap

* [x] Lexer
* [x] Parser
* [x] ASTParser
* [x] VM
  * [x] Code Runner
  * [x] Code Generator
* [x] Go Transpiler

<p align="right">(<a href="#top">back to top</a>)</p>

## Go Limitations

- Type inferencing is no longer allowed within type declarations when converting to Go Code. This is due to a limitation in the `GoVisitor`. It can be improved, but most likely will not be before the due date of this assignment.