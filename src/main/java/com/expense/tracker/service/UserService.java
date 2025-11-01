package com.expense.tracker.service;

import com.expense.tracker.dto.request.UpdateProfileRequest;
import com.expense.tracker.dto.response.UserResponse;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service
 * 
 * Handles business logic for user profile management including
 * viewing and updating user profile information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(User user) {
        log.info("Fetching profile for user ID: {}", user.getId());
        
        // Refresh user entity to get latest data
        User refreshedUser = userRepository.findByIdAndIsActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return new UserResponse(
            refreshedUser.getId(),
            refreshedUser.getName(),
            refreshedUser.getEmail(),
            refreshedUser.getCreatedAt()
        );
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUserProfile(UpdateProfileRequest request, User user) {
        log.info("Updating profile for user ID: {}", user.getId());
        
        // Refresh user entity
        User updatedUser = userRepository.findByIdAndIsActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update name if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            updatedUser.setName(request.getName().trim());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPassword().length() < 8) {
                throw new InvalidRequestException("Password must be at least 8 characters long");
            }
            updatedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Validate that at least one field is being updated
        if (request.getName() == null && request.getPassword() == null) {
            throw new InvalidRequestException("At least one field (name or password) must be provided for update");
        }
        
        User savedUser = userRepository.save(updatedUser);
        log.info("Profile updated successfully for user ID: {}", savedUser.getId());
        
        return new UserResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getCreatedAt()
        );
    }
}


