package com.ROOMIFY.Roomify.repository;

import com.ROOMIFY.Roomify.model.User;
import com.ROOMIFY.Roomify.model.UserRole;
import com.ROOMIFY.Roomify.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DalaliRepository extends JpaRepository<User, Long> {

    // Find all dalali agents
    List<User> findByRole(UserRole role);

    // Find verified dalali agents
    @Query("SELECT u FROM User u WHERE u.role = 'DALALI' AND u.verificationStatus = 'VERIFIED'")
    List<User> findVerifiedDalali();

    // Find pending dalali agents (awaiting verification)
    @Query("SELECT u FROM User u WHERE u.role = 'DALALI' AND u.verificationStatus = 'PENDING'")
    List<User> findPendingDalali();

    // Find dalali by location area
    @Query("SELECT u FROM User u WHERE u.role = 'DALALI' AND u.locationArea LIKE %:area%")
    List<User> findDalaliByLocation(@Param("area") String area);

    // Count total dalali agents
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'DALALI'")
    int countTotalDalali();

    // Count verified dalali agents
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'DALALI' AND u.verificationStatus = 'VERIFIED'")
    int countVerifiedDalali();

    // Find top performing dalali by commission
    @Query("SELECT u FROM User u WHERE u.role = 'DALALI' ORDER BY u.totalCommission DESC")
    List<User> findTopDalaliByCommission();

    // Find dalali by verification status
    List<User> findByRoleAndVerificationStatus(UserRole role, VerificationStatus status);

    // Search dalali by name or business name
    @Query("SELECT u FROM User u WHERE u.role = 'DALALI' AND " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.businessName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchDalali(@Param("query") String query);
}