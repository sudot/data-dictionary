package db

import (
	"database/sql"
	"fmt"
	_ "github.com/mattn/go-sqlite3"
	src "github.com/sudot/data-dictionary/internal"
	"strings"
)

// mysqlTablesSql 获取指定数据库所有表信息
const mysqlTablesSql string = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC"

// mysqlTablesColumnSql
//  TABLE_NAME       COLUMN_NAME     COLUMN_TYPE    COLUMN_KEY    COLUMN_UNIQUE    IS_NULLABLE    COLUMN_DEFAULT    COLUMN_COMMENT
//  test_table_01    id              varchar(32)    PRI           Y                NO             ""                主键id
//  test_table_01    updated_date    datetime       ""            N                YES                              更新时间
//  test_table_02    version         int(11)        ""            N                YES                              版本
const mysqlTablesColumnSql string = `
SELECT COL.TABLE_NAME                            AS TABLE_NAME,
       COL.COLUMN_NAME                           AS COLUMN_NAME,
       COL.COLUMN_TYPE                           AS COLUMN_TYPE,
       IF(COL.COLUMN_KEY = 'PRI', 'Y', 'N')      AS COLUMN_KEY,
       IF(CON.CONSTRAINT_NAME IS NULL, 'N', 'Y') AS COLUMN_UNIQUE,
       IF(COL.IS_NULLABLE = 'YES', 'Y', 'N')     AS IS_NULLABLE,
       COL.COLUMN_DEFAULT                        AS COLUMN_DEFAULT,
       COL.COLUMN_COMMENT                        AS COLUMN_COMMENT
FROM information_schema.COLUMNS COL
LEFT JOIN INFORMATION_SCHEMA.STATISTICS STA ON STA.TABLE_SCHEMA = COL.TABLE_SCHEMA AND STA.TABLE_NAME = COL.TABLE_NAME AND STA.COLUMN_NAME = COL.COLUMN_NAME
LEFT JOIN information_schema.TABLE_CONSTRAINTS CON ON CON.CONSTRAINT_SCHEMA = STA.TABLE_SCHEMA AND CON.TABLE_NAME = STA.TABLE_NAME AND CON.CONSTRAINT_NAME = STA.INDEX_NAME AND CON.CONSTRAINT_TYPE = 'UNIQUE'
WHERE COL.TABLE_SCHEMA = ?
ORDER BY COL.TABLE_NAME ASC,
         CASE COL.COLUMN_KEY WHEN 'PRI' THEN 0 ELSE 1 END ASC,
         COL.COLUMN_NAME ASC`

type MySqlTableDao struct {
	TableDao
	config            src.Config
	db                *sql.DB
	excludeTableNames *src.Set
}

func NewMySqlTableDao(config src.Config) *MySqlTableDao {
	excludeTableNames := src.New()
	for _, v := range strings.Split(config.ExcludeTables, ",") {
		excludeTableNames.Add(v)
	}

	db, err := sql.Open("mysql", fmt.Sprintf("%s:%s@tcp(%s)/%s?charset=utf8", config.User, config.Password, config.Host, config.Schema))
	// 运行后程序就退出了,所以无需关闭db
	if err != nil {
		fmt.Println("数据库连接失败", err)
		return nil
	}
	return &MySqlTableDao{config: config, db: db, excludeTableNames: excludeTableNames}
}
func (tableDao *MySqlTableDao) Tables() ([]src.Table, error) {
	rows, err := tableDao.db.Query(mysqlTablesSql, tableDao.config.Schema)
	defer rows.Close()
	if err != nil {
		fmt.Println("数据库信息查询失败", err)
		return nil, err
	}
	tables := make([]src.Table, 0)
	for rows.Next() {
		table := src.Table{}
		if err := rows.Scan(&table.Name, &table.Comment); err != nil {
			fmt.Println("查询失败", mysqlTablesSql, err)
			return nil, err
		}
		if tableDao.excludeTableNames.Contains(table.Name) {
			continue
		}
		tables = append(tables, table)
	}
	return tables, nil
}

func (tableDao *MySqlTableDao) Columns() ([]src.TableColumn, error) {
	rows, err := tableDao.db.Query(mysqlTablesColumnSql, tableDao.config.Schema)
	defer rows.Close()
	if err != nil {
		fmt.Println("数据库信息查询失败", err)
		return nil, err
	}
	tableColumns := make([]src.TableColumn, 0)
	for rows.Next() {
		tableColumn := src.TableColumn{}
		var columnDefault []byte
		if err := rows.Scan(&tableColumn.TableName, &tableColumn.ColumnName, &tableColumn.ColumnType, &tableColumn.ColumnKey, &tableColumn.ColumnUnique, &tableColumn.IsNullable, &columnDefault, &tableColumn.ColumnComment); err != nil {
			fmt.Println("数据加载失败", tableColumn.TableName, err)
			return nil, err
		}
		if tableDao.excludeTableNames.Contains(tableColumn.TableName) {
			continue
		}
		if columnDefault != nil {
			tableColumn.ColumnDefault = string(columnDefault)
		}
		tableColumns = append(tableColumns, tableColumn)
	}
	return tableColumns, nil
}
