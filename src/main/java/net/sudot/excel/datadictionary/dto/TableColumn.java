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
    private String tableName;
    private String columnName;
    private String columnType;
    private String columnKey;
    private String isNullable;
    private String columnDefault;
    private String columnComment;
}
