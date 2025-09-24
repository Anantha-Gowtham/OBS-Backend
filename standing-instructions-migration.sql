-- Standing Instructions Migration Script
-- Run this script to create the standing_instructions table

-- Create standing_instructions table
CREATE TABLE IF NOT EXISTS standing_instructions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    instruction_id VARCHAR(50) NOT NULL UNIQUE,
    instruction_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    instruction_type ENUM('FUND_TRANSFER', 'BILL_PAYMENT', 'EMI_PAYMENT', 'LOAN_REPAYMENT', 'INVESTMENT', 'INSURANCE_PREMIUM', 'UTILITY_BILL', 'RECURRING_DEPOSIT') NOT NULL,
    from_account VARCHAR(20) NOT NULL,
    to_account VARCHAR(20) NOT NULL,
    beneficiary_name VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL,
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    next_execution_date DATE NOT NULL,
    last_executed DATETIME NULL,
    status ENUM('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED', 'FAILED') NOT NULL DEFAULT 'ACTIVE',
    execution_count INT NOT NULL DEFAULT 0,
    max_executions INT NULL,
    failure_reason VARCHAR(200) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_user_id (user_id),
    INDEX idx_instruction_id (instruction_id),
    INDEX idx_status (status),
    INDEX idx_next_execution_date (next_execution_date),
    INDEX idx_user_status (user_id, status),
    INDEX idx_from_account (from_account),
    INDEX idx_to_account (to_account),
    INDEX idx_instruction_type (instruction_type),
    INDEX idx_frequency (frequency),
    INDEX idx_created_at (created_at),
    INDEX idx_due_instructions (status, next_execution_date)
);

-- Insert sample standing instructions for testing
INSERT INTO standing_instructions (
    user_id, instruction_id, instruction_name, description, instruction_type,
    from_account, to_account, beneficiary_name, amount, frequency,
    start_date, next_execution_date, status
) VALUES
-- User 1 instructions
(1, 'SI1701234561001', 'Monthly Rent Payment', 'Automatic rent payment to landlord', 'FUND_TRANSFER',
 '1001234567890', '2001234567890', 'John Landlord', 25000.00, 'MONTHLY',
 '2024-01-01', '2024-02-01', 'ACTIVE'),

(1, 'SI1701234561002', 'Weekly Grocery Budget', 'Weekly transfer for grocery expenses', 'FUND_TRANSFER',
 '1001234567890', '1001234567891', 'Grocery Savings', 5000.00, 'WEEKLY',
 '2024-01-07', '2024-01-14', 'ACTIVE'),

(1, 'SI1701234561003', 'Life Insurance Premium', 'Monthly life insurance payment', 'INSURANCE_PREMIUM',
 '1001234567890', 'INS001234567', 'Life Insurance Corp', 3500.00, 'MONTHLY',
 '2024-01-15', '2024-02-15', 'ACTIVE'),

-- User 2 instructions
(2, 'SI1701234562001', 'Car EMI Payment', 'Monthly car loan EMI', 'EMI_PAYMENT',
 '2001234567890', 'LOAN001234567', 'ABC Bank Auto Loan', 18000.00, 'MONTHLY',
 '2024-01-05', '2024-02-05', 'ACTIVE'),

(2, 'SI1701234562002', 'Electricity Bill', 'Monthly electricity bill payment', 'UTILITY_BILL',
 '2001234567890', 'ELEC001234567', 'State Electricity Board', 2500.00, 'MONTHLY',
 '2024-01-10', '2024-02-10', 'ACTIVE'),

(2, 'SI1701234562003', 'Recurring Deposit', 'Monthly RD investment', 'RECURRING_DEPOSIT',
 '2001234567890', 'RD001234567', 'Fixed Deposit Account', 10000.00, 'MONTHLY',
 '2024-01-01', '2024-02-01', 'ACTIVE'),

-- User 3 instructions
(3, 'SI1701234563001', 'Home Loan EMI', 'Monthly home loan payment', 'LOAN_REPAYMENT',
 '3001234567890', 'HLOAN001234567', 'XYZ Bank Home Loan', 35000.00, 'MONTHLY',
 '2024-01-01', '2024-02-01', 'ACTIVE'),

(3, 'SI1701234563002', 'Daily SIP Investment', 'Daily systematic investment plan', 'INVESTMENT',
 '3001234567890', 'MF001234567', 'Mutual Fund SIP', 1000.00, 'DAILY',
 '2024-01-01', '2024-01-02', 'ACTIVE'),

(3, 'SI1701234563003', 'Quarterly Tax Payment', 'Quarterly advance tax payment', 'FUND_TRANSFER',
 '3001234567890', 'TAX001234567', 'Income Tax Department', 50000.00, 'QUARTERLY',
 '2024-01-15', '2024-04-15', 'ACTIVE'),

-- Some completed/cancelled instructions for testing
(1, 'SI1701234561004', 'Old Credit Card Payment', 'Cancelled credit card auto payment', 'BILL_PAYMENT',
 '1001234567890', 'CC001234567', 'Credit Card Company', 5000.00, 'MONTHLY',
 '2023-06-01', '2023-12-01', 'CANCELLED'),

(2, 'SI1701234562004', 'Completed Personal Loan', 'Completed personal loan EMI', 'EMI_PAYMENT',
 '2001234567890', 'PLOAN001234567', 'Personal Loan Bank', 8500.00, 'MONTHLY',
 '2023-01-01', '2023-12-01', 'COMPLETED');

-- Add some constraints and triggers
DELIMITER //

-- Trigger to validate end_date is after start_date
CREATE TRIGGER validate_standing_instruction_dates
    BEFORE INSERT ON standing_instructions
    FOR EACH ROW
BEGIN
    IF NEW.end_date IS NOT NULL AND NEW.end_date <= NEW.start_date THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'End date must be after start date';
    END IF;
    
    IF NEW.amount <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Amount must be greater than zero';
    END IF;
END //

-- Trigger to update execution count and next execution date
CREATE TRIGGER update_standing_instruction_execution
    BEFORE UPDATE ON standing_instructions
    FOR EACH ROW
BEGIN
    -- If max_executions is set and execution_count reaches it, set status to COMPLETED
    IF NEW.max_executions IS NOT NULL AND NEW.execution_count >= NEW.max_executions THEN
        SET NEW.status = 'COMPLETED';
    END IF;
    
    -- If end_date has passed, set status to COMPLETED
    IF NEW.end_date IS NOT NULL AND NEW.next_execution_date > NEW.end_date THEN
        SET NEW.status = 'COMPLETED';
    END IF;
END //

DELIMITER ;

-- Create view for active instructions summary
CREATE OR REPLACE VIEW active_instructions_summary AS
SELECT 
    u.username,
    si.instruction_name,
    si.instruction_type,
    si.amount,
    si.frequency,
    si.next_execution_date,
    si.execution_count,
    DATEDIFF(si.next_execution_date, CURDATE()) as days_until_next
FROM standing_instructions si
JOIN users u ON si.user_id = u.id
WHERE si.status = 'ACTIVE'
ORDER BY si.next_execution_date;

-- Create view for instruction statistics
CREATE OR REPLACE VIEW instruction_statistics AS
SELECT 
    user_id,
    COUNT(*) as total_instructions,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_instructions,
    COUNT(CASE WHEN status = 'PAUSED' THEN 1 END) as paused_instructions,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_instructions,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_instructions,
    SUM(CASE WHEN status = 'ACTIVE' THEN amount ELSE 0 END) as total_active_amount,
    SUM(execution_count) as total_executions
FROM standing_instructions
GROUP BY user_id;