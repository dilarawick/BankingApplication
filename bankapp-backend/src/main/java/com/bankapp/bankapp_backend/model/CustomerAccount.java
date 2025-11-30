package com.bankapp.bankapp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerAccount")
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerAccountID;

    @Column(nullable = false)
    private Integer customerID;

    @Column(length = 6, nullable = false)
    private String accountNo;

    private Boolean isPrimary = false;

    private LocalDateTime addedDate;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @PrePersist
    public void prePersist() {
        addedDate = LocalDateTime.now();
    }

    // Getters & setters
    public Integer getCustomerAccountID() {
        return customerAccountID;
    }

    public void setCustomerAccountID(Integer customerAccountID) {
        this.customerAccountID = customerAccountID;
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

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
