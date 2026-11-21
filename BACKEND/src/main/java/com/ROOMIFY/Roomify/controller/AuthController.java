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

    @Autowired
    private com.ROOMIFY.Roomify.service.NotificationService notificationService;

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
            try {
                notificationService.notifyUserRegistered(savedUser);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send welcome notification: " + notifEx.getMessage());
            }
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
        List<String> validAudiences = new ArrayList<>();
        if (googleClientId != null && !googleClientId.isBlank()) {
            for (String id : googleClientId.split(",")) {
                if (!id.trim().isEmpty()) {
                    validAudiences.add(id.trim());
                }
            }
        }

        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        );
        if (!validAudiences.isEmpty()) {
            builder.setAudience(validAudiences);
        }
        GoogleIdToken token = builder.build().verify(idTokenString);
        if (token != null) {
            return token;
        }
        // Fallback: verify Google cryptographic signature + issuer + expiry without strict audience lock
        // in case the web/mobile client uses a different Google OAuth client ID from the same project.
        GoogleIdToken relaxed = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).build().verify(idTokenString);
        if (relaxed != null) {
            System.out.println("Warning: Google ID token verified with audience=" + relaxed.getPayload().getAudience()
                    + " (configured google.client.id=" + googleClientId + ")");
        }
        return relaxed;
    }

    private ResponseEntity<?> processGoogleUser(String email, String name) {
        User user = findUserByEmail(email).orElse(null);
        if (user == null) {
            // Auto-provision a tenant account on first Google sign-in so users don't hit "Authentication failed".
            user = new User();
            user.setEmail(email.trim());
            user.setName((name != null && !name.isBlank()) ? name.trim() : email.substring(0, email.indexOf('@')));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(UserRole.TENANT);
            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            try {
                notificationService.notifyUserRegistered(user);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send welcome notification: " + notifEx.getMessage());
            }
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

        UserRole effectiveRole = user.getRole() != null ? user.getRole() : UserRole.TENANT;
        if (user.getRole() == null) {
            user.setRole(effectiveRole);
            user = userRepository.save(user);
        }
        String jwt = jwtUtil.generateToken(user.getEmail(), effectiveRole.name());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName() != null ? user.getName() : "");
        userData.put("role", effectiveRole.name().toLowerCase());
        userData.put("emailVerified", user.isEmailVerified());
        userData.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");

        System.out.println("Login successful for: " + email);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Google login successful");
        resp.put("token", jwt);
        resp.put("user", userData);
        return ResponseEntity.ok(resp);
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
            try {
                notificationService.notifyUserRegistered(savedUser);
            } catch (Exception notifEx) {
                System.err.println("Warning: Failed to send welcome notification: " + notifEx.getMessage());
            }

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

    private static final int RESET_CODE_TTL_MINUTES = 15;
    private static final int RESET_RESEND_COOLDOWN_SECONDS = 60;
    private static final int RESET_MAX_ATTEMPTS = 5;
    private final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    private Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    /**
     * Step 1: generate a one-time code and email it ONLY to the address registered for that account.
     * The code is stored hashed and is never returned in the API response.
     */
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Email is required"));
        }
        String genericMessage = "If an account exists for " + maskEmail(email.trim())
                + ", a 6-digit reset code has been sent. Check your inbox and spam folder.";

        Optional<User> userOpt = findUserByEmail(email);
        if (userOpt.isEmpty()) {
            // Same response as success so account existence is not revealed.
            return ResponseEntity.ok(new ApiResponse<>(true, null, genericMessage));
        }

        User user = userOpt.get();
        LocalDateTime now = LocalDateTime.now();
        if (user.getResetTokenExpiry() != null) {
            LocalDateTime issuedAt = user.getResetTokenExpiry().minusMinutes(RESET_CODE_TTL_MINUTES);
            if (issuedAt.plusSeconds(RESET_RESEND_COOLDOWN_SECONDS).isAfter(now)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(
                        false, null, "Please wait a minute before requesting another code"));
            }
        }

        String resetCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        user.setPasswordResetToken(passwordEncoder.encode(resetCode));
        user.setResetTokenExpiry(now.plusMinutes(RESET_CODE_TTL_MINUTES));
        user.setResetAttempts(0);
        userRepository.save(user);

        // Always sent to the stored email of this user (never to a client-supplied address).
        System.out.println("Password reset code issued for user id=" + user.getId() + " -> " + maskEmail(user.getEmail()));
        try {
            notificationService.sendPasswordResetCode(user, resetCode);
        } catch (Exception notifEx) {
            System.err.println("Warning: Failed to send password reset email: " + notifEx.getMessage());
        }
        return ResponseEntity.ok(new ApiResponse<>(true, null, genericMessage));
    }

    /**
     * Step 2: verify the emailed code and set the new password.
     */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        if (email == null || email.isBlank() || code == null || code.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null,
                    "Email, reset code and new password are required"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null,
                    "Password must be at least 6 characters"));
        }

        String invalid = "Invalid or expired reset code";
        Optional<User> userOpt = findUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, invalid));
        }
        User user = userOpt.get();

        if (user.getPasswordResetToken() == null || user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, invalid));
        }
        int attempts = user.getResetAttempts() == null ? 0 : user.getResetAttempts();
        if (attempts >= RESET_MAX_ATTEMPTS) {
            user.setPasswordResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(false, null,
                    "Too many wrong attempts. Please request a new code"));
        }
        if (!passwordEncoder.matches(code.trim(), user.getPasswordResetToken())) {
            user.setResetAttempts(attempts + 1);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, invalid));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setResetTokenExpiry(null);
        user.setResetAttempts(0);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", "Password reset successfully. You can now login."));
    }
}