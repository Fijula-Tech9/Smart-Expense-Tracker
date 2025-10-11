package com.expense.tracker.repository;

import com.expense.tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Budget Repository
 * 
 * Provides data access operations for Budget entities including
 * budget management and alert calculations.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all budgets for a user in a specific month/year
     */
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    
    /**
     * Find budget by user, category, month, and year
     */
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
    
    /**
     * Find budget by ID and user (for update/delete operations)
     */
    Optional<Budget> findByIdAndUserId(Long budgetId, Long userId);
    
    /**
     * Find all user's budgets
     */
    List<Budget> findByUserId(Long userId);
    
    /**
     * Get budgets with spent amounts for alerts calculation
     */
    @Query("""
        SELECT 
            b.id,
            b.budgetAmount,
            c.id,
            c.name,
            COALESCE(SUM(t.amount), 0) as spentAmount,
            b.month,
            b.year
        FROM Budget b 
        JOIN b.category c
        LEFT JOIN Transaction t ON (
            t.category.id = c.id 
            AND t.user.id = b.user.id 
            AND t.type = 'EXPENSE'
            AND t.isDeleted = false
            AND MONTH(t.transactionDate) = b.month 
            AND YEAR(t.transactionDate) = b.year
        )
        WHERE b.user.id = :userId 
        AND b.month = :month 
        AND b.year = :year
        GROUP BY b.id, b.budgetAmount, c.id, c.name, b.month, b.year
        ORDER BY c.name
    """)
    List<Object[]> getBudgetsWithSpentAmounts(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    
    /**
     * Check if budget exists for category in month/year
     */
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
    
    /**
     * Get current month budgets for alerts
     */
    @Query("""
        SELECT 
            b.id,
            b.budgetAmount,
            c.id,
            c.name,
            COALESCE(SUM(t.amount), 0) as spentAmount
        FROM Budget b 
        JOIN b.category c
        LEFT JOIN Transaction t ON (
            t.category.id = c.id 
            AND t.user.id = b.user.id 
            AND t.type = 'EXPENSE'
            AND t.isDeleted = false
            AND MONTH(t.transactionDate) = MONTH(CURRENT_DATE) 
            AND YEAR(t.transactionDate) = YEAR(CURRENT_DATE)
        )
        WHERE b.user.id = :userId 
        AND b.month = MONTH(CURRENT_DATE)
        AND b.year = YEAR(CURRENT_DATE)
        GROUP BY b.id, b.budgetAmount, c.id, c.name
        HAVING (COALESCE(SUM(t.amount), 0) / b.budgetAmount) >= 0.8
        ORDER BY (COALESCE(SUM(t.amount), 0) / b.budgetAmount) DESC
    """)
    List<Object[]> getBudgetAlertsForCurrentMonth(@Param("userId") Long userId);
}
