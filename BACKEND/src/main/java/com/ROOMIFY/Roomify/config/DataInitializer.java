package com.ROOMIFY.Roomify.config;

import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
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
            // Create Admin
            if (userRepository.findByEmail("admin@roomify.com").isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@roomify.com");
                admin.setPassword(passwordEncoder.encode("Raphael11111"));
                admin.setRole(UserRole.ADMIN);
                admin.setEmailVerified(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                System.out.println("Roomify: Admin account verified: admin@roomify.com / Raphael11111");
            }

            // Create Super Admin
            if (userRepository.findByEmail("superadmin@roomify.com").isEmpty()) {
                User superAdmin = new User();
                superAdmin.setName("Main Super Admin");
                superAdmin.setEmail("superadmin@roomify.com");
                superAdmin.setPassword(passwordEncoder.encode("Raphael111111"));
                superAdmin.setRole(UserRole.SUPER_ADMIN);
                superAdmin.setEmailVerified(true);
                superAdmin.setCreatedAt(LocalDateTime.now());
                userRepository.save(superAdmin);
                System.out.println("Roomify: Super Admin account verified: superadmin@roomify.com / Raphael111111");
            }

            // Also ensure the personal emails work if they exist
            userRepository.findByEmail("raphaelfrank02@gmail.com").ifPresent(u -> {
                u.setRole(UserRole.ADMIN);
                userRepository.save(u);
            });
            userRepository.findByEmail("raphaelfrank01@gmail.com").ifPresent(u -> {
                u.setRole(UserRole.SUPER_ADMIN);
                userRepository.save(u);
            });

            System.out.println("=== Data Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("!!! Data Initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
