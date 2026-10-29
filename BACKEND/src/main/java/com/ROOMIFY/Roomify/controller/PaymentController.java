package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Booking;
import com.ROOMIFY.Roomify.model.Payment;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.repository.BookingRepository;
import com.ROOMIFY.Roomify.repository.PaymentRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@RequestBody Map<String, Object> payload) {
        String provider = (String) payload.getOrDefault("provider", "Pay in Hand (Cash)");
        String phone = (String) payload.getOrDefault("phone", "");
        Double amount = ((Number) payload.getOrDefault("amount", 0.0)).doubleValue();
        Long bookingId = ((Number) payload.getOrDefault("bookingId", 0L)).longValue();
        Long userId = payload.containsKey("userId") ? ((Number) payload.get("userId")).longValue() : null;

        boolean isPayInHand = provider != null && (
                provider.equalsIgnoreCase("Pay in Hand") ||
                provider.equalsIgnoreCase("Pay in Hand (Cash)") ||
                provider.equalsIgnoreCase("PAY_IN_HAND") ||
                provider.equalsIgnoreCase("Pay on Check-In") ||
                provider.equalsIgnoreCase("Cash") ||
                provider.equalsIgnoreCase("CASH_ON_ARRIVAL")
        );

        if (!isPayInHand && (phone == null || phone.isBlank())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Phone number is required for mobile money payment"));
        }

        if (phone == null || phone.isBlank()) {
            phone = isPayInHand ? "CASH_IN_HAND" : "N/A";
        }

        Long roomId = null;
        Long ownerId = null;

        if (bookingId != null && bookingId > 0) {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                if (userId == null && booking.getUser() != null) {
                    userId = booking.getUser().getId();
                }
                if (booking.getRoom() != null) {
                    roomId = booking.getRoom().getId();
                    ownerId = booking.getRoom().getPostedBy();
                }
                // If paying online, mark CONFIRMED; if paying in hand, mark PENDING_APPROVAL / PENDING_PAYMENT
                if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                    booking.setStatus(isPayInHand ? "PENDING_CASH" : "CONFIRMED");
                    booking.setUpdatedAt(LocalDateTime.now());
                    bookingRepository.save(booking);
                }
            }
        }

        String referenceNo = "ROOMIFY-TZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String status = isPayInHand ? "PENDING_CASH" : "SUCCESS";
        String message = isPayInHand
                ? "Ombi la malipo ya mkononi ya TZS " + amount.longValue() + " limerekodiwa. Utalipa fedha taslimu (Cash) kwa mwenye nyumba wakati wa kufika. Kumbukumbu: " + referenceNo
                : "Malipo ya TZS " + amount.longValue() + " yamekamilika kikamilifu kupitia " + provider + ". Kumbukumbu: " + referenceNo;

        Payment payment = new Payment(
                bookingId,
                roomId,
                userId,
                ownerId,
                amount,
                provider,
                phone,
                referenceNo,
                status,
                message
        );

        Payment savedPayment = paymentRepository.save(payment);

        return ResponseEntity.ok(new ApiResponse<>(true, savedPayment, "Payment processed successfully"));
    }

    @PutMapping("/{paymentId}/confirm-cash")
    public ResponseEntity<ApiResponse<Payment>> confirmCashPayment(@PathVariable Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payment payment = paymentOpt.get();
        payment.setStatus("SUCCESS");
        payment.setMessage("Malipo ya mkononi (Cash) yamethibitishwa kupokelewa kikamilifu na mwenye nyumba.");
        payment.setUpdatedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        if (payment.getBookingId() != null) {
            bookingRepository.findById(payment.getBookingId()).ifPresent(b -> {
                b.setStatus("CONFIRMED");
                b.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(b);
            });
        }

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Cash payment confirmed successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getUserPayments(@PathVariable Long userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, payments, "User payments retrieved"));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getOwnerPayments(@PathVariable Long ownerId) {
        List<Payment> payments = paymentRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        return ResponseEntity.ok(new ApiResponse<>(true, payments, "Owner payments retrieved"));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> getBookingPayment(@PathVariable Long bookingId) {
        Optional<Payment> payment = paymentRepository.findByBookingId(bookingId);
        return payment.map(p -> ResponseEntity.ok(new ApiResponse<>(true, p, "Payment found")))
                .orElseGet(() -> ResponseEntity.ok(new ApiResponse<>(false, null, "No payment found for booking")));
    }

    @GetMapping("/finances/{ownerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOwnerFinances(@PathVariable Long ownerId) {
        List<Payment> payments = paymentRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        double totalRevenue = payments.stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();

        Map<String, Object> finances = new HashMap<>();
        finances.put("totalRevenue", totalRevenue);
        finances.put("completedPaymentsCount", payments.size());
        finances.put("recentPayments", payments);
        finances.put("currency", "TZS");

        return ResponseEntity.ok(new ApiResponse<>(true, finances, "Finances summary retrieved"));
    }
}
