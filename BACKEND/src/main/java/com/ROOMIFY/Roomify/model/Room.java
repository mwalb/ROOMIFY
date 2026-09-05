package com.ROOMIFY.Roomify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "property_type")
    private String propertyType;

    private double price;
    private double latitude;
    private double longitude;
    private String address;

    @Column(name = "posted_by")
    private Long postedBy; // Maps to BIGINT, stores user ID

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "rooms_count")
    private int roomsCount;

    @Column(name = "bathrooms_count")
    private int bathroomsCount;

    private double area;
    private String status;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "bookings_count")
    private int bookingsCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "has_video")
    private boolean hasVideo;

    @Column(name = "has_contract")
    private boolean hasContract;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "contract_url")
    private String contractUrl;

    @Column(name = "image_count")
    private int imageCount;

    // ==================== DALALI (AGENT) FIELDS ====================

    @Column(name = "dalali_id")
    private Long dalaliId;

    @Column(name = "dalali_name")
    private String dalaliName;

    @Column(name = "commission")
    private Double commission = 0.0;

    @Column(name = "commission_rate")
    private Double commissionRate = 0.0;

    @Column(name = "featured")
    private boolean featured = false;

    @Column(name = "promoted")
    private boolean promoted = false;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "interested_count")
    private Integer interestedCount = 0;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rented_at")
    private LocalDateTime rentedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dalali_id", insertable = false, updatable = false)
    private User dalali;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "room_rules", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "rule")
    private List<String> rules = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image")
    @OrderColumn(name = "image_order")
    private List<String> images = new ArrayList<>();

    // ==================== CONSTRUCTORS ====================

    public Room() {
        this.isAvailable = true;
        this.status = "AVAILABLE";
        this.bookingsCount = 0;
        this.viewCount = 0;
        this.interestedCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.roomsCount = 1;
        this.bathroomsCount = 1;
        this.imageCount = 0;
        this.hasVideo = false;
        this.hasContract = false;
        this.featured = false;
        this.promoted = false;
        this.commission = 0.0;
        this.commissionRate = 0.0;
    }

    // ==================== DALALI CONSTRUCTOR ====================

    public Room(String title, String description, double price, String address,
                Long postedBy, Long dalaliId, String dalaliName, double commissionRate) {
        this();
        this.title = title;
        this.description = description;
        this.price = price;
        this.address = address;
        this.postedBy = postedBy;
        this.dalaliId = dalaliId;
        this.dalaliName = dalaliName;
        this.commissionRate = commissionRate;
        this.commission = price * (commissionRate / 100);
        this.status = "PENDING"; // Properties listed by dalali need approval
        this.isAvailable = true;
    }

    // ==================== HELPER METHODS ====================

    @Transient
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    @Transient
    public boolean isRoomAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    @Transient
    public boolean isRented() {
        return "RENTED".equalsIgnoreCase(status);
    }

    @Transient
    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    @Transient
    public String getStatusBadge() {
        if (isRoomAvailable()) return "✓ Available";
        if (isPending()) return "⏳ Pending";
        if (isRented()) return "✓ Rented";
        if (isRejected()) return "✗ Rejected";
        return status;
    }
    @Transient
    public String getFormattedPrice() {
        return String.format("TZS %,.0f", price);
    }

    @Transient
    public String getFormattedPriceMonthly() {
        return getFormattedPrice() + "/month";
    }

    @Transient
    public String getCommissionFormatted() {
        if (commission == null) {
            return "TZS 0";
        }
        return String.format("TZS %,.0f", commission);
    }






    @Transient
    public String getLocationSummary() {
        if (address != null && !address.isEmpty()) {
            String[] parts = address.split(",");
            return parts[0].trim();
        }
        return "Location not specified";
    }

    @Transient
    public String getBookingsText() {
        if (bookingsCount == 0) {
            return "No bookings yet";
        } else if (bookingsCount == 1) {
            return "1 interested tenant";
        } else {
            return bookingsCount + " interested tenants";
        }
    }

    @Transient
    public String getFirstImageUrl() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }

    @Transient
    public void calculateCommission() {
        if (commissionRate > 0 && price > 0) {
            this.commission = price * (commissionRate / 100);
        }
    }

    @Transient
    public void incrementViewCount() {
        this.viewCount++;
    }

    @Transient
    public void incrementInterestedCount() {
        this.interestedCount++;
    }

    @Transient
    public void markAsRented() {
        this.status = "RENTED";
        this.isAvailable = false;
        this.rentedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public void markAsAvailable() {
        this.status = "AVAILABLE";
        this.isAvailable = true;
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public void approve() {
        this.status = "AVAILABLE";
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public void reject(String reason) {
        this.status = "REJECTED";
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public String getPropertySummary() {
        StringBuilder sb = new StringBuilder();
        if (roomsCount > 0) sb.append(roomsCount).append(" bed");
        if (bathroomsCount > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(bathroomsCount).append(" bath");
        }
        if (area > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(String.format("%.0f", area)).append(" m²");
        }
        return sb.toString();
    }
}