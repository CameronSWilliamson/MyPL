package main
import (
	"fmt"
	"time"
)
type Node struct {
	value int
	next *Node
}
type List struct {
	head *Node
	length int
}
func insert(list *List, val int) {
	newNode := makeNode()
	newNode.value = val
	newNode.next = list.head
	list.head = newNode
	list.length = list.length + 1
}
func internal_print(curNode *Node) {
	if curNode == nil {
		return
	}
	fmt.Print(curNode.value)
	fmt.Print(" ")
	internal_print(curNode.next)
}
func merge_sort(list *List) {
	if list.length == 0 {
		return
	}
	list.head = inner_merge_sort(list.head, list.length)
}
func inner_merge_sort(left *Node, len int) *Node {
	if len <= 1 {
		return left
	}
	mid := len / 2
	tmp := left
	for i := 1; i < mid - 1; i++ {
		tmp = tmp.next
	}
	right := tmp.next
	tmp.next = nil
	left = inner_merge_sort(left, mid)
	right = inner_merge_sort(right, len - mid)
	tmp = makeNode()
	cur := tmp
	for left != nil && right != nil {
		if left.value < right.value {
			cur.next = left
			left = left.next
			cur = cur.next
		} else {
			cur.next = right
			right = right.next
			cur = cur.next
		}
	}
	for left != nil {
		cur.next = left
		left = left.next
		cur = cur.next
	}
	for right != nil {
		cur.next = right
		right = right.next
		cur = cur.next
	}
	cur = tmp.next
	
	return cur
}
func main() {
	list := makeList()
	start := time.Now()
	for i := 0; i < 1000; i++ {
		i = i + 1
		insert(&(list), i)
	}
	for i := 1; i < 1000; i++ {
		i = i + 1
		insert(&(list), i)
	}
	fmt.Print(list.head.value)
	fmt.Print(list.head.value)
	end := time.Now()
	internal_print(list.head)
	fmt.Print(end.Sub(start))
	start = time.Now()
	merge_sort(&(list))
	internal_print(list.head)
	end = time.Now()
	end.Sub(start)
}
func makeNode() *Node {
	return &Node{
		value: 0,
		next: nil,
	}
}
func makeList() List {
	return List{
		head: nil,
		length: 0,
	}
}
