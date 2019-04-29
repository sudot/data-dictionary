package net.sudot.excel.datadictionary;

import net.sudot.excel.datadictionary.dto.InParameter;
import net.sudot.excel.datadictionary.excel.WriteWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 程序入口
 *
 * @author tangjialin on 2019-03-01.
 */
public class Appcation {
    private static final Pattern PATTERN = Pattern.compile("(/|\\d\\:).*");
    private static final String CONFIG_FILE = "in-parameter.txt";

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        File file = new File(System.getProperty("sun.java.command"));
        InputStream stream;
        if (Appcation.class.getName().equals(file.getPath())) {
            stream = ClassLoader.getSystemResourceAsStream(CONFIG_FILE);
            if (stream == null) {
                System.err.printf("\n缺少配置文件【%s】\n", CONFIG_FILE);
                return;
            }
        } else {
            File configFile = new File(file.getParent(), CONFIG_FILE);
            if (!configFile.exists()) {
                System.err.printf("\n缺少配置文件【%s】\n", CONFIG_FILE);
                return;
            }
            stream = new FileInputStream(configFile);
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
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
