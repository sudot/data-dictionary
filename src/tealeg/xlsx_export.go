package tealeg

import (
	"fmt"
	"github.com/tealeg/xlsx"
	"strconv"
	"sudot.net/sudot/data-dictionary/src"
)

func init() {
	xlsx.SetDefaultFont(11, "宋体")
}

/**
 * 绘制表目录
 *
 * @param xlsxFile 表格文件
 * @param tables   数据表信息
 */
func DrawCatalogue(xlsxFile *xlsx.File, tables []src.Table) {
	sheet, err := xlsxFile.AddSheet(src.HomeSheetName)
	if err != nil {
		return
	}
	row := sheet.AddRow()
	row.AddCell().Value = "表名 Table Name"
	row.AddCell().Value = "描述 Comments"

	colWidthMap := make(map[int]int, len(row.Cells))
	for index, col := range row.Cells {
		colWidthMap[index] = len(col.Value)
	}
	for _, table := range tables {
		row := sheet.AddRow()
		cell := row.AddCell()
		cell.SetFormula(fmt.Sprintf("=HYPERLINK(\"#%s!A1\",\"%s\")", table.Name, table.Name))
		style := cell.GetStyle()
		style.Font.Underline = true // 加下划线
		style.Font.Color = "0000CC" // 设置字体颜色为蓝色
		row.AddCell().Value = table.Comment

		for index, col := range row.Cells {
			width := len(col.Value)
			if colWidthMap[index] < width {
				colWidthMap[index] = width
			}
		}
	}
	// 设置单元格宽度
	for key, width := range colWidthMap {
		sheet.SetColWidth(key, key, float64(width))
	}

}

/**
 * 绘制每一个数据表的字段信息
 *
 * @param xlsxFile     表格文件
 * @param tables       数据表信息
 * @param tableColumns 数据表字段信息
 */
func DrawTableColumns(xlsxFile *xlsx.File, tables []src.Table, tableColumns map[string][]src.TableColumn) {
	for _, table := range tables {
		sheet, _ := xlsxFile.AddSheet(table.Name)
		// 绘制表格头
		drawTablesSheetHeader(sheet, table)
		row := sheet.AddRow()
		columns := tableColumns[table.Name]
		row.AddCell().Value = "序号 Seq"
		row.AddCell().Value = "字段名 Name"
		row.AddCell().Value = "字段类型 Type"
		row.AddCell().Value = "主键 Primary"
		row.AddCell().Value = "唯一 Unique"
		row.AddCell().Value = "空值 Nullable"
		row.AddCell().Value = "缺省 Default"
		row.AddCell().Value = "注释 Comments"

		colWidthMap := make(map[int]int, len(row.Cells))
		for index, col := range row.Cells {
			colWidthMap[index] = len(col.Value)
		}
		for index, column := range columns {
			row := sheet.AddRow()
			row.AddCell().Value = strconv.Itoa(index + 1)
			row.AddCell().Value = column.ColumnName
			row.AddCell().Value = column.ColumnType
			row.AddCell().Value = column.ColumnKey
			row.AddCell().Value = column.ColumnUnique
			row.AddCell().Value = column.IsNullable
			row.AddCell().Value = column.ColumnDefault
			row.AddCell().Value = column.ColumnComment

			for index, col := range row.Cells {
				width := len(col.Value)
				if colWidthMap[index] < width {
					colWidthMap[index] = width
				}
			}
		}
		// 设置单元格宽度
		for key, width := range colWidthMap {
			sheet.SetColWidth(key, key, float64(width))
		}

	}
}

/**
 * 绘制表格字段详情头信息
 *
 * @param sheet    工作表
 * @param table    表信息
 * @return 返回工作薄实例
 */
func drawTablesSheetHeader(sheet *xlsx.Sheet, table src.Table) {
	row := sheet.AddRow()
	row.AddCell().Value = "表名"
	cell := row.AddCell()
	cell.Merge(6, 0)
	cell.Value = table.Name
	for index := 0; index < 6; index++ {
		row.AddCell()
	}
	cell = row.AddCell()
	cell.SetFormula(fmt.Sprintf("=HYPERLINK(\"#%s!A1\",\"返回首页\")", src.HomeSheetName))
	style := cell.GetStyle()
	style.Font.Underline = true // 加下划线
	style.Font.Color = "0000CC" // 设置字体颜色为蓝色

	row = sheet.AddRow()
	row.AddCell().Value = "注释"
	cell = row.AddCell()
	cell.Merge(6, 0)
	cell.Value = table.Comment

	row = sheet.AddRow()
	row.AddCell().Value = "详细说明"
	cell = row.AddCell()
	cell.Merge(6, 0)
	cell.Value = table.Comment
}
