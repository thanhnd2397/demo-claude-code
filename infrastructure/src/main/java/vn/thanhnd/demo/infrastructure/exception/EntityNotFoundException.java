package vn.thanhnd.demo.infrastructure.exception;

import vn.thanhnd.demo.util.exception.CoreException;

public class EntityNotFoundException extends CoreException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
