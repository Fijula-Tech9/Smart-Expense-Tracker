package com.expense.tracker.service;

import com.expense.tracker.dto.request.BudgetRequest;
import com.expense.tracker.dto.response.BudgetAlertResponse;
import com.expense.tracker.dto.response.BudgetResponse;
import com.expense.tracker.enums.AlertType;
import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.Budget;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.BudgetRepository;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Budget Service
 * 
 * Handles business logic for budget management including
 * setting budgets, calculating spent amounts, and generating alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Set or update budget for a category
     */
    @Transactional
    public BudgetResponse setBudget(BudgetRequest request, User user) {
        log.info("Setting budget for user ID: {}, category ID: {}", user.getId(), request.getCategoryId());
        
        // Validate month and year
        LocalDate now = LocalDate.now();
        if (request.getYear() < now.getYear() || 
            (request.getYear() == now.getYear() && request.getMonth() < now.getMonthValue())) {
            throw new InvalidRequestException("Cannot set budget for past months");
        }
        
        // Validate category exists and is available to user
        Category category = categoryRepository.findByIdAndAvailableToUser(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not available"));
        
        // Budget can only be set for expense categories
        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidRequestException("Budget can only be set for expense categories");
        }
        
        // Check if budget already exists for this category and month
        Budget existingBudget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                    user.getId(), 
                    request.getCategoryId(), 
                    request.getMonth(), 
                    request.getYear()
                )
                .orElse(null);
        
        Budget budget;
        if (existingBudget != null) {
            // Update existing budget
            existingBudget.setBudgetAmount(request.getBudgetAmount());
            budget = budgetRepository.save(existingBudget);
            log.info("Budget updated for category ID: {}", request.getCategoryId());
        } else {
            // Create new budget
            budget = new Budget(user, category, request.getBudgetAmount(), request.getMonth(), request.getYear());
            budget = budgetRepository.save(budget);
            log.info("Budget created successfully with ID: {}", budget.getId());
        }
        
        // Calculate spent amount and return response
        BigDecimal spentAmount = transactionRepository.getSpentAmountForBudget(
            user.getId(), 
            request.getCategoryId(), 
            request.getMonth(), 
            request.getYear()
        );
        
        return new BudgetResponse(
            budget.getId(),
            budget.getCategory().getId(),
            budget.getCategory().getName(),
            budget.getBudgetAmount(),
            spentAmount,
            budget.getMonth(),
            budget.getYear()
        );
    }

    /**
     * Get all budgets for a user in a specific month/year
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year) {
        log.info("Fetching budgets for user ID: {}, month: {}, year: {}", userId, month, year);
        
        // Use current month/year if not provided
        LocalDate now = LocalDate.now();
        if (month == null) {
            month = now.getMonthValue();
        }
        if (year == null) {
            year = now.getYear();
        }
        
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        
        return budgets.stream()
                .map(budget -> {
                    BigDecimal spentAmount = transactionRepository.getSpentAmountForBudget(
                        userId,
                        budget.getCategory().getId(),
                        budget.getMonth(),
                        budget.getYear()
                    );
                    return new BudgetResponse(
                        budget.getId(),
                        budget.getCategory().getId(),
                        budget.getCategory().getName(),
                        budget.getBudgetAmount(),
                        spentAmount,
                        budget.getMonth(),
                        budget.getYear()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get budget by ID
     */
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long budgetId, User user) {
        log.info("Fetching budget ID: {} for user ID: {}", budgetId, user.getId());
        
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        
        BigDecimal spentAmount = transactionRepository.getSpentAmountForBudget(
            user.getId(),
            budget.getCategory().getId(),
            budget.getMonth(),
            budget.getYear()
        );
        
        return new BudgetResponse(
            budget.getId(),
            budget.getCategory().getId(),
            budget.getCategory().getName(),
            budget.getBudgetAmount(),
            spentAmount,
            budget.getMonth(),
            budget.getYear()
        );
    }

    /**
     * Update budget amount
     */
    @Transactional
    public BudgetResponse updateBudget(Long budgetId, BigDecimal budgetAmount, User user) {
        log.info("Updating budget ID: {} for user ID: {}", budgetId, user.getId());
        
        if (budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Budget amount must be greater than 0");
        }
        
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        
        budget.setBudgetAmount(budgetAmount);
        Budget updatedBudget = budgetRepository.save(budget);
        
        BigDecimal spentAmount = transactionRepository.getSpentAmountForBudget(
            user.getId(),
            updatedBudget.getCategory().getId(),
            updatedBudget.getMonth(),
            updatedBudget.getYear()
        );
        
        log.info("Budget updated successfully with ID: {}", updatedBudget.getId());
        
        return new BudgetResponse(
            updatedBudget.getId(),
            updatedBudget.getCategory().getId(),
            updatedBudget.getCategory().getName(),
            updatedBudget.getBudgetAmount(),
            spentAmount,
            updatedBudget.getMonth(),
            updatedBudget.getYear()
        );
    }

    /**
     * Delete budget
     */
    @Transactional
    public void deleteBudget(Long budgetId, User user) {
        log.info("Deleting budget ID: {} for user ID: {}", budgetId, user.getId());
        
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        
        budgetRepository.delete(budget);
        log.info("Budget deleted successfully with ID: {}", budgetId);
    }

    /**
     * Get budget alerts for current month
     */
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getBudgetAlerts(User user) {
        log.info("Fetching budget alerts for user ID: {}", user.getId());
        
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        List<Object[]> alertsData = budgetRepository.getBudgetsWithSpentAmounts(
            user.getId(), 
            currentMonth, 
            currentYear
        );
        
        List<BudgetAlertResponse> alerts = new ArrayList<>();
        
        for (Object[] data : alertsData) {
            Long budgetId = ((Number) data[0]).longValue();
            BigDecimal budgetAmount = (BigDecimal) data[1];
            Long categoryId = ((Number) data[2]).longValue();
            String categoryName = (String) data[3];
            BigDecimal spentAmount = ((BigDecimal) data[4]).compareTo(BigDecimal.ZERO) > 0 
                ? (BigDecimal) data[4] 
                : BigDecimal.ZERO;
            
            // Calculate percentage
            double percentageUsed = 0.0;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = spentAmount.divide(budgetAmount, 4, java.math.RoundingMode.HALF_UP)
                                           .multiply(BigDecimal.valueOf(100))
                                           .doubleValue();
            }
            
            // Generate alert only if >= 80%
            if (percentageUsed >= 80.0) {
                AlertType alertType;
                String message;
                
                if (percentageUsed > 100.0) {
                    alertType = AlertType.EXCEEDED;
                    BigDecimal excess = spentAmount.subtract(budgetAmount);
                    message = String.format("You have exceeded your %s budget by ₹%.2f", 
                                           categoryName, excess.doubleValue());
                } else if (percentageUsed == 100.0) {
                    alertType = AlertType.LIMIT_REACHED;
                    message = String.format("You have reached your %s budget limit", categoryName);
                } else {
                    alertType = AlertType.WARNING;
                    message = String.format("You have used %.1f%% of your %s budget", 
                                           percentageUsed, categoryName);
                }
                
                BudgetAlertResponse alert = new BudgetAlertResponse();
                alert.setCategoryId(categoryId);
                alert.setCategoryName(categoryName);
                alert.setBudgetAmount(budgetAmount);
                alert.setSpentAmount(spentAmount);
                alert.setPercentageUsed(percentageUsed);
                alert.setAlertType(alertType);
                alert.setMessage(message);
                
                alerts.add(alert);
            }
        }
        
        return alerts;
    }
}


