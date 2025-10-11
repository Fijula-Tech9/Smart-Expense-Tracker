package com.expense.tracker.exception;

/**
 * Forbidden Exception
 * 
 * Thrown when a user tries to access or modify resources they don't own.
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
