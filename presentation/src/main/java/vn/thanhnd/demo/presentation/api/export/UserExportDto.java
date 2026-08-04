package vn.thanhnd.demo.presentation.api.export;

import vn.thanhnd.demo.util.annotation.export.CellAlign;
import vn.thanhnd.demo.util.annotation.export.ExcelMerge;
import vn.thanhnd.demo.util.annotation.export.ExportColumn;
import vn.thanhnd.demo.util.annotation.export.ExportStyle;

/**
 * Example row type for {@link ExportController}, demonstrating the annotation-driven export
 * engine's full contract: column ordering ({@code @ExportColumn}), cell styling
 * ({@code @ExportStyle}, XLSX only), and vertical merge of repeated values ({@code @ExcelMerge},
 * XLSX only). Not backed by any real domain model — mock/demo data only.
 */
public record UserExportDto(
        @ExportColumn(headerName = "Full Name", order = 2)
        String fullName,

        @ExportColumn(headerName = "ID", order = 1)
        @ExportStyle(bold = true, align = CellAlign.CENTER)
        String id,

        @ExportColumn(headerName = "Department", order = 3)
        @ExcelMerge
        String department,

        @ExportColumn(headerName = "Email", order = 4)
        String email,

        @ExportColumn(headerName = "Status", order = 5)
        @ExportStyle(backgroundColor = "#FFCC00", fontColor = "#FF0000", align = CellAlign.RIGHT)
        String status) {
}
