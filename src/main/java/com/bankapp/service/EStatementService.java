package com.bankapp.service;

import com.bankapp.dto.EStatementDTO;
import com.bankapp.model.*;
import com.bankapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EStatementService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BankTransferRepository bankTransferRepository;

    public EStatementDTO generateEStatement(String accountNo, LocalDate startDate, LocalDate endDate) {
        // Get account details
        Optional<Account> accountOpt = accountRepository.findByAccountNo(accountNo);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found: " + accountNo);
        }

        Account account = accountOpt.get();

        // Get customer details
        Optional<Customer> customerOpt = customerRepository.findById(account.getCustomerID());
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found for account: " + accountNo);
        }

        Customer customer = customerOpt.get();

        // Get branch details
        Optional<Branch> branchOpt = branchRepository.findById(account.getBranchID());
        String branchName = branchOpt.map(Branch::getBranchName).orElse("Unknown Branch");

        // Get all transactions for the account in the specified period
        List<Transaction> accountTransactions = transactionRepository.findByAccountNoAndTransactionDateBetween(
                accountNo,
                LocalDateTime.of(startDate, java.time.LocalTime.MIN),
                LocalDateTime.of(endDate, java.time.LocalTime.MAX));

        // Get all bills for the account in the specified period
        List<Bill> accountBills = billRepository.findByAccountNoAndPaidDateBetween(
                accountNo,
                LocalDateTime.of(startDate, java.time.LocalTime.MIN),
                LocalDateTime.of(endDate, java.time.LocalTime.MAX));

        // Calculate opening balance at the start of the period
        double openingBalance = calculateOpeningBalance(accountNo, startDate);

        // Get all transactions sorted by date
        List<EStatementDTO.TransactionDetail> transactionDetails = new ArrayList<>();

        // Process account transactions (from Transaction table)
        for (Transaction trans : accountTransactions) {
            EStatementDTO.TransactionDetail detail = new EStatementDTO.TransactionDetail();
            detail.setDate(trans.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MMM-yy")));

            // Create transaction ID based on type and reference
            if ("BANK_TRANSFER".equals(trans.getReferenceType()) && trans.getReferenceId() != null) {
                detail.setTransactionId("TXN" + String.format("%07d", trans.getReferenceId()));
            } else {
                detail.setTransactionId("TXN" + String.format("%07d", trans.getTransactionId()));
            }

            detail.setDescription(trans.getDescription());
            detail.setType(trans.getTransactionType());
            detail.setAmount(trans.getAmount().doubleValue());

            transactionDetails.add(detail);
        }

        // Process bill payments (these should be treated as debits)
        for (Bill bill : accountBills) {
            EStatementDTO.TransactionDetail detail = new EStatementDTO.TransactionDetail();
            detail.setDate(bill.getPaidDate().format(DateTimeFormatter.ofPattern("dd-MMM-yy")));
            detail.setTransactionId("BILL" + String.format("%07d", bill.getBillId()));
            detail.setDescription("Bill Payment - " + bill.getBillerName() + ": " + bill.getInvoiceNumber());
            detail.setType("DEBIT"); // Bill payments are debits
            detail.setAmount(bill.getAmount());

            transactionDetails.add(detail);
        }

        // Sort transactions by date
        transactionDetails.sort((t1, t2) -> {
            LocalDate d1 = LocalDate.parse(t1.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yy"));
            LocalDate d2 = LocalDate.parse(t2.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yy"));
            return d1.compareTo(d2);
        });

        // Calculate balances after each transaction
        double runningBalance = openingBalance;
        for (EStatementDTO.TransactionDetail detail : transactionDetails) {
            if ("CREDIT".equals(detail.getType())) {
                runningBalance += detail.getAmount();
            } else { // DEBIT
                runningBalance -= detail.getAmount();
            }
            detail.setBalance(runningBalance);
        }

        // Calculate account summary
        double totalCredits = transactionDetails.stream()
                .filter(t -> "CREDIT".equals(t.getType()))
                .mapToDouble(EStatementDTO.TransactionDetail::getAmount)
                .sum();

        double totalDebits = transactionDetails.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .mapToDouble(EStatementDTO.TransactionDetail::getAmount)
                .sum();

        double closingBalance = openingBalance + totalCredits - totalDebits;

        EStatementDTO.AccountSummary accountSummary = new EStatementDTO.AccountSummary(
                openingBalance, totalCredits, totalDebits, 0.0, 0.0, closingBalance);

        // Create and return the e-statement
        EStatementDTO eStatement = new EStatementDTO();
        eStatement.setAccountHolder(customer.getName());
        eStatement.setAccountNumber("***" + accountNo.substring(Math.max(0, accountNo.length() - 3))); // Mask account
                                                                                                       // number
        eStatement.setAccountType(account.getAccountType());
        eStatement.setBranch(branchName);
        eStatement.setStatementPeriod(startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) +
                " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        eStatement.setAccountSummary(accountSummary);
        eStatement.setTransactionDetails(transactionDetails);

        return eStatement;
    }

    private double calculateOpeningBalance(String accountNo, LocalDate startDate) {
        // Get the opening balance at the start of the statement period
        // This is the account balance minus all transactions that happened during or
        // after the start date

        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNo));

        double currentBalance = account.getAccountBalance();

        // Get all transactions after the start date
        List<Transaction> futureTransactions = transactionRepository.findByAccountNoAndTransactionDateBetween(
                accountNo,
                LocalDateTime.of(startDate, java.time.LocalTime.MIN),
                LocalDateTime.now());

        // Subtract credits and add debits to get the opening balance
        for (Transaction trans : futureTransactions) {
            if ("CREDIT".equals(trans.getTransactionType())) {
                currentBalance -= trans.getAmount().doubleValue();
            } else { // DEBIT
                currentBalance += trans.getAmount().doubleValue();
            }
        }

        return currentBalance;
    }
}