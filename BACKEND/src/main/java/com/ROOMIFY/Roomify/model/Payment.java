package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long roomId;
    private Long userId;
    private Long ownerId;
    private Long dalaliId;
    private Double amount;
    private String paymentType = "RENT"; // "RENT", "FACILITATION_FEE", or "TOTAL"
    private String provider; // M-PESA, TIGO_PESA, AIRTEL_MONEY, CARD, PAY_ON_CHECK_IN
    private String phone;
    private String referenceNumber;
    private String status; // SUCCESS, PENDING, FAILED
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Payment() {}

    public Payment(Long bookingId, Long roomId, Long userId, Long ownerId, Double amount, String provider, String phone, String referenceNumber, String status, String message) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.userId = userId;
        this.ownerId = ownerId;
        this.amount = amount;
        this.provider = provider;
        this.phone = phone;
        this.referenceNumber = referenceNumber;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
