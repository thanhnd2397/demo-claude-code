package vn.thanhnd.demo.presentation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.exception.DomainError;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

/**
 * Base class for REST API controllers. Centralizes the {@link ResultWrapper}-to-{@link ResponseEntity}
 * conversion shared by every endpoint: success maps to {@code successStatus}, failure maps to
 * {@code 400 Bad Request} carrying the first {@link DomainError}.
 */
public abstract class BaseController {

    protected static final String SERVICE_NAME = "api";

    protected final ResponseMaker responseMaker;

    protected BaseController(ResponseMaker responseMaker) {
        this.responseMaker = responseMaker;
    }

    protected <T> ResponseEntity<RestResponse<T>> toResponseEntity(ResultWrapper<T> result, HttpStatus successStatus) {
        if (result.isSuccess()) {
            RestResponse<T> body = responseMaker.createSuccessResponse(SERVICE_NAME, result.getData());
            return ResponseEntity.status(successStatus).body(body);
        }
        DomainError error = result.getErrors().get(0);
        RestResponse<T> body = responseMaker.createFailResponse(SERVICE_NAME, error.errorCode(), error.errorCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
