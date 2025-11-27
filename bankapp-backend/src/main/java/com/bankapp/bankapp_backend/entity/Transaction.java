package com.bankapp.bankapp_backend.entity;

import com.bankapp.bankapp_backend.enums.BankName;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String senderAccountNumber;

    @Column(nullable = false)
    private String senderAccountName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String recipientAccountNumber;

    @Column(nullable = false)
    private String recipientAccountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankName recipientBank;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        transactionDate = LocalDateTime.now();
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = generateTransactionId();
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    // Constructors
    public Transaction() {
    }

    public Transaction(User sender, String senderAccountNumber, String senderAccountName,
            BigDecimal amount, String recipientAccountNumber, String recipientAccountName,
            BankName recipientBank, String description) {
        this.sender = sender;
        this.senderAccountNumber = senderAccountNumber;
        this.senderAccountName = senderAccountName;
        this.amount = amount;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientAccountName = recipientAccountName;
        this.recipientBank = recipientBank;
        this.description = description;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
