package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletResponse;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Builds and writes {@link RestResponse} envelopes. Inject this interface, not the implementation.
 */
public interface ResponseMaker {

    <T> RestResponse<T> createSuccessResponse(String serviceName, T data);

    <T> RestResponse<T> createFailResponse(String serviceName, String errorCode, String errorMessage);

    void writeResponse(HttpServletResponse response, RestResponse<?> restResponse);
}
