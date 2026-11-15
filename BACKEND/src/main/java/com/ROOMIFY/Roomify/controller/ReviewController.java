package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.dto.ReviewResponseDTO;
import com.ROOMIFY.Roomify.model.Review;
import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.repository.ReviewRepository;
import com.ROOMIFY.Roomify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private List<ReviewResponseDTO> toDTOList(List<Review> reviews) {
        if (reviews.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        return reviews.stream()
                .map(r -> ReviewResponseDTO.fromEntity(r, userMap.get(r.getUserId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getRoomReviews(@PathVariable Long roomId) {
        List<Review> reviews = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        return ResponseEntity.ok(new ApiResponse<>(true, toDTOList(reviews), "Reviews retrieved"));
    }

    @GetMapping("/room/{roomId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomReviewSummary(@PathVariable Long roomId) {
        List<Review> reviews = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", Math.round(avg * 10.0) / 10.0);
        summary.put("totalReviews", reviews.size());

        return ResponseEntity.ok(new ApiResponse<>(true, summary, "Summary retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(@RequestBody Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Rating must be between 1 and 5 stars"));
        }
        review.setCreatedAt(LocalDateTime.now());

        // Obey 3NF: Link with User entity
        User user = null;
        if (review.getUserId() != null) {
            user = userRepository.findById(review.getUserId()).orElse(null);
            if (user != null) {
                review.setUserName(user.getName());
            }
        }
        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(new ApiResponse<>(true, ReviewResponseDTO.fromEntity(saved, user), "Review submitted successfully"));
    }
}
