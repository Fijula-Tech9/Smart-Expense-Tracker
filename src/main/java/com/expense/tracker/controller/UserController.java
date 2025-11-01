package com.expense.tracker.controller;

import com.expense.tracker.dto.request.UpdateProfileRequest;
import com.expense.tracker.dto.response.UserResponse;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * 
 * RESTful API endpoints for user profile management including
 * viewing and updating profile information.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get user profile",
        description = "Retrieves the current authenticated user's profile information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserProfile() {
        User user = getCurrentUser();
        UserResponse response = userService.getUserProfile(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @Operation(
        summary = "Update user profile",
        description = "Updates the current authenticated user's profile. Can update name and/or password. Email cannot be changed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUserProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser();
        UserResponse response = userService.updateUserProfile(request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user from Security Context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}


