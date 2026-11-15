package com.ROOMIFY.Roomify.dto;

import com.ROOMIFY.Roomify.model.Review;
import com.ROOMIFY.Roomify.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponseDTO {

    private Long id;
    private Long roomId;
    private Long userId;
    private String userName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponseDTO() {}

    public static ReviewResponseDTO fromEntity(Review review, User user) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setRoomId(review.getRoomId());
        dto.setUserId(review.getUserId());
        // Resolve userName dynamically from User table in accordance with 3NF
        dto.setUserName(user != null ? user.getName() : review.getUserName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
