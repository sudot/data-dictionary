package main

import (
	"fmt"
	"github.com/tealeg/xlsx"
	"os"
	"sudot.net/sudot/data-dictionary/src"
	"sudot.net/sudot/data-dictionary/src/db"
)

const HOME_SHEET_NAME = "总目录"

func main() {
	config := src.ConfigValue
	tableDao := db.NewMySqlTableDao(config)
	tables, err := tableDao.Tables()
	if err != nil {
		fmt.Println("表信息获取失败")
		os.Exit(-1)
	}
	//columns, err := tableDao.Columns()
	//if err != nil {
	//	fmt.Println("表字段信息获取失败")
	//	os.Exit(-1)
	//}
	//
	//xlsxFile := excelize.NewFile()
	//drawCatalogue(xlsxFile, tables)
	//xlsxFile.SaveAs(config.FilePath)

	xlsxFile := xlsx.NewFile()
	drawCatalogue(xlsxFile, tables)
	xlsxFile.Save(config.FilePath)

}

//func drawCatalogue(xlsxFile *excelize.File, tables []db.Table) {
//	newSheetIndex := xlsxFile.NewSheet(HOME_SHEET_NAME)
//	if newSheetIndex > 1 {
//		// 删除默认的Sheet
//		xlsxFile.DeleteSheet(xlsxFile.GetSheetName(newSheetIndex - 1))
//	}
//	xlsxFile.SetCellStr(HOME_SHEET_NAME, "A1", "表名 Table Name")
//	xlsxFile.SetCellStr(HOME_SHEET_NAME, "B1", "描述 Comments")
//	for index, table := range tables {
//		xlsxFile.SetCellStr(HOME_SHEET_NAME, fmt.Sprintf("A%d", index+2), table.Name)
//		xlsxFile.SetCellHyperLink(HOME_SHEET_NAME, fmt.Sprintf("A%d", index+2), fmt.Sprintf("#%s!A1", table.Name), "Location")
//		xlsxFile.SetCellStr(HOME_SHEET_NAME, fmt.Sprintf("B%d", index+2), table.Comment)
//	}
//}

func drawCatalogue(xlsxFile *xlsx.File, tables []db.Table) {
	sheet, err := xlsxFile.AddSheet(HOME_SHEET_NAME)
	if err != nil {
		return
	}
	row := sheet.AddRow()
	row.AddCell().Value = "表名 Table Name"
	row.AddCell().Value = "描述 Comments"
	for _, table := range tables {
		row := sheet.AddRow()
		cell := row.AddCell()
		cell.SetFormula(fmt.Sprintf("=HYPERLINK(\"#%s!A1\",\"%s\")", table.Name, table.Name))
		style := cell.GetStyle()
		style.Font.Underline = true   //加下划线
		style.Font.Color = "FF0000FF" //设置字体颜色为蓝色
		cell.SetStyle(style)
		row.AddCell().Value = table.Comment
	}
}
