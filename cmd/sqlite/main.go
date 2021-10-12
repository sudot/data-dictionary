package main

import (
	"fmt"
	"github.com/sudot/data-dictionary/internal"
	"github.com/sudot/data-dictionary/internal/db"
	"github.com/sudot/data-dictionary/internal/tealeg"
	"github.com/tealeg/xlsx"
	"os"
)

func main() {
	config := src.ConfigValue
	tableDao := db.NewSqliteTableDao(config)
	tables, err := tableDao.Tables()
	inputBytes := make([]byte, 1)
	if err != nil {
		fmt.Println("表信息获取失败\r\n请按回车键继续")
		os.Stdin.Read(inputBytes)
		os.Exit(-1)
	}
	tableColumns, err := tableDao.Columns(tables)
	if err != nil {
		fmt.Println("表字段信息获取失败\r\n请按回车键继续")
		os.Stdin.Read(inputBytes)
		os.Exit(-1)
	}

	xlsxFile := xlsx.NewFile()
	tealeg.DrawCatalogue(xlsxFile, tables)
	tealeg.DrawTableColumns(xlsxFile, tables, tableColumns)
	xlsxFile.Save(config.FilePath)

	fmt.Printf("数据字典已成功导出到:%s\r\n请按回车键继续", config.FilePath)
	os.Stdin.Read(inputBytes)
}
