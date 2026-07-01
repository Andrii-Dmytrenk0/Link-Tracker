package com.linktracker.exception;

/**
 * Thrown when a requested entity (influencer, click event, etc.) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
