package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.AuditLog;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.repository.AuditLogRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-management")
@CrossOrigin(origins = "*")
public class AdminManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse<>(true, userRepository.findAll(), "Users retrieved"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(new ApiResponse<>(true, auditLogRepository.findAllByOrderByTimestampDesc(), "Audit logs retrieved"));
    }

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PostMapping("/admins")
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
        admin.setCreatedAt(LocalDateTime.now());
        
        User saved = userRepository.save(admin);
        auditService.log("CREATE_ADMIN", "User", saved.getId().toString(), "Admin created with email: " + email);
        
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Admin created successfully"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        UserRole oldRole = user.getRole();
        UserRole newRole = UserRole.valueOf(request.get("role").toUpperCase());
        
        if (oldRole == UserRole.SUPER_ADMIN && newRole != UserRole.SUPER_ADMIN) {
             // Logic to prevent removing last super admin or unauthorized changes to super admins
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);
        auditService.log("UPDATE_ROLE", "User", id.toString(), "Role changed from " + oldRole + " to " + newRole);

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Role updated successfully"));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // boolean enabled = request.get("enabled");
        // user.setEnabled(enabled);
        User saved = userRepository.save(user);
        
        // String action = enabled ? "ACTIVATE" : "DEACTIVATE";
        // auditService.log(action, "User", id.toString(), "Account " + (enabled ? "enabled" : "disabled"));

        return ResponseEntity.ok(new ApiResponse<>(true, saved, "User status updated (field currently disabled in DB)"));
    }
}
