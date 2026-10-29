package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndRoomId(Long userId, Long roomId);
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Transactional
    void deleteByUserIdAndRoomId(Long userId, Long roomId);
}
