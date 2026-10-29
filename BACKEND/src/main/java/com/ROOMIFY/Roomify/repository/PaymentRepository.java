package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Payment> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByReferenceNumber(String referenceNumber);
}
