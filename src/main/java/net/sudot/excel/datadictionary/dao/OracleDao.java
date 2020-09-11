package net.sudot.excel.datadictionary.dao;

import net.sudot.excel.datadictionary.Constant;
import net.sudot.excel.datadictionary.annotation.MeteData;
import net.sudot.excel.datadictionary.dto.Table;
import net.sudot.excel.datadictionary.dto.TableColumn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库表操作
 *
 * @author tangjialin on 2019-03-02.
 */
@MeteData(databaseProductName = "Oracle", driveClassName = "oracle.jdbc.OracleDriver")
public class OracleDao implements IDao {
    /** 获取指定数据库所有表信息 */
    private static final String TABLES_SQL = "SELECT TABLE_NAME,COMMENTS FROM USER_TAB_COMMENTS ORDER BY TABLE_NAME ASC";
    /**
     * TABLE_NAME       COLUMN_NAME     DATA_TYPE    DATA_LENGTH    COLUMN_KEY    COLUMN_UNIQUE    NULLABLE    DATA_DEFAULT    COMMENTS
     * test_table_01    id              varchar      32             Y             Y                N           ""              主键id
     * test_table_01    updated_date    date         0              N             N                Y                           更新时间
     * test_table_02    version         NUMBER       0              N             N                Y                           版本
     */
    private static final String TABLES_COLUMN_SQL = "" +
            "SELECT T.TABLE_NAME,\n" +
            "        T.COLUMN_NAME,\n" +
            "        T.DATA_TYPE,\n" +
            "        T.DATA_LENGTH,\n" +
            "        DECODE(UC.COLUMN_KEY, 1, 'Y', 'N') AS COLUMN_KEY,\n" +
            "        DECODE(UC.COLUMN_UNIQUE, 0, 'Y', 'N') AS COLUMN_UNIQUE,\n" +
            "        T.NULLABLE,\n" +
            "        T.DATA_DEFAULT,\n" +
            "        C.COMMENTS\n" +
            "FROM USER_TAB_COLUMNS T\n" +
            "INNER JOIN USER_COL_COMMENTS C ON C.TABLE_NAME = T.TABLE_NAME AND C.COLUMN_NAME = T.COLUMN_NAME\n" +
            "LEFT JOIN (\n" +
            "  SELECT CU.TABLE_NAME, CU.COLUMN_NAME,\n" +
            "         MAX(CASE AU.CONSTRAINT_TYPE WHEN 'P' THEN 1 ELSE 0 END) AS \"COLUMN_KEY\",\n" +
            "         MAX(CASE AU.CONSTRAINT_TYPE WHEN 'U' THEN 1 ELSE 0 END) AS \"COLUMN_UNIQUE\"\n" +
            "  FROM USER_CONS_COLUMNS CU\n" +
            "  INNER JOIN USER_CONSTRAINTS AU ON AU.CONSTRAINT_NAME = CU.CONSTRAINT_NAME\n" +
            "  WHERE AU.CONSTRAINT_TYPE IN ('P', 'U')\n" +
            "  GROUP BY CU.TABLE_NAME, CU.COLUMN_NAME\n" +
            ") UC ON UC.TABLE_NAME = T.TABLE_NAME AND UC.COLUMN_NAME = T.COLUMN_NAME\n" +
            "WHERE T.TABLE_NAME IN ($INCLUDE_TABLE_NAME$)\n" +
            "ORDER BY T.TABLE_NAME ASC,\n" +
            "         UC.COLUMN_KEY ASC,\n" +
            "         T.COLUMN_NAME ASC";

    /** 数据库连接对象 */
    private Connection connection;
    /** 需要排除的表名 */
    private Set<String> excludeTables;

    /**
     * @param connection    数据库连接对象
     * @param excludeTables 需要排除的表名
     */
    public OracleDao(Connection connection, Set<String> excludeTables) {
        this.connection = connection;
        this.excludeTables = excludeTables;
    }

    @Override
    public List<Table> listTables(String schema) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(TABLES_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            List<Table> tables = new ArrayList<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (excludeTables.contains(tableName)) { continue;}
                tables.add(new Table()
                        .setName(tableName)
                        .setComment(resultSet.getString("COMMENTS")));
            }
            return tables;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<TableColumn>> listTableColumns(String schema, List<String> tableNames) {
        StringBuilder replaceSql = new StringBuilder(tableNames.size() * 2);
        for (int index = tableNames.size(); index > 0; index--) {
            replaceSql.append("?,");
        }
        String sql = Constant.INCLUDE_TABLE_NAME_PATTERN.matcher(TABLES_COLUMN_SQL)
                .replaceAll(replaceSql.deleteCharAt(replaceSql.length() - 1).toString());
        Map<String, List<TableColumn>> tableColumns = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Iterator<String> iterator = tableNames.iterator();
            for (int index = 1; iterator.hasNext(); index++) {
                preparedStatement.setString(index, iterator.next());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int dataLength = resultSet.getInt("DATA_LENGTH");
                    String dataType = resultSet.getString("DATA_TYPE");
                    if (dataLength > 0) {
                        dataType += "(" + dataLength + ")";
                    }
                    String tableName = resultSet.getString("TABLE_NAME");
                    tableColumns.computeIfAbsent(tableName, k -> new ArrayList<>()).add(new TableColumn()
                            .setTableName(tableName)
                            .setColumnName(resultSet.getString("COLUMN_NAME"))
                            .setColumnType(dataType)
                            .setColumnKey(resultSet.getString("COLUMN_KEY"))
                            .setColumnUnique(resultSet.getString("COLUMN_UNIQUE"))
                            .setIsNullable(resultSet.getString("NULLABLE"))
                            .setColumnDefault(resultSet.getString("DATA_DEFAULT"))
                            .setColumnComment(resultSet.getString("COMMENTS")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableColumns;
    }
}
