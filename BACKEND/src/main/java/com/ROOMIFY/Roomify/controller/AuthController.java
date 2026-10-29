package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.Component.JwtUtil;
import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.AuthResponse;
import com.ROOMIFY.Roomify.dto.GoogleRegisterRequest;
import com.ROOMIFY.Roomify.dto.RegisterRequest;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${google.client.id:}")
    private String googleClientId;

    @PostMapping("/google")
    @Transactional
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idTokenString = request.get("idToken");
        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Missing Google ID token")
            );
        }
        if (googleClientId == null || googleClientId.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("success", false, "message", "Google sign-in is not configured")
            );
        }

        try {
            GoogleIdToken verifiedToken = verifyGoogleIdToken(idTokenString);
            if (verifiedToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Invalid or expired Google ID token")
                );
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();
            String email = payload.getEmail();
            if (email == null || email.isBlank() || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Google account email is not verified")
                );
            }

            return processGoogleUser(email, (String) payload.get("name"));
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Google ID token verification could not be completed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("success", false, "message", "Google sign-in verification is temporarily unavailable")
            );
        } catch (Exception e) {
            System.err.println("Google login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Google login failed")
            );
        }
    }

    @PostMapping("/google/register")
    @Transactional
    public ResponseEntity<?> googleRegister(@RequestBody GoogleRegisterRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Missing Google ID token")
            );
        }
        if (googleClientId == null || googleClientId.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("success", false, "message", "Google sign-in is not configured")
            );
        }

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Choose a valid account role")
            );
        }
        if (role != UserRole.TENANT && role != UserRole.OWNER && role != UserRole.DALALI) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "This role cannot be registered with Google")
            );
        }
        if (!hasRequiredGoogleRegistrationDetails(request, role)) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Complete the required profile details for this role")
            );
        }

        try {
            GoogleIdToken verifiedToken = verifyGoogleIdToken(request.getIdToken());
            if (verifiedToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Invalid or expired Google ID token")
                );
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();
            String email = payload.getEmail();
            if (email == null || email.isBlank() || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Google account email is not verified")
                );
            }
            if (userService.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("success", false, "message", "Email already registered. Please login instead.")
                );
            }

            User user = new User();
            String googleName = (String) payload.get("name");
            user.setName(googleName == null || googleName.isBlank() ? email.substring(0, email.indexOf('@')) : googleName);
            user.setEmail(email);
            user.setRole(role);
            user.setEmailVerified(true);
            user.setPhone(request.getPhone());
            user.setNidaNumber(request.getNidaNumber());
            user.setBusinessName(request.getBusinessName());
            user.setLocalAuthorityName(request.getLocalAuthorityName());
            user.setLocalAuthorityPhone(request.getLocalAuthorityPhone());
            user.setLocalAuthorityArea(request.getLocalAuthorityArea());
            user.setLocalAuthorityVillage(request.getLocalAuthorityVillage());
            user.setLocalAuthorityWard(request.getLocalAuthorityWard());
            user.setLocalAuthorityDistrict(request.getLocalAuthorityDistrict());
            user.setLocalAuthorityRegion(request.getLocalAuthorityRegion());
            user.setLicenseNumber(request.getLicenseNumber());
            user.setLocationArea(request.getLocationArea());
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLoginAt(LocalDateTime.now());

            User savedUser = userService.save(user);
            String jwt = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
            return ResponseEntity.ok(new AuthResponse(true, jwt, savedUser, "Google registration successful"));
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Google ID token verification could not be completed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("success", false, "message", "Google sign-in verification is temporarily unavailable")
            );
        } catch (Exception e) {
            System.err.println("Google registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Google registration failed")
            );
        }
    }

    private boolean hasRequiredGoogleRegistrationDetails(GoogleRegisterRequest request, UserRole role) {
        if (!hasText(request.getPhone())) {
            return false;
        }
        if (role == UserRole.TENANT) {
            return true;
        }
        boolean hasCommonDetails = hasText(request.getBusinessName())
                && hasText(request.getNidaNumber())
                && hasText(request.getLocalAuthorityName())
                && hasText(request.getLocalAuthorityPhone())
                && hasText(request.getLocalAuthorityArea())
                && hasText(request.getLocalAuthorityVillage());
        return hasCommonDetails && (role != UserRole.DALALI || hasText(request.getLocationArea()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private GoogleIdToken verifyGoogleIdToken(String idTokenString)
            throws IOException, GeneralSecurityException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        return verifier.verify(idTokenString);
    }

    private ResponseEntity<?> processGoogleUser(String email, String name) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "No Roomify account is registered for this Google email")
            );
        }
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "message", "This account has been suspended")
            );
        }

        if (name != null && !name.isBlank() && (user.getName() == null || user.getName().isBlank())) {
            user.setName(name);
        }
        user.setEmailVerified(true);

        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("role", user.getRole().name().toLowerCase());
        userData.put("emailVerified", user.isEmailVerified());

        System.out.println("Login successful for: " + email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", jwt,
                "user", userData
        ));
    }


    @PostMapping("/api/auth/save-fcm-token")
    public ResponseEntity<ApiResponse<Void>> saveFcmToken(
            @RequestParam Long userId,
            @RequestParam String fcmToken) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setFcmToken(fcmToken);
                userRepository.save(user);
                System.out.println("✅ FCM token saved for user: " + user.getEmail());
                return ResponseEntity.ok(new ApiResponse<>(true, null, "FCM token saved"));
            }
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "User not found"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error saving token: " + e.getMessage()));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest request) {
        try {
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new AuthResponse(false, null, null, "Email already registered"));
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            UserRole userRole = UserRole.valueOf(request.getRole().toUpperCase());
            user.setRole(userRole);

            user.setPhone(request.getPhone());
            user.setNidaNumber(request.getNidaNumber());

            if ("owner".equalsIgnoreCase(request.getRole())) {
                if (request.getBusinessName() != null && !request.getBusinessName().isEmpty()) {
                    user.setBusinessName(request.getBusinessName());
                }
                user.setLocalAuthorityName(request.getLocalAuthorityName() != null ? request.getLocalAuthorityName() : request.getBaloziName());
                user.setLocalAuthorityPhone(request.getLocalAuthorityPhone() != null ? request.getLocalAuthorityPhone() : request.getBaloziPhone());
                user.setLocalAuthorityArea(request.getLocalAuthorityArea() != null ? request.getLocalAuthorityArea() : request.getBaloziArea());
                user.setLocalAuthorityVillage(request.getLocalAuthorityVillage() != null ? request.getLocalAuthorityVillage() : request.getBaloziVillage());
                user.setLocalAuthorityWard(request.getLocalAuthorityWard() != null ? request.getLocalAuthorityWard() : request.getBaloziWard());
                user.setLocalAuthorityDistrict(request.getLocalAuthorityDistrict() != null ? request.getLocalAuthorityDistrict() : request.getBaloziDistrict());
                user.setLocalAuthorityRegion(request.getLocalAuthorityRegion() != null ? request.getLocalAuthorityRegion() : request.getBaloziRegion());
            }

            if ("dalali".equalsIgnoreCase(request.getRole())) {
                if (request.getBusinessName() != null && !request.getBusinessName().isEmpty()) {
                    user.setBusinessName(request.getBusinessName());
                }
                user.setLicenseNumber(request.getLicenseNumber());
                user.setLocationArea(request.getLocationArea());
            }

            user.setEmailVerified(false);

            // Use LocalDateTime.now() instead of System.currentTimeMillis()
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLoginAt(LocalDateTime.now());

            User savedUser = userService.save(user);

            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

            AuthResponse response = new AuthResponse(
                    true,
                    token,
                    savedUser,
                    "Registration successful"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, null, null, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");
            String role = request.get("role");

            // Find user
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found with this email");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userOptional.get();

            if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
                response.put("success", false);
                response.put("message", "Your account has been suspended by an Administrator.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Verify password
            if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);

            // Generate token with role
            String token = jwtUtil.generateToken(updatedUser.getEmail(), updatedUser.getRole().name());

            // Prepare response
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("email", updatedUser.getEmail());
            userData.put("name", updatedUser.getName());
            userData.put("role", updatedUser.getRole().toString().toLowerCase());
            userData.put("emailVerified", updatedUser.isEmailVerified());
            userData.put("status", updatedUser.getStatus());

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", userData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test-token")
    public ResponseEntity<Map<String, Object>> testToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "No token provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            boolean isValid = jwtUtil.validateToken(token);

            response.put("success", true);
            response.put("email", email);
            response.put("isValid", isValid);
            response.put("tokenPreview", token.substring(0, Math.min(50, token.length())) + "...");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(false, null, null, "No token provided"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AuthResponse(false, null, null, "User not found"));
            }

            // Return user and the same token (or refresh it)
            return ResponseEntity.ok(new AuthResponse(true, token, user, "Session restored"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(false, null, null, "Invalid token"));
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<Map<String, Object>> guestLogin() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Create a temporary guest user or return a guest token
            String guestToken = jwtUtil.generateToken("guest@temp.com", "GUEST");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", -1);
            userData.put("email", "guest@temp.com");
            userData.put("name", "Guest User");
            userData.put("role", "guest");
            userData.put("emailVerified", false);

            response.put("success", true);
            response.put("message", "Guest login successful");
            response.put("token", guestToken);
            response.put("user", userData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Guest login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Email is required"));
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, null, "No account found with this email"));
        }
        String resetCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        System.out.println("=== PASSWORD RESET CODE FOR " + email + ": " + resetCode + " ===");
        return ResponseEntity.ok(new ApiResponse<>(true, resetCode, "Reset code generated and sent to " + email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        if (email == null || email.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Email and new password are required"));
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, null, "User not found"));
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", "Password reset successfully. You can now login."));
    }
}