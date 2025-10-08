package com.expense.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top Expenses Response DTO
 * 
 * Used to return top N expenses by amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopExpensesResponse {
    
    private List<TransactionResponse> topExpenses;
}
