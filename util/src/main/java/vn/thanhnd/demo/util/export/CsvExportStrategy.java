package vn.thanhnd.demo.util.export;

import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.annotation.export.ExportFormat;
import vn.thanhnd.demo.util.exception.CoreException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Hand-rolled CSV export strategy: RFC 4180 escaping plus a UTF-8 byte-order-mark prefix so Excel
 * opens non-ASCII text (e.g. Vietnamese) correctly. Ignores {@code @ExportStyle}/{@code @ExcelMerge} —
 * plain text has no concept of cell styling or merging.
 */
@Component
public class CsvExportStrategy implements ExportStrategy {

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Override
    public ExportFormat format() {
        return ExportFormat.CSV;
    }

    @Override
    public byte[] export(List<?> data, List<ExportFieldMetadata> columns) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            buffer.write(UTF8_BOM);
            try (Writer writer = new OutputStreamWriter(buffer, StandardCharsets.UTF_8)) {
                writeRow(writer, columns.stream().map(ExportFieldMetadata::headerName).toList());
                for (Object item : data) {
                    writeRow(writer, readRowValues(item, columns));
                }
            }
        } catch (IOException e) {
            throw new CoreException("Failed to write CSV export", e);
        }
        return buffer.toByteArray();
    }

    private List<String> readRowValues(Object item, List<ExportFieldMetadata> columns) {
        return columns.stream().map(column -> readValue(item, column.field())).toList();
    }

    private String readValue(Object item, Field field) {
        try {
            Object value = field.get(item);
            return value == null ? "" : value.toString();
        } catch (IllegalAccessException e) {
            throw new CoreException("Failed to read field " + field.getName() + " for CSV export", e);
        }
    }

    private void writeRow(Writer writer, List<String> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escape(values.get(i)));
        }
        writer.write("\r\n");
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
