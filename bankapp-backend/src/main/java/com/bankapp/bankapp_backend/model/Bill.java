package com.bankapp.bankapp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Bill")
public class Bill {
    @Id
    @Column(length=50)
    private String billNo;
    private Integer customerID; // optional owner
    private String biller;
    private Double amount;
    private String status = "Unpaid";
    private LocalDateTime dueDate;
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() { createdDate = LocalDateTime.now(); }

    // getters & setters...

    public String getBiller() {
        return biller;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }


}
