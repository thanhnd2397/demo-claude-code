package vn.thanhnd.demo.presentation.api;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.exception.DomainError;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Base class for REST API controllers. Centralizes the {@link ResultWrapper}-to-{@link ResponseEntity}
 * conversion shared by every endpoint: success maps to {@code successStatus}, failure maps to
 * {@code 400 Bad Request} carrying the first {@link DomainError}, with its message resolved from
 * {@code messages(_vi).properties} for the request's locale.
 */
public abstract class BaseController {

    protected static final String SERVICE_NAME = "api";

    protected final ResponseMaker responseMaker;
    protected final MessageSource messageSource;

    protected BaseController(ResponseMaker responseMaker, MessageSource messageSource) {
        this.responseMaker = responseMaker;
        this.messageSource = messageSource;
    }

    protected <T> ResponseEntity<RestResponse<T>> toResponseEntity(ResultWrapper<T> result, HttpStatus successStatus) {
        if (result.isSuccess()) {
            RestResponse<T> body = responseMaker.createSuccessResponse(SERVICE_NAME, result.getData());
            return ResponseEntity.status(successStatus).body(body);
        }
        DomainError error = result.getErrors().get(0);
        String message = messageSource.getMessage(
                error.errorCode(), error.args(), error.errorCode(), LocaleContextHolder.getLocale());
        RestResponse<T> body = responseMaker.createFailResponse(SERVICE_NAME, error.errorCode(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
