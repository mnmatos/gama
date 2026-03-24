package com.digitallib.exception;

public class ValidationException extends Exception {
    public ValidationException(String exceptionText) {
        super(exceptionText);
    }

    public ValidationException(String exceptionText, Throwable cause) {
        super(exceptionText, cause);
    }
}
