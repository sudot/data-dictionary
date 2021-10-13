package main

import (
	"github.com/sudot/data-dictionary/internal"
	"github.com/sudot/data-dictionary/internal/db"
	"github.com/sudot/data-dictionary/internal/tealeg"
	"github.com/tealeg/xlsx"
	"log"
)

func main() {
	config := src.ConfigValue
	tableDao := db.NewMySqlTableDao(config)
	tables, err := tableDao.Tables()
	if err != nil {
		log.Fatal("表信息获取失败")
	}
	columns, err := tableDao.Columns()
	if err != nil {
		log.Fatal("表字段信息获取失败")
	}

	tableColumns := make(map[string][]src.TableColumn)
	for _, column := range columns {
		tableColumns[column.TableName] = append(tableColumns[column.TableName], column)
	}

	xlsxFile := xlsx.NewFile()
	tealeg.DrawCatalogue(xlsxFile, tables)
	tealeg.DrawTableColumns(xlsxFile, tables, tableColumns)
	xlsxFile.Save(config.FilePath)

	log.Printf("数据字典已成功导出到:%s", config.FilePath)
}
