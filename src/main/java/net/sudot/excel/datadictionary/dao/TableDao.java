package net.sudot.excel.datadictionary.dao;

import net.sudot.excel.datadictionary.dto.Table;
import net.sudot.excel.datadictionary.dto.TableColumn;

import java.util.List;
import java.util.Map;

/**
 * 数据表信息操作Dao
 *
 * @author tangjialin on 2019-03-02.
 */
public interface TableDao {

    /**
     * 获取所有的数据表信息
     *
     * @param schema 数据库名称
     * @return 返回数据表信息
     */
    List<Table> listTables(String schema);

    /**
     * 获取所有的数据表信息
     *
     * @param schema     数据库名称
     * @param tableNames 包含的表名称
     * @return 返回数据表信息
     */
    Map<String, List<TableColumn>> listTableColumns(String schema, List<String> tableNames);
}
