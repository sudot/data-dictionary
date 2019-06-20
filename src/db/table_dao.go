package db

import "sudot.net/sudot/data-dictionary/src"

type TableDao interface {
	// 获取所有的表信息
	Tables() ([]src.Table, error)

	// 获取所有表的字段信息
	Columns() ([]src.TableColumn, error)
}
