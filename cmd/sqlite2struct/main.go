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
		structContent := fmt.Sprintf("type %s []struct {", ConvertCamelName(table.Name))
		for _, column := range columns {
			structContent += fmt.Sprintf("%s %s `json:\"%s\"`\n",
				ConvertCamelName(column.ColumnName),
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

// ConvertCamelName 转驼峰并且首字母大写
func ConvertCamelName(name string) string {
	out := make([]rune, 0, len(name))
	isUnderline := false
	for i, c := range name {
		if i == 0 && c >= 'a' && c <= 'z' {
			out = append(out, c-32)
		} else if c == '_' {
			isUnderline = true
		} else {
			if isUnderline {
				isUnderline = false
				if c >= 'a' && c <= 'z' {
					out = append(out, c-32)
					continue
				}
			}
			out = append(out, c)
		}
	}
	return string(out)
}

func SqliteType2GoType(s string) string {
	switch s {
	case "":
		fallthrough
	case "NULL":
		fallthrough
	case "VARCHAR":
		fallthrough
	case "TEXT":
		return "string"
	case "NUMERIC":
		fallthrough
	case "INTEGER":
		return "int"
	case "REAL":
		return "float32"
	case "DOUBLE":
		return "float64"
	case "BOOL":
		return "bool"
	case "BLOB":
		return "[]byte"
	}
	panic("未支持的数据类型：" + s)
}
