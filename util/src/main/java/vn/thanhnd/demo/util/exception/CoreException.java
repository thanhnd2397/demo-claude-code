package vn.thanhnd.demo.util.exception;

/**
 * Base runtime exception for application-wide errors. Infrastructure-specific
 * exceptions (e.g. EntityNotFoundException, RedisException) extend this class.
 */
public class CoreException extends RuntimeException {

    public CoreException(String message) {
        super(message);
    }

    public CoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
