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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category systemCategory;
    private Category customCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        systemCategory = new Category();
        systemCategory.setId(1L);
        systemCategory.setName("Food & Dining");
        systemCategory.setType(CategoryType.EXPENSE);
        systemCategory.setIsSystemCategory(true);

        customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("Pet Expenses");
        customCategory.setType(CategoryType.EXPENSE);
        customCategory.setIsSystemCategory(false);
        customCategory.setUser(testUser);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Custom Category");
        categoryRequest.setType(CategoryType.EXPENSE);
    }

    @Test
    void getAllCategories_Success() {
        // Arrange
        List<Category> categories = Arrays.asList(systemCategory, customCategory);
        when(categoryRepository.findAllAvailableToUser(1L)).thenReturn(categories);

        // Act
        List<CategoryResponse> result = categoryService.getAllCategories(1L, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAllAvailableToUser(1L);
    }

    @Test
    void getAllCategories_WithTypeFilter() {
        // Arrange
        List<Category> categories = Arrays.asList(systemCategory, customCategory);
        when(categoryRepository.findAllAvailableToUserByType(1L, CategoryType.EXPENSE))
                .thenReturn(categories);

        // Act
        List<CategoryResponse> result = categoryService.getAllCategories(1L, CategoryType.EXPENSE);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAllAvailableToUserByType(1L, CategoryType.EXPENSE);
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.of(systemCategory));

        // Act
        CategoryResponse response = categoryService.getCategoryById(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food & Dining", response.getName());
    }

    @Test
    void getCategoryById_NotFound() {
        // Arrange
        when(categoryRepository.findByIdAndAvailableToUser(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryById(1L, 1L);
        });
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryRepository.existsByNameForUser("Custom Category", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(3L);
            return cat;
        });

        // Act
        CategoryResponse response = categoryService.createCategory(categoryRequest, testUser);

        // Assert
        assertNotNull(response);
        assertEquals("Custom Category", response.getName());
        assertFalse(response.getIsSystemCategory());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName() {
        // Arrange
        when(categoryRepository.existsByNameForUser("Custom Category", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            categoryService.createCategory(categoryRequest, testUser);
        });

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Category");
        updateRequest.setType(CategoryType.EXPENSE);

        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(2L, 1L))
                .thenReturn(Optional.of(customCategory));
    // This stubbing is used indirectly; mark as lenient to avoid UnnecessaryStubbingException
    lenient().when(categoryRepository.existsByNameForUser("Updated Category", 1L)).thenReturn(false);
        when(categoryRepository.countActiveTransactionsByCategory(2L)).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setName("Updated Category");
            return cat;
        });

        // Act
        CategoryResponse response = categoryService.updateCategory(2L, updateRequest, testUser);

        // Assert
        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_SystemCategory() {
        // Arrange
        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(1L, 1L))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(systemCategory));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            categoryService.updateCategory(1L, categoryRequest, testUser);
        });
    }

    @Test
    void updateCategory_HasTransactions() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Category");
        updateRequest.setType(CategoryType.INCOME); // Different type

        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(2L, 1L))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.countActiveTransactionsByCategory(2L)).thenReturn(5L);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            categoryService.updateCategory(2L, updateRequest, testUser);
        });
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(2L, 1L))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.countActiveTransactionsByCategory(2L)).thenReturn(0L);

        // Act
        categoryService.deleteCategory(2L, testUser);

        // Assert
        verify(categoryRepository, times(1)).delete(customCategory);
    }

    @Test
    void deleteCategory_HasTransactions() {
        // Arrange
        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(2L, 1L))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.countActiveTransactionsByCategory(2L)).thenReturn(5L);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            categoryService.deleteCategory(2L, testUser);
        });

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_SystemCategory() {
        // Arrange
        when(categoryRepository.findByIdAndUserIdAndIsSystemCategoryFalse(1L, 1L))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(systemCategory));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            categoryService.deleteCategory(1L, testUser);
        });
    }
}

