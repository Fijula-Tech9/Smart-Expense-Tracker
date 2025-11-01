package com.expense.tracker.controller;

import com.expense.tracker.dto.request.CategoryRequest;
import com.expense.tracker.dto.response.CategoryResponse;
import com.expense.tracker.enums.CategoryType;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category Controller
 * 
 * RESTful API endpoints for managing categories including
 * viewing system categories and managing custom categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    /**
     * Get all categories (system + user's custom categories)
     */
    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Retrieves all categories available to the user including system categories and user's custom categories. Optionally filter by type."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) CategoryType type) {
        User user = getCurrentUser();
        List<CategoryResponse> categories = categoryService.getAllCategories(user.getId(), type);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a specific category by its ID. Returns 404 if not found or not available to user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category retrieved successfully",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        User user = getCurrentUser();
        CategoryResponse response = categoryService.getCategoryById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Create custom category
     */
    @PostMapping
    @Operation(
        summary = "Create custom category",
        description = "Creates a new custom category for the user. Category name must be unique per user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Category created successfully",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        User user = getCurrentUser();
        CategoryResponse response = categoryService.createCategory(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update custom category
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update custom category",
        description = "Updates a custom category. System categories cannot be updated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category updated successfully",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update system categories"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        User user = getCurrentUser();
        CategoryResponse response = categoryService.updateCategory(id, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete custom category
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete custom category",
        description = "Deletes a custom category. System categories cannot be deleted. Category must not have any transactions."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Category has existing transactions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete system categories"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        User user = getCurrentUser();
        categoryService.deleteCategory(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Get current authenticated user from Security Context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}


