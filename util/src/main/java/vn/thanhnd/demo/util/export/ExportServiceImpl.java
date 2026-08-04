package vn.thanhnd.demo.util.export;

import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.annotation.export.ExportFormat;
import vn.thanhnd.demo.util.exception.CoreException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExportServiceImpl implements ExportService {

    private final ExportMetadataResolver metadataResolver;
    private final Map<ExportFormat, ExportStrategy> strategiesByFormat;

    public ExportServiceImpl(ExportMetadataResolver metadataResolver, List<ExportStrategy> strategies) {
        this.metadataResolver = metadataResolver;
        this.strategiesByFormat = strategies.stream()
                .collect(Collectors.toMap(ExportStrategy::format, Function.identity()));
    }

    @Override
    public byte[] export(List<?> data, Class<?> type, ExportFormat format) {
        ExportStrategy strategy = strategiesByFormat.get(format);
        if (strategy == null) {
            throw new CoreException("No export strategy registered for format " + format);
        }
        List<ExportFieldMetadata> columns = metadataResolver.resolve(type);
        return strategy.export(data, columns);
    }
}
