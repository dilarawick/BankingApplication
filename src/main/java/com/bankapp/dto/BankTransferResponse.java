package com.bankapp.dto;

import java.time.LocalDateTime;

public class BankTransferResponse {
    private Integer transferId;
    private String senderAccountNo;
    private String recipientAccountNo;
    private String recipientBank;
    private String recipientBranch;
    private String recipientName;
    private Double transferAmount;
    private String reference;
    private String transferStatus;
    private LocalDateTime transferDate;
    private LocalDateTime completedDate;
    private String message;

    // Constructors
    public BankTransferResponse() {
    }

    public BankTransferResponse(String message, String transferStatus) {
        this.message = message;
        this.transferStatus = transferStatus;
    }

    // Getters and Setters
    public Integer getTransferId() {
        return transferId;
    }

    public void setTransferId(Integer transferId) {
        this.transferId = transferId;
    }

    public String getSenderAccountNo() {
        return senderAccountNo;
    }

    public void setSenderAccountNo(String senderAccountNo) {
        this.senderAccountNo = senderAccountNo;
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

    public Double getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Double transferAmount) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}