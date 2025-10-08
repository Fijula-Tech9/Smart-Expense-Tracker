package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Trends Response DTO
 * 
 * Used to return income vs expense trends over multiple months.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendsResponse {
    
    private List<TrendData> trends;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private Integer month;
        private Integer year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netSavings;
        
        /**
         * Constructor that calculates net savings
         */
        public TrendData(Integer month, Integer year, BigDecimal totalIncome, BigDecimal totalExpenses) {
            this.month = month;
            this.year = year;
            this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
            this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
            this.netSavings = this.totalIncome.subtract(this.totalExpenses);
        }
    }
}
