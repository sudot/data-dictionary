package net.sudot.excel.datadictionary;

import net.sudot.excel.datadictionary.dto.InParameter;
import net.sudot.excel.datadictionary.excel.WriteWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 程序入口
 *
 * @author tangjialin on 2019-03-01.
 */
public class Appcation {
    private static final Pattern PATTERN = Pattern.compile("(/|\\d\\:).*");

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        File file = new File(System.getProperty("sun.java.command"));
        properties.load(new FileInputStream(new File(file.getParent(), "conf/in-parameter.txt")));
        InParameter inParameter = new InParameter();
        inParameter.setUrl(properties.getProperty("url"));
        inParameter.setUser(properties.getProperty("user"));
        inParameter.setPassword(properties.getProperty("password"));
        inParameter.setSchema(properties.getProperty("schema"));
        inParameter.setExcludeTablesString(properties.getProperty("exclude-tables-string"));
        String filePath = properties.getProperty("file-path");
        if (!PATTERN.matcher(filePath).matches()) {
            filePath = new File(file.getParent(), filePath).getCanonicalPath();
        }
        inParameter.setFilePath(filePath);
        WriteWorkbook.write(inParameter);
    }

}
