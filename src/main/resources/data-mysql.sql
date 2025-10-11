-- MySQL Database Setup Script for Smart Expense Tracker
-- Run this script to create the database and initial data

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS expense_tracker 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- System Categories Seed Data for Smart Expense Tracker
-- This file contains predefined categories for income and expense types
-- These categories are available to all users and cannot be modified

-- Note: Using MySQL-specific syntax with ON DUPLICATE KEY UPDATE to prevent errors on re-run

-- Income Categories
INSERT INTO categories (id, name, type, is_system_category, user_id, created_at) VALUES
(1, 'Salary', 'INCOME', true, null, NOW()),
(2, 'Freelance', 'INCOME', true, null, NOW()),
(3, 'Investment', 'INCOME', true, null, NOW()),
(4, 'Gift', 'INCOME', true, null, NOW()),
(5, 'Other Income', 'INCOME', true, null, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Expense Categories
INSERT INTO categories (id, name, type, is_system_category, user_id, created_at) VALUES
(6, 'Food & Dining', 'EXPENSE', true, null, NOW()),
(7, 'Transportation', 'EXPENSE', true, null, NOW()),
(8, 'Housing', 'EXPENSE', true, null, NOW()),
(9, 'Utilities', 'EXPENSE', true, null, NOW()),
(10, 'Healthcare', 'EXPENSE', true, null, NOW()),
(11, 'Entertainment', 'EXPENSE', true, null, NOW()),
(12, 'Shopping', 'EXPENSE', true, null, NOW()),
(13, 'Education', 'EXPENSE', true, null, NOW()),
(14, 'Insurance', 'EXPENSE', true, null, NOW()),
(15, 'Other Expense', 'EXPENSE', true, null, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);
