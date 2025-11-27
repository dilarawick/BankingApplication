INSERT INTO transactions 
(transaction_id, sender_id, sender_account_number, sender_account_name, amount, recipient_account_number, recipient_account_name, recipient_bank, description, status, balance_after_transaction, transaction_date, created_at)
VALUES
('TXN100001', 1, '1234567890', 'Alice Smith', 1500.00, '0987654321', 'Bob Johnson', 'Bank A', 'Payment for invoice #123', 'Completed', 8500.00, '2025-11-20 10:15:00', NOW()),
('TXN100002', 2, '2234567890', 'Charlie Brown', 250.50, '1987654321', 'David Lee', 'Bank B', 'Gift', 'Completed', 4750.50, '2025-11-21 14:45:00', NOW()),
('TXN100003', 3, '3234567890', 'Eve Turner', 5000.00, '2987654321', 'Frank Harris', 'Bank C', 'Salary transfer', 'Pending', 12000.00, '2025-11-22 09:30:00', NOW()),
('TXN100004', 4, '4234567890', 'Grace Hall', 75.75, '3987654321', 'Hannah Clark', 'Bank A', 'Coffee reimbursement', 'Completed', 924.25, '2025-11-23 08:10:00', NOW()),
('TXN100005', 5, '5234567890', 'Ian Wright', 320.00, '4987654321', 'Julia Adams', 'Bank B', 'Online purchase', 'Failed', 680.00, '2025-11-24 11:50:00', NOW()),
('TXN100006', 1, '1234567890', 'Alice Smith', 100.00, '5987654321', 'Kevin White', 'Bank C', 'Charity donation', 'Completed', 8400.00, '2025-11-25 16:20:00', NOW()),
('TXN100007', 2, '2234567890', 'Charlie Brown', 2000.00, '6987654321', 'Laura Young', 'Bank A', 'Car installment', 'Pending', 2750.50, '2025-11-26 12:00:00', NOW()),
('TXN100008', 3, '3234567890', 'Eve Turner', 150.00, '7987654321', 'Mike Scott', 'Bank B', 'Dinner payment', 'Completed', 11850.00, '2025-11-27 19:30:00', NOW());
