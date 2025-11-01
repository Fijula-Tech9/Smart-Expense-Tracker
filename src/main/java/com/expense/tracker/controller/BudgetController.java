package com.expense.tracker.controller;

import com.expense.tracker.dto.request.BudgetRequest;
import com.expense.tracker.dto.response.BudgetAlertResponse;
import com.expense.tracker.dto.response.BudgetResponse;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.BudgetService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Budget Controller
 * 
 * RESTful API endpoints for managing budgets including
 * setting budgets, viewing budgets, and receiving alerts.
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budgets", description = "Budget management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    /**
     * Set or update budget for a category
     */
    @PostMapping
    @Operation(
        summary = "Set budget",
        description = "Sets or updates a budget for a specific expense category and month/year. Returns budget with calculated spent amount."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Budget set successfully",
            content = @Content(schema = @Schema(implementation = BudgetResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<BudgetResponse> setBudget(@Valid @RequestBody BudgetRequest request) {
        User user = getCurrentUser();
        BudgetResponse response = budgetService.setBudget(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all budgets for current or specified month
     */
    @GetMapping
    @Operation(
        summary = "Get all budgets",
        description = "Retrieves all budgets for the user. Defaults to current month/year if not specified."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Budgets retrieved successfully",
            content = @Content(schema = @Schema(implementation = BudgetResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        User user = getCurrentUser();
        List<BudgetResponse> budgets = budgetService.getBudgets(user.getId(), month, year);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budget by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get budget by ID",
        description = "Retrieves a specific budget by its ID with calculated spent amount."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Budget retrieved successfully",
            content = @Content(schema = @Schema(implementation = BudgetResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        User user = getCurrentUser();
        BudgetResponse response = budgetService.getBudgetById(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update budget amount
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update budget amount",
        description = "Updates the budget amount for a specific budget."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Budget updated successfully",
            content = @Content(schema = @Schema(implementation = BudgetResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @RequestParam @jakarta.validation.constraints.NotNull 
                           @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
                           @jakarta.validation.constraints.Digits(integer = 13, fraction = 2, message = "Budget amount format is invalid")
                           BigDecimal budgetAmount) {
        User user = getCurrentUser();
        BudgetResponse response = budgetService.updateBudget(id, budgetAmount, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete budget
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete budget",
        description = "Deletes a budget. No alerts will be generated after deletion."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budget deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        User user = getCurrentUser();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Get budget alerts for current month
     */
    @GetMapping("/alerts")
    @Operation(
        summary = "Get budget alerts",
        description = "Retrieves budget alerts for the current month. Returns alerts when budgets are 80%+ used, 100% reached, or exceeded."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Budget alerts retrieved successfully",
            content = @Content(schema = @Schema(implementation = BudgetAlertResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<List<BudgetAlertResponse>> getBudgetAlerts() {
        User user = getCurrentUser();
        List<BudgetAlertResponse> alerts = budgetService.getBudgetAlerts(user);
        return ResponseEntity.ok(alerts);
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

