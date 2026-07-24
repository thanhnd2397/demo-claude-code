package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletResponse;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Builds and writes {@link RestResponse} envelopes. Inject this interface, not the implementation.
 */
public interface ResponseMaker {

    /**
     * Build a success envelope.
     *
     * @param serviceName The logical service name stamped on the response
     * @param data The response payload
     * @return The success RestResponse wrapping the payload
     */
    <T> RestResponse<T> createSuccessResponse(String serviceName, T data);

    /**
     * Build a failure envelope.
     *
     * @param serviceName The logical service name stamped on the response
     * @param errorCode The error code (e.g. {@code E-01-ADMINISTRATOR-0003})
     * @param errorMessage The error message
     * @return The failure RestResponse carrying the error
     */
    <T> RestResponse<T> createFailResponse(String serviceName, String errorCode, String errorMessage);

    /**
     * Serialize an envelope as JSON directly onto the servlet response (used outside controllers,
     * e.g. in filters and security handlers).
     *
     * @param response The servlet response to write to
     * @param restResponse The envelope to serialize
     */
    void writeResponse(HttpServletResponse response, RestResponse<?> restResponse);
}
