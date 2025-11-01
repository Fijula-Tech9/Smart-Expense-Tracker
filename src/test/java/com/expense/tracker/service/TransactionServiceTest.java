package com.expense.tracker.service;

import com.expense.tracker.dto.request.TransactionRequest;
import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category testCategory;
    private TransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food & Dining");
        testCategory.setType(CategoryType.EXPENSE);
        testCategory.setIsSystemCategory(true);

        validRequest = new TransactionRequest();
        validRequest.setType(TransactionType.EXPENSE);
        validRequest.setCategoryId(1L);
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setTransactionDate(LocalDate.now());
        validRequest.setDescription("Test transaction");
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        var response = transactionService.createTransaction(validRequest, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(validRequest, testUser);
        });
    }

    @Test
    void createTransaction_FutureDate() {
        // Arrange
        validRequest.setTransactionDate(LocalDate.now().plusDays(1));
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            transactionService.createTransaction(validRequest, testUser);
        });
    }

    @Test
    void createTransaction_CategoryTypeMismatch() {
        // Arrange
        testCategory.setType(CategoryType.INCOME);
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            transactionService.createTransaction(validRequest, testUser);
        });
    }
}


