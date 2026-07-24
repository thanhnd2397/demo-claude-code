package vn.thanhnd.demo.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Writes a 403 {@link RestResponse} when an authenticated caller lacks the required role/authority.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final String ERROR_CODE = "E-01-ADMINISTRATOR-0009";

    private final ResponseMaker responseMaker;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        RestResponse<Void> body = responseMaker.createFailResponse("api", ERROR_CODE, ERROR_CODE);
        responseMaker.writeResponse(response, body);
    }
}
