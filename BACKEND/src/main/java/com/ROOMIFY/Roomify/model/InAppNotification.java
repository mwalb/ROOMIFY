package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "in_app_notifications")
@Getter
@Setter
public class InAppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String message;
    private String type; // BOOKING, PAYMENT, REVIEW, SYSTEM, PROPERTY
    private Long referenceId; // e.g. bookingId or roomId
    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public InAppNotification() {}

    public InAppNotification(Long userId, String title, String message, String type, Long referenceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
