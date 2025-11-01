package com.expense.tracker.service;

import com.expense.tracker.dto.request.UpdateProfileRequest;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$12$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        updateRequest = new UpdateProfileRequest();
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));

        // Act
        var response = userService.getUserProfile(testUser);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).findByIdAndIsActiveTrue(1L);
    }

    @Test
    void getUserProfile_UserNotFound() {
        // Arrange
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile(testUser);
        });
    }

    @Test
    void updateUserProfile_UpdateName_Success() {
        // Arrange
        updateRequest.setName("Updated Name");
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        var response = userService.updateUserProfile(updateRequest, testUser);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(testUser);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUserProfile_UpdatePassword_Success() {
        // Arrange
        updateRequest.setPassword("newPassword123");
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        var response = userService.updateUserProfile(updateRequest, testUser);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserProfile_UpdateBoth_Success() {
        // Arrange
        updateRequest.setName("Updated Name");
        updateRequest.setPassword("newPassword123");
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        var response = userService.updateUserProfile(updateRequest, testUser);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserProfile_NoFieldsProvided() {
        // Arrange
        // updateRequest has no fields set (both null)
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            userService.updateUserProfile(updateRequest, testUser);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_PasswordTooShort() {
        // Arrange
        updateRequest.setPassword("short");
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            userService.updateUserProfile(updateRequest, testUser);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_UserNotFound() {
        // Arrange
        updateRequest.setName("Updated Name");
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile(updateRequest, testUser);
        });
    }
}

