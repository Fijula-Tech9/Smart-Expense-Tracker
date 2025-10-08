package com.expense.tracker.model;

import com.expense.tracker.enums.CategoryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Entity - Represents income and expense categories
 * 
 * Categories can be:
 * 1. System categories (predefined, available to all users)
 * 2. Custom categories (created by individual users)
 */
@Entity
@Table(name = "categories", 
    indexes = {
        @Index(name = "idx_user_type", columnList = "user_id, type"),
        @Index(name = "idx_system_category", columnList = "is_system_category, type")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_category", columnNames = {"user_id", "name"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;
    
    @Column(name = "is_system_category", nullable = false)
    private Boolean isSystemCategory = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_category_user"))
    private User user; // null for system categories
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Budget> budgets;
    
    /**
     * Constructor for system categories
     */
    public Category(String name, CategoryType type) {
        this.name = name;
        this.type = type;
        this.isSystemCategory = true;
        this.user = null;
    }
    
    /**
     * Constructor for custom user categories
     */
    public Category(String name, CategoryType type, User user) {
        this.name = name;
        this.type = type;
        this.isSystemCategory = false;
        this.user = user;
    }
}
