package main

import (
	"fmt"
	"sudot.net/sudot/data-dictionary/src"
	"sudot.net/sudot/data-dictionary/src/db"
)

func main() {
	list := db.Connection(src.ConfigValue)
	if list == nil {
		fmt.Println("数据表信息获取失败")
	}
	fmt.Println("list", list)
	fmt.Println("ddd")
}
