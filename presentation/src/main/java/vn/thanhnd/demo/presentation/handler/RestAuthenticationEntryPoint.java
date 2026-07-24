package vn.thanhnd.demo.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Writes a 401 {@link RestResponse} when a protected endpoint is called without credentials.
 * Handles the case {@link vn.thanhnd.demo.presentation.filter.JwtAuthenticationFilter} does not:
 * no {@code Authorization} header at all.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ERROR_CODE = "E-01-ADMINISTRATOR-0008";

    private final ResponseMaker responseMaker;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        RestResponse<Void> body = responseMaker.createFailResponse("api", ERROR_CODE, ERROR_CODE);
        responseMaker.writeResponse(response, body);
    }
}
