package vn.thanhnd.demo.application.base;

import vn.thanhnd.demo.domain.exception.DomainError;

import java.util.List;

/**
 * Result of a command-style write use case. Controllers check {@link #isSuccess()} and,
 * on failure, map {@link #getErrors()} without relying solely on global exception handlers.
 *
 * @param <T> success payload type (often {@link Void} for commands)
 */
public class ResultWrapper<T> {

    private final T data;
    private final List<DomainError> errors;
    private final boolean success;

    private ResultWrapper(T data, List<DomainError> errors, boolean success) {
        this.data = data;
        this.errors = errors;
        this.success = success;
    }

    public static <T> ResultWrapper<T> success(T data) {
        return new ResultWrapper<>(data, List.of(), true);
    }

    public static <T> ResultWrapper<T> failure(List<DomainError> errors) {
        return new ResultWrapper<>(null, errors, false);
    }

    public T getData() {
        return data;
    }

    public List<DomainError> getErrors() {
        return errors;
    }

    public boolean isSuccess() {
        return success;
    }
}
