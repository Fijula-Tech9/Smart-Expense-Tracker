package com.expense.tracker.enums;

/**
 * Budget Alert Type Enum
 * 
 * Defines the different levels of budget alerts based on spending percentage:
 * - WARNING: 80-99% of budget used
 * - LIMIT_REACHED: Exactly 100% of budget used  
 * - EXCEEDED: More than 100% of budget used
 */
public enum AlertType {
    WARNING,        // 80-99% used
    LIMIT_REACHED,  // 100% used
    EXCEEDED        // > 100% used
}
