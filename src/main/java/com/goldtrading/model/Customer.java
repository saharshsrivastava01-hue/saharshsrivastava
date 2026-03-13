package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents a customer who buys gold.
 */
public class Customer {

    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String idProof;
    private String idNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Customer(String name, String phone, String email, String address,
                    String idProof, String idNumber) {
        this();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.idProof = idProof;
        this.idNumber = idNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, name=%s, phone=%s, email=%s]",
                id, name, phone, email);
    }
}
