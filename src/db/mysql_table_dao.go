package db

import (
	"database/sql"
	"fmt"
	_ "github.com/go-sql-driver/mysql"
	"os"
	"sudot.net/sudot/data-dictionary/src"
)

/** 获取指定数据库所有表信息 */
const TablesSql string = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC"

/**
 * TABLE_NAME       COLUMN_NAME     COLUMN_TYPE    COLUMN_KEY    COLUMN_UNIQUE    IS_NULLABLE    COLUMN_DEFAULT    COLUMN_COMMENT
 * test_table_01    id              varchar(32)    PRI           Y                NO             ""                主键id
 * test_table_01    updated_date    datetime       ""            N                YES                              更新时间
 * test_table_02    version         int(11)        ""            N                YES                              版本
 */
const TablesColumnSql string = "" +
	"SELECT COL.TABLE_NAME                            AS TABLE_NAME,\n" +
	"       COL.COLUMN_NAME                           AS COLUMN_NAME,\n" +
	"       COL.COLUMN_TYPE                           AS COLUMN_TYPE,\n" +
	"       IF(COL.COLUMN_KEY = 'PRI', 'Y', 'N')      AS COLUMN_KEY,\n" +
	"       IF(CON.CONSTRAINT_NAME IS NULL, 'N', 'Y') AS COLUMN_UNIQUE,\n" +
	"       IF(COL.IS_NULLABLE = 'YES', 'Y', 'N')     AS IS_NULLABLE,\n" +
	"       COL.COLUMN_DEFAULT                        AS COLUMN_DEFAULT,\n" +
	"       COL.COLUMN_COMMENT                        AS COLUMN_COMMENT\n" +
	"FROM information_schema.COLUMNS COL\n" +
	"LEFT JOIN INFORMATION_SCHEMA.STATISTICS STA ON STA.TABLE_SCHEMA = COL.TABLE_SCHEMA AND STA.TABLE_NAME = COL.TABLE_NAME AND STA.COLUMN_NAME = COL.COLUMN_NAME\n" +
	"LEFT JOIN information_schema.TABLE_CONSTRAINTS CON ON CON.CONSTRAINT_SCHEMA = STA.TABLE_SCHEMA AND CON.TABLE_NAME = STA.TABLE_NAME AND CON.CONSTRAINT_NAME = STA.INDEX_NAME AND CON.CONSTRAINT_TYPE = 'UNIQUE'\n" +
	"WHERE COL.TABLE_SCHEMA = ?\n" +
	"ORDER BY COL.TABLE_NAME ASC,\n" +
	"         CASE COL.COLUMN_KEY WHEN 'PRI' THEN 0 ELSE 1 END ASC,\n" +
	"         COL.COLUMN_NAME ASC"

func Connection(config src.Config) map[int]map[string]string {
	db, err := sql.Open("mysql", fmt.Sprintf("%s:%s@tcp(%s)/%s?charset=utf8", config.User, config.Password, config.Host, config.Schema))
	if err != nil {
		fmt.Println("数据库连接失败", err)
		os.Exit(-1)
	}
	query, err := db.Query(TablesSql, "caes")
	if err != nil {
		fmt.Println("数据库信息查询失败", err)
		return nil
	}
	column, _ := query.Columns()
	fmt.Println("数据库信息查询成功", column)

	values := make([][]byte, len(column))     //values是每个列的值，这里获取到byte里
	scans := make([]interface{}, len(column)) //因为每次查询出来的列是不定长的，用len(column)定住当次查询的长度
	for i := range values {                   //让每一行数据都填充到[][]byte里面
		scans[i] = &values[i]
	}
	results := make(map[int]map[string]string) //最后得到的map
	i := 0
	for query.Next() { //循环，让游标往下移动
		if err := query.Scan(scans...); err != nil { //query.Scan查询出来的不定长值放到scans[i] = &values[i],也就是每行都放在values里
			fmt.Println(err)
			return results
		}
		row := make(map[string]string) //每行数据
		for k, v := range values {     //每行数据是放在values里面，现在把它挪到row里
			key := column[k]
			row[key] = string(v)
		}
		results[i] = row //装入结果集中
		i++
	}
	for k, v := range results { //查询出来的数组
		fmt.Println(k, v)
	}
	return results
}
