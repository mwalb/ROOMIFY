package com.ROOMIFY.Roomify.dto;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String businessName;
    private String phone;
    private String nidaNumber;

    // ==================== MTENDAJI / MWENYEKITI WA MTAA FIELDS ====================
    private String localAuthorityName;
    private String localAuthorityPhone;
    private String localAuthorityArea;
    private String localAuthorityVillage;
    private String localAuthorityWard;
    private String localAuthorityDistrict;
    private String localAuthorityRegion;

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

    public String getNidaNumber() { return nidaNumber; }
    public void setNidaNumber(String nidaNumber) { this.nidaNumber = nidaNumber; }

    // ==================== LOCAL AUTHORITY GETTERS & SETTERS ====================
    public String getLocalAuthorityName() { return localAuthorityName; }
    public void setLocalAuthorityName(String localAuthorityName) { this.localAuthorityName = localAuthorityName; }

    public String getLocalAuthorityPhone() { return localAuthorityPhone; }
    public void setLocalAuthorityPhone(String localAuthorityPhone) { this.localAuthorityPhone = localAuthorityPhone; }

    public String getLocalAuthorityArea() { return localAuthorityArea; }
    public void setLocalAuthorityArea(String localAuthorityArea) { this.localAuthorityArea = localAuthorityArea; }

    public String getLocalAuthorityVillage() { return localAuthorityVillage; }
    public void setLocalAuthorityVillage(String localAuthorityVillage) { this.localAuthorityVillage = localAuthorityVillage; }

    public String getLocalAuthorityWard() { return localAuthorityWard; }
    public void setLocalAuthorityWard(String localAuthorityWard) { this.localAuthorityWard = localAuthorityWard; }

    public String getLocalAuthorityDistrict() { return localAuthorityDistrict; }
    public void setLocalAuthorityDistrict(String localAuthorityDistrict) { this.localAuthorityDistrict = localAuthorityDistrict; }

    public String getLocalAuthorityRegion() { return localAuthorityRegion; }
    public void setLocalAuthorityRegion(String localAuthorityRegion) { this.localAuthorityRegion = localAuthorityRegion; }

    // Backward compatibility for Balozi getters/setters
    public String getBaloziName() { return localAuthorityName; }
    public void setBaloziName(String baloziName) { this.localAuthorityName = baloziName; }

    public String getBaloziPhone() { return localAuthorityPhone; }
    public void setBaloziPhone(String baloziPhone) { this.localAuthorityPhone = baloziPhone; }

    public String getBaloziArea() { return localAuthorityArea; }
    public void setBaloziArea(String baloziArea) { this.localAuthorityArea = baloziArea; }

    public String getBaloziVillage() { return localAuthorityVillage; }
    public void setBaloziVillage(String baloziVillage) { this.localAuthorityVillage = baloziVillage; }

    public String getBaloziWard() { return localAuthorityWard; }
    public void setBaloziWard(String baloziWard) { this.localAuthorityWard = baloziWard; }

    public String getBaloziDistrict() { return localAuthorityDistrict; }
    public void setBaloziDistrict(String baloziDistrict) { this.localAuthorityDistrict = baloziDistrict; }

    public String getBaloziRegion() { return localAuthorityRegion; }
    public void setBaloziRegion(String baloziRegion) { this.localAuthorityRegion = baloziRegion; }

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