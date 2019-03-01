package net.sudot.excel.datadictionary;

import net.sudot.excel.datadictionary.dto.InParameter;
import net.sudot.excel.datadictionary.excel.WriteWorkbook;

/**
 * 程序入口
 *
 * @author tangjialin on 2019-03-01.
 */
public class Appcation {

    public static void main(String[] args) {
        InParameter inParameter = new InParameter();
        inParameter.setUrl("jdbc:mysql://127.0.0.1:3306/test?useUnicode=yes&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai");
        inParameter.setUser("root");
        inParameter.setPassword("root");
        inParameter.setSchema("test");
        inParameter.setFilePath("D:/" + inParameter.getSchema() + ".xlsx");
        inParameter.setExcludeTablesString("table_name_03, table_name_04, table_name_05");
        WriteWorkbook.write(inParameter);
    }

}
