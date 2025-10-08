package com.expense.tracker.dto.response;

import com.expense.tracker.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Response DTO
 * 
 * Used to return transaction information including category details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private Long id;
    private TransactionType type;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
