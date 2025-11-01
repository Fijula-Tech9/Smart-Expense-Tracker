package com.expense.tracker.service;

import com.expense.tracker.dto.response.CategoryWiseResponse;
import com.expense.tracker.dto.response.MonthlySummaryResponse;
import com.expense.tracker.dto.response.TopExpensesResponse;
import com.expense.tracker.dto.response.TrendsResponse;
import com.expense.tracker.dto.response.TransactionResponse;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Report Service
 * 
 * Handles business logic for generating financial reports including
 * monthly summaries, category-wise breakdowns, trends, and top expenses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;

    /**
     * Get monthly summary report
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyReports", key = "#user.id + '-' + #month + '-' + #year")
    public MonthlySummaryResponse getMonthlySummary(User user, Integer month, Integer year) {
        log.info("Generating monthly summary for user ID: {}, month: {}, year: {}", user.getId(), month, year);
        
        // Validate month and year
        if (month < 1 || month > 12) {
            throw new InvalidRequestException("Month must be between 1 and 12");
        }
        
        if (year < 2000 || year > 2100) {
            throw new InvalidRequestException("Year must be between 2000 and 2100");
        }
        
        // Get summary data from repository
        Object[] summaryData = transactionRepository.getMonthlySummary(user.getId(), month, year);
        
        BigDecimal totalIncome = (BigDecimal) summaryData[0];
        BigDecimal totalExpenses = (BigDecimal) summaryData[1];
        Long transactionCount = ((Number) summaryData[2]).longValue();
        BigDecimal averageAmount = (BigDecimal) summaryData[3];
        
        // Get largest expense
        Transaction largestExpenseTransaction = transactionRepository
                .getLargestExpenseForMonth(user.getId(), month, year)
                .orElse(null);
        
        TransactionResponse largestExpense = null;
        if (largestExpenseTransaction != null) {
            largestExpense = mapToTransactionResponse(largestExpenseTransaction);
        }
        
        MonthlySummaryResponse response = new MonthlySummaryResponse(
            month, year, totalIncome, totalExpenses, transactionCount, averageAmount
        );
        response.setLargestExpense(largestExpense);
        
        return response;
    }

    /**
     * Get category-wise expense report
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryWiseReports", key = "#user.id + '-' + #month + '-' + #year")
    public CategoryWiseResponse getCategoryWiseReport(User user, Integer month, Integer year) {
        log.info("Generating category-wise report for user ID: {}, month: {}, year: {}", user.getId(), month, year);
        
        // Use current month/year if not provided
        LocalDate now = LocalDate.now();
        if (month == null) {
            month = now.getMonthValue();
        }
        if (year == null) {
            year = now.getYear();
        }
        
        // Get category-wise data
        List<Object[]> categoryData = transactionRepository.getCategoryWiseExpenseReport(user.getId(), month, year);
        
        // Calculate total expenses
        BigDecimal totalExpenses = categoryData.stream()
                .map(data -> (BigDecimal) data[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Map to response DTOs
        List<CategoryWiseResponse.CategoryExpenseData> categories = categoryData.stream()
                .map(data -> {
                    Long categoryId = ((Number) data[0]).longValue();
                    String categoryName = (String) data[1];
                    BigDecimal totalAmount = (BigDecimal) data[2];
                    Long transactionCount = ((Number) data[3]).longValue();
                    
                    return new CategoryWiseResponse.CategoryExpenseData(
                        categoryId, categoryName, totalAmount, transactionCount, totalExpenses
                    );
                })
                .collect(Collectors.toList());
        
        return new CategoryWiseResponse(month, year, totalExpenses, categories);
    }

    /**
     * Get income vs expense trends
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "trendsReports", key = "#user.id + '-' + #months")
    public TrendsResponse getTrends(User user, Integer months) {
        log.info("Generating trends report for user ID: {}, months: {}", user.getId(), months);
        
        // Validate and limit months
        if (months == null || months < 1) {
            months = 6; // Default to 6 months
        }
        if (months > 12) {
            months = 12; // Max 12 months
        }
        
        // Calculate start date
        LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        
        // Get trends data
        List<Object[]> trendsData = transactionRepository.getTrendsData(user.getId(), startDate);
        
        // Map to response DTOs
        List<TrendsResponse.TrendData> trends = trendsData.stream()
                .map(data -> {
                    Integer month = ((Number) data[0]).intValue();
                    Integer year = ((Number) data[1]).intValue();
                    BigDecimal totalIncome = (BigDecimal) data[2];
                    BigDecimal totalExpenses = (BigDecimal) data[3];
                    
                    return new TrendsResponse.TrendData(month, year, totalIncome, totalExpenses);
                })
                .collect(Collectors.toList());
        
        return new TrendsResponse(trends);
    }

    /**
     * Get top expenses
     */
    @Transactional(readOnly = true)
    public TopExpensesResponse getTopExpenses(User user, Integer limit, Integer month, Integer year) {
        log.info("Generating top expenses report for user ID: {}, limit: {}, month: {}, year: {}", 
                user.getId(), limit, month, year);
        
        // Validate and set defaults
        if (limit == null || limit < 1) {
            limit = 10; // Default to 10
        }
        if (limit > 50) {
            limit = 50; // Max 50
        }
        
        // Use current month/year if not provided
        LocalDate now = LocalDate.now();
        if (month == null) {
            month = now.getMonthValue();
        }
        if (year == null) {
            year = now.getYear();
        }
        
        // Get top expenses
        List<Transaction> topTransactions = transactionRepository.getTopExpenses(
            user.getId(), month, year, PageRequest.of(0, limit)
        );
        
        // Map to response DTOs
        List<TransactionResponse> topExpenses = topTransactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
        
        return new TopExpensesResponse(topExpenses);
    }

    /**
     * Map Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setDescription(transaction.getDescription());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}


