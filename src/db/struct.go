package db

type Table struct {
	Name    string
	Comment string
}

type TableColumn struct {
	TableName     string
	ColumnName    string
	ColumnType    string
	ColumnKey     string
	ColumnUnique  string
	IsNullable    string
	ColumnDefault *string
	ColumnComment string
}
