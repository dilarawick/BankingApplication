package com.bankapp.model;

public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String nic;
    private String phoneNumber;
    private String username;
    private String passwordHash;

    // constructors, getters, setters
    public Customer() {}
    public Customer(int customerId, String name, String email, String nic, String phoneNumber, String username, String passwordHash) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.nic = nic;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.passwordHash = passwordHash;
    }
    // getters & setters omitted for brevity - implement all
    // ...
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
