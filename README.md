<div id="top"></div>
<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/CameronSWilliamson/MyPL">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">MyPL Language Compiler</h3>

  <p align="center">
    project_description
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
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

### Built With

* Java
* [Bazel](https://bazel.build/)

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## Usage

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

- Dynamic typing is no longer allowed within typeclasses when converting to Go Code. This is due to a limitation in the `GoVisitor`. It can be improved, but most likely will not be before the due date of this assignment.