package com.expense.tracker.exception;

/**
 * Unauthorized Exception
 * 
 * Thrown when authentication fails or credentials are invalid.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
