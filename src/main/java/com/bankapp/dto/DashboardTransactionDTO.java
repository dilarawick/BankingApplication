package com.bankapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardTransactionDTO {
    private String transactionId;
    private String accountNo;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private String transactionDateStr; // String representation of the date
    private String referenceType;

    public DashboardTransactionDTO() {
    }

    public DashboardTransactionDTO(String transactionId, String accountNo, String transactionType,
            BigDecimal amount, String description, LocalDateTime transactionDate, String referenceType) {
        this.transactionId = transactionId;
        this.accountNo = accountNo;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.referenceType = referenceType;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getTransactionDateStr() {
        return transactionDateStr;
    }

    public void setTransactionDateStr(String transactionDateStr) {
        this.transactionDateStr = transactionDateStr;
    }
}