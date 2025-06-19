package com.footballdynasty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    @NotBlank(message = "Achievement description is required")
    private String description;
    
    @Column(nullable = false)
    @NotBlank(message = "Achievement reward is required")
    private String reward;
    
    @Column(name = "date_completed")
    private Long dateCompleted;
    
    @Enumerated(EnumType.STRING)
    private AchievementType type = AchievementType.GENERAL;
    
    @Enumerated(EnumType.STRING)
    private AchievementRarity rarity = AchievementRarity.COMMON;
    
    private String icon;
    
    private String color;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AchievementType {
        WINS, SEASON, CHAMPIONSHIP, STATISTICS, GENERAL
    }

    public enum AchievementRarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }

    public Achievement() {}

    public Achievement(String description, String reward, AchievementType type) {
        this.description = description;
        this.reward = reward;
        this.type = type;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
    
    public Long getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(Long dateCompleted) { this.dateCompleted = dateCompleted; }
    
    public AchievementType getType() { return type; }
    public void setType(AchievementType type) { this.type = type; }
    
    public AchievementRarity getRarity() { return rarity; }
    public void setRarity(AchievementRarity rarity) { this.rarity = rarity; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}