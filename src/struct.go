package src

type Table struct {
	Name    string // 表名
	Comment string // 表注释
}

type TableColumn struct {
	TableName     string // 序号
	ColumnName    string // 字段名
	ColumnType    string // 字段类型
	ColumnKey     string // 主键
	ColumnUnique  string // 唯一
	IsNullable    string // 空值
	ColumnDefault string // 缺省
	ColumnComment string // 注释
}
