-- Migration script to update user_profiles table with new fields
-- Run this script in your MySQL database

USE obs_banking_system;

-- Add new columns to user_profiles table
ALTER TABLE user_profiles 
ADD COLUMN first_name VARCHAR(100) AFTER aadhaar_number,
ADD COLUMN last_name VARCHAR(100) AFTER first_name,
ADD COLUMN address VARCHAR(500) AFTER last_name;

-- Check the table structure
DESCRIBE user_profiles;

-- Show existing profiles (if any)
SELECT * FROM user_profiles;