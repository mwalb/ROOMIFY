package com.ROOMIFY.Roomify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "timestamp", nullable = false)  // Add this field
    private LocalDateTime timestamp;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "rent_amount")
    private Double rentAmount = 0.0;

    @Column(name = "facilitation_fee")
    private Double facilitationFee = 0.0;

    @Column(name = "listing_type")
    private String listingType = "DIRECT_BY_OWNER";

    @Column(name = "dalali_id")
    private Long dalaliId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    // ==================== TANZANIA VIEWING & TENANCY DETAILS (3NF) ====================

    @Column(name = "booking_type")
    private String bookingType = "RESERVATION"; // "VIEWING" (Ukaguzi wa Nyumba) or "RESERVATION" (Ulipaji wa Chumba)

    @Column(name = "viewing_time_slot")
    private String viewingTimeSlot; // e.g. "ASUBUHI (09:00 - 12:00)", "MCHANA (13:00 - 16:00)", "JIONI (16:30 - 18:30)"

    @Column(name = "meeting_point")
    private String meetingPoint; // e.g. "Kituo cha Mwendokasi / Stand", "Moja kwa moja nyumbani"

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        timestamp = now;  // Set timestamp field
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        timestamp = LocalDateTime.now();  // Update timestamp on every update
    }
}