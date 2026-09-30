package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
    List<NotificationSubscription> findByPropertyId(Long propertyId);
    List<NotificationSubscription> findByUserId(Long userId);
    Optional<NotificationSubscription> findByUserIdAndPropertyId(Long userId, Long propertyId);
}
