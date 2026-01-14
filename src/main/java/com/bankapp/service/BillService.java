package com.bankapp.service;

import com.bankapp.model.Bill;
import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.repository.BillRepository;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.model.Bill.BillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.bankapp.service.SmartSpendService;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SmartSpendService smartSpendService; // For integrating with smart spend feature

    @Autowired
    private TransactionRepository transactionRepository; // For recording bill payment transactions

    // Get unpaid bills for a customer
    public List<Bill> getUnpaidBillsForCustomer(Integer customerId) {
        return billRepository.findUnpaidBillsByCustomer(customerId);
    }

    // Get unpaid bills for a customer by category and biller
    public List<Bill> getUnpaidBillsForCustomer(Integer customerId, String category, String billerName) {
        return billRepository.findUnpaidBillsByCustomerAndCategoryAndBiller(customerId, category, billerName);
    }

    // Get bill by ID
    public Optional<Bill> getBillById(Integer billId) {
        return billRepository.findById(billId);
    }

    // Pay a bill
    @Transactional
    public boolean payBill(Integer billId, String accountNumber, Double amount) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        if (!billOpt.isPresent()) {
            return false;
        }

        Bill bill = billOpt.get();
        if (bill.getBillStatus() != BillStatus.PENDING) {
            return false; // Bill is already paid or cancelled
        }

        // Check if account exists and has sufficient balance
        Optional<Account> accountOpt = accountRepository.findByAccountNo(accountNumber);
        if (!accountOpt.isPresent()) {
            return false;
        }

        Account account = accountOpt.get();
        if (account.getAccountBalance() < amount) {
            return false; // Insufficient funds
        }

        // Update account balance
        account.setAccountBalance(account.getAccountBalance() - amount);
        accountRepository.save(account);

        // Record the debit against the budget if the account has an active budget
        try {
            java.math.BigDecimal amountDecimal = java.math.BigDecimal.valueOf(amount);
            smartSpendService.recordDebitAgainstBudget(accountNumber, amountDecimal,
                    "Bill Payment - " + bill.getBillerName() + ": " + bill.getInvoiceNumber());
        } catch (Exception e) {
            // Log the error but continue with the bill payment
            System.err.println("Failed to record bill payment against budget for account " + accountNumber + ": "
                    + e.getMessage());
        }

        // Record the transaction entry for accounting purposes
        try {
            Transaction transaction = new Transaction();
            transaction.setAccountNo(accountNumber);
            transaction.setTransactionType("DEBIT");
            transaction.setAmount(new BigDecimal(String.valueOf(amount)));
            transaction.setDescription("Bill Payment - " + bill.getBillerName() + ": " + bill.getInvoiceNumber());
            transaction.setReferenceId(bill.getBillId());
            transaction.setReferenceType("BILL_PAYMENT");
            transaction.setTransactionDate(LocalDateTime.now());
            transactionRepository.save(transaction);
        } catch (Exception e) {
            // Log the error but continue with the bill payment
            System.err.println("Failed to record bill payment transaction for account " + accountNumber + ": "
                    + e.getMessage());
        }

        // Update bill status
        bill.setBillStatus(BillStatus.PAID);
        bill.setPaidDate(LocalDateTime.now());
        bill.setAccountNo(accountNumber);
        billRepository.save(bill);

        return true;
    }

    // Create a new bill
    @Transactional
    public Bill createBill(Bill bill) {
        bill.setBillStatus(BillStatus.PENDING);
        bill.setCreatedDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    // Get all bills for a customer
    public List<Bill> getBillsForCustomer(Integer customerId) {
        return billRepository.findByCustomerID(customerId);
    }

    // Get all bills for an account
    public List<Bill> getBillsForAccount(String accountNo) {
        return billRepository.findByAccountNo(accountNo);
    }
}