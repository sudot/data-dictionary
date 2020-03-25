package skylar

import (
	"fmt"
	"github.com/360EntSecGroup-Skylar/excelize/v2"
	"github.com/sudot/data-dictionary/src"
	"strconv"
)

func DrawCatalogue(xlsxFile *excelize.File, tables []src.Table) {
	newSheetIndex := xlsxFile.NewSheet(src.HomeSheetName)
	if newSheetIndex > 1 {
		// 删除默认的Sheet
		xlsxFile.DeleteSheet(xlsxFile.GetSheetName(newSheetIndex - 1))
	}
	xlsxFile.SetCellStr(src.HomeSheetName, "A1", "表名 Table Name")
	xlsxFile.SetCellStr(src.HomeSheetName, "B1", "描述 Comments")

	// 为单元格设置字体和下划线样式
	style, _ := xlsxFile.NewStyle(`{"font":{"color":"#0000CC","underline":"single"}}`)
	for index, table := range tables {
		xlsxFile.SetCellStr(src.HomeSheetName, fmt.Sprintf("A%d", index+2), table.Name)
		xlsxFile.SetCellHyperLink(src.HomeSheetName, fmt.Sprintf("A%d", index+2), fmt.Sprintf("#%s!A1", table.Name), "Location")
		xlsxFile.SetCellStyle(src.HomeSheetName, fmt.Sprintf("A%d", index+2), fmt.Sprintf("A%d", index+2), style)
		xlsxFile.SetCellStr(src.HomeSheetName, fmt.Sprintf("B%d", index+2), table.Comment)
	}

	rows, _ := xlsxFile.Rows(src.HomeSheetName)
	colWidthMap := make(map[int]int, 2)
	for rows.Next() {
		columns, _ := rows.Columns()
		for index, col := range columns {
			width := len(col)
			if colWidthMap[index] < width {
				colWidthMap[index] = width
			}
		}
	}
	xlsxFile.SetColWidth(src.HomeSheetName, "A", "A", float64(colWidthMap[0]))
	xlsxFile.SetColWidth(src.HomeSheetName, "B", "B", float64(colWidthMap[1]))
}

/**
 * 绘制每一个数据表的字段信息
 *
 * @param xlsxFile     表格文件
 * @param tables       数据表信息
 * @param tableColumns 数据表字段信息
 */
func DrawTableColumns(xlsxFile *excelize.File, tables []src.Table, tableColumns map[string][]src.TableColumn) {
	for _, table := range tables {
		xlsxFile.NewSheet(table.Name)
		// 绘制表格头
		drawTablesSheetHeader(xlsxFile, table.Name, table)
		xlsxFile.SetCellStr(table.Name, "A4", "序号 Seq")
		xlsxFile.SetCellStr(table.Name, "B4", "字段名 Name")
		xlsxFile.SetCellStr(table.Name, "C4", "字段类型 Type")
		xlsxFile.SetCellStr(table.Name, "D4", "主键 Primary")
		xlsxFile.SetCellStr(table.Name, "E4", "唯一 Unique")
		xlsxFile.SetCellStr(table.Name, "F4", "空值 Nullable")
		xlsxFile.SetCellStr(table.Name, "G4", "缺省 Default")
		xlsxFile.SetCellStr(table.Name, "H4", "注释 Comments")

		columns := tableColumns[table.Name]
		for index, column := range columns {
			rowIndex := index + 5
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("A%d", rowIndex), strconv.Itoa(index+1))
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("B%d", rowIndex), column.ColumnName)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("C%d", rowIndex), column.ColumnType)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("D%d", rowIndex), column.ColumnKey)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("E%d", rowIndex), column.ColumnUnique)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("F%d", rowIndex), column.IsNullable)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("G%d", rowIndex), column.ColumnDefault)
			xlsxFile.SetCellStr(table.Name, fmt.Sprintf("H%d", rowIndex), column.ColumnComment)
		}

		rows, _ := xlsxFile.Rows(table.Name)
		colWidthMap := make(map[int]int, 8)
		for rows.Next() {
			columns, _ := rows.Columns()
			for index, col := range columns {
				width := len(col)
				if colWidthMap[index] < width {
					colWidthMap[index] = width
				}
			}
		}
		xlsxFile.SetColWidth(table.Name, "A", "A", float64(colWidthMap[0]))
		xlsxFile.SetColWidth(table.Name, "B", "B", float64(colWidthMap[1]))
		xlsxFile.SetColWidth(table.Name, "C", "C", float64(colWidthMap[2]))
		xlsxFile.SetColWidth(table.Name, "D", "D", float64(colWidthMap[3]))
		xlsxFile.SetColWidth(table.Name, "E", "E", float64(colWidthMap[4]))
		xlsxFile.SetColWidth(table.Name, "F", "F", float64(colWidthMap[5]))
		xlsxFile.SetColWidth(table.Name, "G", "G", float64(colWidthMap[6]))
		xlsxFile.SetColWidth(table.Name, "H", "H", float64(colWidthMap[7]))
	}
	xlsxFile.SetActiveSheet(xlsxFile.GetSheetIndex(src.HomeSheetName))
}

/**
 * 绘制表格字段详情头信息
 *
 * @param xlsxFile  表格文件
 * @param sheetName 工作表名称
 * @param table     表信息
 */
func drawTablesSheetHeader(xlsxFile *excelize.File, sheetName string, table src.Table) {
	xlsxFile.SetCellStr(sheetName, "A1", "表名")
	xlsxFile.SetCellStr(sheetName, "B1", table.Name)
	xlsxFile.MergeCell(sheetName, "B1", "H1")

	xlsxFile.SetCellStr(sheetName, "I1", "返回首页")
	xlsxFile.SetCellHyperLink(sheetName, "I1", fmt.Sprintf("%s!A1", src.HomeSheetName), "Location")
	// 为单元格设置字体和下划线样式
	style, _ := xlsxFile.NewStyle(`{"font":{"color":"#0000CC","underline":"single"}}`)
	xlsxFile.SetCellStyle(sheetName, "I1", "I1", style)

	xlsxFile.SetCellStr(sheetName, "A2", "注释")
	xlsxFile.SetCellStr(sheetName, "B2", table.Comment)
	xlsxFile.MergeCell(sheetName, "B2", "H2")

	xlsxFile.SetCellStr(sheetName, "A3", "详细说明")
	xlsxFile.SetCellStr(sheetName, "B3", table.Comment)
	xlsxFile.MergeCell(sheetName, "B3", "H3")
}
