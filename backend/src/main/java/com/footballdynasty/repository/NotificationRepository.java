package com.footballdynasty.repository;

import com.footballdynasty.entity.Notification;
import com.footballdynasty.entity.Notification.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Find notifications for a specific user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find unread notifications for a specific user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Find notifications for a user with pagination
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find unread notifications for a user with pagination
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Count unread notifications for a user
    long countByUserIdAndIsReadFalse(Long userId);

    // Count total notifications for a user
    long countByUserId(Long userId);

    // Count notifications by type for a user
    long countByUserIdAndType(Long userId, NotificationType type);

    // Count unread notifications by type for a user
    long countByUserIdAndTypeAndIsReadFalse(Long userId, NotificationType type);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // Delete old read notifications (older than specified days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("userId") Long userId, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    // Find notifications by type for all users (admin use)
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    // Find recent notifications for admin overview
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findRecentByType(@Param("type") NotificationType type, Pageable pageable);
}