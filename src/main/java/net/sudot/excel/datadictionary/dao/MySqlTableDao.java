package net.sudot.excel.datadictionary.dao;

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
public class MySqlTableDao implements TableDao {
    /** 获取指定数据库所有表信息 */
    private static final String TABLES_SQL = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC";
    /**
     * TABLE_NAME           COLUMN_NAME             COLUMN_TYPE     COLUMN_KEY    IS_NULLABLE     COLUMN_DEFAULT    COLUMN_COMMENT
     * test_table_01        id                      varchar(32)     PRI           NO              ""	            主键id
     * test_table_01        updated_date            datetime        ""            YES                               更新时间
     * test_table_02        version                 int(11)         ""            YES                               版本
     */
    private static final String TABLES_COLUMN_SQL = "" +
            "SELECT TABLE_NAME,\n" +
            "       COLUMN_NAME,\n" +
            "       COLUMN_TYPE,\n" +
            "       IF(COLUMN_KEY = 'PRI', 'Y', 'N') AS COLUMN_KEY,\n" +
            "       IF(IS_NULLABLE = 'YES', 'Y', 'N') AS IS_NULLABLE,\n" +
            "       COLUMN_DEFAULT,\n" +
            "       COLUMN_COMMENT\n" +
            "FROM information_schema.COLUMNS\n" +
            "WHERE TABLE_SCHEMA = ?\n" +
            "ORDER BY TABLE_NAME ASC,\n" +
            "         CASE COLUMN_KEY WHEN 'PRI' THEN 0 ELSE 1 END ASC,\n" +
            "         COLUMN_NAME ASC";

    /** 数据库连接对象 */
    private Connection connection;
    /** 需要排除的表名 */
    private Set<String> excludeTables;

    /**
     * @param connection    数据库连接对象
     * @param excludeTables 需要排除的表名
     */
    public MySqlTableDao(Connection connection, Set<String> excludeTables) {
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
