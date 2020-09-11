package net.sudot.excel.datadictionary.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * 单元格样式处理工具
 *
 * @author tangjialin on 2019-03-01.
 */
public abstract class CellUtils {
    private static final Charset CHARSET = Charset.forName("GBK");
    private static final Pattern PATTERN = Pattern.compile("[A-Z]+");
    private static final int LENGTH = 5;

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
        if (text == null) { return maxLength; }
        int length = text.getBytes(CHARSET).length;
        // 优化大写字母导致宽度不够
        length += PATTERN.matcher(text).find() ? length / LENGTH : 0;
        return Math.max(length, maxLength);
    }

    public static int drawColumnText(Row row, int cellIndex, String text, int maxLength) {
        addCellStyle(row.createCell(cellIndex)).setCellValue(text);
        return calcColumnTextLength(text, maxLength);
    }
}
