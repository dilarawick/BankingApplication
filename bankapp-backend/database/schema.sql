CREATE TABLE transactions (
    TransactionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TransactionCode VARCHAR(255) NOT NULL UNIQUE,
    SenderAccountNo CHAR(6) NOT NULL,
    SenderName VARCHAR(100) NOT NULL,
    Amount DECIMAL(15,2) NOT NULL,
    RecipientAccountNo CHAR(6) NOT NULL,
    RecipientName VARCHAR(100) NOT NULL,
    RecipientBank VARCHAR(100) NOT NULL,
    Description TEXT,
    Status ENUM('Pending','Completed','Failed') NOT NULL DEFAULT 'Pending',
    BalanceAfterTransaction DECIMAL(15,2),
    TransactionDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaction_sender_account FOREIGN KEY (SenderAccountNo) REFERENCES Account(AccountNo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_transaction_recipient_account FOREIGN KEY (RecipientAccountNo) REFERENCES Account(AccountNo) ON DELETE CASCADE ON UPDATE CASCADE,

