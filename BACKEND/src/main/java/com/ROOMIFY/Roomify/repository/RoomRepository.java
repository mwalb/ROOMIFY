package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // ==================== OWNER QUERIES ====================

    // Find rooms posted by a specific user (owner)
    List<Room> findByPostedBy(Long postedBy);

    // Find all available rooms
    List<Room> findByIsAvailableTrue();

    // Find all rooms by status
    List<Room> findByStatus(String status);

    // ==================== DALALI (AGENT) QUERIES ====================

    // Find properties managed by a specific dalali agent
    List<Room> findByDalaliId(Long dalaliId);

    // Find properties by dalali and status
    List<Room> findByDalaliIdAndStatus(Long dalaliId, String status);

    // Find pending properties for a dalali
    @Query("SELECT r FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'PENDING'")
    List<Room> findPendingPropertiesByDalali(@Param("dalaliId") Long dalaliId);

    // Find rented properties for a dalali
    @Query("SELECT r FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'RENTED'")
    List<Room> findRentedPropertiesByDalali(@Param("dalaliId") Long dalaliId);

    // Find available properties for a dalali
    @Query("SELECT r FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'AVAILABLE'")
    List<Room> findAvailablePropertiesByDalali(@Param("dalaliId") Long dalaliId);

    // Find rejected properties for a dalali
    @Query("SELECT r FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'REJECTED'")
    List<Room> findRejectedPropertiesByDalali(@Param("dalaliId") Long dalaliId);

    // ==================== STATISTICS QUERIES ====================

    // Count total properties by dalali
    @Query("SELECT COUNT(r) FROM Room r WHERE r.dalaliId = :dalaliId")
    int countByDalaliId(@Param("dalaliId") Long dalaliId);

    // Count active (available) properties by dalali
    @Query("SELECT COUNT(r) FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'AVAILABLE'")
    int countActiveByDalaliId(@Param("dalaliId") Long dalaliId);

    // Count pending properties by dalali
    @Query("SELECT COUNT(r) FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'PENDING'")
    int countPendingByDalaliId(@Param("dalaliId") Long dalaliId);

    // Count rented properties by dalali
    @Query("SELECT COUNT(r) FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'RENTED'")
    int countRentedByDalaliId(@Param("dalaliId") Long dalaliId);

    // Sum total commission earned by dalali
    @Query("SELECT COALESCE(SUM(r.commission), 0) FROM Room r WHERE r.dalaliId = :dalaliId AND r.status = 'RENTED'")
    Double sumCommissionByDalaliId(@Param("dalaliId") Long dalaliId);

    // Sum commission for current month
    @Query("SELECT COALESCE(SUM(r.commission), 0) FROM Room r WHERE r.dalaliId = :dalaliId " +
            "AND r.status = 'RENTED' AND FUNCTION('MONTH', r.rentedAt) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', r.rentedAt) = FUNCTION('YEAR', CURRENT_DATE)")
    Double sumMonthlyCommissionByDalaliId(@Param("dalaliId") Long dalaliId);

    // Sum total views for dalali's properties
    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM Room r WHERE r.dalaliId = :dalaliId")
    Integer sumViewsByDalaliId(@Param("dalaliId") Long dalaliId);

    // Sum total interested tenants for dalali's properties
    @Query("SELECT COALESCE(SUM(r.interestedCount), 0) FROM Room r WHERE r.dalaliId = :dalaliId")
    Integer sumInterestedByDalaliId(@Param("dalaliId") Long dalaliId);

    // REMOVE THIS METHOD - dalaliRating doesn't exist in Room entity
    // @Query("SELECT COALESCE(AVG(r.dalaliRating), 0) FROM Room r WHERE r.dalaliId = :dalaliId AND r.dalaliRating > 0")
    // Double getAverageRatingByDalaliId(@Param("dalaliId") Long dalaliId);

    // ==================== FEATURED & PROMOTED QUERIES ====================

    // Get featured properties
    List<Room> findByFeaturedTrue();

    // Get featured properties by dalali
    List<Room> findByDalaliIdAndFeaturedTrue(Long dalaliId);

    // Get promoted properties
    List<Room> findByPromotedTrue();

    // ==================== SEARCH QUERIES ====================

    // Search properties by title or location
    @Query("SELECT r FROM Room r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Room> searchByKeyword(@Param("keyword") String keyword);

    // Find properties by price range
    List<Room> findByPriceBetween(double minPrice, double maxPrice);

    // Find properties by location area
    @Query("SELECT r FROM Room r WHERE LOWER(r.address) LIKE LOWER(CONCAT('%', :area, '%'))")
    List<Room> findByLocationArea(@Param("area") String area);

    // ==================== UPDATE QUERIES ====================

    // Update property status
    @Modifying
    @Query("UPDATE Room r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :roomId")
    void updateRoomStatus(@Param("roomId") Long roomId, @Param("status") String status);

    // Increment view count
    @Modifying
    @Query("UPDATE Room r SET r.viewCount = r.viewCount + 1 WHERE r.id = :roomId")
    void incrementViewCount(@Param("roomId") Long roomId);

    // Increment interested count
    @Modifying
    @Query("UPDATE Room r SET r.interestedCount = r.interestedCount + 1 WHERE r.id = :roomId")
    void incrementInterestedCount(@Param("roomId") Long roomId);

    // ==================== VERIFICATION METHODS ====================

    // Check if room exists and is available
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r " +
            "WHERE r.id = :roomId AND r.status = 'AVAILABLE'")
    boolean isRoomAvailableForBooking(@Param("roomId") Long roomId);

    // Check if room belongs to dalali
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r " +
            "WHERE r.id = :roomId AND r.dalaliId = :dalaliId")
    boolean isRoomBelongsToDalali(@Param("roomId") Long roomId, @Param("dalaliId") Long dalaliId);
}