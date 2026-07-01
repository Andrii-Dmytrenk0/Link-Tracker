package com.linktracker.exception;

/**
 * Thrown when attempting to create or update an influencer with a code that
 * is already in use.
 */
public class DuplicateCodeException extends RuntimeException {
    public DuplicateCodeException(String message) {
        super(message);
    }
}
