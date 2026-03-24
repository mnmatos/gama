package com.digitallib.exception;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String exceptionText) {
        super(exceptionText);
    }

    public EntityNotFoundException(String exceptionText, Throwable cause) {
        super(exceptionText, cause);
    }
}
