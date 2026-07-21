package vn.thanhnd.demo.infrastructure.exception;

import vn.thanhnd.demo.util.exception.CoreException;

public class RedisException extends CoreException {

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
