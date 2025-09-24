-- MySQL Database Setup for OBS Banking System
-- Execute this script to create the database

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS obs_banking_system;

-- Use the database
USE obs_banking_system;

-- Grant all privileges to root user (for development)
-- GRANT ALL PRIVILEGES ON obs_banking_system.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;

-- Note: Tables will be created automatically by Hibernate when the application starts
-- due to hibernate.ddl-auto=update configuration

-- Optional: You can verify the database was created
-- SHOW DATABASES;
-- SHOW TABLES;
