package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.Component.JwtUtil;
import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.ROOMIFY.Roomify.service.MediaStorageService mediaStorageService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, user, "Profile retrieved"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, null, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody java.util.Map<String, Object> updates) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, null, "User not found"));
            }

            if (updates != null) {
                if (updates.get("name") instanceof String nameVal && !nameVal.isBlank()) {
                    user.setName(nameVal.trim());
                }
                if (updates.containsKey("phone")) {
                    Object phoneObj = updates.get("phone");
                    user.setPhone(phoneObj instanceof String s ? s.trim() : null);
                }
                if (updates.containsKey("bio")) {
                    Object bioObj = updates.get("bio");
                    user.setBio(bioObj instanceof String s && !s.isBlank() ? s.trim() : null);
                }
                if (updates.get("businessName") instanceof String bn) {
                    user.setBusinessName(bn.trim());
                }
                if (updates.get("locationArea") instanceof String loc) {
                    user.setLocationArea(loc.trim());
                }
                if (updates.get("profileImage") instanceof String img && !img.isBlank()) {
                    user.setProfileImage(img.trim());
                }
            }

            User savedUser = userRepository.saveAndFlush(user);
            return ResponseEntity.ok(new ApiResponse<>(true, savedUser, "Profile updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update profile: " + e.getMessage()));
        }
    }

    @PostMapping("/profile/image")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("image") MultipartFile file) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "No image file provided"));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            byte[] bytes = file.getBytes();
            String detectedContentType = detectImageContentType(bytes, file.getContentType(), file.getOriginalFilename());
            String fileExtension = extensionForContentType(detectedContentType);

            String fileName = UUID.randomUUID().toString() + fileExtension;
            String fileUrl = "/uploads/profiles/" + userId + "/" + fileName;
            mediaStorageService.storeMedia(fileUrl, detectedContentType, bytes);
            user.setProfileImage(fileUrl);
            userRepository.saveAndFlush(user);

            return ResponseEntity.ok(new ApiResponse<>(true, fileUrl, "Image uploaded successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Upload failed: " + e.getMessage()));
        }
    }

    private String detectImageContentType(byte[] bytes, String providedType, String originalName) {
        if (bytes != null && bytes.length >= 12) {
            // PNG: 89 50 4E 47
            if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "image/png";
            }
            // JPEG: FF D8 FF
            if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }
            // GIF: 47 49 46 38
            if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
                return "image/gif";
            }
            // WebP: RIFF....WEBP
            if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
                return "image/webp";
            }
        }
        if (providedType != null && providedType.startsWith("image/")) {
            return providedType;
        }
        if (originalName != null) {
            String lower = originalName.toLowerCase();
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".gif")) return "image/gif";
        }
        return "image/jpeg";
    }

    private String extensionForContentType(String contentType) {
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        if ("image/gif".equals(contentType)) return ".gif";
        return ".jpg";
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody java.util.Map<String, String> request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "New password must be at least 6 characters long"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, null, "User not found"));
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Current password does not match"));
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to change password: " + e.getMessage()));
        }
    }

    @PostMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus("SUSPENDED");
                user.setEmail("deleted_" + System.currentTimeMillis() + "_" + user.getEmail());
                userRepository.save(user);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete account: " + e.getMessage()));
        }
    }

    /**
     * Resolves the user ID either from Spring's SecurityContext (populated by JwtAuthenticationFilter)
     * or by validating and decoding the Bearer token with JwtUtil.
     */
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
            return null;
        }
    }
}
