package com.bankapp.bankapp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Payment")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentID;

    private Integer customerID;
    @Column(length=6)
    private String fromAccountNo;
    private String billNo;
    private String biller;
    private Double amount;
    private String confirmNo;
    private String status; // Success / Failed
    private String failureReason;
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() { createdDate = LocalDateTime.now(); }

    // getters & setters...

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setFromAccountNo(String fromAccountNo) {
        this.fromAccountNo = fromAccountNo;
    }

    public String getFromAccountNo() {
        return fromAccountNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }

    public String getBiller() {
        return biller;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setConfirmNo(String confirmNo) {
        this.confirmNo = confirmNo;
    }

    public String getConfirmNo() {
        return confirmNo;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }


}
