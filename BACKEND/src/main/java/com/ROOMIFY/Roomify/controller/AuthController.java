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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static class VerifiedGoogleProfile {
        final String email;
        final String name;
        final boolean emailVerified;

        VerifiedGoogleProfile(String email, String name, boolean emailVerified) {
            this.email = email;
            this.name = name;
            this.emailVerified = emailVerified;
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String idTokenString = request != null ? request.get("idToken") : null;
        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Missing Google ID token")
            );
        }

        try {
            VerifiedGoogleProfile profile = verifyGoogleProfile(idTokenString.trim());
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Invalid or expired Google ID token")
                );
            }

            if (profile.email == null || profile.email.isBlank() || !profile.emailVerified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Google account email is not verified")
                );
            }

            return processGoogleUser(profile.email, profile.name, httpRequest);
        } catch (Throwable e) {
            System.err.println("Google login failed: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Google login error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()))
            );
        }
    }

    @PostMapping("/google/register")
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
            VerifiedGoogleProfile profile = verifyGoogleProfile(request.getIdToken().trim());
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Invalid or expired Google ID token")
                );
            }

            String email = profile.email;
            if (email == null || email.isBlank() || !profile.emailVerified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Google account email is not verified")
                );
            }
            if (findUserByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("success", false, "message", "Email already registered. Please login instead.")
                );
            }

            User user = new User();
            String googleName = profile.name;
            user.setName(googleName == null || googleName.isBlank() ? email.substring(0, email.indexOf('@')) : googleName);
            user.setEmail(email.trim());
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(role);
            user.setStatus("ACTIVE");
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

            User savedUser = userRepository.saveAndFlush(user);
            try {
                notificationService.notifyUserRegistered(savedUser);
            } catch (Throwable notifEx) {
                System.err.println("Warning: Failed to send welcome notification: " + notifEx.getMessage());
            }
            String jwt = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
            return ResponseEntity.ok(new AuthResponse(true, jwt, savedUser, "Google registration successful"));
        } catch (Throwable e) {
            System.err.println("Google registration failed: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Google registration error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()))
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

    private VerifiedGoogleProfile verifyGoogleProfile(String idTokenString) {
        List<String> validAudiences = new ArrayList<>();
        if (googleClientId != null && !googleClientId.isBlank()) {
            for (String id : googleClientId.split(",")) {
                if (!id.trim().isEmpty()) {
                    validAudiences.add(id.trim());
                }
            }
        }

        // Strategy 1: Local cryptographic verification via GoogleIdTokenVerifier + GsonFactory
        try {
            GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            );
            if (!validAudiences.isEmpty()) {
                builder.setAudience(validAudiences);
            }
            GoogleIdToken token = builder.build().verify(idTokenString);
            if (token == null) {
                token = new GoogleIdTokenVerifier.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance()
                ).build().verify(idTokenString);
            }
            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();
                return new VerifiedGoogleProfile(
                        payload.getEmail(),
                        (String) payload.get("name"),
                        Boolean.TRUE.equals(payload.getEmailVerified())
                );
            }
        } catch (Throwable verifierErr) {
            System.err.println("GoogleIdTokenVerifier warning (falling back to Google tokeninfo endpoint): "
                    + verifierErr.getClass().getName() + ": " + verifierErr.getMessage());
        }

        // Strategy 2: Official Google OAuth2 tokeninfo verification endpoint
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idTokenString, StandardCharsets.UTF_8);
            ResponseEntity<String> resp = restTemplate.getForEntity(URI.create(url), String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode node = objectMapper.readTree(resp.getBody());
                String email = node.path("email").asText(null);
                String name = node.path("name").asText(null);
                String emailVerifiedStr = node.path("email_verified").asText("false");
                boolean emailVerified = "true".equalsIgnoreCase(emailVerifiedStr) || node.path("email_verified").asBoolean(false);
                String iss = node.path("iss").asText("");
                if (email != null && !email.isBlank()
                        && (iss.contains("accounts.google.com") || iss.isEmpty())) {
                    return new VerifiedGoogleProfile(email, name, emailVerified);
                }
            }
        } catch (Throwable tokenInfoErr) {
            System.err.println("Google tokeninfo endpoint error: " + tokenInfoErr.getMessage());
        }

        return null;
    }

    private ResponseEntity<?> processGoogleUser(String email, String name, HttpServletRequest httpRequest) {
        boolean isNewUser = false;
        User user = findUserByEmail(email).orElse(null);
        if (user == null) {
            isNewUser = true;
            // Auto-provision a tenant account on first Google sign-in so users don't hit "Authentication failed".
            user = new User();
            user.setEmail(email.trim());
            user.setName((name != null && !name.isBlank()) ? name.trim() : email.substring(0, email.indexOf('@')));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(UserRole.TENANT);
            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.saveAndFlush(user);
            try {
                notificationService.notifyUserRegistered(user);
            } catch (Throwable notifEx) {
                System.err.println("Warning: Failed to send welcome notification: " + notifEx.getMessage());
            }
        }
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "message", "This account has been suspended")
            );
        }

        if (name != null && !name.isBlank() && (user.getName() == null || user.getName().isBlank())) {
            user.setName(name.trim());
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("ACTIVE");
        }
        user.setEmailVerified(true);

        UserRole effectiveRole = user.getRole() != null ? user.getRole() : UserRole.TENANT;
        user.setRole(effectiveRole);
        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.saveAndFlush(user);

        // If existing user signed in, send login notification
        if (!isNewUser) {
            try {
                String clientIp = extractClientIp(httpRequest);
                String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "Google Sign-In";
                notificationService.notifyUserLogin(user, clientIp, userAgent);
            } catch (Throwable notifEx) {
                System.err.println("Warning: Failed to dispatch Google login notification: " + notifEx.getMessage());
            }
        }

        String jwt = jwtUtil.generateToken(user.getEmail(), effectiveRole.name());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName() != null ? user.getName() : "");
        userData.put("role", effectiveRole.name().toLowerCase());
        userData.put("emailVerified", user.isEmailVerified());
        userData.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
        userData.put("profileImage", user.getProfileImage());
        userData.put("phone", user.getPhone());
        userData.put("bio", user.getBio());
        userData.put("businessName", user.getBusinessName());
        userData.put("locationArea", user.getLocationArea());
        if (user.getVerificationStatus() != null) {
            userData.put("verificationStatus", user.getVerificationStatus().name());
        }

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
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
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

            // Dispatch login notification & alert email
            try {
                String clientIp = extractClientIp(httpRequest);
                String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "Web/Mobile App";
                notificationService.notifyUserLogin(updatedUser, clientIp, userAgent);
            } catch (Exception ex) {
                System.err.println("Warning: Failed to dispatch login notification: " + ex.getMessage());
            }

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
            userData.put("profileImage", updatedUser.getProfileImage());
            userData.put("phone", updatedUser.getPhone());
            userData.put("bio", updatedUser.getBio());
            userData.put("businessName", updatedUser.getBusinessName());
            userData.put("locationArea", updatedUser.getLocationArea());
            if (updatedUser.getVerificationStatus() != null) {
                userData.put("verificationStatus", updatedUser.getVerificationStatus().name());
            }

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
        if (email == null || email.isBlank()) return Optional.empty();
        String trimmed = email.trim();
        try {
            Optional<User> exact = userRepository.findByEmailIgnoreCase(trimmed);
            if (exact.isPresent()) {
                return exact;
            }
            return userRepository.findByEmail(trimmed);
        } catch (RuntimeException dupEx) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(trimmed))
                    .findFirst();
        }
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

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}