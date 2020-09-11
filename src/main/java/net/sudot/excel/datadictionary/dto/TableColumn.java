package net.sudot.excel.datadictionary.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 表格字段信息
 *
 * @author tangjialin on 2019-03-01.
 */
@Getter
@Setter
@Accessors(chain = true)
public class TableColumn {
    /** 序号 */
    private String tableName;
    /** 字段名 */
    private String columnName;
    /** 字段类型 */
    private String columnType;
    /** 主键 */
    private String columnKey;
    /** 唯一 */
    private String columnUnique;
    /** 空值 */
    private String isNullable;
    /** 缺省 */
    private String columnDefault;
    /** 注释 */
    private String columnComment;
}
