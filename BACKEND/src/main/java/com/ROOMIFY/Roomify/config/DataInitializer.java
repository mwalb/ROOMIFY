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
            if (userRepository.findByEmail("raphaelfrank02@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("raphaelfrank02@gmail.com");
                admin.setPassword(passwordEncoder.encode("Raphael11111"));
                admin.setRole(UserRole.ADMIN);
                admin.setEmailVerified(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                System.out.println("Admin user created: raphaelfrank02@gmail.com");
            }

            // Create Super Admin
            if (userRepository.findByEmail("raphaelfrank01@gmail.com").isEmpty()) {
                User superAdmin = new User();
                superAdmin.setName("Super Admin");
                superAdmin.setEmail("raphaelfrank01@gmail.com");
                superAdmin.setPassword(passwordEncoder.encode("Raphael111111"));
                superAdmin.setRole(UserRole.SUPER_ADMIN);
                superAdmin.setEmailVerified(true);
                superAdmin.setCreatedAt(LocalDateTime.now());
                userRepository.save(superAdmin);
                System.out.println("Super Admin user created: raphaelfrank01@gmail.com");
            }
            System.out.println("=== Data Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("!!! Data Initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
