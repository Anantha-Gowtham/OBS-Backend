-- Database migration script for new banking features
-- Run this script to add beneficiary and bill payment tables

-- Create beneficiaries table
CREATE TABLE beneficiaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    beneficiary_type ENUM('INTERNAL', 'EXTERNAL', 'UPI', 'IMPS', 'NEFT', 'RTGS') NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    last_used TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_account_number (account_number),
    INDEX idx_active (is_active),
    INDEX idx_verified (is_verified),
    UNIQUE KEY unique_user_account (user_id, account_number)
);

-- Create bill_payments table
CREATE TABLE bill_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    payment_id VARCHAR(50) NOT NULL UNIQUE,
    bill_type ENUM('ELECTRICITY', 'WATER', 'GAS', 'INTERNET', 'MOBILE', 'DTH', 'INSURANCE', 'LOAN_EMI', 'CREDIT_CARD', 'MUNICIPAL_TAX', 'PROPERTY_TAX', 'EDUCATION_FEE', 'HOSPITAL', 'OTHER') NOT NULL,
    biller_name VARCHAR(100) NOT NULL,
    consumer_number VARCHAR(50) NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    due_amount DECIMAL(15, 2),
    due_date TIMESTAMP NULL,
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    description VARCHAR(500),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    account_number VARCHAR(20),
    failure_reason VARCHAR(200),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status),
    INDEX idx_bill_type (bill_type),
    INDEX idx_payment_date (payment_date),
    INDEX idx_consumer_number (consumer_number),
    INDEX idx_transaction_id (transaction_id)
);

-- Add some sample beneficiaries for existing users (optional)
-- This is commented out - uncomment and modify user_id values if needed
/*
INSERT INTO beneficiaries (user_id, beneficiary_name, account_number, ifsc_code, bank_name, beneficiary_type, is_verified) VALUES
(1, 'John Doe', '1234567890123456', 'SBIN0000123', 'State Bank of India', 'EXTERNAL', TRUE),
(1, 'Jane Smith', '9876543210987654', 'HDFC0001234', 'HDFC Bank', 'EXTERNAL', TRUE),
(1, 'Internal Account', '1000000001', 'OBSB0000001', 'OBS Bank', 'INTERNAL', TRUE);
*/

-- Add some sample bill payments for testing (optional)
-- This is commented out - uncomment and modify user_id values if needed
/*
INSERT INTO bill_payments (user_id, payment_id, bill_type, biller_name, consumer_number, consumer_name, amount, status, account_number, description) VALUES
(1, 'BP1001', 'ELECTRICITY', 'State Electricity Board', 'EB123456789', 'John Doe', 1250.00, 'COMPLETED', '1000000001', 'Monthly electricity bill payment'),
(1, 'BP1002', 'MOBILE', 'Airtel', '9876543210', 'John Doe', 299.00, 'COMPLETED', '1000000001', 'Mobile recharge'),
(1, 'BP1003', 'GAS', 'Bharat Gas', 'GAS987654321', 'John Doe', 850.00, 'PENDING', '1000000001', 'LPG cylinder booking');
*/

-- Update transaction types enum to include new payment types
-- Note: This might require recreating the enum depending on MySQL version
-- ALTER TABLE transactions MODIFY COLUMN type ENUM('CREDIT', 'DEBIT', 'TRANSFER', 'DEPOSIT', 'WITHDRAWAL', 'UPI', 'NEFT', 'RTGS', 'PAYMENT') NOT NULL;

COMMIT;