-- Migration script to update transaction and user_profiles tables
-- Run this script in your MySQL database

USE obs_banking_system;

-- Update transactions table with new fields
ALTER TABLE transactions 
ADD COLUMN transaction_id VARCHAR(50) AFTER flag_reason,
ADD COLUMN recipient_account VARCHAR(50) AFTER transaction_id,
ADD COLUMN recipient_name VARCHAR(100) AFTER recipient_account;

-- Create index on transaction_id for faster lookups
CREATE INDEX idx_transaction_id ON transactions(transaction_id);

-- Update user_profiles table with new fields (if not already done)
ALTER TABLE user_profiles 
ADD COLUMN first_name VARCHAR(100) AFTER aadhaar_number,
ADD COLUMN last_name VARCHAR(100) AFTER first_name,
ADD COLUMN address VARCHAR(500) AFTER last_name;

-- Check the updated table structures
DESCRIBE transactions;
DESCRIBE user_profiles;

-- Show sample data
SELECT id, transaction_id, type, amount, recipient_account, recipient_name FROM transactions LIMIT 5;
SELECT id, user_id, first_name, last_name, phone_number FROM user_profiles LIMIT 5;