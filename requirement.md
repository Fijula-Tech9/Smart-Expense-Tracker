# Smart Expense Tracker - Backend API System
## Complete Requirements Document for VSCode Copilot

---

## 1. PROJECT OVERVIEW

### 1.1 Project Title
**Smart Expense Tracker - Personal Finance Management System**

### 1.2 Project Description
A RESTful backend API system built with Spring Boot that enables users to track their income and expenses, set budgets, receive alerts, and generate financial reports. The system helps users manage their personal finances effectively by providing insights into spending patterns and budget adherence.

### 1.3 Project Objectives
- Enable users to record and categorize income and expenses
- Provide budget management with automated alerts
- Generate monthly financial reports and analytics
- Implement secure user authentication using JWT
- Optimize database queries for fast report generation
- Deploy on AWS cloud infrastructure

### 1.4 Target Users
- Individual users managing personal finances
- Students tracking monthly expenses
- Freelancers monitoring income and expenses
- Anyone wanting to improve financial discipline

---

## 2. FUNCTIONAL REQUIREMENTS

### 2.1 User Management Module

**FR-1.1: User Registration**
- Users can create an account with email, password, and name
- Email must be unique in the system
- Password must be hashed using BCrypt
- Return JWT token upon successful registration

**FR-1.2: User Login**
- Users can login with email and password
- System validates credentials
- Return JWT token valid for 24 hours
- Token must be used for all authenticated endpoints

**FR-1.3: User Profile Management**
- Users can view their profile information
- Users can update their name and password
- Users cannot change their email address

### 2.2 Transaction Management Module

**FR-2.1: Add Transaction**
- Users can add income or expense transactions
- Required fields: type (INCOME/EXPENSE), amount, category, date
- Optional fields: description, payment method
- Amount must be positive decimal number
- Date cannot be in future

**FR-2.2: View Transactions**
- Users can view all their transactions
- Support pagination (default 20 records per page)
- Support filtering by:
  - Transaction type (INCOME/EXPENSE)
  - Category
  - Date range (from date, to date)
  - Amount range (min, max)
- Support sorting by date or amount (ascending/descending)

**FR-2.3: Update Transaction**
- Users can update any field of their own transactions
- Cannot update transaction belonging to another user
- Updated transaction maintains its original ID

**FR-2.4: Delete Transaction**
- Users can soft delete their own transactions
- Deleted transactions marked as inactive, not physically removed
- Deleted transactions excluded from reports

**FR-2.5: View Single Transaction**
- Users can view details of a specific transaction by ID
- Return 404 if transaction not found or doesn't belong to user

### 2.3 Category Management Module

**FR-3.1: View All Categories**
- System provides predefined categories:
  - INCOME: Salary, Freelance, Investment, Gift, Other Income
  - EXPENSE: Food & Dining, Transportation, Housing, Utilities, Healthcare, Entertainment, Shopping, Education, Insurance, Other Expense
- Users can view all available categories

**FR-3.2: Create Custom Category**
- Users can create custom categories
- Category name must be unique per user
- Specify if category is for INCOME or EXPENSE

**FR-3.3: Update Category**
- Users can update name of their custom categories
- Cannot update system-provided categories

**FR-3.4: Delete Category**
- Users can delete only custom categories
- Cannot delete if transactions exist under that category
- System categories cannot be deleted

### 2.4 Budget Management Module

**FR-4.1: Set Budget**
- Users can set monthly budget for each expense category
- Budget amount must be positive
- One budget per category per user
- Budget applies to current month

**FR-4.2: View All Budgets**
- Users can view all their active budgets
- Show category, budget limit, spent amount, remaining amount
- Calculate percentage used

**FR-4.3: Update Budget**
- Users can update budget amount for any category
- Changes apply to current month

**FR-4.4: Delete Budget**
- Users can delete budget for any category
- No budget alerts will be generated after deletion

**FR-4.5: Budget Alerts**
- System calculates budget usage percentage
- Generate alerts when:
  - 80% of budget used (WARNING)
  - 100% of budget used (LIMIT_REACHED)
  - Budget exceeded (EXCEEDED)
- API endpoint returns all active alerts for current month

### 2.5 Reports & Analytics Module

**FR-5.1: Monthly Summary Report**
- Input: Month and Year
- Output:
  - Total income for the month
  - Total expenses for the month
  - Net savings (income - expenses)
  - Number of transactions
  - Average transaction amount

**FR-5.2: Category-wise Expense Report**
- Input: Month and Year (optional, defaults to current month)
- Output: List of expense categories with:
  - Category name
  - Total amount spent
  - Number of transactions
  - Percentage of total expenses
- Sort by amount in descending order

**FR-5.3: Income vs Expense Trend**
- Input: Number of months (default 6, max 12)
- Output: Month-by-month data showing:
  - Month and year
  - Total income
  - Total expenses
  - Net savings
- Useful for visualizing spending patterns

**FR-5.4: Top Expenses**
- Input: Limit (default 10)
- Output: Top N expense transactions by amount
- Shows highest spending instances

---

## 3. NON-FUNCTIONAL REQUIREMENTS

### 3.1 Performance Requirements
- **NFR-1**: API response time < 200ms for CRUD operations
- **NFR-2**: Report generation < 500ms for 1000 transactions
- **NFR-3**: Support 100 concurrent users
- **NFR-4**: Database queries optimized with proper indexing

### 3.2 Security Requirements
- **NFR-5**: All passwords hashed using BCrypt (strength 12)
- **NFR-6**: JWT tokens expire after 24 hours
- **NFR-7**: API endpoints protected (except register/login)
- **NFR-8**: SQL injection prevention using prepared statements
- **NFR-9**: Input validation on all endpoints

### 3.3 Reliability Requirements
- **NFR-10**: System uptime 99.5%
- **NFR-11**: Data backup daily
- **NFR-12**: Transaction rollback on failures

### 3.4 Scalability Requirements
- **NFR-13**: Horizontal scaling capability
- **NFR-14**: Database connection pooling (min 5, max 20)
- **NFR-15**: Redis caching for frequently accessed data

### 3.5 Usability Requirements
- **NFR-16**: RESTful API design principles
- **NFR-17**: Consistent error response format
- **NFR-18**: Swagger UI for API documentation
- **NFR-19**: Clear error messages for validation failures

---

## 4. DATABASE SCHEMA DESIGN

### 4.1 Tables

#### Table: users
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email)
);
```

#### Table: categories
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    is_system_category BOOLEAN DEFAULT FALSE,
    user_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_user_category (user_id, name),
    INDEX idx_user_type (user_id, type)
);
```

#### Table: transactions
```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(500),
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_user_type_date (user_id, type, transaction_date),
    INDEX idx_category (category_id)
);
```

#### Table: budgets
```sql
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    budget_amount DECIMAL(15, 2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    UNIQUE KEY unique_user_category_month (user_id, category_id, month, year),
    INDEX idx_user_month_year (user_id, month, year)
);
```

### 4.2 Sample Data for System Categories
```sql
INSERT INTO categories (name, type, is_system_category) VALUES
-- Income Categories
('Salary', 'INCOME', TRUE),
('Freelance', 'INCOME', TRUE),
('Investment', 'INCOME', TRUE),
('Gift', 'INCOME', TRUE),
('Other Income', 'INCOME', TRUE),
-- Expense Categories
('Food & Dining', 'EXPENSE', TRUE),
('Transportation', 'EXPENSE', TRUE),
('Housing', 'EXPENSE', TRUE),
('Utilities', 'EXPENSE', TRUE),
('Healthcare', 'EXPENSE', TRUE),
('Entertainment', 'EXPENSE', TRUE),
('Shopping', 'EXPENSE', TRUE),
('Education', 'EXPENSE', TRUE),
('Insurance', 'EXPENSE', TRUE),
('Other Expense', 'EXPENSE', TRUE);
```

### 4.3 Entity Relationships
- One User has Many Transactions (1:N)
- One User has Many Budgets (1:N)
- One User has Many Custom Categories (1:N)
- One Category has Many Transactions (1:N)
- One Category has Many Budgets (1:N)

---

## 5. API ENDPOINTS SPECIFICATION

### 5.1 Authentication APIs

#### POST /api/auth/register
**Description**: Register a new user account
**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```
**Response** (201 Created):
```json
{
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```
**Error Responses**:
- 400: Email already exists
- 400: Invalid email format
- 400: Password must be at least 8 characters

#### POST /api/auth/login
**Description**: Login with email and password
**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```
**Response** (200 OK):
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```
**Error Responses**:
- 401: Invalid email or password
- 400: Email and password are required

### 5.2 User Profile APIs

#### GET /api/users/profile
**Description**: Get current user's profile
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-10-01T10:30:00Z"
}
```

#### PUT /api/users/profile
**Description**: Update user profile
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "name": "John Updated",
  "password": "NewPassword123"
}
```
**Response** (200 OK):
```json
{
  "message": "Profile updated successfully",
  "user": {
    "id": 1,
    "name": "John Updated",
    "email": "john@example.com"
  }
}
```

### 5.3 Transaction APIs

#### POST /api/transactions
**Description**: Create a new transaction
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "type": "EXPENSE",
  "categoryId": 6,
  "amount": 1250.50,
  "transactionDate": "2024-10-06",
  "description": "Grocery shopping at Big Bazaar",
  "paymentMethod": "Credit Card"
}
```
**Response** (201 Created):
```json
{
  "message": "Transaction created successfully",
  "transaction": {
    "id": 101,
    "type": "EXPENSE",
    "categoryId": 6,
    "categoryName": "Food & Dining",
    "amount": 1250.50,
    "transactionDate": "2024-10-06",
    "description": "Grocery shopping at Big Bazaar",
    "paymentMethod": "Credit Card",
    "createdAt": "2024-10-06T14:30:00Z"
  }
}
```
**Validation Rules**:
- type: Required, must be INCOME or EXPENSE
- categoryId: Required, must exist
- amount: Required, must be positive number
- transactionDate: Required, cannot be future date
- description: Optional, max 500 characters
- paymentMethod: Optional, max 50 characters

#### GET /api/transactions
**Description**: Get all transactions with filtering and pagination
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- page (optional, default: 0)
- size (optional, default: 20, max: 100)
- type (optional, values: INCOME, EXPENSE)
- categoryId (optional)
- fromDate (optional, format: YYYY-MM-DD)
- toDate (optional, format: YYYY-MM-DD)
- minAmount (optional)
- maxAmount (optional)
- sortBy (optional, values: date, amount, default: date)
- sortOrder (optional, values: asc, desc, default: desc)

**Example Request**:
```
GET /api/transactions?page=0&size=20&type=EXPENSE&fromDate=2024-10-01&toDate=2024-10-31&sortBy=amount&sortOrder=desc
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 101,
      "type": "EXPENSE",
      "categoryId": 6,
      "categoryName": "Food & Dining",
      "amount": 1250.50,
      "transactionDate": "2024-10-06",
      "description": "Grocery shopping",
      "paymentMethod": "Credit Card",
      "createdAt": "2024-10-06T14:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "isLast": false
}
```

#### GET /api/transactions/{id}
**Description**: Get a specific transaction by ID
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "id": 101,
  "type": "EXPENSE",
  "categoryId": 6,
  "categoryName": "Food & Dining",
  "amount": 1250.50,
  "transactionDate": "2024-10-06",
  "description": "Grocery shopping",
  "paymentMethod": "Credit Card",
  "createdAt": "2024-10-06T14:30:00Z",
  "updatedAt": "2024-10-06T14:30:00Z"
}
```
**Error Responses**:
- 404: Transaction not found
- 403: Transaction belongs to another user

#### PUT /api/transactions/{id}
**Description**: Update a transaction
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "type": "EXPENSE",
  "categoryId": 7,
  "amount": 1500.00,
  "transactionDate": "2024-10-06",
  "description": "Updated description",
  "paymentMethod": "Debit Card"
}
```
**Response** (200 OK):
```json
{
  "message": "Transaction updated successfully",
  "transaction": {
    "id": 101,
    "type": "EXPENSE",
    "categoryId": 7,
    "categoryName": "Transportation",
    "amount": 1500.00,
    "transactionDate": "2024-10-06",
    "description": "Updated description",
    "paymentMethod": "Debit Card",
    "updatedAt": "2024-10-06T15:45:00Z"
  }
}
```

#### DELETE /api/transactions/{id}
**Description**: Soft delete a transaction
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "message": "Transaction deleted successfully"
}
```
**Error Responses**:
- 404: Transaction not found
- 403: Transaction belongs to another user

### 5.4 Category APIs

#### GET /api/categories
**Description**: Get all categories (system + user's custom categories)
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- type (optional, values: INCOME, EXPENSE)

**Response** (200 OK):
```json
{
  "categories": [
    {
      "id": 1,
      "name": "Salary",
      "type": "INCOME",
      "isSystemCategory": true
    },
    {
      "id": 6,
      "name": "Food & Dining",
      "type": "EXPENSE",
      "isSystemCategory": true
    },
    {
      "id": 25,
      "name": "My Custom Category",
      "type": "EXPENSE",
      "isSystemCategory": false
    }
  ]
}
```

#### POST /api/categories
**Description**: Create a custom category
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "name": "Pet Expenses",
  "type": "EXPENSE"
}
```
**Response** (201 Created):
```json
{
  "message": "Category created successfully",
  "category": {
    "id": 26,
    "name": "Pet Expenses",
    "type": "EXPENSE",
    "isSystemCategory": false
  }
}
```
**Validation Rules**:
- name: Required, max 100 characters
- type: Required, must be INCOME or EXPENSE
- Category name must be unique per user

#### PUT /api/categories/{id}
**Description**: Update a custom category
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "name": "Pet Care & Expenses"
}
```
**Response** (200 OK):
```json
{
  "message": "Category updated successfully",
  "category": {
    "id": 26,
    "name": "Pet Care & Expenses",
    "type": "EXPENSE",
    "isSystemCategory": false
  }
}
```
**Error Responses**:
- 403: Cannot update system categories
- 404: Category not found
- 400: Category name already exists

#### DELETE /api/categories/{id}
**Description**: Delete a custom category
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "message": "Category deleted successfully"
}
```
**Error Responses**:
- 403: Cannot delete system categories
- 400: Cannot delete category with existing transactions
- 404: Category not found

### 5.5 Budget APIs

#### POST /api/budgets
**Description**: Set a budget for a category
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "categoryId": 6,
  "budgetAmount": 8000.00,
  "month": 10,
  "year": 2024
}
```
**Response** (201 Created):
```json
{
  "message": "Budget set successfully",
  "budget": {
    "id": 15,
    "categoryId": 6,
    "categoryName": "Food & Dining",
    "budgetAmount": 8000.00,
    "month": 10,
    "year": 2024,
    "spentAmount": 0.00,
    "remainingAmount": 8000.00,
    "percentageUsed": 0
  }
}
```
**Validation Rules**:
- categoryId: Required, must exist and be EXPENSE type
- budgetAmount: Required, must be positive
- month: Required, 1-12
- year: Required, cannot be past year
- One budget per category per month per user

#### GET /api/budgets
**Description**: Get all budgets for current month
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- month (optional, default: current month)
- year (optional, default: current year)

**Response** (200 OK):
```json
{
  "budgets": [
    {
      "id": 15,
      "categoryId": 6,
      "categoryName": "Food & Dining",
      "budgetAmount": 8000.00,
      "spentAmount": 6500.00,
      "remainingAmount": 1500.00,
      "percentageUsed": 81.25,
      "month": 10,
      "year": 2024
    },
    {
      "id": 16,
      "categoryId": 7,
      "categoryName": "Transportation",
      "budgetAmount": 3000.00,
      "spentAmount": 2100.00,
      "remainingAmount": 900.00,
      "percentageUsed": 70.00,
      "month": 10,
      "year": 2024
    }
  ]
}
```

#### PUT /api/budgets/{id}
**Description**: Update a budget amount
**Headers**: Authorization: Bearer {token}
**Request Body**:
```json
{
  "budgetAmount": 10000.00
}
```
**Response** (200 OK):
```json
{
  "message": "Budget updated successfully",
  "budget": {
    "id": 15,
    "categoryId": 6,
    "categoryName": "Food & Dining",
    "budgetAmount": 10000.00,
    "spentAmount": 6500.00,
    "remainingAmount": 3500.00,
    "percentageUsed": 65.00,
    "month": 10,
    "year": 2024
  }
}
```

#### DELETE /api/budgets/{id}
**Description**: Delete a budget
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "message": "Budget deleted successfully"
}
```

#### GET /api/budgets/alerts
**Description**: Get budget alerts for current month
**Headers**: Authorization: Bearer {token}
**Response** (200 OK):
```json
{
  "alerts": [
    {
      "categoryId": 6,
      "categoryName": "Food & Dining",
      "budgetAmount": 8000.00,
      "spentAmount": 6500.00,
      "percentageUsed": 81.25,
      "alertType": "WARNING",
      "message": "You have used 81.25% of your Food & Dining budget"
    },
    {
      "categoryId": 11,
      "categoryName": "Entertainment",
      "budgetAmount": 2000.00,
      "spentAmount": 2500.00,
      "percentageUsed": 125.00,
      "alertType": "EXCEEDED",
      "message": "You have exceeded your Entertainment budget by ₹500.00"
    }
  ]
}
```
**Alert Types**:
- WARNING: 80-99% used
- LIMIT_REACHED: 100% used
- EXCEEDED: > 100% used

### 5.6 Reports APIs

#### GET /api/reports/monthly-summary
**Description**: Get monthly income/expense summary
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- month (required, 1-12)
- year (required)

**Example Request**:
```
GET /api/reports/monthly-summary?month=10&year=2024
```

**Response** (200 OK):
```json
{
  "month": 10,
  "year": 2024,
  "totalIncome": 55000.00,
  "totalExpenses": 32450.00,
  "netSavings": 22550.00,
  "transactionCount": 47,
  "averageTransactionAmount": 690.43,
  "largestExpense": {
    "id": 120,
    "amount": 15000.00,
    "categoryName": "Housing",
    "description": "Monthly rent",
    "date": "2024-10-01"
  }
}
```

#### GET /api/reports/category-wise
**Description**: Get category-wise expense breakdown
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- month (optional, default: current month)
- year (optional, default: current year)

**Response** (200 OK):
```json
{
  "month": 10,
  "year": 2024,
  "totalExpenses": 32450.00,
  "categories": [
    {
      "categoryId": 8,
      "categoryName": "Housing",
      "totalAmount": 15000.00,
      "transactionCount": 1,
      "percentageOfTotal": 46.23
    },
    {
      "categoryId": 6,
      "categoryName": "Food & Dining",
      "totalAmount": 8500.00,
      "transactionCount": 18,
      "percentageOfTotal": 26.19
    },
    {
      "categoryId": 7,
      "categoryName": "Transportation",
      "totalAmount": 4200.00,
      "transactionCount": 12,
      "percentageOfTotal": 12.94
    }
  ]
}
```

#### GET /api/reports/trends
**Description**: Get income vs expense trends for multiple months
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- months (optional, default: 6, max: 12)

**Response** (200 OK):
```json
{
  "trends": [
    {
      "month": 5,
      "year": 2024,
      "totalIncome": 50000.00,
      "totalExpenses": 28000.00,
      "netSavings": 22000.00
    },
    {
      "month": 6,
      "year": 2024,
      "totalIncome": 50000.00,
      "totalExpenses": 31000.00,
      "netSavings": 19000.00
    },
    {
      "month": 7,
      "year": 2024,
      "totalIncome": 55000.00,
      "totalExpenses": 29500.00,
      "netSavings": 25500.00
    }
  ]
}
```

#### GET /api/reports/top-expenses
**Description**: Get top N expenses by amount
**Headers**: Authorization: Bearer {token}
**Query Parameters**:
- limit (optional, default: 10, max: 50)
- month (optional, default: current month)
- year (optional, default: current year)

**Response** (200 OK):
```json
{
  "topExpenses": [
    {
      "id": 120,
      "amount": 15000.00,
      "categoryName": "Housing",
      "description": "Monthly rent",
      "transactionDate": "2024-10-01",
      "paymentMethod": "Bank Transfer"
    },
    {
      "id": 145,
      "amount": 2500.00,
      "categoryName": "Shopping",
      "description": "New laptop accessories",
      "transactionDate": "2024-10-15",
      "paymentMethod": "Credit Card"
    }
  ]
}
```

---

## 6. ERROR RESPONSE FORMAT

All error responses follow this consistent format:

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
    },
    {
      "field": "transactionDate",
      "message": "Transaction date cannot be in the future"
    }
  ],
  "path": "/api/transactions"
}
```

### Common HTTP Status Codes:
- **200 OK**: Successful GET, PUT, DELETE
- **201 Created**: Successful POST
- **400 Bad Request**: Validation errors, invalid input
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: User doesn't have permission
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate resource (e.g., email already exists)
- **500 Internal Server Error**: Server-side errors

---

## 7. TECHNOLOGY STACK

### 7.1 Backend Framework
- **Spring Boot 3.2.x**
  - Spring Web (REST APIs)
  - Spring Data JPA (Database ORM)
  - Spring Security (Authentication & Authorization)
  - Spring Validation (Input validation)

### 7.2 Database
- **MySQL 8.0**
  - Primary database for persistent storage
  - ACID compliance for transactions

### 7.3 Caching
- **Redis 7.x** (Optional but recommended)
  - Cache frequently accessed data
  - Store JWT blacklist (for logout functionality)
  - Cache monthly reports

### 7.4 Security
- **JWT (JSON Web Tokens)**
  - Library: io.jsonwebtoken:jjwt (version 0.12.x)
  - Token-based authentication
- **BCrypt**
  - Password hashing (built into Spring Security)

### 7.5 Development Tools
- **Maven** - Dependency management
- **Lombok** - Reduce boilerplate code
- **Swagger/OpenAPI** - API documentation
  - Library: springdoc-openapi-starter-webmvc-ui

### 7.6 Testing Tools
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Postman** - API testing

### 7.7 Deployment
- **AWS EC2** - Application server hosting
- **AWS RDS** - MySQL database hosting
- **AWS VPC** - Network isolation
- **AWS Security Groups** - Firewall rules
- **Docker** (Optional) - Containerization

---

## 8. PROJECT STRUCTURE

### 8.1 Recommended Package Structure
```
src/main/java/com/expense/tracker/
├── ExpenseTrackerApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── CorsConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   ├── CategoryController.java
│   ├── BudgetController.java
│   └── ReportController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── TransactionService.java
│   ├── CategoryService.java
│   ├── BudgetService.java
│   └── ReportService.java
├── repository/
│   ├── UserRepository.java
│   ├── TransactionRepository.java
│   ├── CategoryRepository.java
│   └── BudgetRepository.java
├── model/
│   ├── User.java
│   ├── Transaction.java
│   ├── Category.java
│   └── Budget.java
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── TransactionRequest.java
│   │   ├── CategoryRequest.java
│   │   └── BudgetRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── TransactionResponse.java
│       ├── BudgetResponse.java
│       ├── MonthlySummaryResponse.java
│       └── CategoryWiseResponse.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── util/
    ├── DateUtil.java
    └── ValidationUtil.java

src/main/resources/
├── application.properties
├── application-dev.properties
├── application-prod.properties
└── data.sql (system categories seed data)
```

### 8.2 Maven Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Redis (Optional) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 8.3 Application Configuration (application.properties)
```properties
# Application
spring.application.name=expense-tracker
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000

# JWT Configuration
jwt.secret=YourVeryLongSecretKeyForJWTTokenGenerationAndValidation123456
jwt.expiration=86400000

# Redis Configuration (Optional)
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=60000

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Logging
logging.level.com.expense.tracker=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## 9. IMPLEMENTATION PHASES

### Phase 1: Project Setup (Week 1)
**Tasks:**
1. Create Spring Boot project with dependencies
2. Setup MySQL database
3. Configure application.properties
4. Setup project structure (packages)
5. Add Lombok and validation dependencies
6. Configure Swagger for API documentation

**Deliverables:**
- Project runs successfully on localhost:8080
- Database connection established
- Swagger UI accessible at /swagger-ui.html

### Phase 2: User Authentication Module (Week 1)
**Tasks:**
1. Create User entity and repository
2. Implement registration endpoint
3. Implement login endpoint
4. Setup JWT token generation
5. Implement JWT authentication filter
6. Configure Spring Security
7. Test with Postman

**Deliverables:**
- Users can register and login
- JWT tokens are generated
- Protected endpoints require authentication

### Phase 3: Category Management (Week 1)
**Tasks:**
1. Create Category entity and repository
2. Create seed data for system categories
3. Implement CRUD endpoints for categories
4. Add validation rules
5. Test with Postman

**Deliverables:**
- System categories loaded in database
- Users can create custom categories
- Category CRUD operations working

### Phase 4: Transaction Management (Week 2)
**Tasks:**
1. Create Transaction entity and repository
2. Implement create transaction endpoint
3. Implement get all transactions with filters
4. Implement pagination and sorting
5. Implement update and delete endpoints
6. Add comprehensive validation
7. Test all scenarios with Postman

**Deliverables:**
- Complete transaction CRUD operations
- Filtering and pagination working
- Soft delete implemented

### Phase 5: Budget Management (Week 2)
**Tasks:**
1. Create Budget entity and repository
2. Implement budget CRUD endpoints
3. Implement budget calculation logic (spent amount)
4. Implement budget alerts endpoint
5. Test budget scenarios with Postman

**Deliverables:**
- Budget management fully functional
- Budget alerts working correctly
- Spent amount calculated accurately

### Phase 6: Reports & Analytics (Week 3)
**Tasks:**
1. Implement monthly summary report
2. Implement category-wise report
3. Implement trends report
4. Implement top expenses report
5. Optimize queries with database indexes
6. Test report accuracy with sample data

**Deliverables:**
- All report endpoints working
- Reports show accurate data
- Query performance optimized

### Phase 7: Performance Optimization (Week 3)
**Tasks:**
1. Add database indexes on frequently queried columns
2. Implement Redis caching for reports
3. Optimize N+1 query problems
4. Add connection pooling configuration
5. Benchmark API response times
6. Document optimization improvements

**Deliverables:**
- API response times < 200ms
- Report generation < 500ms
- Caching implemented and working

### Phase 8: Testing & Documentation (Week 4)
**Tasks:**
1. Write unit tests for services
2. Write integration tests for controllers
3. Create comprehensive Postman collection
4. Generate Swagger documentation
5. Test all error scenarios
6. Fix bugs and edge cases

**Deliverables:**
- Test coverage > 70%
- Complete Postman collection
- Swagger documentation complete

### Phase 9: AWS Deployment (Week 4)
**Tasks:**
1. Create AWS account (Free Tier)
2. Setup EC2 instance (t2.micro)
3. Setup RDS MySQL instance (db.t2.micro)
4. Configure Security Groups
5. Deploy application to EC2
6. Test deployed application
7. Document deployment process

**Deliverables:**
- Application deployed on AWS
- Accessible via public URL
- Database on RDS working
- Screenshots for report

### Phase 10: Project Report Writing (Week 4-5)
**Tasks:**
1. Write abstract and project description
2. Document requirements and use cases
3. Create class diagrams
4. Document database schema
5. Explain feature development process
6. Document deployment architecture
7. Write conclusion and references
8. Format according to guidelines

**Deliverables:**
- Complete 40+ page project report
- All diagrams and screenshots included
- Properly formatted and cited

---

## 10. KEY FEATURES FOR OPTIMIZATION (For Report)

### Feature 1: Monthly Summary Report Optimization

**Before Optimization:**
```java
// Slow query - Multiple database hits
public MonthlySummary getMonthlyReport(int month, int year) {
    List<Transaction> transactions = getAllTransactionsForMonth(month, year);
    BigDecimal totalIncome = transactions.stream()
        .filter(t -> t.getType() == INCOME)
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    // ... more calculations
}
```
**Performance:** ~2000ms for 1000 transactions

**After Optimization:**
```java
// Optimized with custom query
@Query("SELECT new com.expense.tracker.dto.MonthlySummary(...) " +
       "FROM Transaction t WHERE t.userId = :userId " +
       "AND MONTH(t.transactionDate) = :month " +
       "AND YEAR(t.transactionDate) = :year")
MonthlySummary getMonthlyReport(@Param("userId") Long userId, 
                                @Param("month") int month,
                                @Param("year") int year);
```
**Performance:** ~150ms for 1000 transactions

**Further Optimization with Redis Cache:**
```java
@Cacheable(value = "monthlyReports", 
           key = "#userId + '-' + #month + '-' + #year")
public MonthlySummary getMonthlyReport(Long userId, int month, int year) {
    // Query database only if cache miss
}
```
**Performance:** ~10ms (cache hit)

**Metrics:**
- Without optimization: 2000ms
- With query optimization: 150ms (92.5% improvement)
- With caching: 10ms (99.5% improvement)

### Feature 2: Transaction Search Optimization

**Database Indexes Added:**
```sql
CREATE INDEX idx_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_user_type_date ON transactions(user_id, type, transaction_date);
CREATE INDEX idx_category ON transactions(category_id);
CREATE INDEX idx_amount ON transactions(amount);
```

**Before Indexes:**
- Search query: Full table scan
- Performance: ~800ms for 10,000 records

**After Indexes:**
- Search query: Index scan
- Performance: ~50ms for 10,000 records
- Improvement: 94% faster

### Feature 3: Budget Alerts Calculation

**Optimization Strategy:**
1. Calculate spent amount using SUM aggregate query
2. Cache budget alerts for current month
3. Invalidate cache when new transaction added

**Performance Metrics:**
- Initial calculation: ~100ms
- Cached retrieval: ~5ms
- 95% improvement on subsequent requests

---

## 11. USE CASE DIAGRAMS

### 11.1 High-Level Use Cases

**Actors:**
- Registered User
- System (for automated tasks)

**Use Cases:**
1. Register Account
2. Login to System
3. Manage Transactions (Add, View, Update, Delete)
4. Manage Categories (Create, View, Update, Delete)
5. Manage Budgets (Set, View, Update, Delete)
6. View Budget Alerts
7. Generate Reports (Monthly, Category-wise, Trends)
8. Update Profile

### 11.2 Detailed Use Case: Add Transaction

**Use Case Name:** Add Transaction
**Actor:** Registered User
**Precondition:** User is logged in with valid JWT token
**Main Flow:**
1. User sends POST request to /api/transactions with transaction data
2. System validates JWT token
3. System validates input data (amount, date, category)
4. System checks if category exists and belongs to user (or is system category)
5. System creates transaction record in database
6. System invalidates cached reports (if using Redis)
7. System returns success response with transaction details

**Alternate Flows:**
- 3a. Validation fails → Return 400 error with validation messages
- 4a. Category not found → Return 404 error
- 7a. Database error → Return 500 error

**Postcondition:** Transaction is saved and appears in user's transaction list

### 11.3 Detailed Use Case: Get Budget Alerts

**Use Case Name:** Get Budget Alerts
**Actor:** Registered User
**Precondition:** User is logged in and has set budgets
**Main Flow:**
1. User sends GET request to /api/budgets/alerts
2. System validates JWT token
3. System retrieves all budgets for current month
4. For each budget, system calculates spent amount from transactions
5. System compares spent amount with budget limit
6. System generates alerts for budgets exceeding 80%
7. System returns list of alerts with details

**Business Rules:**
- WARNING alert: 80-99% of budget used
- LIMIT_REACHED alert: Exactly 100% used
- EXCEEDED alert: More than 100% used

**Postcondition:** User receives current budget status

---

## 12. CLASS DIAGRAM DESCRIPTION

### 12.1 Core Entities

**User Entity:**
- Attributes: id, name, email, password (hashed), createdAt, updatedAt, isActive
- Relationships: One-to-Many with Transaction, Budget, Category

**Transaction Entity:**
- Attributes: id, userId, categoryId, type, amount, transactionDate, description, paymentMethod, createdAt, updatedAt, isDeleted
- Relationships: Many-to-One with User, Many-to-One with Category
- Enums: TransactionType (INCOME, EXPENSE)

**Category Entity:**
- Attributes: id, name, type, isSystemCategory, userId, createdAt
- Relationships: Many-to-One with User (for custom categories), One-to-Many with Transaction, One-to-Many with Budget
- Enums: CategoryType (INCOME, EXPENSE)

**Budget Entity:**
- Attributes: id, userId, categoryId, budgetAmount, month, year, createdAt, updatedAt
- Relationships: Many-to-One with User, Many-to-One with Category
- Calculated Fields: spentAmount, remainingAmount, percentageUsed

### 12.2 Service Layer Classes

**AuthService:**
- Methods: register(), login(), validateToken()

**TransactionService:**
- Methods: createTransaction(), getTransactions(), getTransactionById(), updateTransaction(), deleteTransaction()

**BudgetService:**
- Methods: setBudget(), getBudgets(), updateBudget(), deleteBudget(), getBudgetAlerts()

**ReportService:**
- Methods: getMonthlySummary(), getCategoryWiseReport(), getTrends(), getTopExpenses()

**CategoryService:**
- Methods: getAllCategories(), createCategory(), updateCategory(), deleteCategory()

---

## 13. TESTING STRATEGY

### 13.1 Unit Testing
**Test Coverage Areas:**
- Service layer methods
- Utility classes
- Validation logic
- JWT token generation and validation

**Sample Test Cases:**
```java
@Test
void testCreateTransaction_Success() {
    // Test successful transaction creation
}

@Test
void testCreateTransaction_InvalidAmount() {
    // Test negative amount validation
}

@Test
void testCalculateBudgetPercentage() {
    // Test budget calculation logic
}
```

### 13.2 Integration Testing
**Test Areas:**
- Controller endpoints
- Database operations
- Security filters
- Exception handling

**Sample Integration Test:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {
    @Test
    void testCreateTransaction_WithValidToken() {
        // Test endpoint with authentication
    }
}
```

### 13.3 Postman Testing
**Collections to Create:**
1. Authentication (Register, Login)
2. Transactions (CRUD operations)
3. Categories (CRUD operations)
4. Budgets (CRUD operations)
5. Reports (All report endpoints)

**Test Scenarios:**
- Happy path scenarios
- Validation error scenarios
- Authentication failure scenarios
- Permission denied scenarios

---

## 14. DEPLOYMENT ARCHITECTURE

### 14.1 AWS Components

**EC2 Instance:**
- Type: t2.micro (Free Tier eligible)
- OS: Ubuntu 22.04 LTS
- Java: OpenJDK 17
- Application: Spring Boot JAR file
- Port: 8080

**RDS MySQL:**
- Instance: db.t2.micro (Free Tier eligible)
- Engine: MySQL 8.0
- Storage: 20 GB (Free Tier)
- Multi-AZ: No (to save costs)

**VPC Configuration:**
- Custom VPC with public and private subnets
- EC2 in public subnet (with Elastic IP)
- RDS in private subnet

**Security Groups:**
1. EC2 Security Group:
   - Inbound: Port 22 (SSH), Port 8080 (HTTP)
   - Outbound: All traffic
2. RDS Security Group:
   - Inbound: Port 3306 from EC2 security group only
   - Outbound: All traffic

### 14.2 Deployment Steps Summary

1. **Create RDS Instance**
   - Launch MySQL database
   - Note down endpoint URL
   - Configure security group

2. **Create EC2 Instance**
   - Launch Ubuntu instance
   - Generate and download key pair
   - Configure security group

3. **Setup EC2**
   - SSH into instance
   - Install Java 17
   - Install Maven
   - Clone project from GitHub

4. **Configure Application**
   - Update application.properties with RDS endpoint
   - Build JAR file: `mvn clean package`
   - Run application: `java -jar expense-tracker.jar`

5. **Setup as Service** (Optional)
   - Create systemd service file
   - Enable auto-start on boot

6. **Test Deployment**
   - Access via EC2 public IP
   - Test all endpoints with Postman
   - Verify database connectivity

### 14.3 Environment Variables
```bash
export DB_HOST=your-rds-endpoint.rds.amazonaws.com
export DB_PORT=3306
export DB_NAME=expense_tracker
export DB_USERNAME=admin
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
```

---

## 15. COPILOT PROMPTS FOR DEVELOPMENT

### 15.1 Initial Setup Prompts

**Prompt 1: Project Creation**
```
Create a Spring Boot 3.2 project with the following dependencies:
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL Driver
- Lombok
- Spring Validation
- JWT (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson version 0.12.3)
- springdoc-openapi-starter-webmvc-ui version 2.2.0

Create the basic project structure with packages: controller, service, repository, model, dto, exception, security, config, util
```

**Prompt 2: Database Configuration**
```
Create application.properties file with MySQL configuration for database named "expense_tracker", include JPA/Hibernate settings with ddl-auto=update, show-sql=true, and HikariCP connection pool settings (min 5, max 20 connections)
```

### 15.2 Entity Creation Prompts

**Prompt 3: User Entity**
```
Create a User JPA entity with:
- id (Long, auto-generated)
- name (String, not null, max 100)
- email (String, unique, not null, max 255)
- password (String, not null)
- createdAt, updatedAt (Timestamp, auto-managed)
- isActive (Boolean, default true)

Use Lombok annotations (@Data, @Entity, @Table). Add proper JPA annotations and indexes on email field.
```

**Prompt 4: Transaction Entity**
```
Create a Transaction JPA entity with:
- id (Long, auto-generated)
- user (ManyToOne relationship with User)
- category (ManyToOne relationship with Category)
- type (Enum: INCOME, EXPENSE)
- amount (BigDecimal, not null, precision 15 scale 2)
- transactionDate (LocalDate, not null)
- description (String, max 500, nullable)
- paymentMethod (String, max 50, nullable)
- createdAt, updatedAt (Timestamp)
- isDeleted (Boolean, default false)

Add indexes on: (userId, transactionDate), (userId, type, transactionDate), categoryId
```

**Prompt 5: Category Entity**
```
Create a Category JPA entity with:
- id (Long, auto-generated)
- name (String, not null, max 100)
- type (Enum: INCOME, EXPENSE)
- isSystemCategory (Boolean, default false)
- user (ManyToOne, nullable - null means system category)
- createdAt (Timestamp)

Add unique constraint on (userId, name). Add index on (userId, type).
```

**Prompt 6: Budget Entity**
```
Create a Budget JPA entity with:
- id (Long, auto-generated)
- user (ManyToOne with User)
- category (ManyToOne with Category)
- budgetAmount (BigDecimal, not null, precision 15 scale 2)
- month (Integer, 1-12)
- year (Integer)
- createdAt, updatedAt (Timestamp)

Add unique constraint on (userId, categoryId, month, year). Add index on (userId, month, year).
```

### 15.3 Repository Prompts

**Prompt 7: TransactionRepository**
```
Create TransactionRepository interface extending JpaRepository<Transaction, Long>.
Add custom query methods:
1. findByUserIdAndIsDeletedFalse with Pageable
2. findByUserIdAndTypeAndIsDeletedFalse
3. findByUserIdAndTransactionDateBetweenAndIsDeletedFalse
4. Custom @Query to get monthly summary with SUM of income, SUM of expenses, COUNT
5. Custom @Query to get category-wise expenses grouped by category

Use @Param annotations for query parameters.
```

**Prompt 8: BudgetRepository**
```
Create BudgetRepository extending JpaRepository<Budget, Long>.
Add methods:
1. findByUserIdAndMonthAndYear
2. findByUserIdAndCategoryIdAndMonthAndYear
3. Custom @Query to get budget with spent amount (JOIN with transactions SUM)
```

### 15.4 Service Layer Prompts

**Prompt 9: AuthService**
```
Create AuthService class with methods:
1. register(RegisterRequest) - hash password with BCrypt, save user, generate JWT
2. login(LoginRequest) - validate credentials, generate JWT
3. Private method to generate JWT token with user email as subject, expiration 24 hours

Inject UserRepository, PasswordEncoder, JwtUtil.
Handle exceptions: DuplicateResourceException for existing email, UnauthorizedException for invalid credentials.
```

**Prompt 10: TransactionService**
```
Create TransactionService with methods:
1. createTransaction(TransactionRequest, User) - validate category exists, save transaction
2. getTransactions(filters, pageable, User) - support filtering by type, category, date range, amount range
3. getTransactionById(id, User) - check ownership
4. updateTransaction(id, TransactionRequest, User) - check ownership, update
5. deleteTransaction(id, User) - soft delete, check ownership

Inject TransactionRepository, CategoryRepository.
Throw ResourceNotFoundException, ForbiddenException as needed.
```

**Prompt 11: BudgetService**
```
Create BudgetService with methods:
1. setBudget(BudgetRequest, User) - validate category is EXPENSE type, save or update budget
2. getBudgets(month, year, User) - get all budgets with spent amount calculated
3. getBudgetAlerts(User) - calculate percentage used, return alerts for 80%+
4. updateBudget(id, amount, User) - check ownership, update
5. deleteBudget(id, User) - check ownership, delete

Calculate spent amount by querying transactions SUM grouped by category for given month/year.
Generate alert types: WARNING (80-99%), LIMIT_REACHED (100%), EXCEEDED (>100%).
```

**Prompt 12: ReportService**
```
Create ReportService with methods:
1. getMonthlySummary(month, year, User) - total income, expenses, savings, transaction count
2. getCategoryWiseReport(month, year, User) - expenses grouped by category with percentages
3. getTrends(months, User) - last N months income vs expense data
4. getTopExpenses(limit, month, year, User) - top N transactions by amount

Use custom repository queries for aggregations.
Consider adding @Cacheable annotation for frequently accessed reports.
```

### 15.5 Controller Prompts

**Prompt 13: AuthController**
```
Create AuthController with:
- POST /api/auth/register mapped to authService.register()
- POST /api/auth/login mapped to authService.login()

Use @RestController, @RequestMapping("/api/auth")
Validate request body with @Valid
Return ResponseEntity with appropriate HTTP status
Handle exceptions with @ExceptionHandler or global handler
```

**Prompt 14: TransactionController**
```
Create TransactionController with:
- POST /api/transactions - create transaction
- GET /api/transactions - list with filters (type, categoryId, fromDate, toDate, minAmount, maxAmount) and pagination
- GET /api/transactions/{id} - get by id
- PUT /api/transactions/{id} - update
- DELETE /api/transactions/{id} - soft delete

Use @PreAuthorize or get User from SecurityContext.
Add @ApiOperation annotations for Swagger documentation.
Validate all inputs with @Valid and custom validators.
```

**Prompt 15: BudgetController and ReportController**
```
Create BudgetController with budget CRUD endpoints and alerts endpoint.
Create ReportController with monthly-summary, category-wise, trends, and top-expenses endpoints.

Both controllers should:
- Use proper REST conventions
- Extract authenticated user from SecurityContext
- Return ResponseEntity with appropriate status codes
- Include Swagger annotations
- Handle all exceptions properly
```

### 15.6 Security Configuration Prompts

**Prompt 16: JWT Configuration**
```
Create JwtUtil class with methods:
1. generateToken(String email) - create JWT with claims, expiration
2. validateToken(String token) - verify signature and expiration
3. getEmailFromToken(String token) - extract subject/email
4. isTokenExpired(String token) - check expiration date

Use secret key from application.properties (jwt.secret)
Use io.jsonwebtoken.Jwts for token operations
```

**Prompt 17: Security Configuration**
```
Create SecurityConfig class with:
1. SecurityFilterChain bean - disable CSRF, configure CORS, set session management to STATELESS
2. PasswordEncoder bean - BCryptPasswordEncoder with strength 12
3. AuthenticationManager bean
4. Configure public endpoints: /api/auth/**, /swagger-ui/**, /api-docs/**
5. All other endpoints require authentication
6. Add JWT authentication filter before UsernamePasswordAuthenticationFilter

Use @Configuration, @EnableWebSecurity, @EnableMethodSecurity
```

**Prompt 18: JWT Authentication Filter**
```
Create JwtAuthenticationFilter extending OncePerRequestFilter:
1. Extract JWT token from Authorization header (Bearer scheme)
2. Validate token using JwtUtil
3. Extract user email from token
4. Load UserDetails and set authentication in SecurityContext
5. Continue filter chain

Handle exceptions gracefully, log errors.
```

### 15.7 Exception Handling Prompts

**Prompt 19: Custom Exceptions**
```
Create custom exception classes:
1. ResourceNotFoundException extends RuntimeException
2. DuplicateResourceException extends RuntimeException
3. UnauthorizedException extends RuntimeException
4. ForbiddenException extends RuntimeException

Each should accept message and optional cause.
```

**Prompt 20: Global Exception Handler**
```
Create GlobalExceptionHandler class with @ControllerAdvice:
1. Handle ResourceNotFoundException - return 404
2. Handle DuplicateResourceException - return 409
3. Handle UnauthorizedException - return 401
4. Handle ForbiddenException - return 403
5. Handle MethodArgumentNotValidException - return 400 with field errors
6. Handle generic Exception - return 500

Return consistent error response format with timestamp, status, error, message, errors array, path.
```

### 15.8 Data Seeding Prompt

**Prompt 21: System Categories Seed Data**
```
Create data.sql file in src/main/resources with INSERT statements for system categories:
Income categories: Salary, Freelance, Investment, Gift, Other Income
Expense categories: Food & Dining, Transportation, Housing, Utilities, Healthcare, Entertainment, Shopping, Education, Insurance, Other Expense

Set is_system_category = true and user_id = null for all.
```

### 15.9 Testing Prompts

**Prompt 22: Service Unit Tests**
```
Create unit tests for TransactionService:
1. testCreateTransaction_Success - mock repository save, verify result
2. testCreateTransaction_CategoryNotFound - verify exception thrown
3. testGetTransactions_WithFilters - test filtering logic
4. testDeleteTransaction_NotOwner - verify ForbiddenException

Use JUnit 5, Mockito. Mock all dependencies.
```

**Prompt 23: Controller Integration Tests**
```
Create integration test for TransactionController:
1. Test POST /api/transactions with valid token - expect 201
2. Test POST /api/transactions without token - expect 401
3. Test POST /api/transactions with invalid data - expect 400
4. Test GET /api/transactions with filters - verify response
5. Test DELETE /api/transactions/{id} not owned - expect 403

Use @SpringBootTest, @AutoConfigureMockMvc, MockMvc
Create helper methods to generate JWT tokens for testing.
```

### 15.10 Optimization Prompts

**Prompt 24: Database Indexes**
```
Create migration script or SQL file to add indexes:
1. CREATE INDEX idx_user_date ON transactions(user_id, transaction_date);
2. CREATE INDEX idx_user_type_date ON transactions(user_id, type, transaction_date);
3. CREATE INDEX idx_category ON transactions(category_id);
4. CREATE INDEX idx_email ON users(email);
5. CREATE INDEX idx_user_month_year ON budgets(user_id, month, year);

Document the performance improvement with EXPLAIN query analysis.
```

**Prompt 25: Redis Caching**
```
Add Redis caching configuration:
1. Create CacheConfig class with @EnableCaching
2. Configure RedisCacheManager with TTL settings
3. Add @Cacheable annotation to ReportService methods
4. Add @CacheEvict when transactions are created/updated/deleted
5. Cache keys should include userId, month, year

Test cache hit/miss scenarios and document performance improvements.
```

**Prompt 26: Query Optimization**
```
Optimize ReportService queries:
1. Replace multiple queries with single aggregate query using GROUP BY
2. Use @Query with JPQL for complex aggregations
3. Use native SQL query if JPQL is insufficient
4. Add @EntityGraph to avoid N+1 query problems in transactions with categories
5. Use projections for report DTOs to fetch only required fields

Benchmark query execution time before and after optimization.
```

### 15.11 Documentation Prompts

**Prompt 27: Swagger Configuration**
```
Create SwaggerConfig class:
1. Configure OpenAPI 3.0 documentation
2. Add API info (title, version, description, contact)
3. Add security scheme for JWT Bearer authentication
4. Add global security requirement
5. Customize Swagger UI path

Add @Operation, @ApiResponse annotations to all controller methods.
```

**Prompt 28: README File**
```
Create comprehensive README.md with:
1. Project overview and features
2. Technology stack
3. Prerequisites (Java 17, MySQL 8, Maven)
4. Setup instructions (database creation, configuration)
5. How to run locally
6. API documentation link (Swagger UI)
7. Testing instructions
8. Deployment guide
9. Environment variables
10. Troubleshooting common issues
```

---

## 16. POSTMAN COLLECTION STRUCTURE

### 16.1 Collection Organization

**Folder 1: Authentication**
- POST Register User
- POST Login User

**Folder 2: User Profile**
- GET User Profile
- PUT Update Profile

**Folder 3: Transactions**
- POST Create Transaction
- GET All Transactions
- GET All Transactions (with filters)
- GET Transaction by ID
- PUT Update Transaction
- DELETE Transaction

**Folder 4: Categories**
- GET All Categories
- GET Categories by Type
- POST Create Custom Category
- PUT Update Category
- DELETE Category

**Folder 5: Budgets**
- POST Set Budget
- GET All Budgets
- GET Budgets for Specific Month
- PUT Update Budget
- DELETE Budget
- GET Budget Alerts

**Folder 6: Reports**
- GET Monthly Summary
- GET Category-wise Report
- GET Trends (6 months)
- GET Top Expenses

### 16.2 Environment Variables Setup

**Postman Environment:**
```json
{
  "name": "Expense Tracker - Local",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "auth_token",
      "value": "",
      "enabled": true
    },
    {
      "key": "user_id",
      "value": "",
      "enabled": true
    }
  ]
}
```

### 16.3 Pre-request Scripts

**For authenticated endpoints:**
```javascript
// Add to Collection Pre-request Script
if (pm.environment.get("auth_token")) {
    pm.request.headers.add({
        key: 'Authorization',
        value: 'Bearer ' + pm.environment.get("auth_token")
    });
}
```

### 16.4 Test Scripts

**For Login Request:**
```javascript
// Save token to environment
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("auth_token", jsonData.token);
    pm.environment.set("user_id", jsonData.user.id);
    console.log("Token saved:", jsonData.token);
}
```

**Generic Success Test:**
```javascript
pm.test("Status code is 200 or 201", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});

pm.test("Response has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('message');
});
```

---

## 17. SAMPLE DATA FOR TESTING

### 17.1 User Registration Data
```json
{
  "name": "Rahul Sharma",
  "email": "rahul.sharma@example.com",
  "password": "SecurePass123"
}
```

### 17.2 Sample Transactions

**Income:**
```json
{
  "type": "INCOME",
  "categoryId": 1,
  "amount": 50000.00,
  "transactionDate": "2024-10-01",
  "description": "Monthly salary",
  "paymentMethod": "Bank Transfer"
}
```

**Expenses:**
```json
[
  {
    "type": "EXPENSE",
    "categoryId": 8,
    "amount": 15000.00,
    "transactionDate": "2024-10-01",
    "description": "Monthly rent",
    "paymentMethod": "Bank Transfer"
  },
  {
    "type": "EXPENSE",
    "categoryId": 6,
    "amount": 2500.00,
    "transactionDate": "2024-10-05",
    "description": "Grocery shopping",
    "paymentMethod": "Credit Card"
  },
  {
    "type": "EXPENSE",
    "categoryId": 7,
    "amount": 350.00,
    "transactionDate": "2024-10-06",
    "description": "Uber rides",
    "paymentMethod": "UPI"
  },
  {
    "type": "EXPENSE",
    "categoryId": 11,
    "amount": 649.00,
    "transactionDate": "2024-10-10",
    "description": "Netflix subscription",
    "paymentMethod": "Credit Card"
  }
]
```

### 17.3 Sample Budgets
```json
[
  {
    "categoryId": 6,
    "budgetAmount": 8000.00,
    "month": 10,
    "year": 2024
  },
  {
    "categoryId": 7,
    "budgetAmount": 3000.00,
    "month": 10,
    "year": 2024
  },
  {
    "categoryId": 11,
    "budgetAmount": 2000.00,
    "month": 10,
    "year": 2024
  }
]
```

---

## 18. PERFORMANCE BENCHMARKS

### 18.1 Target Metrics

**API Response Times (95th percentile):**
- Authentication endpoints: < 300ms
- CRUD operations: < 200ms
- Simple reports: < 500ms
- Complex aggregations: < 1000ms
- With caching: < 50ms

**Database Query Performance:**
- Simple SELECT: < 10ms
- With JOIN (2-3 tables): < 50ms
- Aggregation queries: < 100ms
- Filtered pagination: < 100ms

**Concurrent Users:**
- Support 100 concurrent users
- No degradation up to 50 users
- Graceful degradation 50-100 users

### 18.2 Load Testing Scenarios

**Scenario 1: Normal Load**
- 20 concurrent users
- Mixed operations (70% reads, 30% writes)
- Duration: 5 minutes
- Expected: All requests < 500ms

**Scenario 2: Peak Load**
- 100 concurrent users
- Mixed operations
- Duration: 10 minutes
- Expected: 95% requests < 1000ms

**Scenario 3: Report Generation**
- 50 users requesting monthly reports simultaneously
- Expected: < 500ms per request (cached)

### 18.3 Monitoring Points

**Application Metrics:**
- Average response time
- 95th percentile response time
- Error rate
- Throughput (requests/second)

**Database Metrics:**
- Active connections
- Query execution time
- Slow query log
- Connection pool utilization

**System Metrics:**
- CPU utilization
- Memory usage
- Disk I/O
- Network throughput

---

## 19. SECURITY CONSIDERATIONS

### 19.1 Authentication & Authorization
- ✅ Passwords hashed with BCrypt (strength 12)
- ✅ JWT tokens expire after 24 hours
- ✅ No sensitive data in JWT payload
- ✅ User can only access their own data
- ✅ Token validation on every protected endpoint

### 19.2 Input Validation
- ✅ All inputs validated with Bean Validation
- ✅ SQL injection prevention (Prepared Statements)
- ✅ XSS prevention (Input sanitization)
- ✅ Maximum string lengths enforced
- ✅ Date ranges validated

### 19.3 Data Protection
- ✅ Passwords never returned in API responses
- ✅ Soft delete for transactions (data retention)
- ✅ Database credentials in environment variables
- ✅ JWT secret in secure configuration

### 19.4 API Security
- ✅ CORS configuration for specific origins
- ✅ Rate limiting (optional with Spring Cloud Gateway)
- ✅ HTTPS in production (AWS ELB with SSL)
- ✅ Security headers (X-Frame-Options, X-Content-Type-Options)

---

## 20. TROUBLESHOOTING GUIDE

### 20.1 Common Issues & Solutions

**Issue 1: Application won't start - Port 8080 already in use**
```bash
Solution:
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
# Or change port in application.properties
server.port=8081
```

**Issue 2: Cannot connect to MySQL**
```
Solution:
1. Verify MySQL is running: sudo systemctl status mysql
2. Check connection details in application.properties
3. Verify database exists: CREATE DATABASE IF NOT EXISTS expense_tracker;
4. Check user permissions: GRANT ALL ON expense_tracker.* TO 'username'@'localhost';
```

**Issue 3: JWT token expired error**
```
Solution:
1. Login again to get new token
2. Check token expiration time (24 hours default)
3. Update token in Postman environment
```

**Issue 4: 403 Forbidden when accessing own resources**
```
Solution:
1. Verify JWT token is valid
2. Check SecurityContext has authenticated user
3. Verify userId in request matches token userId
4. Check Spring Security configuration
```

**Issue 5: Slow query performance**
```
Solution:
1. Check if indexes are created: SHOW INDEX FROM transactions;
2. Analyze query: EXPLAIN SELECT ...
3. Enable query logging: spring.jpa.show-sql=true
4. Check connection pool settings
5. Consider adding Redis cache
```

**Issue 6: OutOfMemoryError**
```
Solution:
1. Increase JVM heap size: java -Xmx512m -jar app.jar
2. Check for memory leaks (N+1 queries)
3. Add pagination to large result sets
4. Use projections instead of full entities
```

---

## 21. PROJECT MILESTONES & DELIVERABLES

### Week 1: Foundation
**Deliverables:**
- ✅ Project setup complete
- ✅ Database schema created
- ✅ User authentication working
- ✅ Category management complete
- ✅ Basic Postman collection

### Week 2: Core Features
**Deliverables:**
- ✅ Transaction CRUD complete
- ✅ Budget management complete
- ✅ Filtering and pagination working
- ✅ Comprehensive validation
- ✅ Updated Postman collection

### Week 3: Advanced Features
**Deliverables:**
- ✅ All reports working
- ✅ Performance optimization done
- ✅ Caching implemented
- ✅ Unit tests written
- ✅ Swagger documentation complete

### Week 4: Deployment & Documentation
**Deliverables:**
- ✅ AWS deployment complete
- ✅ Integration tests complete
- ✅ Project report draft
- ✅ All diagrams created
- ✅ Final testing done

### Week 5: Finalization
**Deliverables:**
- ✅ Project report complete (40+ pages)
- ✅ All screenshots and diagrams
- ✅ Proper formatting applied
- ✅ References cited
- ✅ Final submission ready

---

## 22. SUCCESS CRITERIA

### 22.1 Functional Completeness
- ✅ All 25+ API endpoints working
- ✅ Authentication and authorization functional
- ✅ All CRUD operations complete
- ✅ Reports generating accurate data
- ✅ Budget alerts working correctly

### 22.2 Technical Quality
- ✅ Code follows Spring Boot best practices
- ✅ Proper MVC architecture
- ✅ Clean code with meaningful names
- ✅ Comprehensive error handling
- ✅ Consistent API response format

### 22.3 Performance
- ✅ API response < 200ms for CRUD
- ✅ Reports < 500ms
- ✅ Database queries optimized
- ✅ Caching implemented
- ✅ Can handle 100 concurrent users

### 22.4 Testing
- ✅ Unit test coverage > 70%
- ✅ Integration tests for controllers
- ✅ All endpoints tested in Postman
- ✅ Edge cases handled

### 22.5 Deployment
- ✅ Successfully deployed on AWS
- ✅ Accessible via public URL
- ✅ Database on RDS working
- ✅ Security groups configured
- ✅ Application stable

### 22.6 Documentation
- ✅ 40+ page project report
- ✅ All required sections complete
- ✅ Class diagrams included
- ✅ Database schema documented
- ✅ Deployment architecture explained
- ✅ Performance optimization detailed
- ✅ Proper formatting and citations

---

## 23. ADDITIONAL RESOURCES

### 23.1 Learning Resources

**Spring Boot:**
- Official Spring Boot Documentation: https://spring.io/projects/spring-boot
- Baeldung Spring Tutorials: https://www.baeldung.com/spring-boot
- Spring Security JWT: https://www.baeldung.com/spring-security-oauth-jwt

**Database Design:**
- MySQL Documentation: https://dev.mysql.com/doc/
- JPA/Hibernate Guide: https://docs.jboss.org/hibernate/orm/current/userguide/html_single/

**AWS Deployment:**
- AWS Free Tier: https://aws.amazon.com/free/
- Deploy Spring Boot on EC2: AWS Documentation
- RDS MySQL Setup: AWS RDS Documentation

**Testing:**
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/

### 23.2 Tools & Software

**Required Software:**
- Java 17 JDK
- MySQL 8.0
- Maven 3.8+
- IntelliJ IDEA / VS Code with Java extensions
- Postman
- Git

**Optional Tools:**
- Docker (for containerization)
- Redis (for caching)
- DBeaver / MySQL Workbench (database GUI)
- Draw.io (for diagrams)

### 23.3 Useful Commands

**Maven Commands:**
```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Skip tests during build
mvn clean install -DskipTests

# Run tests only
mvn test

# Package as JAR
mvn clean package
```

**MySQL Commands:**
```sql
-- Create database
CREATE DATABASE expense_tracker;

-- Use database
USE expense_tracker;

-- Show tables
SHOW TABLES;

-- Describe table
DESCRIBE transactions;

-- Check indexes
SHOW INDEX FROM transactions;
```

**Git Commands:**
```bash
# Initialize repository
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit"

# Push to GitHub
git remote add origin <your-repo-url>
git push -u origin main
```

---

## 24. FINAL CHECKLIST

### Before Starting Development:
- [ ] Read entire requirements document
- [ ] Set up development environment (Java, MySQL, Maven)
- [ ] Create GitHub repository
- [ ] Set up MySQL database
- [ ] Install Postman

### During Development:
- [ ] Follow the implementation phases
- [ ] Test each module after completion
- [ ] Update Postman collection regularly
- [ ] Write clean, documented code
- [ ] Commit code regularly to Git
- [ ] Create class diagrams as you go

### Before Deployment:
- [ ] All endpoints tested in Postman
- [ ] Unit tests written and passing
- [ ] Integration tests complete
- [ ] Performance optimization done
- [ ] Swagger documentation verified
- [ ] README file complete

### Deployment Phase:
- [ ] AWS account created (Free Tier)
- [ ] RDS instance created and accessible
- [ ] EC2 instance launched and configured
- [ ] Application deployed and running
- [ ] Security groups configured properly
- [ ] Application accessible via public URL
- [ ] All endpoints tested on deployed version

### Report Writing:
- [ ] Abstract written (under 300 words)
- [ ] Project description complete
- [ ] Requirements documented
- [ ] Use case diagrams created
- [ ] Class diagrams included
- [ ] Database schema documented
- [ ] Feature development explained
- [ ] Performance optimization detailed
- [ ] Deployment flow documented
- [ ] Technologies explained
- [ ] Conclusion written
- [ ] References cited properly
- [ ] All formatting guidelines followed
- [ ] 40+ pages completed
- [ ] Proofread for errors

### Final Submission:
- [ ] Project report in DOCX format
- [ ] All images and diagrams embedded
- [ ] Table of contents generated
- [ ] Page numbers added
- [ ] Declaration and acknowledgment signed
- [ ] Supervisor approval obtained
- [ ] GitHub repository link included
- [ ] Postman collection exported
- [ ] AWS deployment screenshots included
- [ ] Final review complete

---

## 25. CONCLUSION

This comprehensive requirements document provides everything you need to build a professional-grade **Smart Expense Tracker** backend system using Spring Boot. The project is designed to:

1. **Meet all academic requirements** - 40+ page report with all required sections
2. **Demonstrate technical skills** - RESTful APIs, JWT authentication, database design, optimization
3. **Show real-world application** - Practical personal finance management system
4. **Be achievable in 4-5 weeks** - Well-structured phases and clear deliverables
5. **Stay within budget** - 100% free using AWS Free Tier and open-source technologies

### Key Strengths of This Project:
- ✅ Clear, well-documented requirements
- ✅ Comprehensive API specifications
- ✅ Detailed database schema
- ✅ Step-by-step implementation guide
- ✅ Performance optimization opportunities
- ✅ Copilot-friendly prompts
- ✅ Complete testing strategy
- ✅ AWS deployment guide
- ✅ Report writing guidance

### Next Steps:
1. **Review this document thoroughly**
2. **Set up your development environment**
3. **Start with Phase 1: Project Setup**
4. **Use Copilot prompts to accelerate development**
5. **Test continuously with Postman**
6. **Document as you build**
7. **Deploy to AWS in Week 4**
8. **Write comprehensive report in Week 4-5**

### Support:
- Use the Copilot prompts provided in Section 15
- Refer to troubleshooting guide for common issues
- Consult learning resources in Section 23
- Follow the implementation phases systematically

**Good luck with your project! You have everything you need to create an excellent backend system and submit a professional master's project report.** 🚀

---

## DOCUMENT VERSION INFORMATION
- **Version:** 1.0
- **Created Date:** October 2024
- **Purpose:** Complete requirements for Smart Expense Tracker Backend Project
- **Target Audience:** Master's student developing Spring Boot backend project
- **Estimated Development Time:** 4-5 weeks
- **Estimated Report Writing Time:** 1 week
- **Total Project Duration:** 5-6 weeks