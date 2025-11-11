CREATE DATABASE IF NOT EXISTS BankingSystemDB;

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
    IsPrimary BOOLEAN DEFAULT FALSE,
    AddedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (AccountNo) REFERENCES Account(AccountNo)
);