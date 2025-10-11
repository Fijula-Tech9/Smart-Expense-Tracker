package com.expense.tracker.repository;

import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Repository
 * 
 * Provides data access operations for Transaction entities including
 * complex filtering, pagination, and aggregate queries for reports.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find user's active transactions with pagination
     */
    Page<Transaction> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    /**
     * Find active transaction by ID and user
     */
    Optional<Transaction> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);
    
    /**
     * Find transactions by user and type
     */
    Page<Transaction> findByUserIdAndTypeAndIsDeletedFalse(Long userId, TransactionType type, Pageable pageable);
    
    /**
     * Find transactions by user and category
     */
    Page<Transaction> findByUserIdAndCategoryIdAndIsDeletedFalse(Long userId, Long categoryId, Pageable pageable);
    
    /**
     * Find transactions by date range
     */
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false 
        AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    Page<Transaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    /**
     * Complex filtering query with multiple optional parameters
     */
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND (:type IS NULL OR t.type = :type)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:fromDate IS NULL OR t.transactionDate >= :fromDate)
        AND (:toDate IS NULL OR t.transactionDate <= :toDate)
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
    """)
    Page<Transaction> findTransactionsWithFilters(
        @Param("userId") Long userId,
        @Param("type") TransactionType type,
        @Param("categoryId") Long categoryId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );
    
    /**
     * Get monthly summary data
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpenses,
            COUNT(t) as transactionCount,
            COALESCE(AVG(t.amount), 0) as averageAmount
        FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND MONTH(t.transactionDate) = :month 
        AND YEAR(t.transactionDate) = :year
    """)
    Object[] getMonthlySummary(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    
    /**
     * Get category-wise expense report
     */
    @Query("""
        SELECT 
            c.id,
            c.name,
            COALESCE(SUM(t.amount), 0) as totalAmount,
            COUNT(t) as transactionCount
        FROM Transaction t 
        JOIN t.category c
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND t.type = 'EXPENSE'
        AND MONTH(t.transactionDate) = :month 
        AND YEAR(t.transactionDate) = :year
        GROUP BY c.id, c.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<Object[]> getCategoryWiseExpenseReport(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    
    /**
     * Get trends data for multiple months
     */
    @Query("""
        SELECT 
            MONTH(t.transactionDate) as month,
            YEAR(t.transactionDate) as year,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpenses
        FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND t.transactionDate >= :startDate
        GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
        ORDER BY YEAR(t.transactionDate) DESC, MONTH(t.transactionDate) DESC
    """)
    List<Object[]> getTrendsData(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Get top expenses for a month
     */
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND t.type = 'EXPENSE'
        AND MONTH(t.transactionDate) = :month 
        AND YEAR(t.transactionDate) = :year
        ORDER BY t.amount DESC
    """)
    List<Transaction> getTopExpenses(@Param("userId") Long userId, @Param("month") int month, 
                                   @Param("year") int year, Pageable pageable);
    
    /**
     * Calculate spent amount for budget (specific category and month)
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.category.id = :categoryId
        AND t.type = 'EXPENSE'
        AND t.isDeleted = false
        AND MONTH(t.transactionDate) = :month 
        AND YEAR(t.transactionDate) = :year
    """)
    BigDecimal getSpentAmountForBudget(@Param("userId") Long userId, @Param("categoryId") Long categoryId, 
                                      @Param("month") int month, @Param("year") int year);
    
    /**
     * Get largest expense for a month
     */
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.id = :userId 
        AND t.isDeleted = false
        AND t.type = 'EXPENSE'
        AND MONTH(t.transactionDate) = :month 
        AND YEAR(t.transactionDate) = :year
        ORDER BY t.amount DESC
        LIMIT 1
    """)
    Optional<Transaction> getLargestExpenseForMonth(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
}
