package vn.thanhnd.demo.infrastructure.exception;

import vn.thanhnd.demo.util.exception.CoreException;

public class StorageException extends CoreException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
