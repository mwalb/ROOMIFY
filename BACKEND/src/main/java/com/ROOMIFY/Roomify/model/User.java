package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String phone;

    private String businessName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean emailVerified;
    private String firebaseUid;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLoginAt;

    private String passwordResetToken;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime resetTokenExpiry;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    // ==================== DALALI (AGENT) FIELDS ====================

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "location_area")
    private String locationArea;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_transactions")
    private Integer totalTransactions = 0;

    @Column(name = "total_commission")
    private Double totalCommission = 0.0;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "id_card_image")
    private String idCardImage;

    @Column(name = "license_image")
    private String licenseImage;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "commission_rate")
    private Double commissionRate = 10.0; // Default 10% commission

    @Column(name = "total_properties_sold")
    private Integer totalPropertiesSold = 0;

    @Column(name = "total_views")
    private Integer totalViews = 0;

    @Column(name = "total_interested")
    private Integer totalInterested = 0;

    // ==================== CONSTRUCTORS ====================

    public User() {}

    public User(String name, String email, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    // ==================== DALALI CONSTRUCTOR ====================

    public User(String name, String email, String password, UserRole role,
                String phone, String licenseNumber, String locationArea) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.locationArea = locationArea;
        this.verificationStatus = VerificationStatus.PENDING;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.rating = 0.0;
        this.totalTransactions = 0;
        this.totalCommission = 0.0;
        this.commissionRate = 10.0;
    }

    // ==================== HELPER METHODS ====================

    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(verificationStatus);
    }

    public boolean isPending() {
        return VerificationStatus.PENDING.equals(verificationStatus);
    }

    public boolean isRejected() {
        return VerificationStatus.REJECTED.equals(verificationStatus);
    }

    public String getFormattedRating() {
        if (rating == null || rating == 0.0) {
            return "New Agent";
        }
        return String.format("%.1f ★", rating);
    }

    public String getFormattedCommission() {
        return String.format("TZS %, .0f", totalCommission);
    }

    public void addTransaction(double amount) {
        this.totalTransactions++;
        this.totalCommission += amount;
    }

    public void addView() {
        this.totalViews++;
    }

    public void addInterested() {
        this.totalInterested++;
    }

    // Helper method to convert from long to LocalDateTime if needed
    public void setCreatedAtFromLong(long timestamp) {
        this.createdAt = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);
    }

    public void setLastLoginAtFromLong(long timestamp) {
        this.lastLoginAt = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);
    }
}