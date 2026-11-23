package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.Component.JwtUtil;
import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.DalaliStats;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dalali")
public class DalaliController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private com.ROOMIFY.Roomify.repository.BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<Room>>> getAgentProperties(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            List<Room> properties = roomRepository.findByDalaliId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Properties retrieved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/properties/pending")
    public ResponseEntity<ApiResponse<List<Room>>> getPendingProperties(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            List<Room> properties = roomRepository.findPendingPropertiesByDalali(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Pending properties retrieved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/properties/rented")
    public ResponseEntity<ApiResponse<List<Room>>> getRentedProperties(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            List<Room> properties = roomRepository.findRentedPropertiesByDalali(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, properties, "Rented properties retrieved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DalaliStats>> getDalaliStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            User user = userRepository.findById(userId).orElse(null);

            DalaliStats stats = new DalaliStats();
            stats.setTotalListings(roomRepository.countByDalaliId(userId));
            stats.setActiveListings(roomRepository.countActiveByDalaliId(userId));
            stats.setPendingListings(roomRepository.countPendingByDalaliId(userId));
            stats.setRentedListings(roomRepository.countRentedByDalaliId(userId));

            Double totalFees = bookingRepository.sumFacilitationFeesByDalaliId(userId);
            if (totalFees == null || totalFees == 0) {
                totalFees = roomRepository.sumCommissionByDalaliId(userId);
            }
            stats.setTotalCommission(totalFees != null ? totalFees : 0);

            Double monthlyComm = roomRepository.sumMonthlyCommissionByDalaliId(userId);
            stats.setMonthlyCommission(monthlyComm != null ? monthlyComm : 0);

            Integer totalViews = roomRepository.sumViewsByDalaliId(userId);
            stats.setTotalViews(totalViews != null ? totalViews : 0);

            Integer totalInterested = roomRepository.sumInterestedByDalaliId(userId);
            stats.setTotalInterested(totalInterested != null ? totalInterested : 0);

            stats.setAverageRating(user != null && user.getRating() != null ? user.getRating().floatValue() : 0);

            // Convert enum to string for the response
            if (user != null && user.getVerificationStatus() != null) {
                stats.setVerificationStatus(user.getVerificationStatus().name());
            } else {
                stats.setVerificationStatus("PENDING");
            }

            return ResponseEntity.ok(new ApiResponse<>(true, stats, "Stats retrieved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getDalaliProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, user, "Profile retrieved"));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "User not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/properties/{propertyId}/rented")
    public ResponseEntity<ApiResponse<Void>> markAsRented(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long propertyId) {
        try {
            Long userId = resolveUserId(authHeader);
            Room room = roomRepository.findById(propertyId).orElse(null);

            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Property not found"));
            }

            if (!room.getDalaliId().equals(userId)) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            room.setStatus("RENTED");
            room.setAvailable(false);
            roomRepository.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property marked as rented"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    private Long resolveUserId(String authHeader) {
        try {
            // 1. Try SecurityContextHolder first
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String principalEmail = auth.getName();
                if (principalEmail != null && !principalEmail.isBlank()) {
                    Long id = userRepository.findByEmail(principalEmail).map(User::getId).orElse(null);
                    if (id != null) return id;
                }
            }

            // 2. Fallback to extracting from the Authorization header using JwtUtil
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                if (email != null && !email.isBlank()) {
                    return userRepository.findByEmail(email).map(User::getId).orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
