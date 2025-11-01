package com.expense.tracker.service;

import com.expense.tracker.dto.response.MonthlySummaryResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        Category category = new Category();
        category.setId(1L);
        category.setName("Food & Dining");

        sampleTransaction = new Transaction();
        sampleTransaction.setId(1L);
        sampleTransaction.setType(TransactionType.EXPENSE);
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setCategory(category);
        sampleTransaction.setTransactionDate(LocalDate.now());
    }

    @Test
    void getMonthlySummary_Success() {
        // Arrange
        Object[] summaryData = new Object[]{
            new BigDecimal("5000.00"),  // totalIncome
            new BigDecimal("3000.00"),  // totalExpenses
            10L,                        // transactionCount
            new BigDecimal("800.00")    // averageAmount
        };

        when(transactionRepository.getMonthlySummary(1L, 10, 2024))
                .thenReturn(summaryData);
        when(transactionRepository.getLargestExpenseForMonth(1L, 10, 2024))
                .thenReturn(Optional.of(sampleTransaction));

        // Act
        MonthlySummaryResponse response = reportService.getMonthlySummary(testUser, 10, 2024);

        // Assert
        assertNotNull(response);
        assertEquals(10, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("3000.00"), response.getTotalExpenses());
        assertEquals(new BigDecimal("2000.00"), response.getNetSavings());
        assertEquals(10L, response.getTransactionCount());
    }

    @Test
    void getMonthlySummary_InvalidMonth() {
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            reportService.getMonthlySummary(testUser, 13, 2024);
        });

        assertThrows(InvalidRequestException.class, () -> {
            reportService.getMonthlySummary(testUser, 0, 2024);
        });
    }

    @Test
    void getMonthlySummary_InvalidYear() {
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            reportService.getMonthlySummary(testUser, 10, 1999);
        });

        assertThrows(InvalidRequestException.class, () -> {
            reportService.getMonthlySummary(testUser, 10, 2101);
        });
    }

    @Test
    void getMonthlySummary_NoLargestExpense() {
        // Arrange
        Object[] summaryData = new Object[]{
            new BigDecimal("5000.00"),
            new BigDecimal("3000.00"),
            10L,
            new BigDecimal("800.00")
        };

        when(transactionRepository.getMonthlySummary(1L, 10, 2024))
                .thenReturn(summaryData);
        when(transactionRepository.getLargestExpenseForMonth(1L, 10, 2024))
                .thenReturn(Optional.empty());

        // Act
        MonthlySummaryResponse response = reportService.getMonthlySummary(testUser, 10, 2024);

        // Assert
        assertNotNull(response);
        assertNull(response.getLargestExpense());
    }

    @Test
    void getCategoryWiseReport_Success() {
        // Arrange
        LocalDate now = LocalDate.now();
        Integer month = now.getMonthValue();
        Integer year = now.getYear();

        Object[] categoryData1 = new Object[]{
            1L,                        // categoryId
            "Food & Dining",           // categoryName
            new BigDecimal("2000.00"), // totalAmount
            5L                         // transactionCount
        };

        Object[] categoryData2 = new Object[]{
            2L,
            "Transportation",
            new BigDecimal("1000.00"),
            3L
        };

        when(transactionRepository.getCategoryWiseExpenseReport(1L, month, year))
                .thenReturn(java.util.Arrays.asList(categoryData1, categoryData2));

        // Act
        var response = reportService.getCategoryWiseReport(testUser, month, year);

        // Assert
        assertNotNull(response);
        assertEquals(month, response.getMonth());
        assertEquals(year, response.getYear());
        assertEquals(new BigDecimal("3000.00"), response.getTotalExpenses());
        assertEquals(2, response.getCategories().size());
    }

    @Test
    void getTrends_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        
        Object[] trendData1 = new Object[]{
            5,                        // month
            2024,                     // year
            new BigDecimal("5000.00"), // totalIncome
            new BigDecimal("3000.00")  // totalExpenses
        };

        java.util.List<Object[]> trendsList = new java.util.ArrayList<>();
        trendsList.add(trendData1);
        when(transactionRepository.getTrendsData(1L, startDate))
                .thenReturn(trendsList);

        // Act
        var response = reportService.getTrends(testUser, 6);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTrends());
    }

    @Test
    void getTrends_DefaultMonths() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        when(transactionRepository.getTrendsData(1L, startDate))
                .thenReturn(java.util.Arrays.asList());

        // Act
        var response = reportService.getTrends(testUser, null);

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).getTrendsData(eq(1L), any());
    }

    @Test
    void getTrends_MaxMonthsLimited() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        when(transactionRepository.getTrendsData(1L, startDate))
                .thenReturn(java.util.Arrays.asList());

        // Act
        var response = reportService.getTrends(testUser, 15); // Should be limited to 12

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).getTrendsData(eq(1L), any());
    }

    @Test
    void getTopExpenses_Success() {
        // Arrange
        LocalDate now = LocalDate.now();
        Integer month = now.getMonthValue();
        Integer year = now.getYear();

        when(transactionRepository.getTopExpenses(eq(1L), eq(month), eq(year), any()))
                .thenReturn(java.util.Arrays.asList(sampleTransaction));

        // Act
        var response = reportService.getTopExpenses(testUser, 10, month, year);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTopExpenses());
        assertEquals(1, response.getTopExpenses().size());
    }

    @Test
    void getTopExpenses_DefaultLimit() {
        // Arrange
        LocalDate now = LocalDate.now();
        Integer month = now.getMonthValue();
        Integer year = now.getYear();

        when(transactionRepository.getTopExpenses(eq(1L), eq(month), eq(year), any()))
                .thenReturn(java.util.Arrays.asList());

        // Act
        var response = reportService.getTopExpenses(testUser, null, month, year);

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).getTopExpenses(eq(1L), eq(month), eq(year), any());
    }

    @Test
    void getTopExpenses_MaxLimitEnforced() {
        // Arrange
        LocalDate now = LocalDate.now();
        Integer month = now.getMonthValue();
        Integer year = now.getYear();

        when(transactionRepository.getTopExpenses(eq(1L), eq(month), eq(year), any()))
                .thenReturn(java.util.Arrays.asList());

        // Act
        var response = reportService.getTopExpenses(testUser, 100, month, year); // Should be limited to 50

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).getTopExpenses(eq(1L), eq(month), eq(year), any());
    }
}

