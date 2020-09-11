package net.sudot.excel.datadictionary.dao;

import net.sudot.excel.datadictionary.annotation.MeteData;
import net.sudot.excel.datadictionary.dto.Table;
import net.sudot.excel.datadictionary.dto.TableColumn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库表操作
 *
 * @author tangjialin on 2019-03-02.
 */
@MeteData(databaseProductName = "MySQL", driveClassName = "com.mysql.cj.jdbc.Driver")
public class MySqlDao implements IDao {
    /** 获取指定数据库所有表信息 */
    private static final String TABLES_SQL = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC";
    /**
     * TABLE_NAME       COLUMN_NAME     COLUMN_TYPE    COLUMN_KEY    COLUMN_UNIQUE    IS_NULLABLE    COLUMN_DEFAULT    COLUMN_COMMENT
     * test_table_01    id              varchar(32)    PRI           Y                NO             ""                主键id
     * test_table_01    updated_date    datetime       ""            N                YES                              更新时间
     * test_table_02    version         int(11)        ""            N                YES                              版本
     */
    private static final String TABLES_COLUMN_SQL = "" +
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
            "         COL.COLUMN_NAME ASC";

    /** 数据库连接对象 */
    private Connection connection;
    /** 需要排除的表名 */
    private Set<String> excludeTables;

    /**
     * @param connection    数据库连接对象
     * @param excludeTables 需要排除的表名
     */
    public MySqlDao(Connection connection, Set<String> excludeTables) {
        this.connection = connection;
        this.excludeTables = excludeTables;
    }

    @Override
    public List<Table> listTables(String schema) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(TABLES_SQL)) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Table> tables = new ArrayList<>();
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (excludeTables.contains(tableName)) { continue;}
                    tables.add(new Table()
                            .setName(tableName)
                            .setComment(resultSet.getString("TABLE_COMMENT")));
                }
                return tables;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<TableColumn>> listTableColumns(String schema, List<String> tableNames) {
        Map<String, List<TableColumn>> tableColumns = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(TABLES_COLUMN_SQL)) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (excludeTables.contains(tableName)) { continue;}
                    tableColumns.computeIfAbsent(tableName, k -> new ArrayList<>()).add(new TableColumn()
                            .setTableName(tableName)
                            .setColumnName(resultSet.getString("COLUMN_NAME"))
                            .setColumnType(resultSet.getString("COLUMN_TYPE"))
                            .setColumnKey(resultSet.getString("COLUMN_KEY"))
                            .setColumnUnique("N")
                            .setIsNullable(resultSet.getString("IS_NULLABLE"))
                            .setColumnDefault(resultSet.getString("COLUMN_DEFAULT"))
                            .setColumnComment(resultSet.getString("COLUMN_COMMENT")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableColumns;
    }
}
