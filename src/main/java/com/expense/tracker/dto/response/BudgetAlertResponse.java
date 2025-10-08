package com.expense.tracker.dto.response;

import com.expense.tracker.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Budget Alert Response DTO
 * 
 * Used to return budget alert information when spending exceeds thresholds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertResponse {
    
    private Long categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private Double percentageUsed;
    private AlertType alertType;
    private String message;
    
    /**
     * Constructor that determines alert type and generates message
     */
    public BudgetAlertResponse(Long categoryId, String categoryName, 
                              BigDecimal budgetAmount, BigDecimal spentAmount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        
        // Calculate percentage
        if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.percentageUsed = spentAmount.divide(budgetAmount, 4, java.math.RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100))
                                            .doubleValue();
        } else {
            this.percentageUsed = 0.0;
        }
        
        // Determine alert type and message
        if (percentageUsed >= 100.0) {
            if (percentageUsed > 100.0) {
                this.alertType = AlertType.EXCEEDED;
                BigDecimal excess = spentAmount.subtract(budgetAmount);
                this.message = String.format("You have exceeded your %s budget by ₹%.2f", 
                                           categoryName, excess.doubleValue());
            } else {
                this.alertType = AlertType.LIMIT_REACHED;
                this.message = String.format("You have reached your %s budget limit", categoryName);
            }
        } else if (percentageUsed >= 80.0) {
            this.alertType = AlertType.WARNING;
            this.message = String.format("You have used %.1f%% of your %s budget", 
                                       percentageUsed, categoryName);
        }
    }
}
