package com.footballdynasty.repository;

import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AchievementRequestRepository extends JpaRepository<AchievementRequest, UUID> {
    
    /**
     * Find all pending requests
     */
    List<AchievementRequest> findByStatusOrderByCreatedAtDesc(AchievementRequest.RequestStatus status);
    
    /**
     * Find pending requests with pagination
     */
    Page<AchievementRequest> findByStatusOrderByCreatedAtDesc(AchievementRequest.RequestStatus status, Pageable pageable);
    
    /**
     * Find requests by user ID
     */
    List<AchievementRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find requests by team ID
     */
    List<AchievementRequest> findByTeamIdOrderByCreatedAtDesc(String teamId);
    
    /**
     * Find requests by achievement
     */
    List<AchievementRequest> findByAchievementOrderByCreatedAtDesc(Achievement achievement);
    
    /**
     * Check if user already has a pending request for this achievement
     */
    @Query("SELECT ar FROM AchievementRequest ar WHERE ar.userId = :userId AND ar.achievement = :achievement AND ar.status = 'PENDING'")
    Optional<AchievementRequest> findPendingRequestByUserAndAchievement(@Param("userId") String userId, @Param("achievement") Achievement achievement);
    
    /**
     * Count pending requests
     */
    long countByStatus(AchievementRequest.RequestStatus status);
    
    /**
     * Count requests by date range
     */
    @Query("SELECT COUNT(ar) FROM AchievementRequest ar WHERE ar.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find requests by reviewer
     */
    List<AchievementRequest> findByReviewedByOrderByReviewedAtDesc(String reviewedBy);
    
    /**
     * Find recent requests (last 30 days)
     */
    @Query("SELECT ar FROM AchievementRequest ar WHERE ar.createdAt >= :since ORDER BY ar.createdAt DESC")
    List<AchievementRequest> findRecentRequests(@Param("since") LocalDateTime since);
    
    /**
     * Get request statistics
     */
    @Query("SELECT ar.status, COUNT(ar) FROM AchievementRequest ar GROUP BY ar.status")
    List<Object[]> getRequestStatistics();
}