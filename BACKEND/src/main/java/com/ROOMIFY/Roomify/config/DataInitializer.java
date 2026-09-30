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
            User superAdmin = ensureUser("raphaelfrank01@gmail.com", "Main Super Admin", "Raphael1111#", UserRole.SUPER_ADMIN);

            // 2. Ensure Admin (raphaelfrank02@gmail.com)
            User admin = ensureUser("raphaelfrank02@gmail.com", "Raphael Frank", "Raphael1111@", UserRole.ADMIN);

            // 3. Ensure original default admin for compatibility
            ensureUser("admin@roomify.com", "System Admin", "Raphael11111", UserRole.ADMIN);

            // 4. Ensure original default super admin for compatibility
            ensureUser("superadmin@roomify.com", "Legacy Super Admin", "Raphael111111", UserRole.SUPER_ADMIN);

            // 5. Ensure sample rooms if database has no rooms
            ensureSampleRooms(admin != null ? admin.getId() : superAdmin.getId());

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

    private void ensureSampleRooms(Long defaultOwnerId) {
        if (roomRepository.count() > 0) {
            System.out.println("Roomify: Rooms already exist in database (" + roomRepository.count() + " total). Skipping sample room creation.");
            return;
        }

        System.out.println("Roomify: Seeding initial sample rooms...");

        Room r1 = new Room();
        r1.setTitle("Cozy Studio Downtown");
        r1.setDescription("Perfect for students and young professionals, near city center and transport.");
        r1.setPropertyType("Studio");
        r1.setPrice(450000.0);
        r1.setLatitude(-6.8162);
        r1.setLongitude(39.2783);
        r1.setAddress("Kariakoo, Dar es Salaam, Tanzania");
        r1.setOwnerName("John Mwakalinga");
        r1.setContactPhone("+255 712 345 678");
        r1.setContactEmail("john@roomify.co.tz");
        r1.setRoomsCount(1);
        r1.setBathroomsCount(1);
        r1.setArea(35.0);
        r1.approve();
        r1.setPostedBy(defaultOwnerId);
        r1.setAmenities(Arrays.asList("WiFi", "Parking", "Security"));
        r1.setImages(Arrays.asList("/uploads/rooms/1/room1.svg"));
        r1.setImageCount(1);
        r1.setCreatedAt(LocalDateTime.now());
        r1.setUpdatedAt(LocalDateTime.now());

        Room r2 = new Room();
        r2.setTitle("Modern 2-Bedroom Apartment");
        r2.setDescription("Spacious luxury apartment with sea view and full amenities.");
        r2.setPropertyType("Apartment");
        r2.setPrice(850000.0);
        r2.setLatitude(-6.7537);
        r2.setLongitude(39.2660);
        r2.setAddress("Masaki, Dar es Salaam, Tanzania");
        r2.setOwnerName("Sarah Kimaro");
        r2.setContactPhone("+255 713 456 789");
        r2.setContactEmail("sarah@roomify.co.tz");
        r2.setRoomsCount(2);
        r2.setBathroomsCount(2);
        r2.setArea(85.0);
        r2.approve();
        r2.setFeatured(true);
        r2.setPostedBy(defaultOwnerId);
        r2.setAmenities(Arrays.asList("WiFi", "Parking", "Pool", "Gym", "Security"));
        r2.setImages(Arrays.asList("/uploads/rooms/2/room2.svg"));
        r2.setImageCount(1);
        r2.setCreatedAt(LocalDateTime.now());
        r2.setUpdatedAt(LocalDateTime.now());

        Room r3 = new Room();
        r3.setTitle("Shared Student Room");
        r3.setDescription("Affordable shared accommodation close to university campuses.");
        r3.setPropertyType("Shared Room");
        r3.setPrice(180000.0);
        r3.setLatitude(-6.7885);
        r3.setLongitude(39.2082);
        r3.setAddress("Ubungo, Dar es Salaam, Tanzania");
        r3.setOwnerName("Mwalimu Juma");
        r3.setContactPhone("+255 714 567 890");
        r3.setContactEmail("juma@roomify.co.tz");
        r3.setRoomsCount(1);
        r3.setBathroomsCount(1);
        r3.setArea(20.0);
        r3.approve();
        r3.setPostedBy(defaultOwnerId);
        r3.setAmenities(Arrays.asList("WiFi", "Water"));
        r3.setImages(Arrays.asList("/uploads/rooms/3/room3.svg"));
        r3.setImageCount(1);
        r3.setCreatedAt(LocalDateTime.now());
        r3.setUpdatedAt(LocalDateTime.now());

        Room r4 = new Room();
        r4.setTitle("Luxury Penthouse");
        r4.setDescription("Premium penthouse with panoramic views and top-of-the-line finishes.");
        r4.setPropertyType("Penthouse");
        r4.setPrice(2500000.0);
        r4.setLatitude(-6.7683);
        r4.setLongitude(39.2742);
        r4.setAddress("Oyster Bay, Dar es Salaam, Tanzania");
        r4.setOwnerName("Amina Enterprises");
        r4.setContactPhone("+255 715 678 901");
        r4.setContactEmail("amina@roomify.co.tz");
        r4.setRoomsCount(4);
        r4.setBathroomsCount(3);
        r4.setArea(250.0);
        r4.setStatus("PENDING");
        r4.setVerificationStatus(VerificationStatus.PENDING);
        r4.setAvailable(false);
        r4.setFeatured(true);
        r4.setPromoted(true);
        r4.setPostedBy(defaultOwnerId);
        r4.setAmenities(Arrays.asList("WiFi", "Parking", "Pool", "Gym", "Security", "Elevator", "AC"));
        r4.setImages(Arrays.asList("/uploads/rooms/4/room4.svg"));
        r4.setImageCount(1);
        r4.setCreatedAt(LocalDateTime.now());
        r4.setUpdatedAt(LocalDateTime.now());

        Room r5 = new Room();
        r5.setTitle("Single Room Near Campus");
        r5.setDescription("Walking distance to UDSM, quiet environment for studies.");
        r5.setPropertyType("Single Room");
        r5.setPrice(220000.0);
        r5.setLatitude(-6.7780);
        r5.setLongitude(39.2400);
        r5.setAddress("Kijitonyama, Dar es Salaam, Tanzania");
        r5.setOwnerName("Prof. Mushi");
        r5.setContactPhone("+255 716 789 012");
        r5.setContactEmail("mushi@roomify.co.tz");
        r5.setRoomsCount(1);
        r5.setBathroomsCount(1);
        r5.setArea(25.0);
        r5.markAsRented();
        r5.setVerificationStatus(VerificationStatus.VERIFIED);
        r5.setPostedBy(defaultOwnerId);
        r5.setAmenities(Arrays.asList("WiFi", "Parking", "Security"));
        r5.setImages(Arrays.asList("/uploads/rooms/5/room5.svg"));
        r5.setImageCount(1);
        r5.setCreatedAt(LocalDateTime.now());
        r5.setUpdatedAt(LocalDateTime.now());

        roomRepository.saveAll(Arrays.asList(r1, r2, r3, r4, r5));
        System.out.println("Roomify: Successfully seeded 5 sample rooms.");
    }
}
