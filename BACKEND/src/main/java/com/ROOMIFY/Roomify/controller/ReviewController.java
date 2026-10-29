package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.dto.ApiResponse;
import com.ROOMIFY.Roomify.model.Review;
import com.ROOMIFY.Roomify.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<Review>>> getRoomReviews(@PathVariable Long roomId) {
        List<Review> reviews = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        return ResponseEntity.ok(new ApiResponse<>(true, reviews, "Reviews retrieved"));
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
    public ResponseEntity<ApiResponse<Review>> addReview(@RequestBody Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Rating must be between 1 and 5 stars"));
        }
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(new ApiResponse<>(true, saved, "Review submitted successfully"));
    }
}
