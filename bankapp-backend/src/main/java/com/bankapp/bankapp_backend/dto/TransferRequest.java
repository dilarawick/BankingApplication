package com.bankapp.bankapp_backend.dto;

import com.bankapp.bankapp_backend.enums.BankName;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotBlank(message = "Recipient account number is required")
    @Pattern(regexp = "^[0-9]{10,16}$", message = "Account number must be 10-16 digits")
    private String recipientAccountNumber;

    @NotBlank(message = "Recipient account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String recipientAccountName;

    @NotNull(message = "Bank selection is required")
    private BankName recipientBank;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Constructors
    public TransferRequest() {
    }

    public TransferRequest(BigDecimal amount, String recipientAccountNumber, String recipientAccountName,
            BankName recipientBank, String description) {
        this.amount = amount;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientAccountName = recipientAccountName;
        this.recipientBank = recipientBank;
        this.description = description;
    }

    // Getters and Setters
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
}
