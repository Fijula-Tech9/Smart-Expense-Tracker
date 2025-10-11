package com.expense.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Smart Expense Tracker - Personal Finance Management System
 * 
 * Main application class for the backend API system that enables users to:
 * - Track income and expenses with categorization
 * - Set budgets and receive automated alerts
 * - Generate financial reports and analytics
 * - Manage personal finance with JWT authentication
 * 
 * @author Generated with VSCode Copilot
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
        System.out.println("🚀 Smart Expense Tracker API is running!");
        System.out.println("📋 Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("📖 API Docs: http://localhost:8080/api-docs");
    }
}
