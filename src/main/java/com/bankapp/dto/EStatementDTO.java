package com.bankapp.dto;

import java.util.List;

public class EStatementDTO {
    private String accountHolder;
    private String accountNumber;
    private String accountType;
    private String branch;
    private String statementPeriod;
    private AccountSummary accountSummary;
    private List<TransactionDetail> transactionDetails;

    // Nested classes
    public static class AccountSummary {
        private double openingBalance;
        private double totalCredits;
        private double totalDebits;
        private double feesCharges;
        private double interestEarned;
        private double closingBalance;

        // Constructors
        public AccountSummary() {
        }

        public AccountSummary(double openingBalance, double totalCredits, double totalDebits,
                double feesCharges, double interestEarned, double closingBalance) {
            this.openingBalance = openingBalance;
            this.totalCredits = totalCredits;
            this.totalDebits = totalDebits;
            this.feesCharges = feesCharges;
            this.interestEarned = interestEarned;
            this.closingBalance = closingBalance;
        }

        // Getters and setters
        public double getOpeningBalance() {
            return openingBalance;
        }

        public void setOpeningBalance(double openingBalance) {
            this.openingBalance = openingBalance;
        }

        public double getTotalCredits() {
            return totalCredits;
        }

        public void setTotalCredits(double totalCredits) {
            this.totalCredits = totalCredits;
        }

        public double getTotalDebits() {
            return totalDebits;
        }

        public void setTotalDebits(double totalDebits) {
            this.totalDebits = totalDebits;
        }

        public double getFeesCharges() {
            return feesCharges;
        }

        public void setFeesCharges(double feesCharges) {
            this.feesCharges = feesCharges;
        }

        public double getInterestEarned() {
            return interestEarned;
        }

        public void setInterestEarned(double interestEarned) {
            this.interestEarned = interestEarned;
        }

        public double getClosingBalance() {
            return closingBalance;
        }

        public void setClosingBalance(double closingBalance) {
            this.closingBalance = closingBalance;
        }
    }

    public static class TransactionDetail {
        private String date;
        private String transactionId;
        private String description;
        private String type; // DEBIT or CREDIT
        private double amount;
        private double balance;

        // Constructors
        public TransactionDetail() {
        }

        public TransactionDetail(String date, String transactionId, String description,
                String type, double amount, double balance) {
            this.date = date;
            this.transactionId = transactionId;
            this.description = description;
            this.type = type;
            this.amount = amount;
            this.balance = balance;
        }

        // Getters and setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    // Constructors
    public EStatementDTO() {
    }

    public EStatementDTO(String accountHolder, String accountNumber, String accountType,
            String branch, String statementPeriod, AccountSummary accountSummary,
            List<TransactionDetail> transactionDetails) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.branch = branch;
        this.statementPeriod = statementPeriod;
        this.accountSummary = accountSummary;
        this.transactionDetails = transactionDetails;
    }

    // Getters and setters
    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getStatementPeriod() {
        return statementPeriod;
    }

    public void setStatementPeriod(String statementPeriod) {
        this.statementPeriod = statementPeriod;
    }

    public AccountSummary getAccountSummary() {
        return accountSummary;
    }

    public void setAccountSummary(AccountSummary accountSummary) {
        this.accountSummary = accountSummary;
    }

    public List<TransactionDetail> getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(List<TransactionDetail> transactionDetails) {
        this.transactionDetails = transactionDetails;
    }
}