# Annotation-driven CSV/XLSX Export Module

> Durable copy of the approved plan (Plan Mode writes its working file outside the repo; this is the canonical project artifact — see `.claude/rules/plan-review-checklist.md` note in `CLAUDE.md`).

## Document metadata

| Field | Value |
| --- | --- |
| Created | 2026-08-04 |
| Author | thanhnd (planned with Claude Code) |
| Status | Approved |

## Context

The project has no export/download capability today (confirmed by exploration: zero hits for `poi|opencsv|excel|xlsx|export|download` across `src/main`, and no controller anywhere returns raw bytes — every endpoint returns a `RestResponse<T>` JSON envelope). The user wants a reusable, annotation-driven engine (similar in spirit to EasyExcel) that can serialize any `List<T>` to CSV or XLSX, driven entirely by custom annotations on the target class, with column ordering, cell styling (XLSX only), and vertical cell merging (XLSX only) — plus a small demo (`User` DTO + controller) proving it end-to-end.

This is a generic **technical utility**, not a business use case — it doesn't touch `Administrator` or any existing port/domain model. Confirmed decisions (via user Q&A):
- Lives in `util/` module, next to the existing `ResponseMaker`/`Helper` pattern — reused by any future feature without new inter-module wiring.
- CSV writer is hand-rolled (RFC 4180 escaping + UTF-8 BOM) — no new dependency for something this small.
- Apache POI (`poi` + `poi-ooxml`) is added as a new managed dependency (XLSX only) — first binary-file dependency in the project.
- `@ExcelMerge` supports **vertical merge only** (consecutive rows with equal value in one column collapse into one merged cell — the common "repeated group key" report pattern). No horizontal merge.

## Assumptions

- Field-level reflection with `setAccessible(true)` is used to read values — this works for both plain classes and Java `record`s (records store values in private final fields), so the demo DTO can be a record per project convention, with no getters needed for the export engine itself.
- CSV output ignores `@ExportStyle`/`@ExcelMerge` (no such thing in CSV) — documented, not an error.
- The demo endpoint returns **mock, hardcoded data** (no DB/domain model involved) — it exists purely to demonstrate the annotation contract and download flow, per the request's "DTO ví dụ (User)" wording. It is not wired into the real `Administrator` feature.
- Style colors are supplied as hex strings (e.g. `"#FFCC00"`) and parsed via `java.awt.Color.decode(...)` — matches how style values are normally expressed in annotations (`String`, not `java.awt.Color`, since annotation attributes can't hold arbitrary objects).
- The demo endpoint's auth requirement follows whatever `SecurityConfig` already applies by default (no new `permitAll()` rule added) — will be confirmed by reading `SecurityConfig` during implementation, not decided speculatively here.
- Excel has a hard cap (~64,000) on distinct cell styles per workbook — `XlsxExportStrategy` must cache/reuse `CellStyle` objects by a style key instead of creating one per cell, or large exports will throw `IllegalStateException`. This is called out explicitly so it isn't missed as an edge case.

## Design

**Strategy pattern**, selected by an `ExportFormat` enum (`CSV`, `XLSX`):

```
ExportService (context)
  ├─ ExportMetadataResolver   (reflection: scan @ExportColumn/@ExportStyle/@ExcelMerge, cache per Class<?>)
  ├─ CsvExportStrategy   implements ExportStrategy
  └─ XlsxExportStrategy  implements ExportStrategy
```

`ExportService.export(List<?> data, Class<?> type, ExportFormat format)` → `byte[]`. Both strategies are `@Component`s; `ExportServiceImpl` collects `List<ExportStrategy>` via Spring and dispatches on `strategy.format() == format` — adding a future format (e.g. PDF) means adding one new `@Component`, no existing code touched.

### Annotations (`util/annotation/export/`)

- `@ExportColumn(headerName, order)` — required on every exported field; `order` fixes column position independent of field declaration order (resolver sorts by `order`).
- `@ExportStyle(backgroundColor = "", fontColor = "", bold = false, align = CellAlign.LEFT)` — optional, XLSX only; empty color strings mean "use default".
- `@ExcelMerge` — optional marker on a field; XLSX only. When present, `XlsxExportStrategy` merges a cell vertically with the row above if both belong to the same column and hold equal values.
- `ExportFormat` enum: `CSV`, `XLSX`.
- `CellAlign` enum: `LEFT`, `CENTER`, `RIGHT`.

### Reflection engine

- `ExportFieldMetadata` — internal record: `Field field, String headerName, int order, ExportStyleMeta style (nullable), boolean merge`.
- `ExportMetadataResolver` (interface) / `ExportMetadataResolverImpl` — `resolve(Class<?> type)` scans declared fields for `@ExportColumn`, builds sorted `List<ExportFieldMetadata>`, caches in `ConcurrentHashMap<Class<?>, List<ExportFieldMetadata>>` (reflection is done once per class, not once per export call). Throws `CoreException` if a class has zero `@ExportColumn` fields.

### CSV strategy

Hand-written writer: header row from `headerName`s in `order`; each row RFC-4180-escaped (quote fields containing `,`, `"`, or newline; double up embedded quotes); UTF-8 **BOM** prefix (`EF BB BF`) so Excel opens Vietnamese/UTF-8 text correctly. No style/merge handling — that's a CSV format limitation, not a bug.

### XLSX strategy

Apache POI `XSSFWorkbook`: header row (bold by convention), one data row per item. For each field with `@ExportStyle`, build/reuse a `CellStyle` from a cache keyed by `(bg, font, bold, align)` — never create a fresh `CellStyle` per cell (POI's style-count ceiling, see Assumptions). For fields with `@ExcelMerge`, track the previous row's value per column; on an equal consecutive value, extend the last merged region (`sheet.addMergedRegion`) instead of writing a new cell. Autosize columns after all rows are written. Serialize via `ByteArrayOutputStream`.

## Files

**Dependency wiring**
- `pom.xml` (root) — add `poi`/`poi-ooxml` version to `dependencyManagement` (explicit version, e.g. `5.3.0`; not covered by the Spring Boot parent BOM).
- `util/pom.xml` — add `poi` + `poi-ooxml` as dependencies.

**Annotations** — `util/src/main/java/vn/thanhnd/demo/util/annotation/export/`
- `ExportColumn.java`, `ExportStyle.java`, `ExcelMerge.java`, `ExportFormat.java`, `CellAlign.java`

**Core engine** — `util/src/main/java/vn/thanhnd/demo/util/export/`
- `ExportFieldMetadata.java`, `ExportMetadataResolver.java` + `ExportMetadataResolverImpl.java`
- `ExportStrategy.java`, `CsvExportStrategy.java`, `XlsxExportStrategy.java`
- `ExportService.java` + `ExportServiceImpl.java`

**Constants**
- `util/src/main/java/vn/thanhnd/demo/util/constant/ApplicationConstants.java` — add `CONTENT_TYPE_XLSX` (mirrors existing `CONTENT_TYPE_CSV`).

**Demo** — `presentation/src/main/java/vn/thanhnd/demo/presentation/api/export/`
- `UserExportDto.java` — example record, fields annotated with `@ExportColumn` (+ a couple with `@ExportStyle`/`@ExcelMerge` to show all three in use).
- `ExportController.java` — `GET /export/users?format=csv|xlsx`, builds a small hardcoded `List<UserExportDto>`, calls `ExportService.export(...)`, returns `ResponseEntity<byte[]>` with `Content-Disposition: attachment; filename=...` and the matching content type. This bypasses `BaseController.toResponseEntity` (JSON envelope) deliberately — file downloads are raw bytes, not a `RestResponse<T>`.

**Docs (mandatory per `implement-mode.md`)**
- `docs/architecture/project-overview.md` — new short subsection describing the export module (generic utility, not a use case/port), new row in §5 endpoint table for the demo endpoint, `CONTENT_TYPE_XLSX` note.
- `docs/architecture/architecture-map.html` — mirror the same addition in its JS data (module tree + endpoint table), per its own §9 instructions.

## Verification

1. `./mvnw.cmd clean compile -P=local` — all 6 modules compile clean, no errors.
2. Confirm `@ExportColumn(order=...)` reordering: declare `UserExportDto` fields out of the order they should appear as columns; verify the generated header row matches `order`, not declaration order.
3. Manual runtime check (start app, `curl` the endpoint, then stop the app per the "Runtime testing" rule in `CLAUDE.md`):
   - `GET /api/v1/export/users?format=csv` → response opens correctly in Excel with UTF-8 text intact (BOM present), commas/quotes in any test value escaped correctly.
   - `GET /api/v1/export/users?format=xlsx` → response is a valid `.xlsx` (opens in Excel/LibreOffice), styled cells show the configured background/bold/align, `@ExcelMerge` column shows merged cells for consecutive equal values.
4. Would a staff engineer approve this? — module is self-contained in `util/`, no changes to existing Administrator flows, no new abstractions beyond what's needed for Strategy dispatch + one-time reflection caching.
