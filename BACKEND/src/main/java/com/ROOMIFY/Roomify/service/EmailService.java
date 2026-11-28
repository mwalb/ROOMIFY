package com.ROOMIFY.Roomify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:c32f86001@smtp-brevo.com}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:charlesnestory432@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:ROOMIFY}")
    private String senderName;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile String lastDeliveryStatus = "Ready";

    public String getLastDeliveryStatus() {
        return lastDeliveryStatus;
    }

    public String getCleanApiKey() {
        if (brevoApiKey == null) return "";
        String key = brevoApiKey.trim();
        if (key.startsWith("eyJ")) {
            try {
                byte[] decoded = Base64.getDecoder().decode(key);
                String json = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                if (json.contains("\"api_key\"")) {
                    Map<?, ?> map = objectMapper.readValue(json, Map.class);
                    if (map.containsKey("api_key")) {
                        return map.get("api_key").toString().trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return key;
    }

    /**
     * Asynchronously sends an HTML email via SMTP with automatic Brevo REST API fallback.
     */
    @Async
    public void sendEmailAsync(String toEmail, String toName, String subject, String htmlContent) {
        sendEmail(toEmail, toName, subject, htmlContent);
    }

    /**
     * Synchronously sends an HTML email.
     * Prioritizes Brevo HTTPS REST API when an xkeysib- API key is available (most reliable, zero port blocking).
     * Automatically falls back to SMTP if REST fails or an SMTP key is configured.
     */
    public boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            System.err.println("⚠️ EmailService: Invalid recipient email: " + toEmail);
            return false;
        }

        String recipientName = (toName != null && !toName.trim().isEmpty()) ? toName.trim() : "Roomify User";
        String apiKey = getCleanApiKey();

        // Priority 1: Direct Brevo HTTPS REST API (Instant delivery over port 443, no SMTP port/auth blocks)
        if (apiKey != null && apiKey.startsWith("xkeysib-")) {
            boolean restOk = sendViaBrevoRestApi(toEmail, recipientName, subject, htmlContent);
            if (restOk) {
                return true;
            }
            System.err.println("⚠️ EmailService: Brevo REST API delivery failed. Attempting SMTP fallback...");
        }

        // Priority 2: Configured JavaMailSender (smtp-relay.brevo.com:587)
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(senderEmail, senderName);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                System.out.println("✅ EmailService: Sent via Brevo SMTP to " + toEmail + " | Subject: " + subject);
                return true;
            } catch (Exception smtpEx) {
                System.err.println("⚠️ EmailService: Primary SMTP delivery failed (" + smtpEx.getMessage() + "). Trying fallback credentials and hosts...");
            }
        }

        // Priority 3: Resilient SMTP Fallback with alternate usernames and hosts
        List<String> usernames = new ArrayList<>();
        if (smtpUsername != null && !smtpUsername.isBlank()) usernames.add(smtpUsername);
        if (senderEmail != null && !senderEmail.isBlank() && !usernames.contains(senderEmail)) usernames.add(senderEmail);

        String[] fallbackHosts = new String[]{"smtp-relay.brevo.com", "smtp-relay.sendinblue.com"};
        for (String user : usernames) {
            for (String host : fallbackHosts) {
                try {
                    org.springframework.mail.javamail.JavaMailSenderImpl customSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
                    customSender.setHost(host);
                    customSender.setPort(smtpPort > 0 ? smtpPort : 587);
                    customSender.setUsername(user);
                    customSender.setPassword(smtpPassword);

                    Properties props = customSender.getJavaMailProperties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.starttls.required", "true");
                    props.put("mail.smtp.ssl.trust", "*");
                    props.put("mail.smtp.ssl.checkserveridentity", "false");
                    props.put("mail.smtp.connectiontimeout", "8000");
                    props.put("mail.smtp.timeout", "8000");
                    props.put("mail.smtp.writetimeout", "8000");
                    props.put("mail.debug", "true");

                    MimeMessage message = customSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                    helper.setFrom(senderEmail, senderName);
                    helper.setTo(toEmail);
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);

                    customSender.send(message);
                    System.out.println("✅ EmailService: Sent via Brevo SMTP fallback (" + host + " with user " + user + ") to " + toEmail);
                    return true;
                } catch (Exception ex) {
                    System.err.println("⚠️ EmailService: Fallback SMTP host " + host + " (user: " + user + ") failed: " + ex.getMessage());
                }
            }
        }

        // Priority 4: Brevo v3 Transactional Email REST API (if not attempted earlier)
        if (apiKey == null || !apiKey.startsWith("xkeysib-")) {
            return sendViaBrevoRestApi(toEmail, recipientName, subject, htmlContent);
        }

        return false;
    }

    /**
     * Sends transactional email using Brevo's HTTPS REST API.
     */
    public boolean sendViaBrevoRestApi(String toEmail, String toName, String subject, String htmlContent) {
        try {
            String apiKey = getCleanApiKey();
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("name", senderName, "email", senderEmail));
            payload.put("to", List.of(Map.of("email", toEmail, "name", toName)));
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            String jsonBody = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url("https://api.brevo.com/v3/smtp/email")
                    .addHeader("api-key", apiKey)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                System.out.println(">>> [BREVO REST API] HTTP " + response.code() + " Response: " + responseBody);
                lastDeliveryStatus = "HTTP " + response.code() + ": " + responseBody;
                if (response.isSuccessful()) {
                    System.out.println("✅ EmailService: Sent via Brevo REST API to " + toEmail);
                    return true;
                } else {
                    System.err.println("❌ EmailService: Brevo REST API error (HTTP " + response.code() + "): " + responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            lastDeliveryStatus = "Exception: " + e.getMessage();
            System.err.println("❌ EmailService: Failed to send via Brevo REST API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // HTML EMAIL TEMPLATE BUILDERS
    // =========================================================================

    public String buildWelcomeEmail(String userName, String role) {
        String roleDescription = switch (role.toUpperCase()) {
            case "OWNER" -> "You can list properties, track leases, accept bookings, and manage rental income effortlessly.";
            case "DALALI" -> "You can manage authorized listings, connect owners with tenants, and receive facilitation fees.";
            case "FURNITURE" -> "You can showcase furniture designs, manage orders, and connect with tenants furnishing homes.";
            default -> "Find your dream home or room, book with ease, and interact directly with verified owners.";
        };

        return wrapInBrandedTemplate(
                "Welcome to ROOMIFY!",
                "Hello " + sanitize(userName) + ",",
                """
                <p>Welcome to <strong>ROOMIFY</strong> — your modern digital real estate and property platform!</p>
                <div style="background: #f0f4ff; border-left: 4px solid #1e3a8a; padding: 16px; margin: 20px 0; border-radius: 4px;">
                    <p style="margin: 0; font-weight: 600; color: #1e3a8a;">Your Account Role: %s</p>
                    <p style="margin: 8px 0 0 0; color: #475569;">%s</p>
                </div>
                <p>Your account is now active and ready to use. Start exploring today!</p>
                """.formatted(sanitize(role.toUpperCase()), roleDescription),
                "Explore ROOMIFY",
                "https://roomify.co.tz"
        );
    }

    public String buildPasswordResetEmail(String userName, String resetCode) {
        return wrapInBrandedTemplate(
                "Password Reset Verification Code",
                "Hello " + sanitize(userName) + ",",
                """
                <p>We received a request to reset your ROOMIFY account password. Use the 6-digit verification code below to complete the reset process:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <span style="display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1e3a8a; background: #e0e7ff; padding: 12px 28px; border-radius: 8px; border: 2px dashed #3b82f6;">
                        %s
                    </span>
                </div>
                <p style="color: #64748b; font-size: 13px;">This code will expire shortly. If you did not request a password reset, please ignore this email or reach out to support immediately.</p>
                """.formatted(resetCode),
                null,
                null
        );
    }

    public String buildBookingRequestEmail(String recipientName, String tenantName, String roomTitle, String startDate, String endDate, double totalAmount) {
        return wrapInBrandedTemplate(
                "New Booking Request Received",
                "Hello " + sanitize(recipientName) + ",",
                """
                <p>You have received a new booking request for your property listing!</p>
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                        <tr><td style="color: #64748b; padding: 6px 0;">Property:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Tenant:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Start Date:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">End Date:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Total Value:</td><td style="font-weight: 700; color: #16a34a; font-size: 16px;">TSH %,.0f</td></tr>
                    </table>
                </div>
                <p>Please log in to your ROOMIFY Dashboard to review and accept or decline this booking request.</p>
                """.formatted(sanitize(roomTitle), sanitize(tenantName), sanitize(startDate), sanitize(endDate), totalAmount),
                "View Booking in Dashboard",
                "https://roomify.co.tz/dashboard"
        );
    }

    public String buildBookingConfirmedForTenant(String tenantName, String roomTitle, String startDate, String endDate, double totalAmount) {
        return wrapInBrandedTemplate(
                "Booking Submitted Successfully",
                "Hello " + sanitize(tenantName) + ",",
                """
                <p>Your booking request for <strong>%s</strong> has been submitted successfully!</p>
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                        <tr><td style="color: #64748b; padding: 6px 0;">Property:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Period:</td><td style="font-weight: 600; color: #0f172a;">%s to %s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Total Amount:</td><td style="font-weight: 700; color: #16a34a; font-size: 16px;">TSH %,.0f</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Status:</td><td style="font-weight: 600; color: #eab308;">PENDING REVIEW</td></tr>
                    </table>
                </div>
                <p>The property manager or owner has been notified and will review your booking shortly. You will receive an update once accepted!</p>
                """.formatted(sanitize(roomTitle), sanitize(roomTitle), sanitize(startDate), sanitize(endDate), totalAmount),
                "Check Booking Status",
                "https://roomify.co.tz/tenant"
        );
    }

    public String buildBookingStatusEmail(String tenantName, String roomTitle, String status, String note) {
        boolean isAccepted = "ACCEPTED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
        String headerColor = isAccepted ? "#16a34a" : "#dc2626";
        String statusLabel = isAccepted ? "ACCEPTED & CONFIRMED" : "DECLINED";

        return wrapInBrandedTemplate(
                "Booking Update: " + sanitize(roomTitle),
                "Hello " + sanitize(tenantName) + ",",
                """
                <p>There is an update regarding your booking request for <strong>%s</strong>:</p>
                <div style="text-align: center; margin: 24px 0;">
                    <span style="display: inline-block; font-size: 18px; font-weight: bold; color: white; background: %s; padding: 10px 24px; border-radius: 6px;">
                        %s
                    </span>
                </div>
                <p>%s</p>
                """.formatted(
                        sanitize(roomTitle),
                        headerColor,
                        statusLabel,
                        isAccepted
                                ? "Congratulations! Your reservation has been accepted. You can view all contact details and lease information on your ROOMIFY Tenant Dashboard."
                                : (note != null && !note.isBlank() ? "Reason: " + sanitize(note) : "This booking request was declined. Feel free to explore other verified listings available on ROOMIFY.")
                ),
                "Open Dashboard",
                "https://roomify.co.tz/tenant"
        );
    }

    public String buildBookingCancelledEmail(String ownerName, String tenantName, String roomTitle) {
        return wrapInBrandedTemplate(
                "Booking Cancelled: " + sanitize(roomTitle),
                "Hello " + sanitize(ownerName) + ",",
                """
                <p>We are writing to inform you that the booking for <strong>%s</strong> by <strong>%s</strong> has been cancelled.</p>
                <p>The property status has automatically been updated to <strong>AVAILABLE</strong> and is once again open for bookings from other tenants.</p>
                """.formatted(sanitize(roomTitle), sanitize(tenantName)),
                "View My Properties",
                "https://roomify.co.tz/dashboard"
        );
    }

    public String buildPropertyStatusEmail(String ownerName, String roomTitle, String status, String reason) {
        boolean isApproved = "VERIFIED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status);
        String statusLabel = isApproved ? "VERIFIED & PUBLISHED" : "REVISION REQUIRED";
        String statusColor = isApproved ? "#16a34a" : "#dc2626";

        return wrapInBrandedTemplate(
                "Property Verification: " + sanitize(roomTitle),
                "Hello " + sanitize(ownerName) + ",",
                """
                <p>Your property listing <strong>%s</strong> has completed admin review:</p>
                <div style="text-align: center; margin: 20px 0;">
                    <span style="display: inline-block; font-size: 16px; font-weight: bold; color: white; background: %s; padding: 8px 20px; border-radius: 6px;">
                        %s
                    </span>
                </div>
                <p>%s</p>
                """.formatted(
                        sanitize(roomTitle),
                        statusColor,
                        statusLabel,
                        isApproved
                                ? "Your property is now officially verified and visible to tenants searching on ROOMIFY!"
                                : ("Reason: " + (reason != null && !reason.isBlank() ? sanitize(reason) : "Property details require amendment before publication."))
                ),
                "View Listing",
                "https://roomify.co.tz/dashboard"
        );
    }

    public String buildAvailabilityAlertEmail(String userName, String roomTitle, double price, String location) {
        return wrapInBrandedTemplate(
                "Good News: " + sanitize(roomTitle) + " is Now Available!",
                "Hello " + sanitize(userName) + ",",
                """
                <p>A property you subscribed to receive notifications for is now <strong>AVAILABLE</strong> for rent!</p>
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px; margin: 20px 0;">
                    <p style="font-size: 16px; font-weight: 700; color: #0f172a; margin: 0 0 8px 0;">%s</p>
                    <p style="color: #64748b; margin: 4px 0;">📍 Location: %s</p>
                    <p style="color: #16a34a; font-weight: 700; font-size: 18px; margin: 8px 0 0 0;">TSH %,.0f / month</p>
                </div>
                <p>Demand is high! Book now before someone else takes it.</p>
                """.formatted(sanitize(roomTitle), sanitize(location), price),
                "Book Now",
                "https://roomify.co.tz"
        );
    }

    public String buildUserAccountStatusEmail(String userName, String status, String reason) {
        boolean isVerified = "VERIFIED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status);
        String label = isVerified ? "ACCOUNT VERIFIED" : "ACCOUNT NOTICE";
        String color = isVerified ? "#16a34a" : "#dc2626";

        return wrapInBrandedTemplate(
                "ROOMIFY Account Status Update",
                "Hello " + sanitize(userName) + ",",
                """
                <p>An update has been made to your ROOMIFY account status:</p>
                <div style="text-align: center; margin: 20px 0;">
                    <span style="display: inline-block; font-size: 16px; font-weight: bold; color: white; background: %s; padding: 8px 20px; border-radius: 6px;">
                        %s
                    </span>
                </div>
                <p>%s</p>
                """.formatted(
                        color,
                        label,
                        isVerified
                                ? "Congratulations! Your identity and business credentials have been officially verified by ROOMIFY administrators."
                                : ("Details: " + (reason != null && !reason.isBlank() ? sanitize(reason) : "Your account status has been updated. Please contact support if you have questions."))
                ),
                "Go to Profile",
                "https://roomify.co.tz/profile"
        );
    }

    public String buildLoginAlertEmail(String userName, String ipAddress, String userAgent, String timestamp) {
        return wrapInBrandedTemplate(
                "Security Alert: New Sign-in to Your Account",
                "Hello " + sanitize(userName) + ",",
                """
                <p>We detected a new sign-in to your <strong>ROOMIFY</strong> account.</p>
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                        <tr><td style="color: #64748b; padding: 6px 0;">Time:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">IP Address:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                        <tr><td style="color: #64748b; padding: 6px 0;">Device / Browser:</td><td style="font-weight: 600; color: #0f172a;">%s</td></tr>
                    </table>
                </div>
                <p style="color: #475569; font-size: 13px;">If this was you, you don't need to take any action. If you did not log in, please reset your password immediately to secure your account.</p>
                """.formatted(sanitize(timestamp), sanitize(ipAddress != null ? ipAddress : "Unknown IP"), sanitize(userAgent != null ? userAgent : "Web / Mobile App")),
                "Secure My Account",
                "https://roomify.co.tz/forgot-password"
        );
    }

    // =========================================================================
    // HELPER: Master Email Container
    // =========================================================================

    private String wrapInBrandedTemplate(String title, String greeting, String bodyContent, String buttonText, String buttonUrl) {
        String buttonHtml = (buttonText != null && buttonUrl != null) ? """
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #1e3a8a; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 6px; font-weight: 600; font-size: 15px; display: inline-block;">
                        %s
                    </a>
                </div>
                """.formatted(buttonUrl, buttonText) : "";

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #334155; line-height: 1.6;">
            <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f1f5f9; padding: 30px 15px;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="100%%" style="max-width: 580px; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                            <!-- Header -->
                            <tr>
                                <td style="background: linear-gradient(135deg, #1e3a8a 0%%, #2563eb 100%%); padding: 28px 32px; text-align: left;">
                                    <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 800; letter-spacing: 1px;">ROOMIFY</h1>
                                    <p style="margin: 4px 0 0 0; color: #93c5fd; font-size: 13px;">Smarter Living, Seamless Renting</p>
                                </td>
                            </tr>
                            <!-- Content -->
                            <tr>
                                <td style="padding: 32px;">
                                    <h2 style="margin: 0 0 16px 0; color: #0f172a; font-size: 20px; font-weight: 700;">%s</h2>
                                    <p style="margin: 0 0 16px 0; font-size: 15px; color: #334155;">%s</p>
                                    <div style="font-size: 15px; color: #334155;">
                                        %s
                                    </div>
                                    %s
                                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 30px 0 20px 0;">
                                    <p style="margin: 0; font-size: 13px; color: #94a3b8; text-align: center;">
                                        Have questions? Contact our team at <a href="mailto:support@roomify.co.tz" style="color: #2563eb; text-decoration: none;">support@roomify.co.tz</a>
                                    </p>
                                </td>
                            </tr>
                            <!-- Footer -->
                            <tr>
                                <td style="background-color: #f8fafc; padding: 18px 32px; text-align: center; border-top: 1px solid #e2e8f0;">
                                    <p style="margin: 0; font-size: 12px; color: #64748b;">
                                        &copy; 2026 ROOMIFY. All rights reserved.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(sanitize(title), sanitize(title), greeting, bodyContent, buttonHtml);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
