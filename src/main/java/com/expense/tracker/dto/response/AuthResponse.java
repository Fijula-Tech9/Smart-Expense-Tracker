package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth Response DTO
 * 
 * Used to return authentication results with JWT token and user info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String message;
    private String token;
    private UserResponse user;
    
    /**
     * Constructor for successful authentication
     */
    public AuthResponse(String token, UserResponse user) {
        this.message = "Authentication successful";
        this.token = token;
        this.user = user;
    }
}
