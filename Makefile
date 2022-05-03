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

go: build
	@bazel test --test_output=errors //:go-test

all: lexer parser ast static code vm go
