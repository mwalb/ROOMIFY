package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.BookingRequestDto;
import com.ROOMIFY.Roomify.dto.BookingResponseDTO;
import com.ROOMIFY.Roomify.model.Booking;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.BookingRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.model.VerificationStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ROOMIFY.Roomify.service.NotificationService notificationService;

    @Autowired
    private com.ROOMIFY.Roomify.service.EmailService emailService;

    @Autowired(required = false)
    private com.ROOMIFY.Roomify.service.RoomNotifier roomNotifier;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(@RequestBody BookingRequestDto bookingRequest) {
        try {
            // Check authenticated user role
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authentication required to book a property"));
            }

            User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authenticated user not found"));
            }

            if (!UserRole.TENANT.equals(currentUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Only Tenants are allowed to book properties. Your role (" + currentUser.getRole() + ") is not authorized to book."));
            }

            // FIRST: Check if room exists
            Room room = roomRepository.findById(bookingRequest.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Property not found with id: " + bookingRequest.getRoomId()));

            // SECOND: Check if room is VERIFIED
            if (room.getVerificationStatus() != VerificationStatus.VERIFIED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, null, "Property must be verified before it can be booked"));
            }

            // THIRD: Check if room is available
            if (!room.isAvailable() || !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, null, "Property is currently not available for booking"));
            }

            // FOURTH: Check if room already has an ACTIVE or PENDING booking (only blocks for full reservations)
            boolean isReservation = bookingRequest.getBookingType() == null || "RESERVATION".equalsIgnoreCase(bookingRequest.getBookingType());
            if (isReservation) {
                boolean exists = bookingRepository.existsBlockingBookingByRoomId(bookingRequest.getRoomId());
                System.out.println("Checking for blocking bookings for room " + bookingRequest.getRoomId() + ": " + exists);
                
                if (exists) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponse<>(false, null, "Property already has a pending or active booking request"));
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startLocalDate = LocalDate.parse(bookingRequest.getStartDate(), formatter);
            LocalDate endLocalDate = LocalDate.parse(bookingRequest.getEndDate(), formatter);

            LocalDateTime startDate = startLocalDate.atStartOfDay();
            LocalDateTime endDate = endLocalDate.atStartOfDay();

            // Decoupled Fee Calculation:
            // Rent belongs 100% to the Owner (room.getPrice()).
            // If Dalali is attached, a separate standalone facilitation fee is added, NEVER deducted from rent!
            boolean isDalaliListing = room.getDalaliId() != null && room.getDalaliId() > 0;
            String listingType = isDalaliListing ? "DALALI_VERIFIED" : "DIRECT_BY_OWNER";
            double rentAmount = room.getPrice();
            double facilitationFee = isDalaliListing ? (room.getFacilitationFee() != null && room.getFacilitationFee() > 0 ? room.getFacilitationFee() : 10000.0) : 0.0;
            double calculatedTotal = rentAmount + facilitationFee;

            Booking booking = new Booking();
            booking.setRoom(room);
            booking.setUser(currentUser);
            booking.setStatus(bookingRequest.getStatus() != null ? bookingRequest.getStatus() : "PENDING");
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            booking.setRentAmount(rentAmount);
            booking.setFacilitationFee(facilitationFee);
            booking.setListingType(listingType);
            booking.setDalaliId(room.getDalaliId());
            booking.setTotalPrice(calculatedTotal);
            booking.setStartDate(startDate);
            booking.setEndDate(endDate);
            booking.setNumberOfGuests(bookingRequest.getNumberOfGuests());
            booking.setSpecialRequests(bookingRequest.getSpecialRequests());
            booking.setBookingType(bookingRequest.getBookingType() != null ? bookingRequest.getBookingType() : "RESERVATION");
            booking.setViewingTimeSlot(bookingRequest.getViewingTimeSlot());
            booking.setMeetingPoint(bookingRequest.getMeetingPoint());

            Booking savedBooking = bookingRepository.save(booking);

            try {
                notificationService.notifyBookingCreated(savedBooking);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send booking creation notifications: " + notifEx.getMessage());
            }

            BookingResponseDTO responseDTO = convertToDTO(savedBooking);

            return ResponseEntity.ok(new ApiResponse<>(true, responseDTO, "Booking created successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Failed to create booking: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getUserBookings(@PathVariable Long userId) {
        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);

            List<BookingResponseDTO> dtos = bookings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, dtos, "Bookings retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getOwnerBookings(@PathVariable Long ownerId) {
        try {
            List<Booking> bookings = bookingRepository.findByOwnerOrDalali(ownerId);
            List<BookingResponseDTO> dtos = bookings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, dtos, "Bookings retrieved successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/room/{roomId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> checkUserBooking(
            @PathVariable Long userId,
            @PathVariable Long roomId) {
        try {
            List<Booking> bookings = bookingRepository.findByUserIdAndRoomId(userId, roomId);

            List<BookingResponseDTO> dtos = bookings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, dtos, "Check completed"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(true, new ArrayList<>(), "No bookings found"));
        }
    }

    // ========== NEW ENDPOINTS FOR CONSISTENCY ==========

    // Check if room has ANY booking (by ANY user)
    @GetMapping("/room/{roomId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> isRoomBooked(@PathVariable Long roomId) {
        try {
            boolean hasAnyBooking = bookingRepository.existsAnyBookingByRoomId(roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, hasAnyBooking,
                    hasAnyBooking ? "Room has bookings" : "Room has no bookings"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "Error checking room: " + e.getMessage()));
        }
    }

    // Check if room has ACTIVE booking (ACCEPTED/CONFIRMED)
    @GetMapping("/room/{roomId}/active-exists")
    public ResponseEntity<ApiResponse<Boolean>> isRoomActiveBooking(@PathVariable Long roomId) {
        try {
            boolean hasActiveBooking = bookingRepository.existsActiveBookingByRoomId(roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, hasActiveBooking,
                    hasActiveBooking ? "Room has active booking" : "Room has no active booking"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "Error checking room: " + e.getMessage()));
        }
    }

    // Get count of ALL bookings for a room
    @GetMapping("/room/{roomId}/count")
    public ResponseEntity<ApiResponse<Integer>> getRoomBookingsCount(@PathVariable Long roomId) {
        try {
            int count = bookingRepository.countAllBookingsByRoomId(roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, count, "Count retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 0, "Error getting count: " + e.getMessage()));
        }
    }

    // Get detailed room booking status (for map color logic)
    @GetMapping("/room/{roomId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomBookingStatus(@PathVariable Long roomId) {
        try {
            Map<String, Object> status = new HashMap<>();

            // Total bookings count
            int totalBookings = bookingRepository.countAllBookingsByRoomId(roomId);
            status.put("totalBookings", totalBookings);

            // Has any booking
            status.put("hasAnyBooking", totalBookings > 0);

            // Has active booking (ACCEPTED/CONFIRMED)
            boolean hasActiveBooking = bookingRepository.existsActiveBookingByRoomId(roomId);
            status.put("hasActiveBooking", hasActiveBooking);

            // Count by status
            status.put("pendingCount", bookingRepository.countBookingsByRoomIdAndStatus(roomId, "PENDING"));
            status.put("acceptedCount", bookingRepository.countBookingsByRoomIdAndStatus(roomId, "ACCEPTED"));
            status.put("confirmedCount", bookingRepository.countBookingsByRoomIdAndStatus(roomId, "CONFIRMED"));
            status.put("rejectedCount", bookingRepository.countBookingsByRoomIdAndStatus(roomId, "REJECTED"));
            status.put("cancelledCount", bookingRepository.countBookingsByRoomIdAndStatus(roomId, "CANCELLED"));

            return ResponseEntity.ok(new ApiResponse<>(true, status, "Status retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error getting status: " + e.getMessage()));
        }
    }

    // Check if room has booking by other users (for new user login)
    @GetMapping("/room/{roomId}/other-user-booking")
    public ResponseEntity<ApiResponse<Boolean>> hasOtherUserBooking(
            @PathVariable Long roomId,
            @RequestParam Long currentUserId) {
        try {
            boolean hasOtherBooking = bookingRepository.existsOtherUserBooking(roomId, currentUserId);
            return ResponseEntity.ok(new ApiResponse<>(true, hasOtherBooking,
                    hasOtherBooking ? "Room booked by other user" : "No other user bookings"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "Error checking: " + e.getMessage()));
        }
    }

    // ========== EXISTING ENDPOINTS ==========

    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> acceptBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
            
            // Mark booking as accepted
            booking.setStatus("ACCEPTED");
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            // Sync Room status to RENTED
            Room room = booking.getRoom();
            if (room != null) {
                room.setStatus("RENTED");
                room.setAvailable(false);
                room.setRentedAt(LocalDateTime.now());
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);
                
                // Automatically reject other PENDING bookings for this room
                List<Booking> otherBookings = bookingRepository.findByRoomId(room.getId());
                for (Booking other : otherBookings) {
                    if (!other.getId().equals(bookingId) && "PENDING".equals(other.getStatus())) {
                        other.setStatus("REJECTED");
                        other.setUpdatedAt(LocalDateTime.now());
                        bookingRepository.save(other);
                        try {
                            notificationService.notifyBookingStatusUpdated(other, "REJECTED", "Property was booked by another tenant");
                        } catch (Exception notifEx) {
                            System.err.println("Warning: Failed to notify other tenant: " + notifEx.getMessage());
                        }
                    }
                }
            }

            try {
                notificationService.notifyBookingStatusUpdated(savedBooking, "ACCEPTED", null);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send booking acceptance notification: " + notifEx.getMessage());
            }

            BookingResponseDTO responseDTO = convertToDTO(savedBooking);

            return ResponseEntity.ok(new ApiResponse<>(true, responseDTO, "Booking accepted and room marked as rented"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to accept booking: " + e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> rejectBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
            booking.setStatus("REJECTED");
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            // Ensure room is AVAILABLE if it was pending
            Room room = booking.getRoom();
            if (room != null && "PENDING".equals(room.getStatus())) {
                room.setStatus("AVAILABLE");
                room.setAvailable(true);
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);
            }

            BookingResponseDTO responseDTO = convertToDTO(savedBooking);

            return ResponseEntity.ok(new ApiResponse<>(true, responseDTO, "Booking rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to reject booking: " + e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
            
            String oldStatus = booking.getStatus();
            booking.setStatus("CANCELLED");
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            try {
                notificationService.notifyBookingCancelled(savedBooking);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send booking cancellation notification: " + notifEx.getMessage());
            }

            // If an ACCEPTED/CONFIRMED booking is cancelled, room becomes AVAILABLE again
            if ("ACCEPTED".equals(oldStatus) || "CONFIRMED".equals(oldStatus)) {
                Room room = booking.getRoom();
                if (room != null) {
                    room.setStatus("AVAILABLE");
                    room.setAvailable(true);
                    room.setUpdatedAt(LocalDateTime.now());
                    roomRepository.save(room);
                    try {
                        notificationService.notifyRoomAvailable(room);
                    } catch (Exception availEx) {
                        System.err.println("Warning: Failed to send availability notification: " + availEx.getMessage());
                    }
                }
            }

            BookingResponseDTO responseDTO = convertToDTO(savedBooking);

            return ResponseEntity.ok(new ApiResponse<>(true, responseDTO, "Booking cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to cancel booking: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long bookingId) {
        try {
            bookingRepository.deleteById(bookingId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Booking deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete booking: " + e.getMessage()));
        }
    }

    @GetMapping("/debug/all")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookingsDebug() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            return ResponseEntity.ok(new ApiResponse<>(true, bookings, "Retrieved all " + bookings.size() + " bookings"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    private BookingResponseDTO convertToDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();

        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setRentAmount(booking.getRentAmount() != null ? booking.getRentAmount() : (booking.getRoom() != null ? booking.getRoom().getPrice() : booking.getTotalPrice()));
        dto.setFacilitationFee(booking.getFacilitationFee() != null ? booking.getFacilitationFee() : 0.0);
        dto.setListingType(booking.getListingType() != null ? booking.getListingType() : "DIRECT_BY_OWNER");
        dto.setDalaliId(booking.getDalaliId());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setBookingType(booking.getBookingType() != null ? booking.getBookingType() : "RESERVATION");
        dto.setViewingTimeSlot(booking.getViewingTimeSlot());
        dto.setMeetingPoint(booking.getMeetingPoint());

        User user = booking.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        }

        Room room = booking.getRoom();
        if (room != null) {
            dto.setRoomId(room.getId());
            dto.setRoomTitle(room.getTitle());
            dto.setRoomImageUrl(room.getFirstImageUrl());
        }

        return dto;
    }

    // =========================================================================
    // 1-Click Email Action for Landlord / Owner (Direct Accept/Decline via Email)
    // =========================================================================

    @GetMapping(value = "/email-action", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> handleEmailAction(
            @RequestParam Long bookingId,
            @RequestParam String action,
            @RequestParam(required = false) String token) {

        if (bookingId == null || action == null) {
            return ResponseEntity.badRequest().body(renderEmailActionResultPage(false, "Invalid Request", "Booking ID or action is missing.", null));
        }

        if (!emailService.isValidActionToken(bookingId, action, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(renderEmailActionResultPage(false, "Security Verification Failed", "Invalid or expired action link. Please log in to your dashboard to manage this booking.", null));
        }

        java.util.Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(renderEmailActionResultPage(false, "Booking Not Found", "The requested booking does not exist or may have been deleted.", null));
        }

        Booking booking = bookingOpt.get();
        Room room = booking.getRoom();
        String roomTitle = room != null ? room.getTitle() : "Property Listing";

        if ("ACCEPT".equalsIgnoreCase(action)) {
            if ("ACCEPTED".equalsIgnoreCase(booking.getStatus()) || "CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                return ResponseEntity.ok(renderEmailActionResultPage(true, "Already Accepted", "This booking for '" + roomTitle + "' has already been accepted and confirmed.", roomTitle));
            }

            booking.setStatus("ACCEPTED");
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            if (room != null) {
                room.setStatus("RENTED");
                room.setAvailable(false);
                room.setRentedAt(LocalDateTime.now());
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);

                // Reject other pending bookings for this room
                List<Booking> otherBookings = bookingRepository.findByRoomId(room.getId());
                for (Booking other : otherBookings) {
                    if (!other.getId().equals(bookingId) && "PENDING".equals(other.getStatus())) {
                        other.setStatus("REJECTED");
                        other.setUpdatedAt(LocalDateTime.now());
                        bookingRepository.save(other);
                        try {
                            notificationService.notifyBookingStatusUpdated(other, "REJECTED", "Property was booked by another tenant");
                        } catch (Exception ignored) {}
                    }
                }

                // Broadcast live update across all connected clients via WebSocket
                if (roomNotifier != null) {
                    try {
                        roomNotifier.notifyRoomUpdate(room);
                    } catch (Exception ignored) {}
                }
            }

            try {
                notificationService.notifyBookingStatusUpdated(savedBooking, "ACCEPTED", null);
            } catch (Exception ex) {
                System.err.println("Warning: Failed to dispatch acceptance notification: " + ex.getMessage());
            }

            return ResponseEntity.ok(renderEmailActionResultPage(true, "Booking Accepted! 🎉", "You have successfully accepted the booking for '" + roomTitle + "'. The tenant has been notified immediately via email and in-app notification, and the property is now marked as RENTED.", roomTitle));

        } else if ("REJECT".equalsIgnoreCase(action)) {
            booking.setStatus("REJECTED");
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            try {
                notificationService.notifyBookingStatusUpdated(savedBooking, "REJECTED", "Declined by property owner via email action");
            } catch (Exception ex) {
                System.err.println("Warning: Failed to dispatch rejection notification: " + ex.getMessage());
            }

            return ResponseEntity.ok(renderEmailActionResultPage(true, "Booking Request Declined", "You have declined this booking request. The listing remains available for other tenants.", roomTitle));
        }

        return ResponseEntity.badRequest().body(renderEmailActionResultPage(false, "Unknown Action", "Action '" + action + "' is not supported.", null));
    }

    private String renderEmailActionResultPage(boolean success, String title, String message, String propertyTitle) {
        String statusColor = success ? "#10B981" : "#EF4444";
        String statusBg = success ? "#ECFDF5" : "#FEF2F2";
        String icon = success ? "&#10004;" : "&#10008;";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - ROOMIFY</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        background: linear-gradient(135deg, #f0f4ff 0%%, #e2e8f0 100%%);
                        margin: 0;
                        padding: 24px;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        box-sizing: border-box;
                    }
                    .card {
                        background: #ffffff;
                        max-width: 480px;
                        width: 100%%;
                        border-radius: 20px;
                        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
                        padding: 36px 28px;
                        text-align: center;
                        border: 1px solid #e2e8f0;
                    }
                    .brand {
                        font-size: 20px;
                        font-weight: 900;
                        color: #142170;
                        letter-spacing: 1px;
                        margin-bottom: 24px;
                    }
                    .icon-box {
                        width: 64px;
                        height: 64px;
                        border-radius: 50%%;
                        background: %s;
                        color: %s;
                        display: inline-flex;
                        justify-content: center;
                        align-items: center;
                        font-size: 28px;
                        margin-bottom: 20px;
                    }
                    h1 {
                        font-size: 22px;
                        font-weight: 800;
                        color: #0f172a;
                        margin: 0 0 12px 0;
                    }
                    p {
                        font-size: 14px;
                        color: #475569;
                        line-height: 1.6;
                        margin: 0 0 24px 0;
                    }
                    .btn {
                        display: inline-block;
                        background: #142170;
                        color: #ffffff;
                        text-decoration: none;
                        padding: 12px 28px;
                        border-radius: 10px;
                        font-weight: 700;
                        font-size: 14px;
                        transition: background 0.2s;
                    }
                    .btn:hover {
                        background: #1e3a8a;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="brand">ROOMIFY</div>
                    <div class="icon-box">%s</div>
                    <h1>%s</h1>
                    <p>%s</p>
                    <a href="https://roomify.co.tz" class="btn">Open Roomify App</a>
                </div>
            </body>
            </html>
        """.formatted(title, statusBg, statusColor, icon, title, message);
    }
}