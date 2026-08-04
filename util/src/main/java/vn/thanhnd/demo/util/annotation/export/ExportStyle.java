package vn.thanhnd.demo.util.annotation.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional cell styling for a {@link ExportColumn} field, XLSX only. Ignored by the CSV strategy —
 * plain-text format has no concept of cell styling.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportStyle {

    /**
     * Cell background color as a hex string (e.g. {@code "#FFCC00"}). Empty means "use default".
     */
    String backgroundColor() default "";

    /**
     * Font color as a hex string (e.g. {@code "#FF0000"}). Empty means "use default".
     */
    String fontColor() default "";

    /**
     * Whether the cell text is bold.
     */
    boolean bold() default false;

    /**
     * Horizontal text alignment of the cell.
     */
    CellAlign align() default CellAlign.LEFT;
}
