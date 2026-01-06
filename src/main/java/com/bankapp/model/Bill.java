package com.bankapp.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer billId;

    @Column(name = "CustomerID", nullable = false)
    private Integer customerID;

    @Column(name = "BillerName", nullable = false)
    private String billerName;

    @Column(name = "Category", nullable = false)
    private String category;

    @Column(name = "Reference", nullable = false)
    private String reference;

    @Column(name = "InvoiceNumber")
    private String invoiceNumber;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "DueDate")
    private LocalDate dueDate;

    @Column(name = "BillStatus")
    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "AccountNo")
    private String accountNo;

    // Getters and Setters
    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BillStatus getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(BillStatus billStatus) {
        this.billStatus = billStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    // BillStatus enum
    public enum BillStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }
}