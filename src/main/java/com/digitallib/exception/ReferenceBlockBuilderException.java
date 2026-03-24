package com.digitallib.exception;

public class ReferenceBlockBuilderException extends Exception {
    public ReferenceBlockBuilderException(String exceptionText) {
        super(exceptionText);
    }

    public ReferenceBlockBuilderException(String exceptionText, Throwable cause) {
        super(exceptionText, cause);
    }
}
