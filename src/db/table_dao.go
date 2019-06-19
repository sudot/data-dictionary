package db

type TableDao interface {
	// 获取所有的表信息
	Tables() ([]Table, error)

	// 获取所有表的字段信息
	Columns() ([]TableColumn, error)
}
