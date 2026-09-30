package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.AuditLog;
import com.ROOMIFY.Roomify.model.Booking;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.model.VerificationStatus;
import com.ROOMIFY.Roomify.repository.AuditLogRepository;
import com.ROOMIFY.Roomify.repository.BookingRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin-management")
@CrossOrigin(origins = "*")
public class AdminManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        List<User> users = userRepository.findAll();
        long totalUsers = users.size();
        long tenants = users.stream().filter(u -> UserRole.TENANT.equals(u.getRole())).count();
        long owners = users.stream().filter(u -> UserRole.OWNER.equals(u.getRole())).count();
        long dalalis = users.stream().filter(u -> UserRole.DALALI.equals(u.getRole())).count();
        long admins = users.stream().filter(u -> UserRole.ADMIN.equals(u.getRole()) || UserRole.SUPER_ADMIN.equals(u.getRole())).count();
        long suspendedUsers = users.stream().filter(u -> "SUSPENDED".equalsIgnoreCase(u.getStatus())).count();

        long totalProperties = roomRepository.count();
        long pendingProperties = roomRepository.findByVerificationStatus(VerificationStatus.PENDING).size();
        long verifiedProperties = roomRepository.findByVerificationStatus(VerificationStatus.VERIFIED).size();
        long rejectedProperties = roomRepository.findByVerificationStatus(VerificationStatus.REJECTED).size();
        long suspendedProperties = roomRepository.findAll().stream().filter(r -> "SUSPENDED".equalsIgnoreCase(r.getStatus())).count();
        long availableProperties = roomRepository.findAll().stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
        long rentedProperties = roomRepository.findAll().stream().filter(r -> "RENTED".equalsIgnoreCase(r.getStatus())).count();

        long totalBookings = bookingRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("tenants", tenants);
        stats.put("owners", owners);
        stats.put("dalalis", dalalis);
        stats.put("admins", admins);
        stats.put("suspendedUsers", suspendedUsers);
        stats.put("activeUsers", totalUsers - suspendedUsers);

        stats.put("totalProperties", totalProperties);
        stats.put("pendingProperties", pendingProperties);
        stats.put("verifiedProperties", verifiedProperties);
        stats.put("rejectedProperties", rejectedProperties);
        stats.put("suspendedProperties", suspendedProperties);
        stats.put("availableProperties", availableProperties);
        stats.put("rentedProperties", rentedProperties);

        stats.put("totalBookings", totalBookings);

        return ResponseEntity.ok(new ApiResponse<>(true, stats, "Stats retrieved"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse<>(true, userRepository.findAll(), "Users retrieved"));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByVerificationStatus(VerificationStatus.PENDING);
        return ResponseEntity.ok(new ApiResponse<>(true, pendingUsers, "Pending users retrieved"));
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<ApiResponse<User>> verifyUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setVerifiedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        auditService.log("VERIFY_USER", "User", id.toString(), "User verified: " + user.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "User verified successfully"));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<ApiResponse<User>> rejectUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        String reason = request != null && request.containsKey("reason") ? request.get("reason") : "No reason provided";
        user.setVerificationStatus(VerificationStatus.REJECTED);
        user.setRejectionReason(reason);
        User saved = userRepository.save(user);
        auditService.log("REJECT_USER", "User", id.toString(), "User rejected: " + user.getEmail() + ". Reason: " + reason);

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "User rejected"));
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<User>> suspendUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setStatus("SUSPENDED");
        User saved = userRepository.save(user);
        auditService.log("SUSPEND_USER", "User", id.toString(), "User suspended: " + user.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "User suspended successfully"));
    }

    @PutMapping("/users/{id}/unsuspend")
    public ResponseEntity<ApiResponse<User>> unsuspendUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setStatus("ACTIVE");
        User saved = userRepository.save(user);
        auditService.log("UNSUSPEND_USER", "User", id.toString(), "User unsuspended: " + user.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "User unsuspended successfully"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(new ApiResponse<>(true, auditLogRepository.findAllByOrderByTimestampDesc(), "Audit logs retrieved"));
    }

    @GetMapping("/system-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getSystemLogs() {
        return ResponseEntity.ok(new ApiResponse<>(true, auditLogRepository.findAllByOrderByTimestampDesc(), "System logs retrieved"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(new ApiResponse<>(true, bookingRepository.findAll(), "Bookings retrieved"));
    }

    @GetMapping("/administrators")
    public ResponseEntity<ApiResponse<List<User>>> getAdministrators() {
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> UserRole.ADMIN.equals(u.getRole()) || UserRole.SUPER_ADMIN.equals(u.getRole()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, admins, "Administrators retrieved"));
    }

    @PostMapping("/administrators")
    public ResponseEntity<ApiResponse<User>> createAdmin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Email already exists"));
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setName(request.get("name"));
        admin.setPassword(passwordEncoder.encode(request.get("password")));
        admin.setRole(UserRole.ADMIN);
        admin.setEmailVerified(true);
        admin.setStatus("ACTIVE");
        admin.setVerificationStatus(VerificationStatus.VERIFIED);
        admin.setCreatedAt(LocalDateTime.now());
        
        User saved = userRepository.save(admin);
        auditService.log("CREATE_ADMIN", "User", saved.getId().toString(), "Admin created with email: " + email);
        
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Admin created successfully"));
    }

    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<User>> createAdminLegacy(@RequestBody Map<String, String> request) {
        return createAdmin(request);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        UserRole oldRole = user.getRole();
        UserRole newRole = UserRole.valueOf(request.get("role").toUpperCase());

        user.setRole(newRole);
        User saved = userRepository.save(user);
        auditService.log("UPDATE_ROLE", "User", id.toString(), "Role changed from " + oldRole + " to " + newRole);

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Role updated successfully"));
    }
}
