package com.bankapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`Transaction`")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @Column(name = "AccountNo", nullable = false, length = 6)
    private String accountNo;

    @Column(name = "TransactionType", nullable = false, length = 10)
    private String transactionType; // DEBIT or CREDIT

    @Column(name = "Amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "ReferenceId")
    private Integer referenceId; // ID of the related transfer, bill payment, etc.

    @Column(name = "ReferenceType", length = 50)
    private String referenceType; // Type of the reference (e.g., BANK_TRANSFER, BILL_PAYMENT)

    @Column(name = "TransactionDate", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
        transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
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

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}