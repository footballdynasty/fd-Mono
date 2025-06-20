package com.footballdynasty.dto;

import com.footballdynasty.entity.Achievement;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AchievementDTO {
    
    private UUID id;
    
    @NotBlank(message = "Achievement description is required")
    private String description;
    
    @NotBlank(message = "Achievement reward is required")
    private String reward;
    
    private Long dateCompleted;
    
    @NotNull(message = "Achievement type is required")
    private Achievement.AchievementType type;
    
    @NotNull(message = "Achievement rarity is required")
    private Achievement.AchievementRarity rarity;
    
    private String icon;
    
    private String color;
    
    private Boolean isCompleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public AchievementDTO() {}
    
    // Constructor for creation
    public AchievementDTO(String description, String reward, Achievement.AchievementType type, Achievement.AchievementRarity rarity) {
        this.description = description;
        this.reward = reward;
        this.type = type;
        this.rarity = rarity;
        this.isCompleted = false;
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
    
    public Achievement.AchievementType getType() { return type; }
    public void setType(Achievement.AchievementType type) { this.type = type; }
    
    public Achievement.AchievementRarity getRarity() { return rarity; }
    public void setRarity(Achievement.AchievementRarity rarity) { this.rarity = rarity; }
    
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