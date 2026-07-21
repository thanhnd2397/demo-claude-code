package vn.thanhnd.demo.domain.exception;

/**
 * Thrown by domain models and use cases on business rule violations. Error code format:
 * {@code E-{layer}-{ENTITY}-{sequence}} (e.g. {@code E-01-MEMBER-0002}).
 * Caught by {@code ResultHandler} and converted to a {@link DomainError} inside {@code ResultWrapper.failure(...)}.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
