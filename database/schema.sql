CREATE DATABASE BankingSystemDB;

USE BankingSystemDB;

CREATE TABLE Customer (
    CustomerID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    NIC VARCHAR(20) NOT NULL UNIQUE,
    PhoneNumber VARCHAR(20),
    Username VARCHAR(50) UNIQUE,
    PasswordHash VARCHAR(255)
);

CREATE TABLE Branch (
    BranchID INT AUTO_INCREMENT PRIMARY KEY,
    BranchName VARCHAR(100) NOT NULL,
    City VARCHAR(50) NOT NULL,
    PostalCode VARCHAR(20)
);

CREATE TABLE Account (
    AccountNo CHAR(6) PRIMARY KEY,
    CustomerID INT NOT NULL,
    AccountType ENUM('Savings', 'Current', 'Fixed Deposit') NOT NULL,
    BranchID INT NOT NULL,
    AccountBalance DECIMAL(15,2) DEFAULT 0.00,
    AccountStatus ENUM('Active', 'Inactive', 'Closed') DEFAULT 'Active',
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastUpdatedDate DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID)
);

CREATE TABLE CustomerAccount (
    CustomerAccountID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerID INT NOT NULL,
    AccountNo CHAR(6) NOT NULL,
    AccountType VARCHAR(20),
    AccountNickname VARCHAR(50),
    IsPrimary BOOLEAN DEFAULT FALSE,
    AddedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (AccountNo) REFERENCES Account(AccountNo)
);

CREATE TABLE BankTransfer (
    TransferId INT AUTO_INCREMENT PRIMARY KEY,
    SenderCustomerId INT NOT NULL,
    SenderAccountNo CHAR(6) NOT NULL,
    RecipientAccountNo CHAR(6) NOT NULL,
    RecipientBank VARCHAR(100) NOT NULL,
    RecipientBranch VARCHAR(100) NOT NULL,
    RecipientName VARCHAR(100) NOT NULL,
    TransferAmount DECIMAL(15,2) NOT NULL,
    Reference VARCHAR(255),
    TransferStatus ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    TransferDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    CompletedDate DATETIME NULL,
    FOREIGN KEY (SenderCustomerId) REFERENCES Customer(CustomerID),
    FOREIGN KEY (SenderAccountNo) REFERENCES Account(AccountNo)
);

CREATE TABLE Bill (
    BillId INT AUTO_INCREMENT PRIMARY KEY,
    CustomerID INT NOT NULL,
    BillerName VARCHAR(100) NOT NULL,
    Category VARCHAR(50) NOT NULL,
    Reference VARCHAR(100) NOT NULL,
    InvoiceNumber VARCHAR(100),
    Amount DECIMAL(15,2) NOT NULL,
    DueDate DATE,
    BillStatus ENUM('PENDING', 'PAID', 'OVERDUE', 'CANCELLED') DEFAULT 'PENDING',
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    PaidDate DATETIME NULL,
    AccountNo CHAR(6),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (AccountNo) REFERENCES Account(AccountNo)
);

CREATE TABLE Budget (
    BudgetId INT AUTO_INCREMENT PRIMARY KEY,
    CustomerID INT NOT NULL,
    AccountNo CHAR(6) NOT NULL,
    BudgetLimit DECIMAL(15,2) NOT NULL,
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedDate DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    IsActive BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (AccountNo) REFERENCES Account(AccountNo)
);

CREATE TABLE BudgetTransaction (
    TransactionId INT AUTO_INCREMENT PRIMARY KEY,
    BudgetId INT NOT NULL,
    TransactionAmount DECIMAL(15,2) NOT NULL,
    TransactionDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    Description VARCHAR(255),
    Category VARCHAR(50),
    PaymentType VARCHAR(20),
    TransactionType ENUM('AUTOMATIC', 'MANUAL') DEFAULT 'AUTOMATIC',
    FOREIGN KEY (BudgetId) REFERENCES Budget(BudgetId)
);