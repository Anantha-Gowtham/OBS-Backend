-- OBS Database Complete Reset with Test Users
-- This script will drop all tables and recreate them with test data
-- WARNING: This will permanently delete all existing data

-- First, disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all existing tables
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS loan_application;
DROP TABLE IF EXISTS kyc_request;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS branch;
DROP TABLE IF EXISTS users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Create users table with plain text password support
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Will store plain text for testing
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    role ENUM('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'USER') DEFAULT 'USER',
    is_enabled BOOLEAN DEFAULT TRUE,
    is_account_non_expired BOOLEAN DEFAULT TRUE,
    is_account_non_locked BOOLEAN DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create branch table
CREATE TABLE branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    manager_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- Create account table
CREATE TABLE account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT') DEFAULT 'SAVINGS',
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (branch_id) REFERENCES branch(id)
);

-- Create card table
CREATE TABLE card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(16) UNIQUE NOT NULL,
    card_type ENUM('DEBIT', 'CREDIT') NOT NULL,
    card_tier VARCHAR(20) DEFAULT 'BASIC',
    card_network VARCHAR(20) DEFAULT 'RUPAY',
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    cvv VARCHAR(4) NOT NULL,
    expiry_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    credit_limit DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (account_id) REFERENCES account(id)
);

-- Create other necessary tables
CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert test users with plain text passwords
INSERT INTO users (username, password, email, first_name, last_name, phone, date_of_birth, address, role) VALUES
-- Super Admin
('superadmin', 'admin123', 'superadmin@obs.com', 'Super', 'Admin', '+91-9999999999', '1980-01-01', 'OBS Head Office, Mumbai', 'SUPER_ADMIN'),
-- Admin
('admin', 'admin123', 'admin@obs.com', 'System', 'Admin', '+91-9999999998', '1985-05-15', 'OBS Admin Office, Delhi', 'ADMIN'),
-- Manager
('manager', 'manager123', 'manager@obs.com', 'Branch', 'Manager', '+91-9999999997', '1982-03-20', 'Main Branch, Bangalore', 'MANAGER'),
-- Employee
('employee', 'emp123', 'employee@obs.com', 'Bank', 'Employee', '+91-9999999996', '1990-07-10', 'Branch Office, Chennai', 'EMPLOYEE'),
-- Test Users with different balance levels for card tier testing
('user1', 'user123', 'user1@test.com', 'Test', 'User1', '+91-9876543210', '1995-01-15', '123 Test Street, Mumbai', 'USER'),
('user2', 'user123', 'user2@test.com', 'Test', 'User2', '+91-9876543211', '1992-05-20', '456 Test Avenue, Delhi', 'USER'),
('user3', 'user123', 'user3@test.com', 'Test', 'User3', '+91-9876543212', '1988-09-30', '789 Test Road, Bangalore', 'USER'),
('richuser', 'rich123', 'richuser@test.com', 'Rich', 'User', '+91-9876543213', '1985-12-25', '999 Luxury Villa, Mumbai', 'USER'),
('premium', 'premium123', 'premium@test.com', 'Premium', 'Customer', '+91-9876543214', '1980-06-15', '777 Elite Tower, Delhi', 'USER');

-- Insert test branch
INSERT INTO branch (branch_code, branch_name, address, phone, email, manager_id) VALUES
('MAIN001', 'Main Branch Mumbai', 'OBS Main Branch, Nariman Point, Mumbai', '+91-22-12345678', 'main@obs.com', 3);

-- Insert test accounts with different balance levels for card tier testing
INSERT INTO account (account_number, account_type, user_id, branch_id, balance) VALUES
-- Admin accounts
('1000000000000001', 'CURRENT', 1, 1, 1000000.00),
('1000000000000002', 'CURRENT', 2, 1, 500000.00),
('1000000000000003', 'CURRENT', 3, 1, 750000.00),
('1000000000000004', 'SAVINGS', 4, 1, 250000.00),
-- User accounts with different balances for card tier testing
('1000000000000005', 'SAVINGS', 5, 1, 25000.00),    -- Basic tier (0-50K)
('1000000000000006', 'SAVINGS', 6, 1, 125000.00),   -- Silver tier (50K-200K)
('1000000000000007', 'SAVINGS', 7, 1, 350000.00),   -- Gold tier (200K-500K)
('1000000000000008', 'SAVINGS', 8, 1, 2500000.00),  -- Platinum tier (500K-1M) -> actually 2.5M for higher tier
('1000000000000009', 'CURRENT', 9, 1, 8500000.00);  -- Black Elite tier (5M+)

-- Insert default Basic cards for all users (auto-issued)
INSERT INTO card (card_number, card_type, card_tier, card_network, user_id, account_id, cvv, expiry_date) VALUES
('6000123456789001', 'DEBIT', 'BASIC', 'RUPAY', 1, 1, '123', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789002', 'DEBIT', 'BASIC', 'RUPAY', 2, 2, '234', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789003', 'DEBIT', 'BASIC', 'RUPAY', 3, 3, '345', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789004', 'DEBIT', 'BASIC', 'RUPAY', 4, 4, '456', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789005', 'DEBIT', 'BASIC', 'RUPAY', 5, 5, '567', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789006', 'DEBIT', 'BASIC', 'RUPAY', 6, 6, '678', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789007', 'DEBIT', 'BASIC', 'RUPAY', 7, 7, '789', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789008', 'DEBIT', 'BASIC', 'RUPAY', 8, 8, '890', DATE_ADD(CURDATE(), INTERVAL 3 YEAR)),
('6000123456789009', 'DEBIT', 'BASIC', 'RUPAY', 9, 9, '901', DATE_ADD(CURDATE(), INTERVAL 3 YEAR));

-- Show created data
SELECT 'USERS CREATED:' as Info;
SELECT id, username, email, first_name, last_name, role FROM users;

SELECT 'ACCOUNTS CREATED:' as Info;
SELECT a.id, a.account_number, u.username, a.balance, a.account_type 
FROM account a 
JOIN users u ON a.user_id = u.id;

SELECT 'CARDS CREATED:' as Info;
SELECT c.id, c.card_number, u.username, c.card_type, c.card_tier, c.card_network 
FROM card c 
JOIN users u ON c.user_id = u.id;

SELECT 'DATABASE RESET COMPLETE' as Status;