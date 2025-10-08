package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Category Wise Response DTO
 * 
 * Used to return category-wise expense breakdown report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWiseResponse {
    
    private Integer month;
    private Integer year;
    private BigDecimal totalExpenses;
    private List<CategoryExpenseData> categories;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpenseData {
        private Long categoryId;
        private String categoryName;
        private BigDecimal totalAmount;
        private Long transactionCount;
        private Double percentageOfTotal;
        
        /**
         * Constructor that calculates percentage
         */
        public CategoryExpenseData(Long categoryId, String categoryName, 
                                  BigDecimal totalAmount, Long transactionCount, 
                                  BigDecimal totalExpenses) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.transactionCount = transactionCount != null ? transactionCount : 0L;
            
            // Calculate percentage
            if (totalExpenses != null && totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                this.percentageOfTotal = this.totalAmount.divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP)
                                                        .multiply(BigDecimal.valueOf(100))
                                                        .doubleValue();
            } else {
                this.percentageOfTotal = 0.0;
            }
        }
    }
}
