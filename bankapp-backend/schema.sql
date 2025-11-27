-- Banking Application - Transactions Table Schema
-- Database: MySQL
-- Compatible with the Transaction entity in the bankapp-backend project

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    -- Primary key
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Unique transaction identifier
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    
    -- Foreign key to users table (sender)
    sender_id BIGINT NOT NULL,
    
    -- Sender information
    sender_account_number VARCHAR(255) NOT NULL,
    sender_account_name VARCHAR(255) NOT NULL,
    
    -- Transaction amount (precision: 15 digits, scale: 2 decimal places)
    amount DECIMAL(15, 2) NOT NULL,
    
    -- Recipient information
    recipient_account_number VARCHAR(255) NOT NULL,
    recipient_account_name VARCHAR(255) NOT NULL,
    recipient_bank VARCHAR(50) NOT NULL,
    
    -- Transaction details
    description TEXT,
    status VARCHAR(50) NOT NULL,
    balance_after_transaction DECIMAL(15, 2),
    
    -- Timestamps
    transaction_date DATETIME NOT NULL,
    created_at DATETIME,
    
    -- Foreign key constraint
    CONSTRAINT fk_transactions_sender 
        FOREIGN KEY (sender_id) 
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Indexes for performance optimization
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_status (status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_sender_status (sender_id, status, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Valid values for status field (for reference)
-- Status can be: SUCCESS, FAILED, PENDING

-- Valid values for recipient_bank field (for reference)
-- BankName enum values: NOVA_BANK, STATE_BANK, CITY_BANK, NATIONAL_BANK, 
-- FEDERAL_BANK, METRO_BANK, REGIONAL_BANK, COMMERCIAL_BANK, PEOPLE_BANK, TRUST_BANK
