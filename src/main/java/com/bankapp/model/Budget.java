package com.bankapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer budgetId;

    @Column(name = "CustomerID", nullable = false)
    private Integer customerID;

    @Column(name = "AccountNo", nullable = false, length = 6)
    private String accountNo;

    @Column(name = "BudgetLimit", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetLimit;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;

    @Column(name = "IsActive")
    private Boolean isActive;

    // Getters and Setters
    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}