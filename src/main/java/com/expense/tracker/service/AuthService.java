package com.expense.tracker.service;

import com.expense.tracker.dto.request.LoginRequest;
import com.expense.tracker.dto.request.RegisterRequest;
import com.expense.tracker.dto.response.AuthResponse;
import com.expense.tracker.dto.response.UserResponse;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.UnauthorizedException;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * Handles user registration, login, and JWT token generation.
 * Implements security best practices with password hashing and validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user account
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email address is already registered");
        }
        
        // Create new user using builder
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        // Create response
        UserResponse userResponse = new UserResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getCreatedAt()
        );
        
        return new AuthResponse(token, userResponse);
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        log.info("User authenticated successfully: {}", user.getEmail());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Create response
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
        
        return new AuthResponse(token, userResponse);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user email from JWT token
     */
    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }
}
