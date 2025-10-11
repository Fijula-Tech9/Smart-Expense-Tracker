-- System Categories Seed Data for Smart Expense Tracker
-- This file contains predefined categories for income and expense types
-- These categories are available to all users and cannot be modified

-- Income Categories
INSERT INTO categories (id, name, type, is_system_category, user_id, created_at) VALUES
(1, 'Salary', 'INCOME', true, null, CURRENT_TIMESTAMP()),
(2, 'Freelance', 'INCOME', true, null, CURRENT_TIMESTAMP()),
(3, 'Investment', 'INCOME', true, null, CURRENT_TIMESTAMP()),
(4, 'Gift', 'INCOME', true, null, CURRENT_TIMESTAMP()),
(5, 'Other Income', 'INCOME', true, null, CURRENT_TIMESTAMP());

-- Expense Categories
INSERT INTO categories (id, name, type, is_system_category, user_id, created_at) VALUES
(6, 'Food & Dining', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(7, 'Transportation', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(8, 'Housing', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(9, 'Utilities', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(10, 'Healthcare', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(11, 'Entertainment', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(12, 'Shopping', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(13, 'Education', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(14, 'Insurance', 'EXPENSE', true, null, CURRENT_TIMESTAMP()),
(15, 'Other Expense', 'EXPENSE', true, null, CURRENT_TIMESTAMP());
