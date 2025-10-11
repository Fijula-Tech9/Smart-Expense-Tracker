package com.expense.tracker.repository;

import com.expense.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * 
 * Provides data access operations for User entities including
 * authentication queries and user management operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address (used for login)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find active user by email address
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    /**
     * Check if email already exists (for registration validation)
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by ID and ensure they are active
     */
    Optional<User> findByIdAndIsActiveTrue(Long id);
    
    /**
     * Count total active users (for admin statistics)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    /**
     * Find user with their transactions count (for profile info)
     */
    @Query("""
        SELECT u, COUNT(t) as transactionCount 
        FROM User u 
        LEFT JOIN u.transactions t 
        WHERE u.id = :userId AND u.isActive = true 
        AND (t.isDeleted = false OR t.isDeleted IS NULL)
        GROUP BY u
    """)
    Optional<Object[]> findUserWithTransactionCount(@Param("userId") Long userId);
}
