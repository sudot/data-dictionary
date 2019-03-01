package net.sudot.excel.datadictionary.excel;

import net.sudot.excel.datadictionary.Constant;
import net.sudot.excel.datadictionary.dto.InParameter;
import net.sudot.excel.datadictionary.dto.Table;
import net.sudot.excel.datadictionary.dto.TableColumn;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 表格写出
 *
 * @author tangjialin on 2019-03-01.
 */
public abstract class WriteWorkbook {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件操作
     *
     * @param inParameter
     * @throws Exception
     */
    public static void write(InParameter inParameter) {
        Set<String> excludeTables = new HashSet<>();
        String[] split = Constant.SPLIT_PATTERN.split(inParameter.getExcludeTablesString());
        for (String tableName : split) {
            excludeTables.add(tableName.trim());
        }

        try (Connection connection = DriverManager.getConnection(inParameter.getUrl(), inParameter.getUser(), inParameter.getPassword())) {
            connection.setReadOnly(true);
            List<Table> tables = loadTable(connection, inParameter.getSchema(), excludeTables);
            Map<String, List<TableColumn>> tableColumns = loadTableColumns(connection, inParameter.getSchema(), excludeTables);

            Workbook workbook = drawCatalogue(WorkbookFactory.create(true), tables);
            drawTableColumns(workbook, tables, tableColumns);

            try (FileOutputStream fileOutputStream = new FileOutputStream(inParameter.getFilePath())) {
                workbook.write(fileOutputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有的数据表信息
     *
     * @param connection    数据库连接对象
     * @param schema        数据库名称
     * @param excludeTables 需要排除的表名
     * @return 返回数据表信息
     */
    public static List<Table> loadTable(Connection connection, String schema, Set<String> excludeTables) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(Constant.TABLES_SQL)) {
            preparedStatement.setString(1, schema);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Table> tables = new ArrayList<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (excludeTables.contains(tableName)) { continue;}
                tables.add(new Table()
                        .setName(tableName)
                        .setComment(resultSet.getString("TABLE_COMMENT")));
            }
            return tables;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有的数据表信息
     *
     * @param connection    数据库连接对象
     * @param schema        数据库名称
     * @param excludeTables 需要排除的表名
     * @return 返回数据表信息
     */
    public static Map<String, List<TableColumn>> loadTableColumns(Connection connection, String schema, Set<String> excludeTables) {
        Map<String, List<TableColumn>> tableColumns = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(Constant.TABLES_COLUMN_SQL)) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (excludeTables.contains(tableName)) { continue;}
                    List<TableColumn> columnList = Optional.ofNullable(tableColumns.get(tableName)).orElseGet(ArrayList::new);
                    tableColumns.put(tableName, columnList);
                    columnList.add(new TableColumn()
                            .setTableName(tableName)
                            .setColumnName(resultSet.getString("COLUMN_NAME"))
                            .setColumnType(resultSet.getString("COLUMN_TYPE"))
                            .setColumnKey(resultSet.getString("COLUMN_KEY"))
                            .setIsNullable(resultSet.getString("IS_NULLABLE"))
                            .setColumnDefault(resultSet.getString("COLUMN_DEFAULT"))
                            .setColumnComment(resultSet.getString("COLUMN_COMMENT")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableColumns;
    }

    /**
     * 绘制表目录
     *
     * @param workbook 工作表实例
     * @param tables   数据表信息
     * @return 工作表实例
     */
    public static Workbook drawCatalogue(Workbook workbook, List<Table> tables) {
        Map<Integer, Integer> columnTextLengthMap = new HashMap<>();
        Sheet sheet = workbook.createSheet(Constant.HOME_SHEET_NAME);
        Row sheetRow = sheet.createRow(0);
        CellUtils.addCellStyleAtHeader(workbook, sheetRow.createCell(0)).setCellValue("表名\r\nTable Name");
        CellUtils.addCellStyleAtHeader(workbook, sheetRow.createCell(1)).setCellValue("描述\r\nComments");

        CreationHelper createHelper = workbook.getCreationHelper();
        Iterator<Table> iterator = tables.iterator();
        for (int index = 0; iterator.hasNext(); index++) {
            Table table = iterator.next();
            Row row = sheet.createRow(index + 1);
            Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            hyperlink.setAddress(String.format("%s!A1", table.getName()));
            Cell cell = row.createCell(0);
            cell.setHyperlink(hyperlink);
            cell.setCellValue(table.getName());
            row.createCell(1).setCellValue(table.getComment());

            columnTextLengthMap.put(0, CellUtils.calcColumnTextLength(table.getName(), columnTextLengthMap.getOrDefault(0, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
            columnTextLengthMap.put(1, CellUtils.calcColumnTextLength(table.getComment(), columnTextLengthMap.getOrDefault(0, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
        }
        for (Map.Entry<Integer, Integer> entry : columnTextLengthMap.entrySet()) {
            sheet.setColumnWidth(entry.getKey(), 255 * entry.getValue());
        }
        return workbook;
    }

    /**
     * 绘制每一个数据表的字段信息
     *
     * @param workbook     工作表实例
     * @param tables       数据表信息
     * @param tableColumns 数据表字段信息
     * @return 工作表实例
     */
    public static Workbook drawTableColumns(Workbook workbook, List<Table> tables, Map<String, List<TableColumn>> tableColumns) {
        tables.forEach(table -> {
            Map<Integer, Integer> columnTextLengthMap = new HashMap<>();
            Sheet sheet = drawTablesSheetHeader(workbook, table);
            int dataRowIndex = 3;
            Row headerRow = sheet.createRow(dataRowIndex++);
            int firstCellIndex = -1;
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("序号\r\nSeq.");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("字段名\r\nName");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("字段类型\r\nType");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("主键\r\nPrimary");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("空值\r\nNullable");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("缺省\r\nDefault");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("注释\r\nComments");
            Iterator<TableColumn> iterator = tableColumns.get(table.getName()).iterator();
            for (int rowIndex = dataRowIndex; iterator.hasNext(); rowIndex++) {
                firstCellIndex = -1;
                TableColumn column = iterator.next();
                Row row = sheet.createRow(rowIndex);
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, String.valueOf(rowIndex - dataRowIndex + 1), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnName(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnType(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnKey(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getIsNullable(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnDefault(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnComment(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
            }
            for (Map.Entry<Integer, Integer> entry : columnTextLengthMap.entrySet()) {
                sheet.setColumnWidth(entry.getKey(), 255 * entry.getValue());
            }
        });
        return workbook;
    }

    /**
     * 绘制表格字段详情头信息
     *
     * @param workbook 表格
     * @param table    表信息
     * @return 返回工作薄实例
     */
    public static Sheet drawTablesSheetHeader(Workbook workbook, Table table) {
        Sheet sheet = workbook.createSheet(table.getName());
        int rowIndex = -1;
        Row row = sheet.createRow(++rowIndex);
        row.createCell(0).setCellValue("表名");
        row.createCell(1).setCellValue(table.getName());

        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        hyperlink.setAddress(String.format("%s!A1", Constant.HOME_SHEET_NAME));
        Cell cell = row.createCell(7);
        cell.setCellValue("返回首页");
        cell.setHyperlink(hyperlink);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 6));

        row = sheet.createRow(++rowIndex);
        row.createCell(0).setCellValue("注释");
        row.createCell(1).setCellValue(table.getComment());
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 6));

        row = sheet.createRow(++rowIndex);
        row.createCell(0).setCellValue("详细说明");
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 6));
        return sheet;
    }

}
