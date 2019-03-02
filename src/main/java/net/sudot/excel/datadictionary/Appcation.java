package net.sudot.excel.datadictionary;

import net.sudot.excel.datadictionary.dto.InParameter;
import net.sudot.excel.datadictionary.excel.WriteWorkbook;

import java.util.Properties;

/**
 * 程序入口
 *
 * @author tangjialin on 2019-03-01.
 */
public class Appcation {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("in-parameter.txt"));
        InParameter inParameter = new InParameter();
        inParameter.setUrl(properties.getProperty("url"));
        inParameter.setUser(properties.getProperty("user"));
        inParameter.setPassword(properties.getProperty("password"));
        inParameter.setSchema(properties.getProperty("schema"));
        inParameter.setExcludeTablesString(properties.getProperty("exclude-tables-string"));
        inParameter.setFilePath(properties.getProperty("file-path"));
        WriteWorkbook.write(inParameter);
    }

}
