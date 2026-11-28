package com.ROOMIFY.Roomify.service;

import com.ROOMIFY.Roomify.model.*;
import com.ROOMIFY.Roomify.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private InAppNotificationRepository inAppNotificationRepository;

    @Autowired
    private NotificationTokenRepository tokenRepository;

    @Autowired
    private NotificationSubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FCMService fcmService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    // =========================================================================
    // Core Dispatcher: In-App DB + WebSocket + FCM Push
    // =========================================================================

    public InAppNotification sendInAppAndPush(Long userId, String title, String message, String type, Long referenceId) {
        if (userId == null) return null;

        InAppNotification notification = new InAppNotification(userId, title, message, type, referenceId);
        try {
            notification = inAppNotificationRepository.save(notification);

            // WebSocket broadcast for instant in-app update
            if (messagingTemplate != null) {
                try {
                    messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
                } catch (Exception wsEx) {
                    System.err.println("⚠️ WebSocket broadcast warning: " + wsEx.getMessage());
                }
            }

            // FCM Push notification
            try {
                tokenRepository.findByUserId(userId).ifPresent(t -> {
                    if (t.isActive() && t.getFcmToken() != null && !t.getFcmToken().isBlank()) {
                        fcmService.sendNotificationToUser(
                                t.getFcmToken(),
                                title,
                                message,
                                Map.of(
                                        "type", type != null ? type : "GENERAL",
                                        "referenceId", referenceId != null ? referenceId.toString() : ""
                                )
                        );
                    }
                });
            } catch (Exception pushEx) {
                System.err.println("⚠️ FCM Push warning: " + pushEx.getMessage());
            }

        } catch (Exception e) {
            System.err.println("⚠️ NotificationService: Failed to save in-app notification: " + e.getMessage());
        }

        return notification;
    }

    // =========================================================================
    // User Lifecycle Notifications
    // =========================================================================

    @Async
    public void notifyUserRegistered(User user) {
        if (user == null || user.getEmail() == null) return;

        try {
            // 1. In-App Notification
            sendInAppAndPush(
                    user.getId(),
                    "Welcome to ROOMIFY!",
                    "Your account has been successfully created. Explore properties or list yours today!",
                    "SYSTEM",
                    user.getId()
            );

            // 2. Welcome Email
            String html = emailService.buildWelcomeEmail(
                    user.getName() != null ? user.getName() : "Roomify Member",
                    user.getRole() != null ? user.getRole().name() : "TENANT"
            );
            emailService.sendEmail(user.getEmail(), user.getName(), "Welcome to ROOMIFY!", html);

            // 3. Mark welcomeSent as true in database
            user.setWelcomeSent(true);
            userRepository.save(user);
            System.out.println("✅ Welcome notification & email dispatched to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyUserRegistered: " + e.getMessage());
        }
    }

    @Async
    public void notifyUserLogin(User user, String ipAddress, String userAgent) {
        if (user == null || user.getEmail() == null) return;

        try {
            // First time login check: If user hasn't received a welcome email yet (including first-time Google sign-ins), send it first!
            if (!Boolean.TRUE.equals(user.getWelcomeSent())) {
                notifyUserRegistered(user);
            }

            // 1. In-App Notification (so user sees their active session alert)
            sendInAppAndPush(
                    user.getId(),
                    "New Sign-In Detected",
                    "You recently signed in to your ROOMIFY account.",
                    "SECURITY",
                    user.getId()
            );

            // 2. Security Login Alert Email
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
            String html = emailService.buildLoginAlertEmail(
                    user.getName() != null ? user.getName() : "Roomify Member",
                    ipAddress,
                    userAgent,
                    timestamp
            );
            emailService.sendEmail(
                    user.getEmail(),
                    user.getName(),
                    "ROOMIFY - Security Alert: New Sign-in Detected",
                    html
            );
        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyUserLogin: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetCode(User user, String resetCode) {
        if (user == null || user.getEmail() == null) return;

        try {
            String html = emailService.buildPasswordResetEmail(
                    user.getName() != null ? user.getName() : "Roomify User",
                    resetCode
            );
            emailService.sendEmail(user.getEmail(), user.getName(), "ROOMIFY - Password Reset Code", html);
        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in sendPasswordResetCode: " + e.getMessage());
        }
    }

    @Async
    public void notifyUserAccountStatus(User user, String status, String reason) {
        if (user == null || user.getEmail() == null) return;

        try {
            boolean isVerified = "VERIFIED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status);
            String title = isVerified ? "Account Verified!" : "Account Notice";
            String msg = isVerified ? "Your ROOMIFY account has been officially verified." : "Your account status has been updated: " + reason;

            sendInAppAndPush(user.getId(), title, msg, "SYSTEM", user.getId());

            String html = emailService.buildUserAccountStatusEmail(
                    user.getName() != null ? user.getName() : "Roomify Member",
                    status,
                    reason
            );
            emailService.sendEmail(user.getEmail(), user.getName(), title, html);

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyUserAccountStatus: " + e.getMessage());
        }
    }

    // =========================================================================
    // Booking Lifecycle Notifications
    // =========================================================================

    @Async
    public void notifyBookingCreated(Booking booking) {
        if (booking == null) return;

        try {
            User tenant = booking.getUser();
            Room room = booking.getRoom();
            String roomTitle = room != null ? room.getTitle() : "Property Listing";
            String startDateStr = booking.getStartDate() != null ? booking.getStartDate().toLocalDate().toString() : "TBD";
            String endDateStr = booking.getEndDate() != null ? booking.getEndDate().toLocalDate().toString() : "TBD";
            double total = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0;

            // 1. Notify Tenant (Confirmation)
            if (tenant != null) {
                sendInAppAndPush(
                        tenant.getId(),
                        "Booking Request Sent",
                        "Your request for '" + roomTitle + "' has been submitted and is pending review.",
                        "BOOKING",
                        booking.getId()
                );

                if (tenant.getEmail() != null) {
                    String tenantHtml = emailService.buildBookingConfirmedForTenant(
                            tenant.getName() != null ? tenant.getName() : "Tenant",
                            roomTitle,
                            startDateStr,
                            endDateStr,
                            total
                    );
                    emailService.sendEmail(tenant.getEmail(), tenant.getName(), "Booking Submitted: " + roomTitle, tenantHtml);
                }
            }

            // 2. Notify Owner (Action Required)
            if (room != null && room.getPostedBy() != null) {
                User owner = userRepository.findById(room.getPostedBy()).orElse(null);
                String tenantName = tenant != null && tenant.getName() != null ? tenant.getName() : "A Tenant";
                String ownerEmail = owner != null ? owner.getEmail() : room.getContactEmail();
                String ownerName = owner != null && owner.getName() != null ? owner.getName() : (room.getOwnerName() != null ? room.getOwnerName() : "Property Owner");

                sendInAppAndPush(
                        room.getPostedBy(),
                        "New Booking Request",
                        tenantName + " requested to book '" + roomTitle + "' for TSH " + String.format("%,.0f", total),
                        "BOOKING",
                        booking.getId()
                );

                if (ownerEmail != null && !ownerEmail.isBlank()) {
                    String ownerHtml = emailService.buildBookingRequestEmail(
                            ownerName,
                            tenantName,
                            roomTitle,
                            startDateStr,
                            endDateStr,
                            total,
                            booking.getId()
                    );
                    emailService.sendEmail(ownerEmail, ownerName, "New Booking Request: " + roomTitle, ownerHtml);
                }
            }

            // If Dalali is managing this listing separately
            if (room != null && room.getDalaliId() != null && room.getDalaliId() > 0) {
                userRepository.findById(room.getDalaliId()).ifPresent(dalali -> {
                    String tenantName = tenant != null && tenant.getName() != null ? tenant.getName() : "A Tenant";
                    sendInAppAndPush(
                            dalali.getId(),
                            "New Managed Listing Booking",
                            tenantName + " requested to book your managed listing '" + roomTitle + "'",
                            "BOOKING",
                            booking.getId()
                    );
                    if (dalali.getEmail() != null) {
                        String dalaliHtml = emailService.buildBookingRequestEmail(
                                dalali.getName() != null ? dalali.getName() : "Dalali Partner",
                                tenantName,
                                roomTitle,
                                startDateStr,
                                endDateStr,
                                total,
                                booking.getId()
                        );
                        emailService.sendEmail(dalali.getEmail(), dalali.getName(), "New Managed Booking Request: " + roomTitle, dalaliHtml);
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyBookingCreated: " + e.getMessage());
        }
    }

    @Async
    public void notifyBookingStatusUpdated(Booking booking, String status, String note) {
        if (booking == null) return;

        try {
            User tenant = booking.getUser();
            Room room = booking.getRoom();
            String roomTitle = room != null ? room.getTitle() : "Property";

            if (tenant != null) {
                boolean isAccepted = "ACCEPTED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
                String title = isAccepted ? "Booking Accepted! 🎉" : "Booking Update";
                String msg = isAccepted
                        ? "Your booking for '" + roomTitle + "' has been ACCEPTED by the owner!"
                        : "Your booking for '" + roomTitle + "' was not accepted." + (note != null ? " Reason: " + note : "");

                sendInAppAndPush(tenant.getId(), title, msg, "BOOKING", booking.getId());

                if (tenant.getEmail() != null) {
                    String html = emailService.buildBookingStatusEmail(
                            tenant.getName() != null ? tenant.getName() : "Tenant",
                            roomTitle,
                            status,
                            note
                    );
                    emailService.sendEmail(tenant.getEmail(), tenant.getName(), title + ": " + roomTitle, html);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyBookingStatusUpdated: " + e.getMessage());
        }
    }

    @Async
    public void notifyBookingCancelled(Booking booking) {
        if (booking == null) return;

        try {
            User tenant = booking.getUser();
            Room room = booking.getRoom();
            String roomTitle = room != null ? room.getTitle() : "Property";
            String tenantName = tenant != null && tenant.getName() != null ? tenant.getName() : "A Tenant";

            if (room != null && room.getPostedBy() != null) {
                User owner = userRepository.findById(room.getPostedBy()).orElse(null);
                String ownerEmail = owner != null ? owner.getEmail() : room.getContactEmail();
                String ownerName = owner != null && owner.getName() != null ? owner.getName() : (room.getOwnerName() != null ? room.getOwnerName() : "Property Owner");

                sendInAppAndPush(
                        room.getPostedBy(),
                        "Booking Cancelled",
                        "The booking for '" + roomTitle + "' by " + tenantName + " was cancelled. The room is available again.",
                        "BOOKING",
                        booking.getId()
                );

                if (ownerEmail != null && !ownerEmail.isBlank()) {
                    String html = emailService.buildBookingCancelledEmail(
                            ownerName,
                            tenantName,
                            roomTitle
                    );
                    emailService.sendEmail(ownerEmail, ownerName, "Booking Cancelled: " + roomTitle, html);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyBookingCancelled: " + e.getMessage());
        }
    }

    // =========================================================================
    // Property Lifecycle Notifications
    // =========================================================================

    @Async
    public void notifyPropertyStatusUpdated(Room room, String status, String reason) {
        if (room == null || room.getPostedBy() == null) return;

        try {
            User owner = userRepository.findById(room.getPostedBy()).orElse(null);
            String ownerEmail = owner != null ? owner.getEmail() : room.getContactEmail();
            String ownerName = owner != null && owner.getName() != null ? owner.getName() : (room.getOwnerName() != null ? room.getOwnerName() : "Property Owner");

            boolean isApproved = "VERIFIED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status);
            String title = isApproved ? "Property Verified! ✅" : "Property Verification Update";
            String msg = isApproved
                    ? "Congratulations! Your property '" + room.getTitle() + "' has been verified and published."
                    : "Your property '" + room.getTitle() + "' requires changes: " + (reason != null ? reason : "Please review details.");

            sendInAppAndPush(room.getPostedBy(), title, msg, "PROPERTY", room.getId());

            if (ownerEmail != null && !ownerEmail.isBlank()) {
                String html = emailService.buildPropertyStatusEmail(
                        ownerName,
                        room.getTitle(),
                        status,
                        reason
                );
                emailService.sendEmail(ownerEmail, ownerName, title + ": " + room.getTitle(), html);
            }

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyPropertyStatusUpdated: " + e.getMessage());
        }
    }

    @Async
    public void notifyRoomAvailable(Room room) {
        if (room == null || room.getId() == null) return;

        try {
            List<NotificationSubscription> subs = subscriptionRepository.findByPropertyId(room.getId());
            if (subs == null || subs.isEmpty()) return;

            for (NotificationSubscription sub : subs) {
                if (sub.getUserId() == null) continue;

                userRepository.findById(sub.getUserId()).ifPresent(user -> {
                    String title = "Available Now: " + room.getTitle();
                    String msg = "The property '" + room.getTitle() + "' you subscribed to is now available for rent!";

                    sendInAppAndPush(user.getId(), title, msg, "PROPERTY", room.getId());

                    if (user.getEmail() != null) {
                        String html = emailService.buildAvailabilityAlertEmail(
                                user.getName() != null ? user.getName() : "Roomify Member",
                                room.getTitle(),
                                room.getPrice() > 0 ? room.getPrice() : 0.0,
                                (room.getAddress() != null && !room.getAddress().isBlank()) ? room.getAddress() : "Tanzania"
                        );
                        emailService.sendEmail(user.getEmail(), user.getName(), title, html);
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("❌ NotificationService: Error in notifyRoomAvailable: " + e.getMessage());
        }
    }
}
