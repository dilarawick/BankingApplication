package com.bankapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private String accountNo;
    private int customerId;
    private String accountType;
    private int branchId;
    private BigDecimal accountBalance;
    private String accountStatus;

    // getters/setters
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public java.math.BigDecimal getAccountBalance() { return accountBalance; }
    public void setAccountBalance(java.math.BigDecimal accountBalance) { this.accountBalance = accountBalance; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
