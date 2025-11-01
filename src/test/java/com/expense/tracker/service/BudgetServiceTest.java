package com.expense.tracker.service;

import com.expense.tracker.dto.request.BudgetRequest;
import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.Budget;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.BudgetRepository;
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
 * Unit tests for BudgetService
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category expenseCategory;
    private BudgetRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Food & Dining");
        expenseCategory.setType(CategoryType.EXPENSE);
        expenseCategory.setIsSystemCategory(true);

        validRequest = new BudgetRequest();
        validRequest.setCategoryId(1L);
        validRequest.setBudgetAmount(new BigDecimal("5000.00"));
        LocalDate now = LocalDate.now();
        validRequest.setMonth(now.getMonthValue());
        validRequest.setYear(now.getYear());
    }

    @Test
    void setBudget_Success() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(expenseCategory));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                eq(1L), eq(1L), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(transactionRepository.getSpentAmountForBudget(
                eq(1L), eq(1L), anyInt(), anyInt())).thenReturn(BigDecimal.ZERO);

        // Act
        var response = budgetService.setBudget(validRequest, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getBudgetAmount());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void setBudget_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.setBudget(validRequest, testUser);
        });
    }

    @Test
    void setBudget_IncomeCategoryNotAllowed() {
        // Arrange
        expenseCategory.setType(CategoryType.INCOME);
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(expenseCategory));

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            budgetService.setBudget(validRequest, testUser);
        });
    }

    @Test
    void setBudget_PastMonthNotAllowed() {
        // Arrange
        validRequest.setMonth(LocalDate.now().getMonthValue() - 1);
        // No need to stub categoryRepository as validation happens before that

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            budgetService.setBudget(validRequest, testUser);
        });
    }
}

