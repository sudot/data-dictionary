package main

import (
	"sudot.net/sudot/data-dictionary/src"
	"sudot.net/sudot/data-dictionary/src/db"
)

func main() {
	list := db.Connection(src.ConfigValue)
	println("list", list)
	println("ddd")
}
