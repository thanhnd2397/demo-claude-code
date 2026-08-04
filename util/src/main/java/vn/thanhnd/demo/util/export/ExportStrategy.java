package vn.thanhnd.demo.util.export;

import vn.thanhnd.demo.util.annotation.export.ExportFormat;

import java.util.List;

/**
 * A single output format's serialization strategy for the export engine. Implementations are Spring
 * beans collected by {@link ExportServiceImpl} and dispatched by {@link #format()}.
 */
public interface ExportStrategy {

    /**
     * The format this strategy handles.
     *
     * @return The export format
     */
    ExportFormat format();

    /**
     * Serialize the given data to this strategy's format, using the resolved column metadata.
     *
     * @param data The rows to export
     * @param columns Resolved, order-sorted column metadata for the row type
     * @return The serialized file content
     */
    byte[] export(List<?> data, List<ExportFieldMetadata> columns);
}
