package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.repository.BookingRepository;
import com.ROOMIFY.Roomify.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/audit")
@CrossOrigin(origins = "*")
public class SystemAuditController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/orphaned-properties")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOrphanedProperties() {
        List<Room> allRooms = roomRepository.findAll();
        List<Map<String, Object>> orphaned = new ArrayList<>();

        for (Room room : allRooms) {
            boolean isOrphaned = false;
            String reason = "";

            if (room.getPostedBy() == null) {
                isOrphaned = true;
                reason = "postedBy is NULL";
            } else {
                Optional<User> owner = userRepository.findById(room.getPostedBy());
                if (owner.isEmpty()) {
                    isOrphaned = true;
                    reason = "Owner user with ID " + room.getPostedBy() + " does not exist";
                } else if (!isValidOwnerRole(owner.get().getRole())) {
                    isOrphaned = true;
                    reason = "User " + owner.get().getName() + " has role " + owner.get().getRole() + ", which is not a valid owner role";
                }
            }

            if (isOrphaned) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", room.getId());
                info.put("title", room.getTitle());
                info.put("postedBy", room.getPostedBy());
                info.put("reason", reason);
                orphaned.add(info);
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(true, orphaned, "Found " + orphaned.size() + " orphaned properties"));
    }

    @PostMapping("/cleanup-orphaned-properties")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupOrphanedProperties() {
        List<Room> allRooms = roomRepository.findAll();
        int deletedCount = 0;
        int failedCount = 0;

        for (Room room : allRooms) {
            boolean shouldDelete = false;

            if (room.getPostedBy() == null) {
                shouldDelete = true;
            } else {
                Optional<User> owner = userRepository.findById(room.getPostedBy());
                if (owner.isEmpty() || !isValidOwnerRole(owner.get().getRole())) {
                    shouldDelete = true;
                }
            }

            if (shouldDelete) {
                try {
                    // Handle dependent records
                    // Bookings are associated via @ManyToOne with Room in the Booking entity.
                    // We need to delete them or they will cause FK violations if not cascaded.
                    // Checking BookingRepository for room bookings.
                    bookingRepository.deleteInBatch(bookingRepository.findByRoomId(room.getId()));
                    
                    roomRepository.delete(room);
                    deletedCount++;
                } catch (Exception e) {
                    failedCount++;
                    System.err.println("Failed to delete orphaned room " + room.getId() + ": " + e.getMessage());
                }
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("deleted", deletedCount);
        result.put("failed", failedCount);

        return ResponseEntity.ok(new ApiResponse<>(true, result, "Cleanup complete"));
    }

    private boolean isValidOwnerRole(UserRole role) {
        if (role == null) return false;
        return role == UserRole.OWNER || role == UserRole.DALALI || role == UserRole.ADMIN;
    }
}
