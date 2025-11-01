package com.expense.tracker.service;

import com.expense.tracker.dto.request.CategoryRequest;
import com.expense.tracker.dto.response.CategoryResponse;
import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.ForbiddenException;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category Service
 * 
 * Handles business logic for category management including
 * viewing system and custom categories, and CRUD operations for custom categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories available to user (system + custom)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long userId, CategoryType type) {
        log.info("Fetching categories for user ID: {}, type: {}", userId, type);
        
        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.findAllAvailableToUserByType(userId, type);
        } else {
            categories = categoryRepository.findAllAvailableToUser(userId);
        }
        
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId, Long userId) {
        log.info("Fetching category ID: {} for user ID: {}", categoryId, userId);
        
        Category category = categoryRepository.findByIdAndAvailableToUser(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not available"));
        
        return mapToResponse(category);
    }

    /**
     * Create custom category
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        log.info("Creating custom category for user ID: {}", user.getId());
        
        // Check if category name already exists for this user (including system categories)
        if (categoryRepository.existsByNameForUser(request.getName(), user.getId())) {
            throw new DuplicateResourceException(
                String.format("Category with name '%s' already exists", request.getName())
            );
        }
        
        // Create new custom category
        Category category = new Category(request.getName(), request.getType(), user);
        Category savedCategory = categoryRepository.save(category);
        
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return mapToResponse(savedCategory);
    }

    /**
     * Update custom category
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, User user) {
        log.info("Updating category ID: {} for user ID: {}", categoryId, user.getId());
        
        // Find category (must be custom, not system)
        Category category = categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(categoryId, user.getId())
                .orElseThrow(() -> {
                    // Check if it exists but is system category
                    if (categoryRepository.findById(categoryId).isPresent()) {
                        throw new ForbiddenException("Cannot update system categories");
                    }
                    return new ResourceNotFoundException("Category not found");
                });
        
        // Check if new name conflicts with existing category
        if (!category.getName().equalsIgnoreCase(request.getName()) && 
            categoryRepository.existsByNameForUser(request.getName(), user.getId())) {
            throw new DuplicateResourceException(
                String.format("Category with name '%s' already exists", request.getName())
            );
        }
        
        // Update category (name can be changed, type should remain same for data integrity)
        // We allow type change only if no transactions exist
        if (!category.getType().equals(request.getType())) {
            Long transactionCount = categoryRepository.countActiveTransactionsByCategory(categoryId);
            if (transactionCount > 0) {
                throw new InvalidRequestException(
                    "Cannot change category type as it has existing transactions"
                );
            }
        }
        
        category.setName(request.getName());
        category.setType(request.getType());
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        
        return mapToResponse(updatedCategory);
    }

    /**
     * Delete custom category
     */
    @Transactional
    public void deleteCategory(Long categoryId, User user) {
        log.info("Deleting category ID: {} for user ID: {}", categoryId, user.getId());
        
        // Find category (must be custom, not system)
        Category category = categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(categoryId, user.getId())
                .orElseThrow(() -> {
                    // Check if it exists but is system category
                    if (categoryRepository.findById(categoryId).isPresent()) {
                        throw new ForbiddenException("Cannot delete system categories");
                    }
                    return new ResourceNotFoundException("Category not found");
                });
        
        // Check if category has active transactions
        Long transactionCount = categoryRepository.countActiveTransactionsByCategory(categoryId);
        if (transactionCount > 0) {
            throw new InvalidRequestException(
                String.format("Cannot delete category with %d existing transaction(s)", transactionCount)
            );
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", categoryId);
    }

    /**
     * Map Category entity to CategoryResponse DTO
     */
    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setType(category.getType());
        response.setIsSystemCategory(category.getIsSystemCategory());
        return response;
    }
}


