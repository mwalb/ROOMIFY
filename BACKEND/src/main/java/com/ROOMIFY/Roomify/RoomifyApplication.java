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
				
				System.out.println("Roomify: Application startup cleanup complete.");
			} catch (Exception e) {
				System.err.println("Roomify: Startup cleanup failed: " + e.getMessage());
				e.printStackTrace();
			}

		};
	}
}
