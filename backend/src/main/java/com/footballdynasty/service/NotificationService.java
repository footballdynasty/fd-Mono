package com.footballdynasty.service;

import com.footballdynasty.entity.Notification;
import com.footballdynasty.entity.Notification.NotificationType;
import com.footballdynasty.entity.User;
import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementRequest;
import com.footballdynasty.repository.NotificationRepository;
import com.footballdynasty.repository.UserRepository;
import com.footballdynasty.repository.AchievementRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AchievementRequestRepository achievementRequestRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                             UserRepository userRepository,
                             AchievementRequestRepository achievementRequestRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.achievementRequestRepository = achievementRequestRepository;
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(Long userId, boolean unreadOnly, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        } else {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
    }

    /**
     * Get notification statistics for a user
     */
    public Map<String, Object> getUserNotificationStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        long total = notificationRepository.countByUserId(userId);
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        
        stats.put("total", total);
        stats.put("unread", unread);
        
        // Count by type
        Map<String, Long> byType = new HashMap<>();
        for (NotificationType type : NotificationType.values()) {
            long count = notificationRepository.countByUserIdAndType(userId, type);
            byType.put(type.name(), count);
        }
        stats.put("byType", byType);
        
        return stats;
    }

    /**
     * Get inbox count for navbar badge (includes notifications and admin requests)
     */
    public Map<String, Object> getInboxCount(Long userId, boolean isAdmin) {
        Map<String, Object> result = new HashMap<>();
        
        long notifications = notificationRepository.countByUserIdAndIsReadFalse(userId);
        long achievementRequests = 0;
        
        if (isAdmin) {
            achievementRequests = achievementRequestRepository.countByStatus(AchievementRequest.RequestStatus.PENDING);
        }
        
        result.put("notifications", notifications);
        result.put("achievementRequests", achievementRequests);
        result.put("total", notifications + achievementRequests);
        result.put("isAdmin", isAdmin);
        
        return result;
    }

    /**
     * Mark a notification as read
     */
    public void markAsRead(UUID notificationId, Long userId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        
        if (notification.isPresent() && notification.get().getUserId().equals(userId)) {
            notification.get().setRead(true);
            notificationRepository.save(notification.get());
            logger.debug("Marked notification {} as read for user {}", notificationId, userId);
        } else {
            logger.warn("Attempted to mark non-existent or unauthorized notification {} as read for user {}", 
                       notificationId, userId);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId);
        logger.debug("Marked {} notifications as read for user {}", updated, userId);
        return updated;
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(UUID notificationId, Long userId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        
        if (notification.isPresent() && notification.get().getUserId().equals(userId)) {
            notificationRepository.delete(notification.get());
            logger.debug("Deleted notification {} for user {}", notificationId, userId);
        } else {
            logger.warn("Attempted to delete non-existent or unauthorized notification {} for user {}", 
                       notificationId, userId);
        }
    }

    /**
     * Create an achievement request notification for admins
     */
    public void createAchievementRequestNotification(AchievementRequest request, Achievement achievement) {
        // Get all admin users - for now, we'll need to get all users and filter by role
        List<User> allUsers = userRepository.findAll();
        List<User> admins = allUsers.stream()
            .filter(user -> user.getRoles().contains(com.footballdynasty.entity.Role.COMMISSIONER))
            .toList();
        
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setType(NotificationType.ACHIEVEMENT_REQUEST);
            notification.setTitle("New Achievement Request");
            notification.setMessage(String.format("%s has requested completion of '%s'", 
                                                 request.getUserDisplayName(), achievement.getDescription()));
            notification.setUserId(admin.getId());
            
            // Add request data
            Map<String, String> data = new HashMap<>();
            data.put("requestId", request.getId().toString());
            data.put("achievementId", achievement.getId().toString());
            data.put("achievementName", achievement.getDescription());
            data.put("userId", request.getUserId());
            data.put("userName", request.getUserDisplayName());
            data.put("teamName", request.getTeamName());
            data.put("url", "/admin/inbox");
            notification.setData(data);
            
            notificationRepository.save(notification);
        }
        
        logger.info("Created achievement request notifications for {} admins", admins.size());
    }

    /**
     * Create an achievement completed notification
     */
    public void createAchievementCompletedNotification(Long userId, Achievement achievement) {
        Notification notification = new Notification();
        notification.setType(NotificationType.ACHIEVEMENT_COMPLETED);
        notification.setTitle("Achievement Completed!");
        notification.setMessage(String.format("You've completed the achievement '%s'", achievement.getDescription()));
        notification.setUserId(userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("achievementId", achievement.getId().toString());
        data.put("achievementName", achievement.getDescription());
        data.put("url", "/achievements");
        notification.setData(data);
        
        notificationRepository.save(notification);
        logger.info("Created achievement completed notification for user {}", userId);
    }

    /**
     * Create an achievement approved notification
     */
    public void createAchievementApprovedNotification(AchievementRequest request, Achievement achievement) {
        // Look up the user by the identifier stored in the request
        Optional<User> userOpt = userRepository.findByUsername(request.getUserId());
        if (!userOpt.isPresent()) {
            logger.error("Cannot create approved notification: User not found with identifier {}", request.getUserId());
            return;
        }
        
        User user = userOpt.get();
        
        Notification notification = new Notification();
        notification.setType(NotificationType.ACHIEVEMENT_APPROVED);
        notification.setTitle("Achievement Request Approved!");
        notification.setMessage(String.format("Your request for '%s' has been approved", achievement.getDescription()));
        notification.setUserId(user.getId());
        
        Map<String, String> data = new HashMap<>();
        data.put("requestId", request.getId().toString());
        data.put("achievementId", achievement.getId().toString());
        data.put("achievementName", achievement.getDescription());
        data.put("url", "/achievements");
        notification.setData(data);
        
        notificationRepository.save(notification);
        logger.info("Created achievement approved notification for user {} (ID: {})", request.getUserId(), user.getId());
    }

    /**
     * Create an achievement rejected notification
     */
    public void createAchievementRejectedNotification(AchievementRequest request, Achievement achievement) {
        // Look up the user by the identifier stored in the request
        Optional<User> userOpt = userRepository.findByUsername(request.getUserId());
        if (!userOpt.isPresent()) {
            logger.error("Cannot create rejected notification: User not found with identifier {}", request.getUserId());
            return;
        }
        
        User user = userOpt.get();
        
        Notification notification = new Notification();
        notification.setType(NotificationType.ACHIEVEMENT_REJECTED);
        notification.setTitle("Achievement Request Rejected");
        notification.setMessage(String.format("Your request for '%s' has been rejected", achievement.getDescription()));
        notification.setUserId(user.getId());
        
        Map<String, String> data = new HashMap<>();
        data.put("requestId", request.getId().toString());
        data.put("achievementId", achievement.getId().toString());
        data.put("achievementName", achievement.getDescription());
        data.put("url", "/achievements");
        notification.setData(data);
        
        notificationRepository.save(notification);
        logger.info("Created achievement rejected notification for user {} (ID: {})", request.getUserId(), user.getId());
    }

    /**
     * Clean up old read notifications (called periodically)
     */
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        List<User> users = userRepository.findAll();
        int totalDeleted = 0;
        
        for (User user : users) {
            int deleted = notificationRepository.deleteOldReadNotifications(user.getId(), cutoffDate);
            totalDeleted += deleted;
        }
        
        logger.info("Cleaned up {} old read notifications older than {} days", totalDeleted, daysToKeep);
    }
}