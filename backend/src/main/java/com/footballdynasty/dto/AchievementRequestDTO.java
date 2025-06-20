package com.footballdynasty.dto;

import com.footballdynasty.entity.AchievementRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AchievementRequestDTO {
    
    private UUID id;
    
    @NotNull(message = "Achievement ID is required")
    private UUID achievementId;
    
    private String achievementDescription;
    private String achievementRarity;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String userDisplayName;
    private String teamId;
    private String teamName;
    
    @NotNull(message = "Request status is required")
    private AchievementRequest.RequestStatus status;
    
    private String requestReason;
    private String adminNotes;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional display fields
    private String statusDisplay;
    private String timeAgo;
    
    // Constructors
    public AchievementRequestDTO() {}
    
    public AchievementRequestDTO(UUID achievementId, String userId, String requestReason) {
        this.achievementId = achievementId;
        this.userId = userId;
        this.requestReason = requestReason;
        this.status = AchievementRequest.RequestStatus.PENDING;
    }
    
    // Helper method to generate display information
    public void generateDisplayInfo() {
        this.statusDisplay = generateStatusDisplay();
        this.timeAgo = generateTimeAgo();
    }
    
    private String generateStatusDisplay() {
        if (status == null) return "Unknown";
        
        switch (status) {
            case PENDING:
                return "Pending Review";
            case APPROVED:
                return "Approved";
            case REJECTED:
                return "Rejected";
            default:
                return status.toString();
        }
    }
    
    private String generateTimeAgo() {
        if (createdAt == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(createdAt, now).toDays();
        long hours = java.time.Duration.between(createdAt, now).toHours();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (days > 0) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (hours > 0) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (minutes > 0) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else {
            return "Just now";
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getAchievementId() { return achievementId; }
    public void setAchievementId(UUID achievementId) { this.achievementId = achievementId; }
    
    public String getAchievementDescription() { return achievementDescription; }
    public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
    
    public String getAchievementRarity() { return achievementRarity; }
    public void setAchievementRarity(String achievementRarity) { this.achievementRarity = achievementRarity; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public AchievementRequest.RequestStatus getStatus() { return status; }
    public void setStatus(AchievementRequest.RequestStatus status) { 
        this.status = status;
        generateDisplayInfo();
    }
    
    public String getRequestReason() { return requestReason; }
    public void setRequestReason(String requestReason) { this.requestReason = requestReason; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt;
        generateDisplayInfo();
    }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }
    
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
}