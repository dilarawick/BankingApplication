package com.bankapp.bankapp_backend.dto;

import com.bankapp.bankapp_backend.enums.BankName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionReceipt {

    private String transactionId;
    private LocalDateTime timestamp;
    private String senderAccountNumber;
    private String senderAccountName;
    private BigDecimal amountTransferred;
    private String recipientAccountNumber;
    private String recipientAccountName;
    private BankName recipientBank;
    private String description;
    private BigDecimal remainingBalance;
    private String status;
    private String message;

    // Constructors
    public TransactionReceipt() {
    }

    public TransactionReceipt(String transactionId, LocalDateTime timestamp, String senderAccountNumber,
            String senderAccountName, BigDecimal amountTransferred, String recipientAccountNumber,
            String recipientAccountName, BankName recipientBank, String description,
            BigDecimal remainingBalance, String status, String message) {
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.senderAccountNumber = senderAccountNumber;
        this.senderAccountName = senderAccountName;
        this.amountTransferred = amountTransferred;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientAccountName = recipientAccountName;
        this.recipientBank = recipientBank;
        this.description = description;
        this.remainingBalance = remainingBalance;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getSenderAccountName() {
        return senderAccountName;
    }

    public void setSenderAccountName(String senderAccountName) {
        this.senderAccountName = senderAccountName;
    }

    public BigDecimal getAmountTransferred() {
        return amountTransferred;
    }

    public void setAmountTransferred(BigDecimal amountTransferred) {
        this.amountTransferred = amountTransferred;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
    }

    public String getRecipientAccountName() {
        return recipientAccountName;
    }

    public void setRecipientAccountName(String recipientAccountName) {
        this.recipientAccountName = recipientAccountName;
    }

    public BankName getRecipientBank() {
        return recipientBank;
    }

    public void setRecipientBank(BankName recipientBank) {
        this.recipientBank = recipientBank;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
