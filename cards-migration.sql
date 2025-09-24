-- Database migration script for Card Management System
-- Run this script to update cards table with new fields

-- First, check if cards table exists and add missing columns
ALTER TABLE cards 
ADD COLUMN IF NOT EXISTS daily_limit DECIMAL(15, 2) NOT NULL DEFAULT 50000.00,
ADD COLUMN IF NOT EXISTS monthly_limit DECIMAL(15, 2) NOT NULL DEFAULT 500000.00,
ADD COLUMN IF NOT EXISTS contactless_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS online_transaction_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS international_usage_enabled BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS block_reason VARCHAR(200),
ADD COLUMN IF NOT EXISTS last_used TIMESTAMP NULL;

-- Update card status enum to include new values
ALTER TABLE cards MODIFY COLUMN status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'BLOCKED', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'PENDING';

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_status ON cards(status);
CREATE INDEX IF NOT EXISTS idx_cards_type ON cards(card_type);
CREATE INDEX IF NOT EXISTS idx_cards_expiry ON cards(expiry_date);

-- If cards table doesn't exist, create it (for fresh installations)
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    card_number VARCHAR(16) NOT NULL UNIQUE,
    card_holder_name VARCHAR(30) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(3) NOT NULL,
    card_type ENUM('DEBIT', 'CREDIT', 'PREPAID') NOT NULL DEFAULT 'DEBIT',
    status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'BLOCKED', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    pin VARCHAR(4) NOT NULL,
    daily_limit DECIMAL(15, 2) NOT NULL DEFAULT 50000.00,
    monthly_limit DECIMAL(15, 2) NOT NULL DEFAULT 500000.00,
    contactless_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    online_transaction_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    international_usage_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    block_reason VARCHAR(200),
    last_used TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_card_number (card_number),
    INDEX idx_status (status),
    INDEX idx_card_type (card_type),
    INDEX idx_expiry_date (expiry_date)
);

-- Sample data for testing (optional - remove in production)
-- This will create demo cards for existing accounts
/*
INSERT IGNORE INTO cards (account_id, card_number, card_holder_name, expiry_date, cvv, card_type, status, pin, daily_limit, monthly_limit)
SELECT 
    id as account_id,
    CONCAT('4520', LPAD(id, 12, '0')) as card_number,
    'DEMO USER' as card_holder_name,
    DATE_ADD(CURRENT_DATE, INTERVAL 5 YEAR) as expiry_date,
    '123' as cvv,
    'DEBIT' as card_type,
    'ACTIVE' as status,
    '1234' as pin,
    50000.00 as daily_limit,
    500000.00 as monthly_limit
FROM accounts 
WHERE id <= 5; -- Only create for first 5 accounts
*/

-- Verify the migration
SELECT 'Cards table structure updated successfully' AS status;