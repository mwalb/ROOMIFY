package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ========== ORIGINAL METHODS (UNCHANGED) ==========

    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.room.id = :roomId")
    List<Booking> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT b FROM Booking b WHERE b.room.postedBy = :ownerId OR b.room.dalaliId = :ownerId")
    List<Booking> findByOwnerOrDalali(@Param("ownerId") Long ownerId);

    List<Booking> findByRoomId(Long roomId);

    // ========== ORIGINAL CONSISTENCY METHODS ==========

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId")
    boolean existsAnyBookingByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId AND (b.status = 'PENDING' OR b.status = 'ACCEPTED' OR b.status = 'CONFIRMED')")
    boolean existsBlockingBookingByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId AND (b.status = 'ACCEPTED' OR b.status = 'CONFIRMED')")
    boolean existsActiveBookingByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId")
    int countAllBookingsByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId AND b.status = :status")
    int countBookingsByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") String status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId AND b.user.id != :userId")
    boolean existsOtherUserBooking(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(b.facilitationFee), 0) FROM Booking b WHERE b.dalaliId = :dalaliId AND (b.status = 'ACCEPTED' OR b.status = 'CONFIRMED')")
    Double sumFacilitationFeesByDalaliId(@Param("dalaliId") Long dalaliId);

    // ========== ANALYTICS METHODS (NATIVE QUERIES - MOST RELIABLE) ==========

    @Query(value = "SELECT " +
            "CASE " +
            "   WHEN r.price < 50 THEN 'Under $50' " +
            "   WHEN r.price BETWEEN 50 AND 100 THEN '$50 - $100' " +
            "   WHEN r.price BETWEEN 101 AND 200 THEN '$101 - $200' " +
            "   WHEN r.price BETWEEN 201 AND 500 THEN '$201 - $500' " +
            "   ELSE 'Over $500' " +
            "END as price_range, " +
            "COUNT(b.id), AVG(r.price) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "GROUP BY price_range " +
            "ORDER BY COUNT(b.id) DESC",
            nativeQuery = true)
    List<Object[]> getBookingCountsByPriceRange();

    @Query(value = "SELECT " +
            "SPLIT_PART(r.address, ',', 1) as area, " +
            "COUNT(b.id), AVG(r.price), " +
            "(COUNT(b.id) * 100.0 / NULLIF((SELECT COUNT(*) FROM bookings WHERE status IN ('ACCEPTED', 'CONFIRMED')), 0)) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "AND r.address IS NOT NULL AND r.address != '' " +
            "GROUP BY area " +
            "ORDER BY COUNT(b.id) DESC",
            nativeQuery = true)
    List<Object[]> getBookingCountsByArea();

    @Query(value = "SELECT " +
            "TO_CHAR(b.created_at, 'YYYY-MM') as month, " +
            "COUNT(b.id), COALESCE(SUM(r.price), 0) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "AND b.created_at >= (CURRENT_TIMESTAMP - INTERVAL '12 months') " +
            "GROUP BY TO_CHAR(b.created_at, 'YYYY-MM') " +
            "ORDER BY month DESC",
            nativeQuery = true)
    List<Object[]> getMonthlyBookingTrends();

    @Query(value = "SELECT " +
            "r.price, COUNT(b.id) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "GROUP BY r.price " +
            "ORDER BY COUNT(b.id) DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> getMostFrequentExactPrices();

    @Query(value = "SELECT " +
            "r.title, r.id, COUNT(b.id), COALESCE(SUM(r.price), 0) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "GROUP BY r.id, r.title " +
            "ORDER BY SUM(r.price) DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> getTopRevenueRooms();

    @Query(value = "SELECT " +
            "TO_CHAR(b.created_at, 'Month') as month, " +
            "COALESCE(SUM(r.price), 0) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "AND EXTRACT(YEAR FROM b.created_at) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(MONTH FROM b.created_at), TO_CHAR(b.created_at, 'Month') " +
            "ORDER BY EXTRACT(MONTH FROM b.created_at)",
            nativeQuery = true)
    List<Object[]> getMonthlyRevenueCurrentYear();

    @Query(value = "SELECT " +
            "b.status, COUNT(*) " +
            "FROM bookings b " +
            "GROUP BY b.status",
            nativeQuery = true)
    List<Object[]> getBookingStatusDistribution();

    @Query(value = "SELECT " +
            "AVG(EXTRACT(EPOCH FROM (b.updated_at - b.created_at)) / 3600.0) " +
            "FROM bookings b " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED')",
            nativeQuery = true)
    Double getAverageApprovalTimeHours();

    @Query(value = "SELECT " +
            "u.name, u.email, COUNT(b.id) " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "GROUP BY u.id, u.name, u.email " +
            "ORDER BY COUNT(b.id) DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> getTopActiveUsers();

    @Query(value = "SELECT " +
            "TO_CHAR(b.created_at, 'Day') as day_of_week, " +
            "COUNT(b.id) " +
            "FROM bookings b " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "GROUP BY EXTRACT(DOW FROM b.created_at), TO_CHAR(b.created_at, 'Day') " +
            "ORDER BY COUNT(b.id) DESC",
            nativeQuery = true)
    List<Object[]> getBookingsByDayOfWeek();

    @Query(value = "SELECT " +
            "(SELECT COUNT(*) FROM bookings WHERE status = 'CANCELLED') * 100.0 / NULLIF(COUNT(*), 0) " +
            "FROM bookings",
            nativeQuery = true)
    Double getCancellationRate();

    @Query(value = "SELECT " +
            "SPLIT_PART(r.address, ',', 1) as area, " +
            "AVG(r.price), COUNT(b.id) " +
            "FROM bookings b " +
            "JOIN rooms r ON b.room_id = r.id " +
            "WHERE b.status IN ('ACCEPTED', 'CONFIRMED') " +
            "AND r.address IS NOT NULL AND r.address != '' " +
            "GROUP BY area " +
            "ORDER BY AVG(r.price) DESC",
            nativeQuery = true)
    List<Object[]> getAveragePriceByArea();

    @Query(value = "SELECT b.status, COUNT(*) FROM bookings b WHERE b.room_id = :roomId GROUP BY b.status", nativeQuery = true)
    List<Object[]> getBookingStatusCountsByRoom(@Param("roomId") Long roomId);

    @Query(value = "SELECT * FROM bookings b WHERE b.created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<Booking> findBookingsBetweenDatesNative(@Param("startDate") String startDate, @Param("endDate") String endDate);
}