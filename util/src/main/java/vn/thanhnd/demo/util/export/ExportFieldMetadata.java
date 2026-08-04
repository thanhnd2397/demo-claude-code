package vn.thanhnd.demo.util.export;

import vn.thanhnd.demo.util.annotation.export.ExportStyle;

import java.lang.reflect.Field;

/**
 * Resolved export metadata for a single field, in the order it should appear as a column.
 *
 * @param field The reflected field, already made accessible
 * @param headerName The column header text
 * @param order The column position (ascending), as declared by {@code @ExportColumn}
 * @param style The field's {@code @ExportStyle} annotation, or {@code null} if absent (XLSX only)
 * @param merge Whether the field is annotated {@code @ExcelMerge} (XLSX only)
 */
public record ExportFieldMetadata(Field field, String headerName, int order, ExportStyle style, boolean merge) {
}
