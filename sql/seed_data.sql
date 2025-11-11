INSERT INTO Customer (Name, Email, NIC, PhoneNumber, Username, PasswordHash)
VALUES 
('Alice Johnson', 'alice.johnson@example.com', '123456789V', '0711234567', NULL, NULL),
('Bob Smith', 'bob.smith@example.com', '987654321V', '0717654321', NULL, NULL),
('Charlie Brown', 'charlie.brown@example.com', '456123789V', '0715555555', NULL, NULL),
('Diana Prince', 'diana.prince@example.com', '789456123V', '0719999999', NULL, NULL),
('Edward King', 'edward.king@example.com', '321654987V', '0718888888', NULL, NULL);

INSERT INTO Branch (BranchName, City, PostalCode)
VALUES
('Central Branch', 'Colombo', '00100'),
('Fort Branch', 'Colombo', '00200'),
('Kandy Branch', 'Kandy', '20000'),
('Galle Branch', 'Galle', '80000'),
('Jaffna Branch', 'Jaffna', '40000');

INSERT INTO Account (AccountNo, CustomerID, AccountType, BranchID, AccountBalance, AccountStatus)
VALUES
('AC0001', 1, 'Savings', 1, 5000.00, 'Active'),
('AC0002', 1, 'Current', 2, 12000.50, 'Active'),
('AC0003', 1, 'Fixed Deposit', 3, 30000.00, 'Active');

INSERT INTO Account (AccountNo, CustomerID, AccountType, BranchID, AccountBalance, AccountStatus)
VALUES
('AC0004', 2, 'Savings', 1, 8000.00, 'Active'),
('AC0005', 3, 'Current', 4, 1500.75, 'Active'),
('AC0006', 4, 'Savings', 5, 2200.00, 'Active'),
('AC0007', 5, 'Fixed Deposit', 3, 50000.00, 'Active');