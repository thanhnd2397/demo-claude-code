package vn.thanhnd.demo.presentation.api.export;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thanhnd.demo.util.annotation.export.ExportFormat;
import vn.thanhnd.demo.util.constant.ApplicationConstants;
import vn.thanhnd.demo.util.export.ExportService;

import java.util.List;
import java.util.Locale;

/**
 * Demo endpoint proving the annotation-driven export engine end-to-end against mock, hardcoded
 * data (no DB/domain model involved). Returns raw file bytes, not a {@code RestResponse<T>} JSON
 * envelope, so this deliberately does not extend {@code BaseController}.
 */
@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Export a mock list of users.
     * GET /api/v1/export/users
     * <p>
     * Serializes a hardcoded {@code List<UserExportDto>} to CSV or XLSX, driven by
     * {@code @ExportColumn}/{@code @ExportStyle}/{@code @ExcelMerge} on {@link UserExportDto}.
     *
     * @param format Desired output format, {@code csv} (default) or {@code xlsx}
     * @return The exported file as an attachment download
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> exportUsers(@RequestParam(defaultValue = "csv") String format) {
        ExportFormat exportFormat = ExportFormat.valueOf(format.toUpperCase(Locale.ROOT));
        byte[] content = exportService.export(mockUsers(), UserExportDto.class, exportFormat);

        String extension = exportFormat.name().toLowerCase(Locale.ROOT);
        String contentType = exportFormat == ExportFormat.XLSX
                ? ApplicationConstants.CONTENT_TYPE_XLSX
                : ApplicationConstants.CONTENT_TYPE_CSV;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("users." + extension).build().toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }

    private List<UserExportDto> mockUsers() {
        return List.of(
                new UserExportDto("Alice Nguyen", "U-001", "Engineering", "alice@example.com", "ACTIVE"),
                new UserExportDto("Bob Tran", "U-002", "Engineering", "bob@example.com", "ACTIVE"),
                new UserExportDto("Charlie Le", "U-003", "Sales", "charlie@example.com", "INACTIVE"),
                new UserExportDto("Dana Pham", "U-004", "Sales", "dana@example.com", "ACTIVE"),
                new UserExportDto("Evan Vu", "U-005", "Marketing", "evan@example.com", "ACTIVE"));
    }
}
