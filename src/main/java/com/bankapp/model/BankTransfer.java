package com.bankapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "BankTransfer")
public class BankTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transferId;

    @Column(nullable = false)
    private Integer senderCustomerId;

    @ManyToOne
    @JoinColumn(name = "SenderAccountNo", referencedColumnName = "AccountNo", nullable = false)
    private Account senderAccount;

    @Column(nullable = false, length = 6)
    private String recipientAccountNo;

    @Column(nullable = false)
    private String recipientBank;

    @Column(nullable = false)
    private String recipientBranch;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal transferAmount;

    @Column(length = 255)
    private String reference;

    @Column(nullable = false)
    private String transferStatus = "PENDING"; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime transferDate;

    @Column
    private LocalDateTime completedDate;

    @PrePersist
    public void prePersist() {
        transferDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        completedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTransferId() {
        return transferId;
    }

    public void setTransferId(Integer transferId) {
        this.transferId = transferId;
    }

    public Integer getSenderCustomerId() {
        return senderCustomerId;
    }

    public void setSenderCustomerId(Integer senderCustomerId) {
        this.senderCustomerId = senderCustomerId;
    }

    public Account getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(Account senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getSenderAccountNo() {
        return senderAccount != null ? senderAccount.getAccountNo() : null;
    }

    public void setSenderAccountNo(String senderAccountNo) {
        if (this.senderAccount == null) {
            this.senderAccount = new Account();
        }
        this.senderAccount.setAccountNo(senderAccountNo);
    }

    public String getRecipientAccountNo() {
        return recipientAccountNo;
    }

    public void setRecipientAccountNo(String recipientAccountNo) {
        this.recipientAccountNo = recipientAccountNo;
    }

    public String getRecipientBank() {
        return recipientBank;
    }

    public void setRecipientBank(String recipientBank) {
        this.recipientBank = recipientBank;
    }

    public String getRecipientBranch() {
        return recipientBranch;
    }

    public void setRecipientBranch(String recipientBranch) {
        this.recipientBranch = recipientBranch;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
}