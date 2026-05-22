package com.digitallib.transcription;

/**
 * Exception thrown by any {@link TranscriptionAdapter} implementation.
 */
public class TranscriptionException extends Exception {
    public TranscriptionException(String message) { super(message); }
    public TranscriptionException(String message, Throwable cause) { super(message, cause); }
}

