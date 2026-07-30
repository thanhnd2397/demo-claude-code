package vn.thanhnd.demo.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

import java.util.Locale;

/**
 * Writes a 403 {@link RestResponse} when an authenticated caller lacks the required role/authority.
 * <p>
 * Runs in the Spring Security filter chain, before {@code DispatcherServlet} — {@code LocaleContextHolder}
 * is not populated yet here, so the locale is resolved from the request manually.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final String ERROR_CODE = "E-01-ADMINISTRATOR-0009";

    private final ResponseMaker responseMaker;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(ERROR_CODE, null, ERROR_CODE, locale);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        RestResponse<Void> body = responseMaker.createFailResponse("api", ERROR_CODE, message);
        responseMaker.writeResponse(response, body);
    }
}
