CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    sender_account_number VARCHAR(255) NOT NULL,
    sender_account_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    recipient_account_number VARCHAR(255) NOT NULL,
    recipient_account_name VARCHAR(255) NOT NULL,
    recipient_bank VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    balance_after_transaction DECIMAL(15, 2),
    transaction_date DATETIME NOT NULL,
    created_at DATETIME,
    CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_id) REFERENCES users(id)ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for performance optimization
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_status (status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_sender_status (sender_id, status, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;