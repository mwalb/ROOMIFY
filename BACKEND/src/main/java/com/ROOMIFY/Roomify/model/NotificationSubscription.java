package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_subscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "property_id"})
})
@Getter
@Setter
public class NotificationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "notification_type")
    private String notificationType = "AVAILABILITY";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public NotificationSubscription() {}

    public NotificationSubscription(Long userId, Long propertyId) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.createdAt = LocalDateTime.now();
    }
}
