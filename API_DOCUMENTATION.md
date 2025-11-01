# Smart Expense Tracker API Documentation

## Overview
The Smart Expense Tracker is a comprehensive expense management application built with Spring Boot 3.2, providing secure user authentication, expense tracking, budget management, and financial reporting capabilities.

## Base URL
- Development: `http://localhost:8080`
- Production: Configured per deployment environment

## Authentication
All protected endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Authentication Endpoints

#### Register New User
- **URL**: `/api/auth/register`
- **Method**: `POST`
- **Auth Required**: No
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```
- **Response**: Returns JWT token and user information

#### User Login
- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Auth Required**: No
- **Request Body**:
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```
- **Response**: Returns JWT token and user information

#### Health Check
- **URL**: `/api/auth/health`
- **Method**: `GET`
- **Auth Required**: No

---

### User Profile Endpoints

#### Get User Profile
- **URL**: `/api/users/profile`
- **Method**: `GET`
- **Auth Required**: Yes
- **Response**: User profile information (id, name, email, createdAt)

#### Update User Profile
- **URL**: `/api/users/profile`
- **Method**: `PUT`
- **Auth Required**: Yes
- **Request Body**:
```json
{
  "name": "John Updated",
  "password": "NewPassword123"
}
```
- **Note**: Both fields are optional; provide only what you want to update

---

### Category Endpoints

#### Get All Categories
- **URL**: `/api/categories`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**: 
  - `type` (optional): `INCOME` or `EXPENSE` to filter by type
- **Response**: List of system and custom categories

#### Get Category by ID
- **URL**: `/api/categories/{id}`
- **Method**: `GET`
- **Auth Required**: Yes

#### Create Custom Category
- **URL**: `/api/categories`
- **Method**: `POST`
- **Auth Required**: Yes
- **Request Body**:
```json
{
  "name": "Pet Expenses",
  "type": "EXPENSE"
}
```

#### Update Custom Category
- **URL**: `/api/categories/{id}`
- **Method**: `PUT`
- **Auth Required**: Yes
- **Request Body**:
```json
{
  "name": "Pet Care & Expenses",
  "type": "EXPENSE"
}
```
- **Note**: Cannot update system categories

#### Delete Custom Category
- **URL**: `/api/categories/{id}`
- **Method**: `DELETE`
- **Auth Required**: Yes
- **Note**: Cannot delete if category has transactions

---

### Transaction Endpoints

#### Create Transaction
- **URL**: `/api/transactions`
- **Method**: `POST`
- **Auth Required**: Yes
- **Request Body**:
```json
{
  "type": "EXPENSE",
  "categoryId": 6,
  "amount": 1250.50,
  "transactionDate": "2024-10-06",
  "description": "Grocery shopping",
  "paymentMethod": "Credit Card"
}
```

#### Get All Transactions (with filtering)
- **URL**: `/api/transactions`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `type` (optional): `INCOME` or `EXPENSE`
  - `categoryId` (optional): Filter by category
  - `fromDate` (optional): Start date (YYYY-MM-DD)
  - `toDate` (optional): End date (YYYY-MM-DD)
  - `minAmount` (optional): Minimum amount
  - `maxAmount` (optional): Maximum amount
  - `sortBy` (optional): `transactionDate`, `amount`, or `createdAt` (default: `transactionDate`)
  - `sortOrder` (optional): `asc` or `desc` (default: `desc`)
  - `page` (optional): Page number (default: 0)
  - `size` (optional): Page size (default: 20, max: 100)
- **Response**: Paginated list of transactions

#### Get Transaction by ID
- **URL**: `/api/transactions/{id}`
- **Method**: `GET`
- **Auth Required**: Yes

#### Update Transaction
- **URL**: `/api/transactions/{id}`
- **Method**: `PUT`
- **Auth Required**: Yes
- **Request Body**: Same as create transaction

#### Delete Transaction (Soft Delete)
- **URL**: `/api/transactions/{id}`
- **Method**: `DELETE`
- **Auth Required**: Yes

---

### Budget Endpoints

#### Set Budget
- **URL**: `/api/budgets`
- **Method**: `POST`
- **Auth Required**: Yes
- **Request Body**:
```json
{
  "categoryId": 6,
  "budgetAmount": 8000.00,
  "month": 10,
  "year": 2024
}
```
- **Note**: Only for expense categories. Creates new budget or updates existing one.

#### Get All Budgets
- **URL**: `/api/budgets`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `month` (optional): Month 1-12 (default: current month)
  - `year` (optional): Year (default: current year)
- **Response**: List of budgets with calculated spent amounts

#### Get Budget by ID
- **URL**: `/api/budgets/{id}`
- **Method**: `GET`
- **Auth Required**: Yes

#### Update Budget Amount
- **URL**: `/api/budgets/{id}`
- **Method**: `PUT`
- **Auth Required**: Yes
- **Query Parameter**: `budgetAmount` (required)

#### Delete Budget
- **URL**: `/api/budgets/{id}`
- **Method**: `DELETE`
- **Auth Required**: Yes

#### Get Budget Alerts
- **URL**: `/api/budgets/alerts`
- **Method**: `GET`
- **Auth Required**: Yes
- **Response**: List of budgets with alerts (WARNING: 80-99%, LIMIT_REACHED: 100%, EXCEEDED: >100%)

---

### Report Endpoints

#### Monthly Summary Report
- **URL**: `/api/reports/monthly-summary`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `month` (required): Month 1-12
  - `year` (required): Year
- **Response**: Total income, expenses, savings, transaction count, average amount, largest expense

#### Category-wise Expense Report
- **URL**: `/api/reports/category-wise`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `month` (optional): Month 1-12 (default: current month)
  - `year` (optional): Year (default: current year)
- **Response**: Expenses grouped by category with amounts, counts, and percentages

#### Income vs Expense Trends
- **URL**: `/api/reports/trends`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `months` (optional): Number of months (default: 6, max: 12)
- **Response**: Month-by-month income, expenses, and net savings

#### Top Expenses
- **URL**: `/api/reports/top-expenses`
- **Method**: `GET`
- **Auth Required**: Yes
- **Query Parameters**:
  - `limit` (optional): Number of top expenses (default: 10, max: 50)
  - `month` (optional): Month 1-12 (default: current month)
  - `year` (optional): Year (default: current year)
- **Response**: Top N expenses sorted by amount

---

## Error Responses

All error responses follow this format:
```json
{
  "timestamp": "2024-10-06T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    }
  ],
  "path": "/api/transactions"
}
```

### Common HTTP Status Codes:
- `200 OK`: Successful GET, PUT, DELETE
- `201 Created`: Successful POST
- `400 Bad Request`: Validation errors, invalid input
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User doesn't have permission
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate resource (e.g., email already exists)
- `500 Internal Server Error`: Server-side errors

---

## Security
- **JWT Authentication**: Stateless token-based authentication (24h expiration)
- **BCrypt Password Hashing**: Strength 12 for secure password storage
- **CORS Configuration**: Configured for cross-origin requests
- **Input Validation**: Comprehensive validation on all endpoints
- **SQL Injection Prevention**: JPA with parameterized queries

## Database
- **MySQL** (Production) / **H2** (Development)
- **JPA/Hibernate ORM**: Object-relational mapping
- **Database Migrations**: Automatic schema creation
- **Connection Pooling**: HikariCP (min: 5, max: 20 connections)

## Swagger Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs JSON**: `http://localhost:8080/api-docs`

## Deployment
- **AWS Deployment Ready**: EC2, RDS, Elastic Beanstalk configurations
- **Docker Support**: Containerization ready
- **Environment Profiles**: dev, test, prod configurations
- **CI/CD Ready**: Maven build and deployment scripts included

