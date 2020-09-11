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
@MeteData(databaseProductName = "SQLite", driveClassName = "org.sqlite.JDBC")
public class SQLiteDao implements IDao {
    /** 获取指定数据库所有表信息 */
    private static final String TABLES_SQL = "SELECT name FROM sqlite_master WHERE TYPE = 'table' ORDER BY NAME";
    /**
     * cid  name          type     notnull  dflt_value  pk
     * 0    id            INTEGER  1                    1
     * 1    GID           VARCHAR  0                    0
     * 2    dateGID       INTEGER  0                    0
     * 3    balance       DOUBLE   0        0           0
     * 4    currencyName  VARCHAR  0                    0
     */
    private static final String TABLES_COLUMN_SQL = "PRAGMA table_info(%s)";

    /** 数据库连接对象 */
    private Connection connection;
    /** 需要排除的表名 */
    private Set<String> excludeTables;

    /**
     * @param connection    数据库连接对象
     * @param excludeTables 需要排除的表名
     */
    public SQLiteDao(Connection connection, Set<String> excludeTables) {
        this.connection = connection;
        this.excludeTables = excludeTables;
    }

    @Override
    public List<Table> listTables(String schema) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(TABLES_SQL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Table> tables = new ArrayList<>();
                while (resultSet.next()) {
                    String tableName = resultSet.getString("name");
                    if (excludeTables.contains(tableName)) { continue;}
                    tables.add(new Table()
                            .setName(tableName)
                            .setComment(null));
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
        for (String tableName : tableNames) {
            if (excludeTables.contains(tableName)) { continue; }
            List<TableColumn> columnList = tableColumns.computeIfAbsent(tableName, k -> new ArrayList<>());
            String sql = String.format(TABLES_COLUMN_SQL, tableName);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        columnList.add(new TableColumn()
                                .setTableName(tableName)
                                .setColumnName(resultSet.getString("name"))
                                .setColumnType(resultSet.getString("type"))
                                .setColumnKey(resultSet.getString("pk"))
                                .setColumnUnique("N")
                                .setIsNullable(resultSet.getString("notnull"))
                                .setColumnDefault(resultSet.getString("dflt_value"))
                                .setColumnComment(""));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return tableColumns;
    }
}
