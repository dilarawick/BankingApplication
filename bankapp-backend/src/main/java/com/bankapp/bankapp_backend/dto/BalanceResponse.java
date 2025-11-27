package com.bankapp.bankapp_backend.dto;

import java.math.BigDecimal;

public class BalanceResponse {

    private String accountNumber;
    private String accountName;
    private BigDecimal balance;
    private boolean sufficientBalance;
    private String message;

    // Constructors
    public BalanceResponse() {
    }

    public BalanceResponse(String accountNumber, String accountName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.sufficientBalance = true;
    }

    public BalanceResponse(String accountNumber, String accountName, BigDecimal balance,
            boolean sufficientBalance, String message) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.sufficientBalance = sufficientBalance;
        this.message = message;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isSufficientBalance() {
        return sufficientBalance;
    }

    public void setSufficientBalance(boolean sufficientBalance) {
        this.sufficientBalance = sufficientBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
