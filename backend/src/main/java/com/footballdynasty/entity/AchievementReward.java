package com.footballdynasty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievement_rewards")
public class AchievementReward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType type;
    
    @Column(name = "trait_name")
    private String traitName; // Only used for TRAIT_BOOST type
    
    @Column(name = "boost_amount")
    @Min(value = 1, message = "Boost amount must be positive")
    private Integer boostAmount; // Amount of boost for traits or game restarts
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public AchievementReward() {}
    
    public AchievementReward(Achievement achievement, RewardType type, String traitName, Integer boostAmount) {
        this.achievement = achievement;
        this.type = type;
        this.traitName = traitName;
        this.boostAmount = boostAmount;
    }
    
    // Enums
    public enum RewardType {
        TRAIT_BOOST,
        GAME_RESTART
    }
    
    public enum TraitType {
        // Basic Physical Traits
        SPEED("Speed"),
        STRENGTH("Strength"), 
        AGILITY("Agility"),
        ACCELERATION("Acceleration"),
        AWARENESS("Awareness"),
        TOUGHNESS("Toughness"),
        STAMINA("Stamina"),
        JUMPING("Jumping"),
        
        // Ball Carrier Traits
        BREAK_TACKLE("Break Tackle"),
        TRUCKING("Trucking"),
        CHANGE_OF_DIRECTION("Change of Direction"),
        BALL_CARRIER_VISION("Ball Carrier Vision"),
        STIFF_ARM("Stiff Arm"),
        SPIN_MOVE("Spin Move"),
        JUKE_MOVE("Juke Move"),
        CARRYING("Carrying"),
        
        // Quarterback Traits
        THROW_POWER("Throw Power"),
        SHORT_ACCURACY("Short Accuracy"),
        MEDIUM_ACCURACY("Medium Accuracy"),
        DEEP_ACCURACY("Deep Accuracy"),
        SCRAMBLING("Scrambling"),
        THROW_UNDER_PRESSURE("Throw Under Pressure"),
        BREAK_SACK("Break Sack"),
        PLAY_ACTION("Play Action"),
        
        // Receiving Traits
        CATCHING("Catching"),
        SHORT_ROUTE_RUNNING("Short Route Running"),
        MEDIUM_ROUTE_RUNNING("Medium Route Running"),
        DEEP_ROUTE_RUNNING("Deep Route Running"),
        CATCH_IN_TRAFFIC("Catch in Traffic"),
        SPECTACULAR_CATCH("Spectacular Catch"),
        RELEASE("Release"),
        
        // Blocking Traits
        PASS_BLOCK("Pass Block"),
        PASS_BLOCK_POWER("Pass Block Power"),
        PASS_BLOCK_FINESSE("Pass Block Finesse"),
        RUN_BLOCK("Run Block"),
        RUN_BLOCK_POWER("Run Block Power"),
        RUN_BLOCK_FINESSE("Run Block Finesse"),
        LEAD_BLOCK("Lead Block"),
        IMPACT_BLOCKING("Impact Blocking"),
        
        // Defensive Traits
        TACKLE("Tackle"),
        POWER_MOVES("Power Moves"),
        PASS_RUSH_MOVES("Pass Rush Moves"),
        FINESSE_MOVES("Finesse Moves"),
        BLOCK_SHEDDING("Block Shedding"),
        PURSUIT("Pursuit"),
        PLAY_RECOGNITION("Play Recognition"),
        MAN_COVERAGE("Man Coverage"),
        ZONE_COVERAGE("Zone Coverage"),
        PRESS("Press"),
        
        // Special Teams
        KICK_POWER("Kick Power"),
        KICK_ACCURACY("Kick Accuracy"),
        RETURN("Return");
        
        private final String displayName;
        
        TraitType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static TraitType fromString(String text) {
            for (TraitType trait : TraitType.values()) {
                if (trait.displayName.equalsIgnoreCase(text) || trait.name().equalsIgnoreCase(text)) {
                    return trait;
                }
            }
            throw new IllegalArgumentException("No trait found for: " + text);
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Achievement getAchievement() { return achievement; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }
    
    public RewardType getType() { return type; }
    public void setType(RewardType type) { this.type = type; }
    
    public String getTraitName() { return traitName; }
    public void setTraitName(String traitName) { this.traitName = traitName; }
    
    public Integer getBoostAmount() { return boostAmount; }
    public void setBoostAmount(Integer boostAmount) { this.boostAmount = boostAmount; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}