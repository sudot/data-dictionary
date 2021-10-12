package main

import (
	"fmt"
	"github.com/sudot/data-dictionary/internal"
	"github.com/sudot/data-dictionary/internal/db"
	"io/fs"
	"io/ioutil"
	"os"
	"strings"
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

	var contents []string
	contents = append(contents, "package main")

	for _, table := range tables {
		columns := tableColumns[table.Name]
		structContent := fmt.Sprintf("type %s []struct {", table.Name)
		for _, column := range columns {
			structContent += fmt.Sprintf("%s %s `json:\"%s\"`\n",
				StructName(column.ColumnName),
				SqliteType2GoType(column.ColumnType),
				column.ColumnName,
			)
		}
		structContent += "}"
		contents = append(contents, structContent)
	}

	ioutil.WriteFile(config.FilePath, []byte(strings.Join(contents, "\n\n")), fs.ModePerm)
	fmt.Printf("数据字典已成功导出到:%s", config.FilePath)
}

func StructName(name string) string {
	return strings.ToUpper(name[:1]) + name[1:]
}

func SqliteType2GoType(s string) string {
	switch s {
	case "":
		fallthrough
	case "VARCHAR":
		fallthrough
	case "TEXT":
		return "string"
	case "INTEGER":
		fallthrough
	case "INTEGER DOUBLE":
		return "int"
	case "DOUBLE":
		return "float64"
	case "BOOL":
		return "bool"
	case "BLOB":
		return "[]byte"
	}
	return ""
}
