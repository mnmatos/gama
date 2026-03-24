package com.digitallib.exception;

public class RepositoryException extends Exception {
    public RepositoryException(String exceptionText) {
        super(exceptionText);
    }

    public RepositoryException(String exceptionText, Throwable cause) {
        super(exceptionText, cause);
    }
}
