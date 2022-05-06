package main

import (
	"fmt"
	"strconv"
	"time"
)
type Node struct {
	value int
	left *Node
	right *Node
}
func make_tree(val int) *Node {
	ptr := makeNode()
	ptr.value = val
	return ptr
}
func insert(root *Node, val int) {
	if root == nil {
		return
	}
	if val <= root.value {
		if root.left == nil {
			root.left = makeNode()
			root.left.value = val
		} else {
			insert(root.left, val)
		}
	} else {
		if root.right == nil {
			root.right = makeNode()
			root.right.value = val
		} else {
			insert(root.right, val)
		}
	}
}
func print_tree(root *Node) {
	if root != nil {
		print_tree(root.left)
		fmt.Print(strconv.Itoa(root.value) + " ")
		print_tree(root.right)
	}
}
func height(root *Node) int {
	if root == nil {
		return 0
	} else {
		left_height := height(root.left)
		right_height := height(root.right)
		if (left_height) >= right_height {
			return 1 + left_height
		} else {
			return 1 + right_height
		}
	}
}
func erase(root *Node) {
	if root == nil {
		return
	}
	erase(root.left)
	erase(root.right)
	
}
func main() {
	tree := make_tree(10)
	start := time.Now()
	for i := 0; i < 10000; i++ {
		i = i + 1
		insert(tree, i)
	}
	for i := 1; i < 10000; i++ {
		i = i + 1
		insert(tree, i)
	}
	end := time.Now()
	insertTime := end.Sub(start)
	fmt.Print("Tree Values: ")
	start = time.Now()
	print_tree(tree)
	end = time.Now()
	printTime := end.Sub(start)
	fmt.Print("\n")
	start = time.Now()
	fmt.Print("Tree Height: ")
	end = time.Now()
	heightTime := end.Sub(start)
	fmt.Print(strconv.Itoa(height(tree)))
	fmt.Print("\n")
	start = time.Now()
	erase(tree)
	end = time.Now()
	fmt.Print("insert time: ")
	fmt.Print(insertTime)
	fmt.Print("\nprint time: ")
	fmt.Print(printTime)
	fmt.Print("\nheight time: ")
	fmt.Print(heightTime)
	fmt.Print("\nerase time: ")
	fmt.Print(end.Sub(start))
}
func makeNode() *Node {
	return &Node{
		value: 0,
		left: nil,
		right: nil,
	}
}
