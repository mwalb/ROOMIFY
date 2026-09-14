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
	public CommandLineRunner cleanupRooms(RoomRepository roomRepository) {
		return args -> {
			try {
				System.out.println("Cleaning up rooms with no owner...");
				roomRepository.deleteRoomsWithNoOwner();
				System.out.println("Cleanup complete.");
			} catch (Exception e) {
				System.err.println("Cleanup failed: " + e.getMessage());
			}
		};
	}

}
