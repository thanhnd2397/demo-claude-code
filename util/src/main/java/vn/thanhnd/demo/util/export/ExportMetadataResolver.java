package vn.thanhnd.demo.util.export;

import java.util.List;

/**
 * Resolves a class's exported columns by scanning its fields for {@code @ExportColumn}
 * (plus optional {@code @ExportStyle}/{@code @ExcelMerge}). Inject this interface, not the
 * implementation.
 */
public interface ExportMetadataResolver {

    /**
     * Resolve the ordered list of exported columns for the given type.
     *
     * @param type The class to scan (e.g. a DTO record)
     * @return Column metadata sorted by {@code @ExportColumn#order()}, ascending
     */
    List<ExportFieldMetadata> resolve(Class<?> type);
}
