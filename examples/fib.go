package main

import "fmt"

type Vertex struct {
	X int
}

func 

func fibonacci(n int) int {
	if n < 2 {
		return n
	}
	return fibonacci(n-1) + fibonacci(n-2)
}

func main() {
	fmt.Println(fibonacci(7)) // 13
}
