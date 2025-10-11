package com.expense.tracker.exception;

/**
 * Resource Not Found Exception
 * 
 * Thrown when a requested resource (user, transaction, category, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
