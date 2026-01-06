package com.bankapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class BankTransferRequest {
    @NotNull(message = "Sender account number is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{6}$", message = "Invalid sender account number format")
    private String senderAccountNo;

    @NotNull(message = "Recipient account number is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{6}$", message = "Invalid recipient account number format")
    private String recipientAccountNo;

    @NotNull(message = "Recipient bank is required")
    @Size(min = 2, max = 100, message = "Recipient bank name must be between 2 and 100 characters")
    private String recipientBank;

    @NotNull(message = "Recipient branch is required")
    @Size(min = 2, max = 100, message = "Recipient branch name must be between 2 and 100 characters")
    private String recipientBranch;

    @NotNull(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    private String recipientName;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal transferAmount;

    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;

    // Getters and Setters
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
}