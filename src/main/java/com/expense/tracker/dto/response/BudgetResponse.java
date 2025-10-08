package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Budget Response DTO
 * 
 * Used to return budget information with calculated spending data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageUsed;
    private Integer month;
    private Integer year;
    
    /**
     * Constructor that calculates derived fields
     */
    public BudgetResponse(Long id, Long categoryId, String categoryName, 
                         BigDecimal budgetAmount, BigDecimal spentAmount, 
                         Integer month, Integer year) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        this.month = month;
        this.year = year;
        
        // Calculate derived fields
        this.remainingAmount = this.budgetAmount.subtract(this.spentAmount);
        if (this.budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.percentageUsed = this.spentAmount.divide(this.budgetAmount, 4, java.math.RoundingMode.HALF_UP)
                                                 .multiply(BigDecimal.valueOf(100))
                                                 .doubleValue();
        } else {
            this.percentageUsed = 0.0;
        }
    }
}
