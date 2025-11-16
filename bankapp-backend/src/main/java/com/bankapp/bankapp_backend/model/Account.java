package com.bankapp.bankapp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Account")
public class Account {
    @Id
    @Column(length=6)
    private String accountNo;

    @Column(nullable=false)
    private Integer customerID; // original owner ID

    @Column(nullable=false)
    private String accountType; // Savings/Current/Fixed Deposit

    @Column(nullable=false)
    private Integer branchID;

    private Double accountBalance = 0.0;

    private String accountStatus = "Active";

    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
        lastUpdatedDate = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        lastUpdatedDate = LocalDateTime.now();
    }

    // getters & setters
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public Integer getCustomerID() { return customerID; }
    public void setCustomerID(Integer customerID) { this.customerID = customerID; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Integer getBranchID() { return branchID; }
    public void setBranchID(Integer branchID) { this.branchID = branchID; }

    public Double getAccountBalance() { return accountBalance; }
    public void setAccountBalance(Double accountBalance) { this.accountBalance = accountBalance; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastUpdatedDate() { return lastUpdatedDate; }
}
