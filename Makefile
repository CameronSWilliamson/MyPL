build:
	@clear
	@bazel build //:mypl

lexer: build
	@bazel test --test_output=errors //:lexer-test

parser: build
	@bazel test --test_output=errors //:parser-test

ast: build
	@bazel test --test_output=errors //:ast-parser-test

static: build
	@bazel test --test_output=errors //:static-checker-test

code: build
	@bazel test --test_output=errors //:code-generator-test

vm: build
	@bazel test --test_output=errors //:vm-test

all: build
	@bazel test --test_output=errors //...

hello: build
	@bazel-bin/mypl --lex examples/hello.mypl

pretty1: build
	@bazel build //:mypl
	@bazel-bin/mypl --print examples/print-1.mypl


pretty2: build
	@bazel build //:mypl
	@bazel-bin/mypl --print examples/print-2.mypl


pretty3: build
	@bazel build //:mypl
	@bazel-bin/mypl --print examples/print-3.mypl
