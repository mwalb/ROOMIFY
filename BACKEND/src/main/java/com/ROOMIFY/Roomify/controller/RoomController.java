package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.service.FCMService;
import com.ROOMIFY.Roomify.service.RoomNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomRepository repo;

    @Autowired
    private NotificationController notificationController;

    @Autowired
    private RoomNotifier notifier;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    // ==================== EXISTING ENDPOINTS ====================

    // Create a new room
    @PostMapping
    @Transactional
    public ResponseEntity<?> addRoom(@RequestBody Room room) {
        try {
            System.out.println("Received room: " + room);
            System.out.println("PostedBy: " + room.getPostedBy());

            if (room == null) {
                return ResponseEntity.badRequest().body("Room is null");
            }

            // Set default values
            if (room.getCreatedAt() == null) {
                room.setCreatedAt(LocalDateTime.now());
            }
            if (room.getUpdatedAt() == null) {
                room.setUpdatedAt(LocalDateTime.now());
            }
            if (room.getStatus() == null || room.getStatus().isEmpty()) {
                room.setStatus("PENDING");
            }
            if (room.getRoomsCount() <= 0) {
                room.setRoomsCount(1);
            }
            if (room.getBathroomsCount() <= 0) {
                room.setBathroomsCount(1);
            }
            if (room.getAmenities() == null) {
                room.setAmenities(new ArrayList<>());
            }
            if (room.getRules() == null) {
                room.setRules(new ArrayList<>());
            }
            if (room.getImages() == null) {
                room.setImages(new ArrayList<>());
            }

            room.setAvailable(true);
            if (room.getBookingsCount() == 0) {
                room.setBookingsCount(0);
            }
            if (room.getViewCount() == null) {
                room.setViewCount(0);
            }
            if (room.getInterestedCount() == null) {
                room.setInterestedCount(0);
            }
            if (room.getImageCount() == 0 && room.getImages() != null) {
                room.setImageCount(room.getImages().size());
            }
            room.setHasVideo(room.isHasVideo());
            room.setHasContract(room.isHasContract());

            // Calculate commission if dalali is involved
            if (room.getDalaliId() != null && room.getDalaliId() > 0 && room.getCommissionRate() > 0) {
                room.calculateCommission();
            }

            // Validate required fields
            if (room.getPostedBy() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("postedBy is required");
            }
            if (room.getTitle() == null || room.getTitle().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("title is required");
            }
            if (room.getContactPhone() == null || room.getContactPhone().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("contactPhone is required");
            }

            System.out.println("Attempting to save room with postedBy: " + room.getPostedBy());
            Room saved = repo.saveAndFlush(room);
            System.out.println("Room saved successfully with ID: " + saved.getId());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            System.err.println("ERROR SAVING ROOM: " + e.getMessage());
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            System.err.println("Root cause: " + rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + rootCause.getMessage());
        }
    }

    // Update room
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) return ResponseEntity.notFound().build();

            room.setTitle(roomDetails.getTitle());
            room.setDescription(roomDetails.getDescription());
            room.setPrice(roomDetails.getPrice());
            room.setLatitude(roomDetails.getLatitude());
            room.setLongitude(roomDetails.getLongitude());
            room.setAddress(roomDetails.getAddress());
            room.setPropertyType(roomDetails.getPropertyType());
            room.setContactPhone(roomDetails.getContactPhone());
            room.setContactEmail(roomDetails.getContactEmail());
            room.setOwnerName(roomDetails.getOwnerName());
            room.setAmenities(roomDetails.getAmenities());
            room.setRules(roomDetails.getRules());
            room.setRoomsCount(roomDetails.getRoomsCount());
            room.setBathroomsCount(roomDetails.getBathroomsCount());
            room.setArea(roomDetails.getArea());
            room.setAvailable(roomDetails.isAvailable());
            room.setHasVideo(roomDetails.isHasVideo());
            room.setHasContract(roomDetails.isHasContract());
            room.setVideoUrl(roomDetails.getVideoUrl());
            room.setContractUrl(roomDetails.getContractUrl());
            room.setImages(roomDetails.getImages());
            room.setImageCount(roomDetails.getImages() != null ? roomDetails.getImages().size() : 0);
            room.setStatus(roomDetails.getStatus());
            room.setUpdatedAt(LocalDateTime.now());

            // Update commission if needed
            if (room.getDalaliId() != null && room.getCommissionRate() > 0) {
                room.calculateCommission();
            }

            Room updated = repo.save(room);
            notifier.notifyRoomUpdate(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Delete room
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) return ResponseEntity.notFound().build();

            repo.deleteById(id);
            notifier.notifyRoomUpdate(room);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room");
        }
    }

    // ==================== FIXED METHODS ====================

    // Get room by ID - FIXED with proper lazy loading handling
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            // Increment view count (using a separate query to avoid issues)
            try {
                repo.incrementViewCount(id);
            } catch (Exception e) {
                System.err.println("Failed to increment view count: " + e.getMessage());
            }

            // Force initialize lazy collections while session is open
            if (room.getImages() != null) {
                room.getImages().size(); // Force initialization
            }
            if (room.getAmenities() != null) {
                room.getAmenities().size(); // Force initialization
            }
            if (room.getRules() != null) {
                room.getRules().size(); // Force initialization
            }

            // Set dalali to null to avoid circular reference during serialization
            room.setDalali(null);

            return ResponseEntity.ok(room);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all rooms - FIXED with proper lazy loading handling
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Room> rooms = repo.findAll();

            // Initialize lazy collections for each room
            for (Room room : rooms) {
                if (room.getImages() != null) {
                    room.getImages().size();
                }
                if (room.getAmenities() != null) {
                    room.getAmenities().size();
                }
                if (room.getRules() != null) {
                    room.getRules().size();
                }
                room.setDalali(null);
            }

            return ResponseEntity.ok(rooms);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all rooms posted by a specific user (owner) - FIXED
    @GetMapping("/owner/{postedBy}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomsByOwner(@PathVariable Long postedBy) {
        try {
            List<Room> rooms = repo.findByPostedBy(postedBy);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) {
                    room.getImages().size();
                }
                if (room.getAmenities() != null) {
                    room.getAmenities().size();
                }
                if (room.getRules() != null) {
                    room.getRules().size();
                }
                room.setDalali(null);
            }

            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all available rooms - FIXED
    @GetMapping("/available")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAvailableRooms() {
        try {
            List<Room> rooms = repo.findByIsAvailableTrue();

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) {
                    room.getImages().size();
                }
                if (room.getAmenities() != null) {
                    room.getAmenities().size();
                }
                if (room.getRules() != null) {
                    room.getRules().size();
                }
                room.setDalali(null);
            }

            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get rooms by status - FIXED
    @GetMapping("/status/{status}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomsByStatus(@PathVariable String status) {
        try {
            List<Room> rooms = repo.findByStatus(status);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) {
                    room.getImages().size();
                }
                if (room.getAmenities() != null) {
                    room.getAmenities().size();
                }
                if (room.getRules() != null) {
                    room.getRules().size();
                }
                room.setDalali(null);
            }

            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // ==================== BOOKING COUNT ENDPOINT ====================

    @GetMapping("/{roomId}/bookings/count")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Integer>> getBookingCount(@PathVariable Long roomId) {
        try {
            Room room = repo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, room.getBookingsCount(), "Count retrieved"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // ==================== DALALI (AGENT) ENDPOINTS ====================

    // Get properties managed by a specific dalali agent
    @GetMapping("/dalali/{dalaliId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByDalali(@PathVariable Long dalaliId) {
        try {
            List<Room> rooms = repo.findByDalaliId(dalaliId);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get pending properties for a dalali
    @GetMapping("/dalali/{dalaliId}/pending")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getPendingPropertiesByDalali(@PathVariable Long dalaliId) {
        try {
            List<Room> rooms = repo.findPendingPropertiesByDalali(dalaliId);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Pending properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get rented properties for a dalali
    @GetMapping("/dalali/{dalaliId}/rented")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getRentedPropertiesByDalali(@PathVariable Long dalaliId) {
        try {
            List<Room> rooms = repo.findRentedPropertiesByDalali(dalaliId);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Rented properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get available properties for a dalali
    @GetMapping("/dalali/{dalaliId}/available")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getAvailablePropertiesByDalali(@PathVariable Long dalaliId) {
        try {
            List<Room> rooms = repo.findAvailablePropertiesByDalali(dalaliId);

            // Initialize lazy collections
            for (Room room : rooms) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Available properties retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Approve a property (for admin or owner)
    @PutMapping("/{roomId}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> approveProperty(@PathVariable Long roomId) {
        try {
            Room room = repo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            room.approve();
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Reject a property
    @PutMapping("/{roomId}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> rejectProperty(
            @PathVariable Long roomId,
            @RequestParam(required = false) String reason) {
        try {
            Room room = repo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            room.reject(reason != null ? reason : "No reason provided");
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property rejected"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Mark property as rented
    @PutMapping("/{roomId}/rented")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAsRented(@PathVariable Long roomId) {
        try {
            Room room = repo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            room.markAsRented();
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property marked as rented"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Mark property as featured
    @PutMapping("/{roomId}/featured")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAsFeatured(@PathVariable Long roomId) {
        try {
            Room room = repo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            room.setFeatured(true);
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property marked as featured"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Increment interested count
    @PostMapping("/{roomId}/interested")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> incrementInterested(@PathVariable Long roomId) {
        try {
            repo.incrementInterestedCount(roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Interested count incremented"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Check if room is available for booking
    @GetMapping("/{roomId}/is-available")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Boolean>> isRoomAvailableForBooking(@PathVariable Long roomId) {
        try {
            boolean isAvailable = repo.isRoomAvailableForBooking(roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, isAvailable, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get featured properties
    @GetMapping("/featured")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getFeaturedProperties() {
        try {
            List<Room> featured = repo.findByFeaturedTrue();

            // Initialize lazy collections
            for (Room room : featured) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, featured, "Featured properties retrieved"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Search properties by keyword
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> searchProperties(@RequestParam String keyword) {
        try {
            List<Room> results = repo.searchByKeyword(keyword);

            // Initialize lazy collections
            for (Room room : results) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, results, "Search results retrieved"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get properties by price range
    @GetMapping("/price-range")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getPropertiesByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        try {
            List<Room> results = repo.findByPriceBetween(min, max);

            // Initialize lazy collections
            for (Room room : results) {
                if (room.getImages() != null) room.getImages().size();
                if (room.getAmenities() != null) room.getAmenities().size();
                if (room.getRules() != null) room.getRules().size();
                room.setDalali(null);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, results, "Properties retrieved by price range"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // ==================== MEDIA UPLOAD ENDPOINTS ====================

    // Upload images for a room
    @PostMapping("/{roomId}/images")
    @Transactional
    public ResponseEntity<ApiResponse<List<String>>> uploadRoomImages(
            @PathVariable Long roomId,
            @RequestParam("images") MultipartFile[] files) {

        try {
            Room room = repo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = Paths.get(uploadDir, "rooms", String.valueOf(roomId), "images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String originalFileName = file.getOriginalFilename();
                String contentType = file.getContentType();
                String fileExtension = "";
                
                if (contentType != null) {
                    if (contentType.equals("image/webp") || (originalFileName != null && originalFileName.toLowerCase().endsWith(".webp"))) {
                        fileExtension = ".webp";
                    } else if (contentType.equals("image/jpeg")) {
                        fileExtension = ".jpg";
                    } else if (contentType.equals("image/png")) {
                        fileExtension = ".png";
                    }
                }
                
                if (fileExtension.isEmpty() && originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                } else if (fileExtension.isEmpty()) {
                    fileExtension = ".jpg";
                }
                
                String fileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, file.getBytes());

                String fileUrl = "/uploads/rooms/" + roomId + "/images/" + fileName;
                imageUrls.add(fileUrl);
            }

            // Re-fetch to ensure we are in a clean state within the transaction
            Room managedRoom = repo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            if (managedRoom.getImages() == null) {
                managedRoom.setImages(new ArrayList<>());
            }
            
            // Add new images directly to the managed collection
            managedRoom.getImages().addAll(imageUrls);
            
            // Update metadata
            managedRoom.setImageCount(managedRoom.getImages().size());
            managedRoom.setUpdatedAt(LocalDateTime.now());
            
            // Save and flush to commit immediately
            repo.saveAndFlush(managedRoom);

            System.out.println("Room " + roomId + " images updated in DB. Total count: " + managedRoom.getImages().size());

            return ResponseEntity.ok(new ApiResponse<>(true, managedRoom.getImages(), "Images uploaded successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload images: " + e.getMessage()));
        }
    }

    // Upload video for a room
    @PostMapping("/{roomId}/video")
    @Transactional
    public ResponseEntity<ApiResponse<String>> uploadRoomVideo(
            @PathVariable Long roomId,
            @RequestParam("video") MultipartFile file) {

        try {
            Room room = repo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiResponse<>(false, null, "Video file too large. Max size is 50MB"));
            }

            Path uploadPath = Paths.get(uploadDir, "rooms", String.valueOf(roomId), "videos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String videoUrl = "/uploads/rooms/" + roomId + "/videos/" + fileName;

            room.setVideoUrl(videoUrl);
            room.setHasVideo(true);
            room.setUpdatedAt(LocalDateTime.now());
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, videoUrl, "Video uploaded successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload video: " + e.getMessage()));
        }
    }

    // Upload contract for a room
    @PostMapping("/{roomId}/contract")
    @Transactional
    public ResponseEntity<ApiResponse<String>> uploadRoomContract(
            @PathVariable Long roomId,
            @RequestParam("contract") MultipartFile file) {

        try {
            Room room = repo.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiResponse<>(false, null, "Contract file too large. Max size is 10MB"));
            }

            Path uploadPath = Paths.get(uploadDir, "rooms", String.valueOf(roomId), "contracts");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String contractUrl = "/uploads/rooms/" + roomId + "/contracts/" + fileName;

            room.setContractUrl(contractUrl);
            room.setHasContract(true);
            room.setUpdatedAt(LocalDateTime.now());
            repo.save(room);

            return ResponseEntity.ok(new ApiResponse<>(true, contractUrl, "Contract uploaded successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload contract: " + e.getMessage()));
        }
    }
}