package vn.thanhnd.demo.util.annotation.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as an exported column. Required on every field the export engine should serialize
 * to CSV/XLSX — fields without this annotation are ignored.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportColumn {

    /**
     * The column header text written to the first row of the export.
     */
    String headerName();

    /**
     * The column's position in the export, ascending. Independent of field declaration order.
     */
    int order();
}
