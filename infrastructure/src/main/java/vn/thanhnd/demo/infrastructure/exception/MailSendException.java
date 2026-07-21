package vn.thanhnd.demo.infrastructure.exception;

import vn.thanhnd.demo.util.exception.CoreException;

public class MailSendException extends CoreException {

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
