package com.bankapp.bankapp_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name="Branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer branchID;

    @Column(nullable=false)
    private String branchName;

    @Column(nullable=false)
    private String city;

    private String postalCode;

    // getters & setters
    public Integer getBranchID() { return branchID; }
    public void setBranchID(Integer branchID) { this.branchID = branchID; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
