package com.expense.tracker.service;

import com.expense.tracker.dto.request.TransactionRequest;
import com.expense.tracker.dto.response.PagedResponse;
import com.expense.tracker.dto.response.TransactionResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.ForbiddenException;
import com.expense.tracker.exception.InvalidRequestException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Transaction Service
 * 
 * Handles all business logic for transaction management including
 * CRUD operations, filtering, pagination, and validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        log.info("Creating transaction for user ID: {}", user.getId());
        
        // Validate category exists and is available to user
        Category category = categoryRepository.findByIdAndAvailableToUser(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not available"));
        
        // Validate category type matches transaction type
        if (!category.getType().name().equals(request.getType().name())) {
            throw new InvalidRequestException(
                String.format("Category type (%s) does not match transaction type (%s)", 
                    category.getType(), request.getType())
            );
        }
        
        // Validate transaction date is not in future
        if (request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Transaction date cannot be in the future");
        }
        
        // Create and save transaction
        Transaction transaction = new Transaction(
            user,
            category,
            request.getType(),
            request.getAmount(),
            request.getTransactionDate(),
            request.getDescription(),
            request.getPaymentMethod()
        );
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        
        return mapToResponse(savedTransaction);
    }

    /**
     * Get all transactions with filtering and pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(
            TransactionType type,
            Long categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String sortBy,
            String sortOrder,
            int page,
            int size,
            User user) {
        
        log.info("Fetching transactions for user ID: {} with filters", user.getId());
        
        // Validate pagination parameters
        if (size > 100) {
            size = 100; // Max page size
        }
        if (size < 1) {
            size = 20; // Default page size
        }
        
        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidRequestException("Start date cannot be after end date");
        }
        
        // Validate amount range
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new InvalidRequestException("Minimum amount cannot be greater than maximum amount");
        }
        
        // Create sort object
        Sort sort = createSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Execute query with filters
        Page<Transaction> transactions = transactionRepository.findTransactionsWithFilters(
            user.getId(),
            type,
            categoryId,
            fromDate,
            toDate,
            minAmount,
            maxAmount,
            pageable
        );
        
        // Map to response DTOs
        Page<TransactionResponse> responsePage = transactions.map(this::mapToResponse);
        
        return new PagedResponse<>(responsePage);
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId, User user) {
        log.info("Fetching transaction ID: {} for user ID: {}", transactionId, user.getId());
        
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        return mapToResponse(transaction);
    }

    /**
     * Update transaction
     */
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request, User user) {
        log.info("Updating transaction ID: {} for user ID: {}", transactionId, user.getId());
        
        // Find transaction
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        // Validate category exists and is available to user
        Category category = categoryRepository.findByIdAndAvailableToUser(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not available"));
        
        // Validate category type matches transaction type
        if (!category.getType().name().equals(request.getType().name())) {
            throw new InvalidRequestException(
                String.format("Category type (%s) does not match transaction type (%s)", 
                    category.getType(), request.getType())
            );
        }
        
        // Validate transaction date is not in future
        if (request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Transaction date cannot be in the future");
        }
        
        // Update transaction fields
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod());
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction updated successfully with ID: {}", updatedTransaction.getId());
        
        return mapToResponse(updatedTransaction);
    }

    /**
     * Delete transaction (soft delete)
     */
    @Transactional
    public void deleteTransaction(Long transactionId, User user) {
        log.info("Deleting transaction ID: {} for user ID: {}", transactionId, user.getId());
        
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        // Soft delete
        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);
        
        log.info("Transaction soft deleted successfully with ID: {}", transactionId);
    }

    /**
     * Create Sort object from sort parameters
     */
    private Sort createSort(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "transactionDate"; // Default sort by date
        }
        
        if (sortOrder == null || sortOrder.isEmpty() || !sortOrder.equalsIgnoreCase("asc")) {
            sortOrder = "desc"; // Default descending
        }
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        // Validate sort field
        if (!sortBy.equals("transactionDate") && !sortBy.equals("amount") && !sortBy.equals("createdAt")) {
            sortBy = "transactionDate"; // Default if invalid
        }
        
        return Sort.by(direction, sortBy);
    }

    /**
     * Map Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setDescription(transaction.getDescription());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}

