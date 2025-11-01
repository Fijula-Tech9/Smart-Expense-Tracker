package com.expense.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

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
