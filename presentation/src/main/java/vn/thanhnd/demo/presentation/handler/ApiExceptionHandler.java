package vn.thanhnd.demo.presentation.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.util.exception.CoreException;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Converts exceptions raised under {@code presentation.api} into a {@link RestResponse} envelope.
 */
@RestControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.api")
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final ResponseMaker responseMaker;

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<RestResponse<Void>> handleDomainValidation(DomainValidationException e) {
        RestResponse<Void> body = responseMaker.createFailResponse("api", e.getMessage(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<RestResponse<Void>> handleCore(CoreException e) {
        RestResponse<Void> body = responseMaker.createFailResponse("api", "E-00-CORE-0001", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
