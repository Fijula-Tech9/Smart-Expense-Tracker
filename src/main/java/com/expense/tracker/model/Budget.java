package com.expense.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Budget Entity - Represents a monthly budget for a specific expense category
 * 
 * Users can set budget limits for expense categories to track their spending.
 * The system calculates spent amounts and generates alerts when limits are approached.
 */
@Entity
@Table(name = "budgets", 
    indexes = {
        @Index(name = "idx_user_month_year", columnList = "user_id, month, year"),
        @Index(name = "idx_category", columnList = "category_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_category_month", 
                         columnNames = {"user_id", "category_id", "month", "year"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_user"))
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_category"))
    private Category category;
    
    @Column(name = "budget_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetAmount;
    
    @Column(name = "month", nullable = false)
    private Integer month; // 1-12
    
    @Column(name = "year", nullable = false) 
    private Integer year;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating a new budget
     */
    public Budget(User user, Category category, BigDecimal budgetAmount, 
                 Integer month, Integer year) {
        this.user = user;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
    }
}
