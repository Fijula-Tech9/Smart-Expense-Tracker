package com.expense.tracker.service;

import com.expense.tracker.dto.request.LoginRequest;
import com.expense.tracker.dto.request.RegisterRequest;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.UnauthorizedException;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        existingUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$12$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashedPassword");
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        // Act
        var response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getUser());
        assertEquals("Test User", response.getUser().getName());
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void register_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        // Act
        var response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        verify(userRepository, times(1)).findByEmailAndIsActiveTrue("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$12$hashedPassword");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$12$hashedPassword")).thenReturn(false);

        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void validateToken_Success() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertTrue(result);
        verify(jwtUtil, times(1)).validateToken(token);
    }
}

