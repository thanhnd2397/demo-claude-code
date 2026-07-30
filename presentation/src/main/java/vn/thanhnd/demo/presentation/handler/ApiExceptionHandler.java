package vn.thanhnd.demo.presentation.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.util.exception.CoreException;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

import java.util.stream.Collectors;

/**
 * Converts exceptions raised under {@code presentation.api} into a {@link RestResponse} envelope.
 * Error messages are resolved from {@code messages(_vi).properties} for the request's locale,
 * falling back to the raw code when no translation exists.
 */
@RestControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.api")
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private static final String VALIDATION_ERROR_CODE = "E-00-CORE-0002";

    private final ResponseMaker responseMaker;
    private final MessageSource messageSource;

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<RestResponse<Void>> handleDomainValidation(DomainValidationException e) {
        String code = e.getMessage();
        String message = messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
        RestResponse<Void> body = responseMaker.createFailResponse("api", code, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles {@code @Valid} DTO binding failures (e.g. {@code @NotBlank}, {@code @Size}). Without
     * this handler, Spring falls back to its default resolver, which forwards to {@code /error} —
     * a dispatch that re-enters the security filter chain and, since it carries no credentials,
     * gets masked as a 401 "authentication required" instead of the real validation error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        RestResponse<Void> body = responseMaker.createFailResponse("api", VALIDATION_ERROR_CODE, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<RestResponse<Void>> handleCore(CoreException e) {
        String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        RestResponse<Void> body = responseMaker.createFailResponse("api", "E-00-CORE-0001", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
