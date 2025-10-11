package com.expense.tracker.repository;

import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository
 * 
 * Provides data access operations for Category entities including
 * system categories and user-specific custom categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all system categories
     */
    List<Category> findByIsSystemCategoryTrue();
    
    /**
     * Find all system categories by type
     */
    List<Category> findByIsSystemCategoryTrueAndType(CategoryType type);
    
    /**
     * Find all categories available to a user (system + their custom categories)
     */
    @Query("""
        SELECT c FROM Category c 
        WHERE c.isSystemCategory = true 
        OR (c.isSystemCategory = false AND c.user.id = :userId)
        ORDER BY c.isSystemCategory DESC, c.name ASC
    """)
    List<Category> findAllAvailableToUser(@Param("userId") Long userId);
    
    /**
     * Find categories available to user by type
     */
    @Query("""
        SELECT c FROM Category c 
        WHERE (c.isSystemCategory = true OR c.user.id = :userId) 
        AND c.type = :type
        ORDER BY c.isSystemCategory DESC, c.name ASC
    """)
    List<Category> findAllAvailableToUserByType(@Param("userId") Long userId, @Param("type") CategoryType type);
    
    /**
     * Find user's custom categories
     */
    List<Category> findByUserIdAndIsSystemCategoryFalse(Long userId);
    
    /**
     * Find user's custom categories by type
     */
    List<Category> findByUserIdAndIsSystemCategoryFalseAndType(Long userId, CategoryType type);
    
    /**
     * Check if category name exists for user (including system categories)
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Category c 
        WHERE LOWER(c.name) = LOWER(:name) 
        AND (c.isSystemCategory = true OR c.user.id = :userId)
    """)
    boolean existsByNameForUser(@Param("name") String name, @Param("userId") Long userId);
    
    /**
     * Find category by ID that's available to user
     */
    @Query("""
        SELECT c FROM Category c 
        WHERE c.id = :categoryId 
        AND (c.isSystemCategory = true OR c.user.id = :userId)
    """)
    Optional<Category> findByIdAndAvailableToUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);
    
    /**
     * Find custom category by ID and user (for update/delete operations)
     */
    Optional<Category> findByIdAndUserIdAndIsSystemCategoryFalse(Long categoryId, Long userId);
    
    /**
     * Count transactions for a category (used before deletion)
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    Long countActiveTransactionsByCategory(@Param("categoryId") Long categoryId);
}
