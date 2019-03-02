package net.sudot.excel.datadictionary.excel;

import net.sudot.excel.datadictionary.Constant;
import net.sudot.excel.datadictionary.dao.MySqlTableDao;
import net.sudot.excel.datadictionary.dao.OracleTableDao;
import net.sudot.excel.datadictionary.dao.TableDao;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表格写出
 *
 * @author tangjialin on 2019-03-01.
 */
public abstract class WriteWorkbook {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件操作
     *
     * @param inParameter 输入参数
     */
    public static void write(InParameter inParameter) {
        Set<String> excludeTables = new HashSet<>();
        String[] split = Constant.SPLIT_PATTERN.split(inParameter.getExcludeTablesString());
        for (String tableName : split) {
            excludeTables.add(tableName.trim());
        }

        try (Connection connection = DriverManager.getConnection(inParameter.getUrl(), inParameter.getUser(), inParameter.getPassword())) {
            connection.setReadOnly(true);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            TableDao tableDao = new MySqlTableDao(connection, excludeTables);
            if ("Oracle".equals(databaseProductName)) {
                tableDao = new OracleTableDao(connection, excludeTables);
            }
            List<Table> tables = tableDao.listTables(inParameter.getSchema());
            List<String> tableNames = tables.stream().map(Table::getName).collect(Collectors.toList());
            Map<String, List<TableColumn>> tableColumns = tableDao.listTableColumns(inParameter.getSchema(), tableNames);

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
            List<TableColumn> columnList = tableColumns.get(table.getName());
            if (columnList == null) { return; }
            Map<Integer, Integer> columnTextLengthMap = new HashMap<>();
            Sheet sheet = drawTablesSheetHeader(workbook, table);
            int dataRowIndex = 3;
            Row headerRow = sheet.createRow(dataRowIndex++);
            int firstCellIndex = -1;
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("序号\r\nSeq.");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("字段名\r\nName");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("字段类型\r\nType");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("主键\r\nPrimary");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("唯一\r\nUnique");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("空值\r\nNullable");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("缺省\r\nDefault");
            CellUtils.addCellStyleAtHeader(workbook, headerRow.createCell(++firstCellIndex)).setCellValue("注释\r\nComments");
            Iterator<TableColumn> iterator = columnList.iterator();
            for (int rowIndex = dataRowIndex; iterator.hasNext(); rowIndex++) {
                firstCellIndex = -1;
                TableColumn column = iterator.next();
                Row row = sheet.createRow(rowIndex);
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, String.valueOf(rowIndex - dataRowIndex + 1), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnName(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnType(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnKey(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
                columnTextLengthMap.put(++firstCellIndex, CellUtils.drawColumnText(row, firstCellIndex, column.getColumnUnique(), columnTextLengthMap.getOrDefault(firstCellIndex, Constant.DEFAULT_COLUMN_TEXT_LENGTH)));
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
        int mergedLastCellIndex = 7;
        {
            Row row = sheet.createRow(++rowIndex);
            row.createCell(0).setCellValue("表名");
            row.createCell(1).setCellValue(table.getName());
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, mergedLastCellIndex));

            CreationHelper createHelper = workbook.getCreationHelper();
            Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            hyperlink.setAddress(String.format("%s!A1", Constant.HOME_SHEET_NAME));
            Cell cell = row.createCell(mergedLastCellIndex + 1);
            cell.setCellValue("返回首页");
            cell.setHyperlink(hyperlink);
        }

        {
            Row row = sheet.createRow(++rowIndex);
            row.createCell(0).setCellValue("注释");
            row.createCell(1).setCellValue(table.getComment());
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, mergedLastCellIndex));
        }

        {
            Row row = sheet.createRow(++rowIndex);
            row.createCell(0).setCellValue("详细说明");
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, mergedLastCellIndex));
        }
        return sheet;
    }

}
