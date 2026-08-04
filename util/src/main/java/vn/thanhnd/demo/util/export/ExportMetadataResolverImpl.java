package vn.thanhnd.demo.util.export;

import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.annotation.export.ExcelMerge;
import vn.thanhnd.demo.util.annotation.export.ExportColumn;
import vn.thanhnd.demo.util.annotation.export.ExportStyle;
import vn.thanhnd.demo.util.exception.CoreException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ExportMetadataResolverImpl implements ExportMetadataResolver {

    private final ConcurrentMap<Class<?>, List<ExportFieldMetadata>> cache = new ConcurrentHashMap<>();

    @Override
    public List<ExportFieldMetadata> resolve(Class<?> type) {
        return cache.computeIfAbsent(type, this::scan);
    }

    private List<ExportFieldMetadata> scan(Class<?> type) {
        List<ExportFieldMetadata> metadata = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            ExportColumn column = field.getAnnotation(ExportColumn.class);
            if (column == null) {
                continue;
            }
            field.setAccessible(true);
            ExportStyle style = field.getAnnotation(ExportStyle.class);
            boolean merge = field.isAnnotationPresent(ExcelMerge.class);
            metadata.add(new ExportFieldMetadata(field, column.headerName(), column.order(), style, merge));
        }
        if (metadata.isEmpty()) {
            throw new CoreException("No @ExportColumn field found on " + type.getName());
        }
        metadata.sort(Comparator.comparingInt(ExportFieldMetadata::order));
        return metadata;
    }
}
