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
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(@RequestBody BookingRequestDto bookingRequest) {
        try {
            // FIRST: Check if room is available in Room entity
            Room room = roomRepository.findById(bookingRequest.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + bookingRequest.getRoomId()));

            if (!room.isAvailable() || !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, null, "Room is currently not available for booking"));
            }

            // SECOND: Check if room already has an ACTIVE or PENDING booking
            boolean exists = bookingRepository.existsBlockingBookingByRoomId(bookingRequest.getRoomId());
            System.out.println("Checking for blocking bookings for room " + bookingRequest.getRoomId() + ": " + exists);
            
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, null, "Room already has a pending or active booking request"));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startLocalDate = LocalDate.parse(bookingRequest.getStartDate(), formatter);
            LocalDate endLocalDate = LocalDate.parse(bookingRequest.getEndDate(), formatter);

            LocalDateTime startDate = startLocalDate.atStartOfDay();
            LocalDateTime endDate = endLocalDate.atStartOfDay();

            User user = userRepository.findById(bookingRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + bookingRequest.getUserId()));

            Booking booking = new Booking();
            booking.setRoom(room);
            booking.setUser(user);
            booking.setStatus(bookingRequest.getStatus() != null ? bookingRequest.getStatus() : "PENDING");
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            booking.setTotalPrice(bookingRequest.getTotalPrice());
            booking.setStartDate(startDate);
            booking.setEndDate(endDate);
            booking.setNumberOfGuests(bookingRequest.getNumberOfGuests());
            booking.setSpecialRequests(bookingRequest.getSpecialRequests());

            Booking savedBooking = bookingRepository.save(booking);

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
                    }
                }
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

            // If an ACCEPTED/CONFIRMED booking is cancelled, room becomes AVAILABLE again
            if ("ACCEPTED".equals(oldStatus) || "CONFIRMED".equals(oldStatus)) {
                Room room = booking.getRoom();
                if (room != null) {
                    room.setStatus("AVAILABLE");
                    room.setAvailable(true);
                    room.setUpdatedAt(LocalDateTime.now());
                    roomRepository.save(room);
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
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setSpecialRequests(booking.getSpecialRequests());

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
}