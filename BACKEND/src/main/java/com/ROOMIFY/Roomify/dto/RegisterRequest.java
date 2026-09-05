package com.ROOMIFY.Roomify.dto;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String businessName;
    private String phone;

    // ==================== DALALI (AGENT) FIELDS ====================
    private String licenseNumber;
    private String locationArea;
    private String verificationStatus;

    // ==================== EXISTING GETTERS & SETTERS ====================
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ==================== DALALI GETTERS & SETTERS ====================
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLocationArea() { return locationArea; }
    public void setLocationArea(String locationArea) { this.locationArea = locationArea; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    // ==================== HELPER METHODS ====================
    public boolean isDalali() {
        return "dalali".equalsIgnoreCase(role);
    }

    public boolean isOwner() {
        return "owner".equalsIgnoreCase(role);
    }

    public boolean isTenant() {
        return "tenant".equalsIgnoreCase(role);
    }
}