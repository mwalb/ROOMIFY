package com.ROOMIFY.Roomify.config;

import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

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

            System.out.println("=== Data Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("!!! Data Initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureUser(String email, String name, String rawPassword, UserRole role) {
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
        user.setEmailVerified(true); // Ensure verified
        userRepository.save(user);
    }
}
