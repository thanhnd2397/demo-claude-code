package vn.thanhnd.demo.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

import java.util.Locale;

/**
 * Writes a 401 {@link RestResponse} when a protected endpoint is called without credentials.
 * Handles the case {@link vn.thanhnd.demo.presentation.filter.JwtAuthenticationFilter} does not:
 * no {@code Authorization} header at all.
 * <p>
 * Runs in the Spring Security filter chain, before {@code DispatcherServlet} — {@code LocaleContextHolder}
 * is not populated yet here, so the locale is resolved from the request manually.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ERROR_CODE = "E-01-ADMINISTRATOR-0008";

    private final ResponseMaker responseMaker;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(ERROR_CODE, null, ERROR_CODE, locale);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        RestResponse<Void> body = responseMaker.createFailResponse("api", ERROR_CODE, message);
        responseMaker.writeResponse(response, body);
    }
}
