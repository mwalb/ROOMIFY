package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Favorite;
import com.ROOMIFY.Roomify.model.Room;
import com.ROOMIFY.Roomify.repository.FavoriteRepository;
import com.ROOMIFY.Roomify.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<Room>>> getUserFavorites(@PathVariable Long userId) {
        try {
            List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<Room> rooms = new ArrayList<>();
            for (Favorite fav : favorites) {
                roomRepository.findById(fav.getRoomId()).ifPresent(rooms::add);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, rooms, "Favorites retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, new ArrayList<>(), "Failed to fetch favorites: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/{roomId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@PathVariable Long userId, @PathVariable Long roomId) {
        try {
            boolean exists = favoriteRepository.existsByUserIdAndRoomId(userId, roomId);
            return ResponseEntity.ok(new ApiResponse<>(true, exists, "Status retrieved"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, false, "Error checking favorite: " + e.getMessage()));
        }
    }

    @PostMapping("/{userId}/{roomId}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(@PathVariable Long userId, @PathVariable Long roomId) {
        try {
            Optional<Favorite> existing = favoriteRepository.findByUserIdAndRoomId(userId, roomId);
            if (existing.isPresent()) {
                favoriteRepository.deleteByUserIdAndRoomId(userId, roomId);
                return ResponseEntity.ok(new ApiResponse<>(true, false, "Removed from favorites"));
            } else {
                Favorite fav = new Favorite(userId, roomId);
                favoriteRepository.save(fav);

                // Increment interested count on room as positive feedback
                roomRepository.findById(roomId).ifPresent(room -> {
                    room.setInterestedCount(room.getInterestedCount() + 1);
                    roomRepository.save(room);
                });

                return ResponseEntity.ok(new ApiResponse<>(true, true, "Added to favorites"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, false, "Failed to toggle favorite: " + e.getMessage()));
        }
    }
}
