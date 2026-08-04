package vn.thanhnd.demo.util.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.annotation.export.CellAlign;
import vn.thanhnd.demo.util.annotation.export.ExportFormat;
import vn.thanhnd.demo.util.annotation.export.ExportStyle;
import vn.thanhnd.demo.util.exception.CoreException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Apache POI-backed XLSX export strategy: header row, one data row per item, optional cell styling
 * ({@code @ExportStyle}) and vertical merge of consecutive equal values ({@code @ExcelMerge}).
 * {@link CellStyle} instances are cached per export call, keyed by their style attributes — Excel
 * caps the number of distinct styles a workbook may hold, so a fresh style must never be created
 * per cell.
 */
@Component
public class XlsxExportStrategy implements ExportStrategy {

    private static final String SHEET_NAME = "Sheet1";

    @Override
    public ExportFormat format() {
        return ExportFormat.XLSX;
    }

    @Override
    public byte[] export(List<?> data, List<ExportFieldMetadata> columns) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            Map<String, CellStyle> styleCache = new HashMap<>();

            writeHeaderRow(workbook, sheet, columns, styleCache);
            List<List<Object>> rawValues = readRowValues(data, columns);
            writeDataRows(sheet, columns, rawValues, styleCache);
            mergeConsecutiveValues(sheet, columns, rawValues);

            for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                sheet.autoSizeColumn(colIdx);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            workbook.write(buffer);
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new CoreException("Failed to write XLSX export", e);
        }
    }

    private void writeHeaderRow(
            XSSFWorkbook workbook, Sheet sheet, List<ExportFieldMetadata> columns, Map<String, CellStyle> styleCache) {
        Row header = sheet.createRow(0);
        CellStyle headerStyle = getOrCreateStyle(workbook, styleCache, "", "", true, CellAlign.LEFT);
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            Cell cell = header.createCell(colIdx);
            cell.setCellValue(columns.get(colIdx).headerName());
            cell.setCellStyle(headerStyle);
        }
    }

    private List<List<Object>> readRowValues(List<?> data, List<ExportFieldMetadata> columns) {
        List<List<Object>> rows = new ArrayList<>(data.size());
        for (Object item : data) {
            List<Object> row = new ArrayList<>(columns.size());
            for (ExportFieldMetadata column : columns) {
                row.add(readValue(item, column.field()));
            }
            rows.add(row);
        }
        return rows;
    }

    private Object readValue(Object item, Field field) {
        try {
            return field.get(item);
        } catch (IllegalAccessException e) {
            throw new CoreException("Failed to read field " + field.getName() + " for XLSX export", e);
        }
    }

    private void writeDataRows(
            Sheet sheet, List<ExportFieldMetadata> columns, List<List<Object>> rawValues, Map<String, CellStyle> styleCache) {
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        for (int rowIdx = 0; rowIdx < rawValues.size(); rowIdx++) {
            Row row = sheet.createRow(rowIdx + 1);
            List<Object> values = rawValues.get(rowIdx);
            for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                ExportFieldMetadata column = columns.get(colIdx);
                Cell cell = row.createCell(colIdx);
                if (!column.merge() || isMergeGroupStart(rawValues, rowIdx, colIdx)) {
                    setCellValue(cell, values.get(colIdx));
                }
                ExportStyle style = column.style();
                if (style != null) {
                    cell.setCellStyle(getOrCreateStyle(
                            workbook, styleCache, style.backgroundColor(), style.fontColor(), style.bold(), style.align()));
                }
            }
        }
    }

    private boolean isMergeGroupStart(List<List<Object>> rawValues, int rowIdx, int colIdx) {
        if (rowIdx == 0) {
            return true;
        }
        Object current = rawValues.get(rowIdx).get(colIdx);
        Object previous = rawValues.get(rowIdx - 1).get(colIdx);
        return !Objects.equals(current, previous);
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private void mergeConsecutiveValues(Sheet sheet, List<ExportFieldMetadata> columns, List<List<Object>> rawValues) {
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            if (!columns.get(colIdx).merge()) {
                continue;
            }
            int runStart = 0;
            while (runStart < rawValues.size()) {
                int runEnd = runStart;
                Object value = rawValues.get(runStart).get(colIdx);
                while (runEnd + 1 < rawValues.size() && Objects.equals(rawValues.get(runEnd + 1).get(colIdx), value)) {
                    runEnd++;
                }
                if (runEnd > runStart) {
                    sheet.addMergedRegion(new CellRangeAddress(runStart + 1, runEnd + 1, colIdx, colIdx));
                }
                runStart = runEnd + 1;
            }
        }
    }

    private CellStyle getOrCreateStyle(
            XSSFWorkbook workbook, Map<String, CellStyle> styleCache, String backgroundColor, String fontColor,
            boolean bold, CellAlign align) {
        String key = backgroundColor + "|" + fontColor + "|" + bold + "|" + align;
        return styleCache.computeIfAbsent(key, k -> buildStyle(workbook, backgroundColor, fontColor, bold, align));
    }

    private CellStyle buildStyle(XSSFWorkbook workbook, String backgroundColor, String fontColor, boolean bold, CellAlign align) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(toHorizontalAlignment(align));

        Font font = workbook.createFont();
        font.setBold(bold);
        if (!fontColor.isBlank()) {
            ((XSSFFont) font).setColor(new XSSFColor(Color.decode(fontColor), null));
        }
        style.setFont(font);

        if (!backgroundColor.isBlank()) {
            style.setFillForegroundColor(new XSSFColor(Color.decode(backgroundColor), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private HorizontalAlignment toHorizontalAlignment(CellAlign align) {
        return switch (align) {
            case CENTER -> HorizontalAlignment.CENTER;
            case RIGHT -> HorizontalAlignment.RIGHT;
            case LEFT -> HorizontalAlignment.LEFT;
        };
    }
}
