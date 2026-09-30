package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.NotificationSubscription;
import com.ROOMIFY.Roomify.model.NotificationToken;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.NotificationSubscriptionRepository;
import com.ROOMIFY.Roomify.repository.NotificationTokenRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import com.ROOMIFY.Roomify.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    private FCMService fcmService;

    @Autowired
    private NotificationTokenRepository tokenRepository;

    @Autowired
    private NotificationSubscriptionRepository subscriptionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                if (email != null && !email.isBlank()) {
                    return userRepository.findByEmail(email).orElse(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting authenticated user: " + e.getMessage());
        }
        return null;
    }

    // REGISTER TOKEN
    @PostMapping("/register-token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestParam Long userId,
            @RequestParam String fcmToken) {

        try {
            NotificationToken token = tokenRepository
                    .findByUserId(userId)
                    .orElse(new NotificationToken());

            token.setUserId(userId);
            token.setFcmToken(fcmToken);
            token.setActive(true);

            tokenRepository.save(token);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, null, "Token saved")
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // SEND TO USER
    @PostMapping("/send-to-user")
    public ResponseEntity<ApiResponse<Void>> sendToUser(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String body) {

        try {
            NotificationToken token = tokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Token not found"));

            fcmService.sendNotificationToUser(
                    token.getFcmToken(),
                    title,
                    body,
                    new HashMap<>()
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, null, "Sent")
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // ROOM NOTIFICATION
    @PostMapping("/send-room-notification")
    public ResponseEntity<ApiResponse<Void>> sendRoomNotification(@RequestParam Long roomId) {

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            List<NotificationToken> tokens = tokenRepository.findAll();

            for (NotificationToken t : tokens) {
                fcmService.sendNotificationToUser(
                        t.getFcmToken(),
                        "New Room Available",
                        room.getTitle(),
                        Map.of("type", "new_room", "roomId", String.valueOf(roomId))
                );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, null, "Notifications sent")
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // NOTIFY ME SUBSCRIPTION
    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> subscribeToProperty(@RequestParam Long propertyId) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, null, "Authentication required to subscribe for notifications"));
            }
            
            Room room = roomRepository.findById(propertyId).orElse(null);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, null, "Property not found"));
            }

            if (subscriptionRepository.existsByUserIdAndPropertyId(user.getId(), propertyId)) {
                return ResponseEntity.ok(new ApiResponse<>(true, null, "Already subscribed to notifications for this property"));
            }

            NotificationSubscription sub = new NotificationSubscription(user.getId(), propertyId);
            subscriptionRepository.save(sub);

            return ResponseEntity.ok(new ApiResponse<>(true, null, "Successfully subscribed to availability notifications"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/subscribed")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Boolean>> checkSubscription(@RequestParam Long propertyId) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse<>(true, false, "Not authenticated"));
            }
            boolean subscribed = subscriptionRepository.existsByUserIdAndPropertyId(user.getId(), propertyId);
            return ResponseEntity.ok(new ApiResponse<>(true, subscribed, "Subscription status retrieved"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "Error: " + e.getMessage()));
        }
    }
}
