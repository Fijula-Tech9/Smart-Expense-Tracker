package com.expense.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 * 
 * Configures API documentation with JWT authentication support
 * and provides comprehensive API information for developers.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Expense Tracker API")
                        .description("""
                                A comprehensive personal finance management system that helps users track their 
                                income and expenses, set budgets, receive alerts, and generate detailed financial reports.
                                
                                ## Key Features:
                                - 🔐 Secure JWT-based authentication
                                - 💰 Income and expense tracking with categories
                                - 📊 Budget management with automated alerts
                                - 📈 Comprehensive financial reports and analytics
                                - 🎯 Custom categories and filtering options
                                - 📱 RESTful API design for mobile/web integration
                                
                                ## Getting Started:
                                1. Register a new account at `/api/auth/register`
                                2. Login to get your JWT token at `/api/auth/login`
                                3. Use the 'Authorize' button above to add your Bearer token
                                4. Start managing your finances with the API endpoints below
                                
                                ## Authentication:
                                All protected endpoints require a valid JWT token in the Authorization header:
                                `Authorization: Bearer <your-jwt-token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Expense Tracker Support")
                                .email("support@expensetracker.com")
                                .url("https://github.com/yourusername/expense-tracker"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from the login endpoint")));
    }
}
