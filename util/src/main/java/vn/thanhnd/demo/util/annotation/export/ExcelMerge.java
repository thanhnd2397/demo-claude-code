package vn.thanhnd.demo.util.annotation.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for a {@link ExportColumn} field, XLSX only. Consecutive rows holding an equal value in
 * this column are collapsed into one vertically merged cell. Ignored by the CSV strategy — plain-text
 * format has no concept of cell merging. No horizontal merge support.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelMerge {
}
