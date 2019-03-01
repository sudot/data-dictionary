package net.sudot.excel.datadictionary.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.nio.charset.StandardCharsets;

/**
 * 单元格样式处理工具
 *
 * @author tangjialin on 2019-03-01.
 */
public abstract class CellUtils {

    public static Cell addCellStyle(Cell cell) {
        cell.setCellType(CellType.STRING);
        return cell;
    }

    public static Cell addCellStyleAtHeader(Workbook workbook, Cell cell) {
        addCellStyle(cell);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWrapText(true);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    public static int calcColumnTextLength(String text, int maxLength) {
        int length = text == null ? maxLength : text.getBytes(StandardCharsets.UTF_8).length;
        return length > maxLength ? length : maxLength;
    }

    public static int drawColumnText(Row row, int cellIndex, String text, int maxLength) {
        addCellStyle(row.createCell(cellIndex)).setCellValue(text);
        return calcColumnTextLength(text, maxLength);
    }
}
