package net.sudot.excel.datadictionary.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 入参
 *
 * @author tangjialin on 2019-03-01.
 */
@Getter
@Setter
@Accessors(chain = true)
public class InParameter implements Serializable {
    /** 数据库连接 */
    private String url;
    /** 数据库登录用户 */
    private String user;
    /** 数据库登录密码 */
    private String password;
    /** 需要生成数据字典的数据库名称(MySql有效) */
    private String schema;
    /** 不生成数据字典的数据库表名 */
    private String excludeTablesString;
    /** 数据字典存储路径,支持相对路径和绝对路径,相对路径相对于data-dictionary.jar所在目录 */
    private String filePath;
}
