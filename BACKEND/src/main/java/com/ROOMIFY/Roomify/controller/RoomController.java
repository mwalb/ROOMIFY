package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.service.FCMService;
import com.ROOMIFY.Roomify.service.AuditService;
import com.ROOMIFY.Roomify.service.RoomNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import com.ROOMIFY.Roomify.model.Property;
import com.ROOMIFY.Roomify.model.VerificationStatus;
import com.ROOMIFY.Roomify.repository.PropertyRepository;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository repo;

    @Autowired
    private NotificationController notificationController;

    @Autowired
    private RoomNotifier notifier;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private com.ROOMIFY.Roomify.service.NotificationService notificationService;

    @Autowired
    private com.ROOMIFY.Roomify.service.MediaStorageService mediaStorageService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    private Path getUploadBasePath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(path) && Files.exists(Paths.get("BACKEND", uploadDir))) {
            return Paths.get("BACKEND", uploadDir).toAbsolutePath().normalize();
        }
        return path;
    }

    private User getAuthenticatedUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                if (email != null && !email.isBlank()) {
                    return userRepository.findByEmailIgnoreCase(email)
                            .orElseGet(() -> userRepository.findByEmail(email).orElse(null));
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting authenticated user: " + e.getMessage());
        }
        return null;
    }

    private boolean isAdminRole(User user) {
        return user != null && (UserRole.ADMIN.equals(user.getRole()) || UserRole.SUPER_ADMIN.equals(user.getRole()));
    }

    private boolean canManageRoomMedia(Room room, User currentUser) {
        if (currentUser == null) {
            return false;
        }
        if (isAdminRole(currentUser)) {
            return true;
        }
        if (room.getPostedBy() != null && room.getPostedBy().equals(currentUser.getId())) {
            return true;
        }
        if (room.getDalaliId() != null && room.getDalaliId().equals(currentUser.getId())) {
            return true;
        }
        if (room.getPropertyId() != null) {
            Property prop = propertyRepository.findById(room.getPropertyId()).orElse(null);
            if (prop != null && prop.getOwnerId() != null && prop.getOwnerId().equals(currentUser.getId())) {
                return true;
            }
        }
        // If room has no owner assigned yet (e.g. freshly created), claim it for the current user
        if (room.getPostedBy() == null || room.getPostedBy() == 0L) {
            room.setPostedBy(currentUser.getId());
            repo.save(room);
            return true;
        }
        return false;
    }

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

            // Obey 3NF: Synchronize poster contact details from User entity
            if (room.getPostedBy() != null) {
                userRepository.findById(room.getPostedBy()).ifPresent(user -> {
                    if (room.getOwnerName() == null || room.getOwnerName().isBlank()) {
                        room.setOwnerName(user.getName());
                    }
                    if (room.getContactPhone() == null || room.getContactPhone().isBlank()) {
                        room.setContactPhone(user.getPhone());
                    }
                    if (room.getContactEmail() == null || room.getContactEmail().isBlank()) {
                        room.setContactEmail(user.getEmail());
                    }
                });
            }

            // Validate required fields
            if (room.getPostedBy() == null || room.getPostedBy() == 0L) {
                User currentUser = getAuthenticatedUser();
                if (currentUser != null) {
                    room.setPostedBy(currentUser.getId());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("postedBy is required");
                }
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
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) return ResponseEntity.notFound().build();

            // Strict Ownership Verification
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authentication required to edit properties."));
            }

            boolean isOwner = room.getPostedBy() != null && room.getPostedBy().equals(currentUser.getId());
            boolean isDalali = room.getDalaliId() != null && room.getDalaliId().equals(currentUser.getId());
            boolean isAdmin = isAdminRole(currentUser);

            if (!isOwner && !isDalali && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Access denied: You do not have permission to edit this property."));
            }

            room.setTitle(roomDetails.getTitle());
            room.setDescription(roomDetails.getDescription());
            room.setPrice(roomDetails.getPrice());
            room.setLatitude(roomDetails.getLatitude());
            room.setLongitude(roomDetails.getLongitude());
            room.setAddress(roomDetails.getAddress());
            room.setPropertyType(roomDetails.getPropertyType());

            // Obey 3NF: If poster is linked to User, update from User or sync back
            if (room.getPostedBy() != null) {
                userRepository.findById(room.getPostedBy()).ifPresent(user -> {
                    if (roomDetails.getContactPhone() == null || roomDetails.getContactPhone().isBlank()) {
                        room.setContactPhone(user.getPhone());
                    } else {
                        room.setContactPhone(roomDetails.getContactPhone());
                    }
                    if (roomDetails.getContactEmail() == null || roomDetails.getContactEmail().isBlank()) {
                        room.setContactEmail(user.getEmail());
                    } else {
                        room.setContactEmail(roomDetails.getContactEmail());
                    }
                    if (roomDetails.getOwnerName() == null || roomDetails.getOwnerName().isBlank()) {
                        room.setOwnerName(user.getName());
                    } else {
                        room.setOwnerName(roomDetails.getOwnerName());
                    }
                });
            } else {
                room.setContactPhone(roomDetails.getContactPhone());
                room.setContactEmail(roomDetails.getContactEmail());
                room.setOwnerName(roomDetails.getOwnerName());
            }

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

            if ("AVAILABLE".equalsIgnoreCase(updated.getStatus()) && updated.isAvailable()) {
                try {
                    notificationService.notifyRoomAvailable(updated);
                } catch (Exception notifEx) {
                    System.err.println("Warning: Failed to notify room subscribers: " + notifEx.getMessage());
                }
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error updating room: " + e.getMessage()));
        }
    }

    // Delete room
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) return ResponseEntity.notFound().build();

            // Strict Ownership Verification
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authentication required to delete properties."));
            }

            boolean isOwner = room.getPostedBy() != null && room.getPostedBy().equals(currentUser.getId());
            boolean isDalali = room.getDalaliId() != null && room.getDalaliId().equals(currentUser.getId());
            boolean isAdmin = isAdminRole(currentUser);

            if (!isOwner && !isDalali && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Access denied: You do not have permission to delete this property."));
            }

            repo.deleteById(id);
            notifier.notifyRoomUpdate(room);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting room");
        }
    }

    // ==================== FIXED METHODS ====================

    // Get room by ID - STRICT ROLE-BASED ACCESS CONTROL
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Room not found"));
            }

            User user = getAuthenticatedUser();
            if (user != null && !isAdminRole(user)) {
                if (UserRole.OWNER.equals(user.getRole())) {
                    if (room.getPostedBy() != null && !room.getPostedBy().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse<>(false, null, "Access denied: Owners can only view details of their own properties."));
                    }
                } else if (UserRole.DALALI.equals(user.getRole())) {
                    boolean isMyDalali = room.getDalaliId() != null && room.getDalaliId().equals(user.getId());
                    boolean isMyPosted = room.getPostedBy() != null && room.getPostedBy().equals(user.getId());
                    if (!isMyDalali && !isMyPosted) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse<>(false, null, "Access denied: Dalalis can only view details of properties managed by them."));
                    }
                }
            }

            // Increment view count
            try {
                repo.incrementViewCount(id);
            } catch (Exception e) {
                System.err.println("Failed to increment view count: " + e.getMessage());
            }

            return ResponseEntity.ok(room);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all rooms - ROLE-BASED LISTING FOR EXPLORE/MAP
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllRooms() {
        try {
            User user = getAuthenticatedUser();
            List<Room> rooms;
            if (user != null) {
                if (isAdminRole(user)) {
                    rooms = repo.findAll();
                } else if (UserRole.OWNER.equals(user.getRole())) {
                    rooms = repo.findByPostedBy(user.getId());
                } else if (UserRole.DALALI.equals(user.getRole())) {
                    rooms = repo.findByDalaliIdOrPostedBy(user.getId(), user.getId());
                } else {
                    // TENANT
                    rooms = repo.findByVerificationStatus(VerificationStatus.VERIFIED);
                }
            } else {
                // Anonymous Guest
                rooms = repo.findByVerificationStatus(VerificationStatus.VERIFIED);
            }
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get properties for currently authenticated user
    @GetMapping("/my-properties")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyProperties() {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authentication required"));
            }
            List<Room> rooms;
            if (UserRole.OWNER.equals(user.getRole())) {
                rooms = repo.findByPostedBy(user.getId());
            } else if (UserRole.DALALI.equals(user.getRole())) {
                rooms = repo.findByDalaliIdOrPostedBy(user.getId(), user.getId());
            } else if (isAdminRole(user)) {
                rooms = repo.findAll();
            } else {
                rooms = repo.findByVerificationStatus(VerificationStatus.VERIFIED);
            }
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all rooms posted by a specific user (owner) - STRICT AUTHORIZATION
    @GetMapping("/owner/{postedBy}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomsByOwner(@PathVariable Long postedBy) {
        try {
            User user = getAuthenticatedUser();
            Long effectiveOwnerId = postedBy;
            if (user != null && UserRole.OWNER.equals(user.getRole()) && !user.getId().equals(postedBy) && !isAdminRole(user)) {
                effectiveOwnerId = user.getId();
            }
            List<Room> rooms = repo.findByPostedBy(effectiveOwnerId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Get all available rooms - Returns only verified & available rooms
    @GetMapping("/available")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAvailableRooms() {
        try {
            List<Room> rooms = repo.findByIsAvailableTrueAndVerificationStatus(VerificationStatus.VERIFIED);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Location suggestions endpoint starting from first character
    // Location suggestions endpoint starting from first character
    @GetMapping("/locations/suggestions")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getLocationSuggestions(@RequestParam(required = false, defaultValue = "") String query) {
        try {
            Set<String> uniqueLocations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

            // 1. Live locations from Database properties
            List<Room> rooms = repo.findAll();
            for (Room r : rooms) {
                String addr = r.getAddress();
                if (addr != null && !addr.isBlank()) {
                    String[] parts = addr.split(",");
                    for (String part : parts) {
                        String clean = part.trim();
                        if (!clean.isEmpty()) uniqueLocations.add(clean);
                    }
                    uniqueLocations.add(addr.trim());
                }
                if (r.getNeighbourhoodPlaces() != null) {
                    for (String np : r.getNeighbourhoodPlaces()) {
                        if (np != null && !np.isBlank()) uniqueLocations.add(np.trim());
                    }
                }
                if (r.getNearestCampus() != null && !r.getNearestCampus().isBlank()) {
                    uniqueLocations.add(r.getNearestCampus().trim());
                }
            }

            // 2. Canonical Tanzanian urban areas & campuses to guarantee instant reload on 1st letter
            List<String> tzHubs = Arrays.asList(
                "Sinza", "Sinza Mori", "Sinza Palestina", "Sinza Mugabe", "Sinza Kijiweni", "Sinza Kwa Remmy",
                "Mikocheni", "Mikocheni A", "Mikocheni B", "Regent Estate, Mikocheni", "Shoppers Plaza, Mikocheni", "Palm Village, Mikocheni",
                "Masaki", "Msasani", "Oysterbay", "Ada Estate", "Namanga", "Coco Beach",
                "Kijitonyama", "Makumbusho", "Victoria", "Morocco", "Mwenge", "Mlimani City", "Survey",
                "University of Dar es Salaam (UDSM)", "Ardhi University (ARU)", "Makongo Juu", "Changanyikeni",
                "Kawe", "Mbezi Beach", "Mbezi Beach Africana", "Mbezi Tanki Bovu", "Goba", "Salasala", "Tegeta", "Kunduchi", "Bahari Beach", "Boko", "Bunju", "Madale",
                "Ubungo", "Ubungo Riverside", "Kimara", "Kimara Korogwe", "Kimara Mwisho", "Kimara Baruti", "Mbezi Luis", "Kibamba", "Mabibo", "Manzese", "Magomeni",
                "Kariakoo", "Ilala", "Upanga", "Posta", "Kivukoni", "City Centre", "Tabata", "Segerea", "Kinyerezi",
                "Kigamboni", "Ferry", "Tuangoma", "Mbagala", "Temeke", "Chang'ombe", "Kurasini", "Tandika",
                "Institute of Finance Management (IFM)", "College of Business Education (CBE)", "Dar es Salaam Institute of Technology (DIT)",
                "Dodoma", "Dodoma City", "Area C, Dodoma", "Area D, Dodoma", "Kizota", "Nzuguni", "University of Dodoma (UDOM)",
                "Arusha", "Arusha City", "Njiro", "Sakina", "Kijenge", "Sanawari",
                "Mwanza", "Mwanza City", "Nyamagana", "Ilemela", "Capripoint", "Kirumba",
                "Morogoro", "Morogoro Town", "Sua", "Kilakala",
                "Zanzibar", "Stone Town", "Mbweni", "Chukwani", "Bububu"
            );
            uniqueLocations.addAll(tzHubs);

            String q = query == null ? "" : query.trim().toLowerCase();
            if (q.isEmpty()) {
                // Return top popular hubs
                List<String> defaultHubs = Arrays.asList("Sinza", "Mikocheni", "Masaki", "Mwenge", "Mbezi Beach", "Kijitonyama", "University of Dar es Salaam (UDSM)", "Kariakoo", "Kigamboni");
                return ResponseEntity.ok(defaultHubs);
            }

            List<String> exactMatches = new ArrayList<>();
            List<String> prefixMatches = new ArrayList<>();
            List<String> wordPrefixMatches = new ArrayList<>();
            List<String> substringMatches = new ArrayList<>();

            for (String loc : uniqueLocations) {
                String lower = loc.toLowerCase();
                if (lower.equals(q)) {
                    exactMatches.add(loc);
                } else if (lower.startsWith(q)) {
                    prefixMatches.add(loc);
                } else {
                    boolean wordPref = false;
                    for (String w : lower.split("[\\s,()\\-]+")) {
                        if (w.startsWith(q)) {
                            wordPref = true;
                            break;
                        }
                    }
                    if (wordPref) {
                        wordPrefixMatches.add(loc);
                    } else if (lower.contains(q)) {
                        substringMatches.add(loc);
                    }
                }
            }

            exactMatches.sort(String.CASE_INSENSITIVE_ORDER);
            prefixMatches.sort(String.CASE_INSENSITIVE_ORDER);
            wordPrefixMatches.sort(String.CASE_INSENSITIVE_ORDER);
            substringMatches.sort(String.CASE_INSENSITIVE_ORDER);

            List<String> results = new ArrayList<>();
            results.addAll(exactMatches);
            results.addAll(prefixMatches);
            results.addAll(wordPrefixMatches);
            results.addAll(substringMatches);

            // Limit to top 25 matches for high performance
            if (results.size() > 25) {
                results = results.subList(0, 25);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Admin: Get pending properties
    @GetMapping("/admin/pending")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getPendingPropertiesForAdmin() {
        try {
            User user = getAuthenticatedUser();
            if (user == null || !isAdminRole(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Admin access required"));
            }
            List<Room> pending = repo.findByVerificationStatus(VerificationStatus.PENDING);
            return ResponseEntity.ok(new ApiResponse<>(true, pending, "Pending properties retrieved"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Admin: Verify property
    @PutMapping("/admin/{id}/verify")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> verifyPropertyForAdmin(@PathVariable Long id) {
        try {
            User user = getAuthenticatedUser();
            if (user == null || !isAdminRole(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Admin access required"));
            }
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }
            room.approve();
            room.setVerifiedBy(user.getId());
            repo.save(room);
            try {
                notificationService.notifyPropertyStatusUpdated(room, "VERIFIED", null);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send property verification notification: " + notifEx.getMessage());
            }
            auditService.log("VERIFY_PROPERTY", "Room", id.toString(), "Property verified by admin: " + user.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Admin: Reject property
    @PutMapping("/admin/{id}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> rejectPropertyForAdmin(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            User user = getAuthenticatedUser();
            if (user == null || !isAdminRole(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Admin access required"));
            }
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }
            String reason = (request != null && request.containsKey("reason")) ? request.get("reason") : "No reason provided";
            room.reject(reason);
            room.setVerifiedBy(user.getId());
            repo.save(room);
            try {
                notificationService.notifyPropertyStatusUpdated(room, "REJECTED", reason);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send property rejection notification: " + notifEx.getMessage());
            }
            auditService.log("REJECT_PROPERTY", "Room", id.toString(), "Property rejected by admin: " + user.getEmail() + ". Reason: " + reason);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property rejected"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Admin: Suspend property
    @PutMapping("/admin/{id}/suspend")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> suspendPropertyForAdmin(@PathVariable Long id) {
        try {
            User user = getAuthenticatedUser();
            if (user == null || !isAdminRole(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Admin access required"));
            }
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }
            room.setStatus("SUSPENDED");
            room.setAvailable(false);
            repo.save(room);
            auditService.log("SUSPEND_PROPERTY", "Room", id.toString(), "Property suspended by admin: " + user.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property suspended successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    // Admin: Unsuspend property
    @PutMapping("/admin/{id}/unsuspend")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> unsuspendPropertyForAdmin(@PathVariable Long id) {
        try {
            User user = getAuthenticatedUser();
            if (user == null || !isAdminRole(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Admin access required"));
            }
            Room room = repo.findById(id).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }
            room.setStatus("AVAILABLE");
            room.setAvailable(true);
            repo.save(room);
            try {
                notificationService.notifyRoomAvailable(room);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to notify room subscribers: " + notifEx.getMessage());
            }
            auditService.log("UNSUSPEND_PROPERTY", "Room", id.toString(), "Property unsuspended by admin: " + user.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Property unsuspended successfully"));
        } catch (Exception e) {
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
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/property/{propertyId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByProperty(@PathVariable Long propertyId) {
        try {
            List<Room> rooms = repo.findByPropertyId(propertyId);
            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Rooms for property retrieved"));
        } catch (Exception e) {
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

    // ==================== METADATA ENDPOINTS ====================

    @GetMapping("/types")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getPropertyTypes() {
        try {
            List<String> types = repo.findDistinctPropertyTypes();
            // If empty, return some defaults
            if (types.isEmpty()) {
                types = Arrays.asList("Room", "Apartment", "Studio", "House", "Office");
            }
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Arrays.asList("Room", "Apartment", "Studio", "House", "Office"));
        }
    }

    @GetMapping("/areas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getAreaSuggestions() {
        try {
            List<String> addresses = repo.findDistinctAddresses();
            java.util.Set<String> areas = new java.util.HashSet<>();
            
            for (String addr : addresses) {
                if (addr == null || addr.isEmpty()) continue;
                String[] parts = addr.split(",");
                if (parts.length > 0) {
                    areas.add(parts[0].trim());
                }
            }
            
            return ResponseEntity.ok(new ArrayList<>(areas));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/amenities")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getStandardAmenities() {
        return ResponseEntity.ok(Arrays.asList(
                "Maji ya DAWASA (24/7 Flow)",
                "LUKU ya Kujitegemea (Private Meter)",
                "Reserve Water Tank (Simtank)",
                "WiFi",
                "Parking ya Gari",
                "Air Conditioning (AC)",
                "Furnished",
                "Security (Mlinzi / Fensi)",
                "Balcony",
                "Water Included",
                "Electricity Included",
                "Pool",
                "Gym"
        ));
    }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== DALALI (AGENT) ENDPOINTS ====================

    // Get properties managed by a specific dalali agent - STRICT AUTHORIZATION
    @GetMapping("/dalali/{dalaliId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByDalali(@PathVariable Long dalaliId) {
        try {
            User user = getAuthenticatedUser();
            Long effectiveDalaliId = dalaliId;
            if (user != null && UserRole.DALALI.equals(user.getRole()) && !user.getId().equals(dalaliId) && !isAdminRole(user)) {
                effectiveDalaliId = user.getId();
            }
            List<Room> rooms = repo.findByDalaliId(effectiveDalaliId);
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
            
            auditService.log("APPROVE_PROPERTY", "Room", roomId.toString(), "Property approved: " + room.getTitle());

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
            
            auditService.log("REJECT_PROPERTY", "Room", roomId.toString(), "Property rejected: " + room.getTitle() + ". Reason: " + reason);

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

            // Verify Ownership for media upload
            User currentUser = getAuthenticatedUser();
            if (currentUser != null && !canManageRoomMedia(room, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Access denied: You do not have permission to upload media to this property."));
            }

            List<String> imageUrls = new ArrayList<>();
            Path uploadPath = getUploadBasePath().resolve("rooms").resolve(String.valueOf(roomId)).resolve("images");
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
                String fileUrl = "/uploads/rooms/" + roomId + "/images/" + fileName;
                mediaStorageService.storeMedia(fileUrl, contentType, file.getBytes());
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

            // Verify Ownership for video upload
            User currentUser = getAuthenticatedUser();
            if (currentUser != null && !canManageRoomMedia(room, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Access denied: You do not have permission to upload video to this property."));
            }

            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiResponse<>(false, null, "Video file too large. Max size is 50MB"));
            }

            Path uploadPath = getUploadBasePath().resolve("rooms").resolve(String.valueOf(roomId)).resolve("videos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? ".mp4" : fileExtension);
            String videoUrl = "/uploads/rooms/" + roomId + "/videos/" + fileName;
            mediaStorageService.storeMedia(videoUrl, file.getContentType(), file.getBytes());

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

            // Verify Ownership for contract upload
            User currentUser = getAuthenticatedUser();
            if (currentUser != null && !canManageRoomMedia(room, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, null, "Access denied: You do not have permission to upload contract to this property."));
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiResponse<>(false, null, "Contract file too large. Max size is 10MB"));
            }

            Path uploadPath = getUploadBasePath().resolve("rooms").resolve(String.valueOf(roomId)).resolve("contracts");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? ".pdf" : fileExtension);
            String contractUrl = "/uploads/rooms/" + roomId + "/contracts/" + fileName;
            mediaStorageService.storeMedia(contractUrl, file.getContentType(), file.getBytes());

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