package vn.thanhnd.demo.util.export;

import vn.thanhnd.demo.util.annotation.export.ExportFormat;

import java.util.List;

/**
 * Annotation-driven export engine: serializes any {@code List<T>} to CSV or XLSX, driven entirely
 * by {@code @ExportColumn}/{@code @ExportStyle}/{@code @ExcelMerge} on {@code T}'s fields.
 * Inject this interface, not the implementation.
 */
public interface ExportService {

    /**
     * Export the given data to the requested format.
     *
     * @param data The rows to export
     * @param type The row type, scanned for {@code @ExportColumn} fields
     * @param format The desired output format
     * @return The serialized file content
     */
    byte[] export(List<?> data, Class<?> type, ExportFormat format);
}
