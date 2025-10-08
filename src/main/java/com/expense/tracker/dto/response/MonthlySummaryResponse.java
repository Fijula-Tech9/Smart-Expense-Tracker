package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monthly Summary Response DTO
 * 
 * Used to return comprehensive monthly financial summary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {
    
    private Integer month;
    private Integer year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private Long transactionCount;
    private BigDecimal averageTransactionAmount;
    private TransactionResponse largestExpense;
    
    /**
     * Constructor for basic summary (without largest expense)
     */
    public MonthlySummaryResponse(Integer month, Integer year, BigDecimal totalIncome,
                                 BigDecimal totalExpenses, Long transactionCount,
                                 BigDecimal averageTransactionAmount) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        this.netSavings = this.totalIncome.subtract(this.totalExpenses);
        this.transactionCount = transactionCount != null ? transactionCount : 0L;
        this.averageTransactionAmount = averageTransactionAmount != null ? averageTransactionAmount : BigDecimal.ZERO;
    }
}
