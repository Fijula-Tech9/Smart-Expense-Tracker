package com.expense.tracker.exception;

/**
 * Duplicate Resource Exception
 * 
 * Thrown when attempting to create a resource that already exists (e.g., email already registered).
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
