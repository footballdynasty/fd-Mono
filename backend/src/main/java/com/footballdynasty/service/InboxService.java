package com.footballdynasty.service;

import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementRequest;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.repository.AchievementRepository;
import com.footballdynasty.repository.AchievementRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class InboxService {
    
    private static final Logger logger = LoggerFactory.getLogger(InboxService.class);
    
    private final AchievementRequestRepository requestRepository;
    private final AchievementRepository achievementRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public InboxService(AchievementRequestRepository requestRepository,
                       AchievementRepository achievementRepository,
                       NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.achievementRepository = achievementRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Submit an achievement completion request
     */
    public AchievementRequest submitAchievementRequest(UUID achievementId, String userId, 
                                                     String userDisplayName, String teamId, 
                                                     String teamName, String requestReason) {
        logger.info("Processing achievement request for user: {} achievement: {}", userId, achievementId);
        
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + achievementId));
        
        // Check if user already has a pending request for this achievement
        Optional<AchievementRequest> existingRequest = requestRepository
                .findPendingRequestByUserAndAchievement(userId, achievement);
        
        if (existingRequest.isPresent()) {
            throw new IllegalStateException("You already have a pending request for this achievement");
        }
        
        // Check if achievement is already completed
        if (achievement.getIsCompleted()) {
            throw new IllegalStateException("This achievement has already been completed");
        }
        
        AchievementRequest request = new AchievementRequest(
            achievement, userId, userDisplayName, teamId, teamName, requestReason
        );
        
        AchievementRequest savedRequest = requestRepository.save(request);
        logger.info("Achievement request created: {} for user: {} achievement: {}", 
                   savedRequest.getId(), userId, achievement.getDescription());
        
        // Create notification for admins
        notificationService.createAchievementRequestNotification(savedRequest, achievement);
        
        return savedRequest;
    }
    
    /**
     * Get all pending requests with pagination
     */
    @Transactional(readOnly = true)
    public Page<AchievementRequest> getPendingRequests(Pageable pageable) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(AchievementRequest.RequestStatus.PENDING, pageable);
    }
    
    /**
     * Get all pending requests
     */
    @Transactional(readOnly = true)
    public List<AchievementRequest> getAllPendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(AchievementRequest.RequestStatus.PENDING);
    }
    
    /**
     * Get requests by user
     */
    @Transactional(readOnly = true)
    public List<AchievementRequest> getRequestsByUser(String userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get requests by team
     */
    @Transactional(readOnly = true)
    public List<AchievementRequest> getRequestsByTeam(String teamId) {
        return requestRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }
    
    /**
     * Approve an achievement request
     */
    public AchievementRequest approveRequest(UUID requestId, String adminUserId, String adminNotes) {
        logger.info("Approving achievement request: {} by admin: {}", requestId, adminUserId);
        
        AchievementRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement request not found with ID: " + requestId));
        
        if (request.getStatus() != AchievementRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Can only approve pending requests");
        }
        
        // Update request status
        request.setStatus(AchievementRequest.RequestStatus.APPROVED);
        request.setReviewedBy(adminUserId);
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminNotes(adminNotes);
        
        // Complete the achievement
        Achievement achievement = request.getAchievement();
        achievement.setIsCompleted(true);
        achievement.setDateCompleted(System.currentTimeMillis());
        
        achievementRepository.save(achievement);
        AchievementRequest savedRequest = requestRepository.save(request);
        
        logger.info("Achievement request approved: {} for achievement: {} by admin: {}", 
                   requestId, achievement.getDescription(), adminUserId);
        
        // Create notification for user
        notificationService.createAchievementApprovedNotification(savedRequest, achievement);
        
        return savedRequest;
    }
    
    /**
     * Reject an achievement request
     */
    public AchievementRequest rejectRequest(UUID requestId, String adminUserId, String adminNotes) {
        logger.info("Rejecting achievement request: {} by admin: {}", requestId, adminUserId);
        
        AchievementRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement request not found with ID: " + requestId));
        
        if (request.getStatus() != AchievementRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending requests");
        }
        
        request.setStatus(AchievementRequest.RequestStatus.REJECTED);
        request.setReviewedBy(adminUserId);
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminNotes(adminNotes);
        
        AchievementRequest savedRequest = requestRepository.save(request);
        
        logger.info("Achievement request rejected: {} for achievement: {} by admin: {}", 
                   requestId, request.getAchievement().getDescription(), adminUserId);
        
        // Create notification for user
        notificationService.createAchievementRejectedNotification(savedRequest, request.getAchievement());
        
        return savedRequest;
    }
    
    /**
     * Get request by ID
     */
    @Transactional(readOnly = true)
    public AchievementRequest getRequestById(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement request not found with ID: " + requestId));
    }
    
    /**
     * Get inbox statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInboxStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long pendingCount = requestRepository.countByStatus(AchievementRequest.RequestStatus.PENDING);
        long approvedCount = requestRepository.countByStatus(AchievementRequest.RequestStatus.APPROVED);
        long rejectedCount = requestRepository.countByStatus(AchievementRequest.RequestStatus.REJECTED);
        
        stats.put("pendingRequests", pendingCount);
        stats.put("approvedRequests", approvedCount);
        stats.put("rejectedRequests", rejectedCount);
        stats.put("totalRequests", pendingCount + approvedCount + rejectedCount);
        
        // Recent activity (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentRequests = requestRepository.countByDateRange(weekAgo, LocalDateTime.now());
        stats.put("recentRequests", recentRequests);
        
        return stats;
    }
    
    /**
     * Delete a request (admin only)
     */
    public void deleteRequest(UUID requestId) {
        logger.info("Deleting achievement request: {}", requestId);
        
        AchievementRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement request not found with ID: " + requestId));
        
        requestRepository.delete(request);
        logger.info("Achievement request deleted: {}", requestId);
    }
}