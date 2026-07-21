package vn.thanhnd.demo.util.helper;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.constant.ApplicationConstants;
import vn.thanhnd.demo.util.response.ErrorModel;
import vn.thanhnd.demo.util.response.RestResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ResponseMakerImpl implements ResponseMaker {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public <T> RestResponse<T> createSuccessResponse(String serviceName, T data) {
        return new RestResponse<>(serviceName, true, null, data);
    }

    @Override
    public <T> RestResponse<T> createFailResponse(String serviceName, String errorCode, String errorMessage) {
        return new RestResponse<>(serviceName, false, List.of(new ErrorModel(errorCode, errorMessage)), null);
    }

    @Override
    public void writeResponse(HttpServletResponse response, RestResponse<?> restResponse) {
        response.setContentType(ApplicationConstants.CONTENT_TYPE_JSON);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write(jsonMapper.writeValueAsString(restResponse));
        } catch (IOException e) {
            // Best-effort: response stream may already be committed/closed by the client.
            org.slf4j.LoggerFactory.getLogger(ResponseMakerImpl.class)
                    .warn("Failed to write RestResponse to HttpServletResponse", e);
        }
    }
}
