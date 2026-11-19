package com.ROOMIFY.Roomify.config;

import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.model.VerificationStatus;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            System.out.println("=== Starting Data Initialization ===");

            // 1. Ensure Super Admin (raphaelfrank01@gmail.com)
            ensureUser("raphaelfrank01@gmail.com", "Main Super Admin", "Raphael1111#", UserRole.SUPER_ADMIN);

            // 2. Ensure Admin (raphaelfrank02@gmail.com)
            ensureUser("raphaelfrank02@gmail.com", "Raphael Frank", "Raphael1111@", UserRole.ADMIN);

            // 3. Ensure original default admin for compatibility
            ensureUser("admin@roomify.com", "System Admin", "Raphael11111", UserRole.ADMIN);

            // 4. Ensure original default super admin for compatibility
            ensureUser("superadmin@roomify.com", "Legacy Super Admin", "Raphael111111", UserRole.SUPER_ADMIN);

            // 5. Cleanup hardcoded sample rooms if present in database
            cleanupSampleRooms();

            System.out.println("=== Data Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("!!! Data Initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User ensureUser(String email, String name, String rawPassword, UserRole role) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setCreatedAt(LocalDateTime.now());
            user.setEmailVerified(true);
            System.out.println("Roomify: Creating new " + role + " account: " + email);
        } else {
            System.out.println("Roomify: Updating existing account to " + role + ": " + email);
        }

        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmailVerified(true);
        user.setStatus("ACTIVE");
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        return userRepository.save(user);
    }

    private void cleanupSampleRooms() {
        List<String> sampleTitles = Arrays.asList(
            "Cozy Studio Downtown",
            "Modern 2-Bedroom Apartment",
            "Shared Student Room",
            "Luxury Penthouse",
            "Single Room Near Campus"
        );
        List<Room> hardcodedRooms = roomRepository.findAll().stream()
            .filter(r -> r.getTitle() != null && sampleTitles.contains(r.getTitle()))
            .toList();

        if (!hardcodedRooms.isEmpty()) {
            roomRepository.deleteAll(hardcodedRooms);
            System.out.println("Roomify: Successfully removed " + hardcodedRooms.size() + " hardcoded sample rooms from database.");
        } else {
            System.out.println("Roomify: No hardcoded sample rooms found in database.");
        }
    }
}
