package com.digitallib.exception;

public class EntityNotFoundException extends Throwable {
    public EntityNotFoundException(String exceptionText) {
        super(exceptionText);
    }
}
