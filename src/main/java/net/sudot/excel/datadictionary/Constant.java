package net.sudot.excel.datadictionary;

import net.sudot.excel.datadictionary.excel.CellUtils;

import java.util.regex.Pattern;

/**
 * 常量
 *
 * @author tangjialin on 2019-03-01.
 */
public interface Constant {
    /** 字符串切分符(所有空白(包括空格,换行.tab缩进等所有的空白)、逗号、分号 */
    Pattern SPLIT_PATTERN = Pattern.compile("[\\s,;]");
    String HOME_SHEET_NAME = "总目录";
    int DEFAULT_COLUMN_TEXT_LENGTH = CellUtils.calcColumnTextLength("哈哈哈哈", 0);
    /** 获取指定数据库所有表信息 */
    String TABLES_SQL = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC";
    /**
     * TABLE_NAME           COLUMN_NAME             COLUMN_TYPE     COLUMN_KEY    IS_NULLABLE     COLUMN_DEFAULT    COLUMN_COMMENT
     * test_table_01        id                      varchar(32)     PRI           NO              ""	            主键id
     * test_table_01        updated_date            datetime        ""            YES                               更新时间
     * test_table_02        version                 int(11)         ""            YES                               版本
     */
    String TABLES_COLUMN_SQL = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC, CASE COLUMN_KEY WHEN 'PRI' THEN 0 ELSE 1 END ASC, COLUMN_NAME ASC";

}
