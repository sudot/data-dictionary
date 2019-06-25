package main

import (
	"fmt"
	"github.com/tealeg/xlsx"
	"os"
	"sudot.net/sudot/data-dictionary/src"
	"sudot.net/sudot/data-dictionary/src/db"
	"sudot.net/sudot/data-dictionary/src/tealeg"
)

func main() {
	config := src.ConfigValue
	tableDao := db.NewMySqlTableDao(config)
	tables, err := tableDao.Tables()
	inputBytes := make([]byte, 1)
	if err != nil {
		fmt.Println("表信息获取失败\r\n请按回车键继续")
		os.Stdin.Read(inputBytes)
		os.Exit(-1)
	}
	columns, err := tableDao.Columns()
	if err != nil {
		fmt.Println("表字段信息获取失败\r\n请按回车键继续")
		os.Stdin.Read(inputBytes)
		os.Exit(-1)
	}

	tableColumns := make(map[string][]src.TableColumn)
	for _, column := range columns {
		tableColumns[column.TableName] = append(tableColumns[column.TableName], column)
	}

	xlsxFile := xlsx.NewFile()
	tealeg.DrawCatalogue(xlsxFile, tables)
	tealeg.DrawTableColumns(xlsxFile, tables, tableColumns)
	xlsxFile.Save(config.FilePath)

	fmt.Printf("数据字典已成功导出到:%s\r\n请按回车键继续", config.FilePath)
	os.Stdin.Read(inputBytes)
}
