package com.expense.tracker.controller;

import com.expense.tracker.dto.request.LoginRequest;
import com.expense.tracker.dto.request.RegisterRequest;
import com.expense.tracker.dto.response.AuthResponse;
import com.expense.tracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user registration, login, and authentication-related operations.
 * These endpoints are public and do not require JWT authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register a new user account",
        description = "Create a new user account with email, name, and password. Returns JWT token for immediate authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email address is already registered"
        )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login with email and password
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login with email and password",
        description = "Authenticate user with email and password. Returns JWT token valid for 24 hours."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password"
        )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    @Operation(
        summary = "Authentication service health check",
        description = "Check if authentication service is running properly"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running");
    }
}
