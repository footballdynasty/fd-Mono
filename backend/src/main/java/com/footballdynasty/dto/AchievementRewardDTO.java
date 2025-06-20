package com.footballdynasty.dto;

import com.footballdynasty.entity.AchievementReward;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AchievementRewardDTO {
    
    private UUID id;
    
    @NotNull(message = "Reward type is required")
    private AchievementReward.RewardType type;
    
    private String traitName; // Only used for TRAIT_BOOST type
    
    @Min(value = 1, message = "Boost amount must be positive")
    private Integer boostAmount;
    
    private Boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for frontend display
    private String displayName;
    private String description;
    private String category; // basic, intermediate, advanced, elite
    
    // Constructors
    public AchievementRewardDTO() {}
    
    public AchievementRewardDTO(AchievementReward.RewardType type, String traitName, Integer boostAmount) {
        this.type = type;
        this.traitName = traitName;
        this.boostAmount = boostAmount;
        this.generateDisplayInfo();
    }
    
    /**
     * Generate user-friendly display information
     */
    public void generateDisplayInfo() {
        if (type == AchievementReward.RewardType.GAME_RESTART) {
            this.displayName = "Game Restart";
            this.description = "+" + (boostAmount != null ? boostAmount : 0) + " game restart" + (boostAmount != null && boostAmount > 1 ? "s" : "");
            this.category = "special";
        } else if (type == AchievementReward.RewardType.TRAIT_BOOST && traitName != null) {
            this.displayName = traitName;
            this.description = "+" + (boostAmount != null ? boostAmount : 0) + " " + traitName;
            this.category = determineTraitCategory(traitName);
        }
    }
    
    private String determineTraitCategory(String trait) {
        // Basic traits
        if (isBasicTrait(trait)) return "basic";
        // Elite traits
        if (isEliteTrait(trait)) return "elite";
        // Advanced traits
        if (isAdvancedTrait(trait)) return "advanced";
        // Default to intermediate
        return "intermediate";
    }
    
    private boolean isBasicTrait(String trait) {
        return trait.equals("Speed") || trait.equals("Stamina") || trait.equals("Carrying") || 
               trait.equals("Catching") || trait.equals("Tackle");
    }
    
    private boolean isEliteTrait(String trait) {
        return trait.equals("Deep Accuracy") || trait.equals("Spectacular Catch") || 
               trait.equals("Break Sack") || trait.equals("Finesse Moves") || 
               trait.equals("Press Coverage") || trait.equals("Return");
    }
    
    private boolean isAdvancedTrait(String trait) {
        return trait.equals("Medium Accuracy") || trait.equals("Throw Power") || 
               trait.equals("Pass Rush Moves") || trait.equals("Block Shedding");
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public AchievementReward.RewardType getType() { return type; }
    public void setType(AchievementReward.RewardType type) { 
        this.type = type;
        generateDisplayInfo();
    }
    
    public String getTraitName() { return traitName; }
    public void setTraitName(String traitName) { 
        this.traitName = traitName;
        generateDisplayInfo();
    }
    
    public Integer getBoostAmount() { return boostAmount; }
    public void setBoostAmount(Integer boostAmount) { 
        this.boostAmount = boostAmount;
        generateDisplayInfo();
    }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}