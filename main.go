package main

import (
	"fmt"
	"sudot.net/sudot/data-dictionary/src"
	"sudot.net/sudot/data-dictionary/src/db"
)

func main() {
	list := db.Connection(src.ConfigValue)
	fmt.Println("list", list)
	fmt.Println("ddd")
}
