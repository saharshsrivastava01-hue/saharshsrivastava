package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents a gold supplier from whom we purchase gold.
 */
public class Supplier {

    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String companyName;
    private String licenseNumber;
    private double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supplier() {
        this.rating = 5.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Supplier(String name, String phone, String email, String address,
                    String companyName, String licenseNumber) {
        this();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.companyName = companyName;
        this.licenseNumber = licenseNumber;
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

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = Math.max(0, Math.min(5, rating)); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Supplier[id=%d, name=%s, company=%s, rating=%.1f]",
                id, name, companyName, rating);
    }
}
