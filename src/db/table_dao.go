package db

type TableDao interface {
	// 获取所有的表信息
	listTables(schema string) []Table

	// 获取所有表的字段信息
	listTableColumns(schema string, tableNames []string) map[string][]TableColumn
}
