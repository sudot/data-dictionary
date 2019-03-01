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
    private String url;
    private String user;
    private String password;
    private String schema;
    private String filePath;
    private String excludeTablesString;
}
