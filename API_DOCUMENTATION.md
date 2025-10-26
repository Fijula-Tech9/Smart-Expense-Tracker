# Smart Expense Tracker API Documentation

## Overview
The Smart Expense Tracker is a comprehensive expense management application built with Spring Boot, providing secure user authentication and expense tracking capabilities.

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Categories
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Transactions
- `GET /api/transactions` - Get user transactions
- `POST /api/transactions` - Create new transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Budgets
- `GET /api/budgets` - Get user budgets
- `POST /api/budgets` - Create new budget
- `PUT /api/budgets/{id}` - Update budget
- `DELETE /api/budgets/{id}` - Delete budget

## Security
- JWT-based authentication
- Role-based access control
- Password encryption with BCrypt

## Database
- MySQL database support
- JPA/Hibernate ORM
- Database migrations and seeding

## Deployment
- AWS deployment ready
- Docker containerization support
- Environment-specific configurations
