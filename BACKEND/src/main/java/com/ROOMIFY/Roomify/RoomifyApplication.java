package com.ROOMIFY.Roomify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;

@SpringBootApplication(exclude = {
		R2dbcAutoConfiguration.class,
		R2dbcTransactionManagerAutoConfiguration.class
})
public class RoomifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomifyApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			com.ROOMIFY.Roomify.repository.UserRepository userRepository,
			com.ROOMIFY.Roomify.repository.RoomRepository roomRepository,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
		return args -> {
			try {
				// 1. Cleanup rooms
				System.out.println("Roomify: Cleaning up rooms with no owner...");
				roomRepository.deleteRoomsWithNoOwner();
				
				// 2. Create Admin
				if (userRepository.findByEmail("raphaelfrank02@gmail.com").isEmpty()) {
					com.ROOMIFY.Roomify.model.User admin = new com.ROOMIFY.Roomify.model.User();
					admin.setName("Admin User");
					admin.setEmail("raphaelfrank02@gmail.com");
					admin.setPassword(passwordEncoder.encode("Raphael11111"));
					admin.setRole(com.ROOMIFY.Roomify.model.UserRole.ADMIN);
					admin.setEmailVerified(true);
					admin.setCreatedAt(java.time.LocalDateTime.now());
					userRepository.save(admin);
					System.out.println("Roomify: Admin user created: raphaelfrank02@gmail.com");
				}

				// 3. Create Super Admin
				if (userRepository.findByEmail("raphaelfrank01@gmail.com").isEmpty()) {
					com.ROOMIFY.Roomify.model.User superAdmin = new com.ROOMIFY.Roomify.model.User();
					superAdmin.setName("Super Admin");
					superAdmin.setEmail("raphaelfrank01@gmail.com");
					superAdmin.setPassword(passwordEncoder.encode("Raphael111111"));
					superAdmin.setRole(com.ROOMIFY.Roomify.model.UserRole.SUPER_ADMIN);
					superAdmin.setEmailVerified(true);
					superAdmin.setCreatedAt(java.time.LocalDateTime.now());
					userRepository.save(superAdmin);
					System.out.println("Roomify: Super Admin user created: raphaelfrank01@gmail.com");
				}
				
				System.out.println("Roomify: Data initialization complete.");
			} catch (Exception e) {
				System.err.println("Roomify: Initialization failed: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}
}
