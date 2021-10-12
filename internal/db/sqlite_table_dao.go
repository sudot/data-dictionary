package db

import (
	"database/sql"
	"fmt"
	_ "github.com/go-sql-driver/mysql"
	"github.com/sudot/data-dictionary/internal"
	"strings"
)

// sqliteTablesSql 获取指定数据库所有表信息
const sqliteTablesSql string = "SELECT name FROM sqlite_master WHERE TYPE = 'table' ORDER BY NAME ASC"

// sqliteTablesColumnSql
//  cid  name          type     notnull  dflt_value  pk
//  0    id            INTEGER  1                    1
//  1    GID           VARCHAR  0                    0
//  2    dateGID       INTEGER  0                    0
//  3    balance       DOUBLE   0        0           0
//  4    currencyName  VARCHAR  0                    0
const sqliteTablesColumnSql string = "PRAGMA table_info(%s)"

type SqliteTableDao struct {
	TableDao
	config            src.Config
	db                *sql.DB
	excludeTableNames *src.Set
}

func NewSqliteTableDao(config src.Config) *SqliteTableDao {
	excludeTableNames := src.New()
	for _, v := range strings.Split(config.ExcludeTables, ",") {
		excludeTableNames.Add(v)
	}

	db, err := sql.Open("sqlite3", config.Host)
	// 运行后程序就退出了,所以无需关闭db
	if err != nil {
		fmt.Println("数据库连接失败", err)
		return nil
	}
	return &SqliteTableDao{config: config, db: db, excludeTableNames: excludeTableNames}
}
func (tableDao *SqliteTableDao) Tables() ([]src.Table, error) {
	rows, err := tableDao.db.Query(sqliteTablesSql)
	defer rows.Close()
	if err != nil {
		fmt.Println("数据库信息查询失败", err)
		return nil, err
	}
	tables := make([]src.Table, 0)
	for rows.Next() {
		table := src.Table{}
		if err := rows.Scan(&table.Name); err != nil {
			fmt.Println("查询失败", sqliteTablesSql, err)
			return nil, err
		}
		if tableDao.excludeTableNames.Contains(table.Name) {
			continue
		}
		tables = append(tables, table)
	}
	return tables, nil
}

func (tableDao *SqliteTableDao) Columns(tables []src.Table) (map[string][]src.TableColumn, error) {
	m := make(map[string][]src.TableColumn)
	for _, table := range tables {
		if tableDao.excludeTableNames.Contains(table.Name) {
			continue
		}
		rows, err := Query2Map(tableDao.db, fmt.Sprintf(sqliteTablesColumnSql, table.Name))
		if err != nil {
			fmt.Println("数据库信息查询失败", err)
			return nil, err
		}
		tableColumns := make([]src.TableColumn, len(rows))
		m[table.Name] = tableColumns

		for i, row := range rows {
			tableColumn := src.TableColumn{
				TableName:     table.Name,
				ColumnName:    StrVal(row["name"]),
				ColumnType:    StrVal(row["type"]),
				ColumnKey:     StrVal(row["pk"]),
				IsNullable:    StrVal(row["notnull"]),
				ColumnDefault: StrVal(row["dflt_value"]),
			}
			tableColumns[i] = tableColumn
		}
	}
	return m, nil
}
