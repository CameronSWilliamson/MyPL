FILE=""
GOFILE=$(FILE).go
MYPLFILE=$(FILE).mypl

build:
	@clear
	@bazel build //:mypl

lexertest: build
	@bazel test --test_output=errors //:lexer-test

parsertest: build
	@bazel test --test_output=errors //:parser-test

asttest: build
	@bazel test --test_output=errors //:ast-parser-test

statictest: build
	@bazel test --test_output=errors //:static-checker-test

codetest: build
	@bazel test --test_output=errors //:code-generator-test

vmtest: build
	@bazel test --test_output=errors //:vm-test

gotest: build
	@bazel test --test_output=errors //:go-test

all: lexer parser ast static code vm go

go: build
	@bazel-bin/mypl --go $(MYPLFILE)

run: build
	@bazel-bin/mypl $(MYPLFILE)

lex: build
	@bazel-bin/mypl --lex $(MYPLFILE)

parse: build
	@bazel-bin/mypl --parse $(MYPLFILE)

print: build
	@bazel-bin/mypl --print $(MYPLFILE)

check: build
	@bazel-bin/mypl --check $(MYPLFILE)

ir: build
	@bazel-bin/mypl --ir $(MYPLFILE)

gorun: build go
	@go run $(GOFILE)