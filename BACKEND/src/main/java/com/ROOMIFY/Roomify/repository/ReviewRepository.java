package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<Review> findByUserId(Long userId);
}
