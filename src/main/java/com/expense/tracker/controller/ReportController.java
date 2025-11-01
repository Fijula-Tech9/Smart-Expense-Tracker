package com.expense.tracker.controller;

import com.expense.tracker.dto.response.CategoryWiseResponse;
import com.expense.tracker.dto.response.MonthlySummaryResponse;
import com.expense.tracker.dto.response.TopExpensesResponse;
import com.expense.tracker.dto.response.TrendsResponse;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Report Controller
 * 
 * RESTful API endpoints for generating financial reports including
 * monthly summaries, category-wise breakdowns, trends, and top expenses.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reports and analytics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    /**
     * Get monthly summary report
     */
    @GetMapping("/monthly-summary")
    @Operation(
        summary = "Get monthly summary",
        description = "Generates a comprehensive monthly financial summary including total income, expenses, savings, and statistics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Monthly summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = MonthlySummaryResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid month or year"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        User user = getCurrentUser();
        MonthlySummaryResponse response = reportService.getMonthlySummary(user, month, year);
        return ResponseEntity.ok(response);
    }

    /**
     * Get category-wise expense report
     */
    @GetMapping("/category-wise")
    @Operation(
        summary = "Get category-wise expense report",
        description = "Generates a breakdown of expenses by category with amounts, transaction counts, and percentages."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category-wise report retrieved successfully",
            content = @Content(schema = @Schema(implementation = CategoryWiseResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<CategoryWiseResponse> getCategoryWiseReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        User user = getCurrentUser();
        CategoryWiseResponse response = reportService.getCategoryWiseReport(user, month, year);
        return ResponseEntity.ok(response);
    }

    /**
     * Get income vs expense trends
     */
    @GetMapping("/trends")
    @Operation(
        summary = "Get income vs expense trends",
        description = "Generates month-by-month trends showing income, expenses, and net savings for the specified number of months (default: 6, max: 12)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Trends report retrieved successfully",
            content = @Content(schema = @Schema(implementation = TrendsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<TrendsResponse> getTrends(
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        User user = getCurrentUser();
        TrendsResponse response = reportService.getTrends(user, months);
        return ResponseEntity.ok(response);
    }

    /**
     * Get top expenses
     */
    @GetMapping("/top-expenses")
    @Operation(
        summary = "Get top expenses",
        description = "Retrieves the top N expense transactions by amount for the specified month/year (default: current month, limit: 10, max: 50)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Top expenses retrieved successfully",
            content = @Content(schema = @Schema(implementation = TopExpensesResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<TopExpensesResponse> getTopExpenses(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        User user = getCurrentUser();
        TopExpensesResponse response = reportService.getTopExpenses(user, limit, month, year);
        return ResponseEntity.ok(response);
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


