package com.footballdynasty.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievement_requests")
public class AchievementRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId; // User who requested the achievement
    
    @Column(name = "user_display_name")
    private String userDisplayName; // Display name for easier identification
    
    @Column(name = "team_id")
    private String teamId; // Team context if applicable
    
    @Column(name = "team_name")
    private String teamName; // Team name for easier identification
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason; // User's reason/evidence for completion
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // Admin's notes when approving/rejecting
    
    @Column(name = "reviewed_by")
    private String reviewedBy; // Admin who reviewed the request
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
    
    // Constructors
    public AchievementRequest() {}
    
    public AchievementRequest(Achievement achievement, String userId, String userDisplayName, 
                            String teamId, String teamName, String requestReason) {
        this.achievement = achievement;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.requestReason = requestReason;
        this.status = RequestStatus.PENDING;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Achievement getAchievement() { return achievement; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getRequestReason() { return requestReason; }
    public void setRequestReason(String requestReason) { this.requestReason = requestReason; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}