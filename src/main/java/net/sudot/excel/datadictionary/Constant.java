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
    /** 包含的表 */
    Pattern INCLUDE_TABLE_NAME_PATTERN = Pattern.compile("\\$INCLUDE_TABLE_NAME\\$");
}
