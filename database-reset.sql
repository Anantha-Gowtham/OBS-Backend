-- OBS Database Reset Script
-- Run this script to clean up the database and resolve schema conflicts

-- First, disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables in the correct order (reverse of dependencies)
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

-- Verify all tables are dropped
SHOW TABLES;

-- The application will now recreate all tables with proper schema on next startup