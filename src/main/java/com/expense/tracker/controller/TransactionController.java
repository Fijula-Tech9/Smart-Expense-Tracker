package com.expense.tracker.controller;

import com.expense.tracker.dto.request.TransactionRequest;
import com.expense.tracker.dto.response.PagedResponse;
import com.expense.tracker.dto.response.TransactionResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.UserDetailsServiceImpl;
import com.expense.tracker.service.TransactionService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction Controller
 * 
 * RESTful API endpoints for managing user transactions including
 * CRUD operations, filtering, and pagination.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Create a new transaction
     */
    @PostMapping
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new income or expense transaction with category, amount, and date. Returns the created transaction."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        User user = getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all transactions with filtering and pagination
     */
    @GetMapping
    @Operation(
        summary = "Get all transactions",
        description = "Retrieves user's transactions with optional filtering by type, category, date range, amount range. Supports pagination and sorting."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transactions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "transactionDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        User user = getCurrentUser();
        PagedResponse<TransactionResponse> response = transactionService.getTransactions(
            type, categoryId, fromDate, toDate, minAmount, maxAmount,
            sortBy, sortOrder, page, size, user
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a specific transaction by its ID. Returns 404 if not found or doesn't belong to user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction retrieved successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        User user = getCurrentUser();
        TransactionResponse response = transactionService.getTransactionById(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update transaction
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update transaction",
        description = "Updates an existing transaction. Only the transaction owner can update it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction updated successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to update this transaction")
    })
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        User user = getCurrentUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete transaction (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete transaction",
        description = "Soft deletes a transaction. The transaction is marked as deleted and excluded from future queries."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this transaction")
    })
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        User user = getCurrentUser();
        transactionService.deleteTransaction(id, user);
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


