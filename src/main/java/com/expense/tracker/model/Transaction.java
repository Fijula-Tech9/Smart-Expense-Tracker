package com.expense.tracker.model;

import com.expense.tracker.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Entity - Represents a financial transaction (income or expense)
 * 
 * This entity stores all transaction details including amount, category, date,
 * and optional description and payment method information.
 */
@Entity
@Table(name = "transactions", 
    indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_user_type_date", columnList = "user_id, type, transaction_date"),
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_user_deleted", columnList = "user_id, is_deleted"),
        @Index(name = "idx_amount", columnList = "amount")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_user"))
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_category"))
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    /**
     * Constructor for creating a new transaction
     */
    public Transaction(User user, Category category, TransactionType type, 
                      BigDecimal amount, LocalDate transactionDate) {
        this.user = user;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.isDeleted = false;
    }
    
    /**
     * Constructor for creating a new transaction with optional fields
     */
    public Transaction(User user, Category category, TransactionType type, 
                      BigDecimal amount, LocalDate transactionDate, 
                      String description, String paymentMethod) {
        this(user, category, type, amount, transactionDate);
        this.description = description;
        this.paymentMethod = paymentMethod;
    }
}
