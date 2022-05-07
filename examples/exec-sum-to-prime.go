package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
)
func is_prime(n int) bool {
	m := n / 2
	v := 2
	for v <= m {
		r := n / v
		p := r * v
		if p == n {
			return false
		}
		v = v + 1
	}
	return true
}
func main() {
	fmt.Print("Please enter integer values to sum (prime number to quit)\n")
	sum := 0
	for {
		fmt.Print(">> Enter an int: ")
		val := stoi(read())
		if is_prime(val) {
			fmt.Print("The sum is: " + strconv.Itoa(sum) + "\n")
			fmt.Print("Goodbye!\n")
			return
		}
		sum = sum + val
	}
}
func stoi(s string) int {
	num, _ := strconv.Atoi(s)
	return num
}
func read() string {
	reader := bufio.NewReader(os.Stdin)
	text, _ := reader.ReadString('\n')
	return text
}
