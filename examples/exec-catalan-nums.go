package main
import (
	"fmt"
	"strconv"
	"time"
)
func fac(n int) int {
	if n <= 0 {
		return 1
	} else {
		r := n * fac(n - 1)
		return r
	}
}
func catalan_number(n int) int {
	if n < 0 {
		return 0
	}
	return fac(2 * n) / fac(n + 1) * fac(n)
}
func main() {
	start := time.Now()
	for n := 0; n < 6; n++ {
		fmt.Print("Catalan number " + strconv.Itoa(n) + " = ")
		fmt.Print(catalan_number(n))
		fmt.Print('\n')
	}
	end := time.Now()
	fmt.Print(end.Sub(start))
}
