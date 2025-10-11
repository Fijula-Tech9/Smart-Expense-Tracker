package com.expense.tracker.exception;

/**
 * Invalid Request Exception
 * 
 * Thrown when request data is invalid or business rules are violated.
 */
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
