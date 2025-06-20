package com.footballdynasty.config;

import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementReward;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Configurable achievement reward system
 * Allows easy modification of reward mappings via application properties
 */
@Configuration
@ConfigurationProperties(prefix = "achievement.rewards")
public class AchievementRewardConfig {
    
    /**
     * Default reward configurations by rarity level
     */
    private Map<String, RarityRewards> defaults = new HashMap<>();
    
    /**
     * Specific achievement overrides (achievement description -> custom rewards)
     */
    private Map<String, List<RewardDefinition>> specific = new HashMap<>();
    
    /**
     * Trait categorization for easier configuration
     */
    private TraitCategories traits = new TraitCategories();
    
    public AchievementRewardConfig() {
        initializeDefaults();
        initializeTraitCategories();
    }
    
    private void initializeDefaults() {
        // COMMON Achievement Rewards
        RarityRewards common = new RarityRewards();
        common.setGameRestarts(0);
        common.getTraitOptions().add(new TraitReward("Speed", 1));
        common.getTraitOptions().add(new TraitReward("Stamina", 1));
        common.getTraitOptions().add(new TraitReward("Carrying", 1));
        common.getTraitOptions().add(new TraitReward("Catching", 1));
        common.getTraitOptions().add(new TraitReward("Tackle", 1));
        defaults.put("COMMON", common);
        
        // UNCOMMON Achievement Rewards
        RarityRewards uncommon = new RarityRewards();
        uncommon.setGameRestarts(0);
        uncommon.getTraitOptions().add(new TraitReward("Strength", 2));
        uncommon.getTraitOptions().add(new TraitReward("Agility", 2));
        uncommon.getTraitOptions().add(new TraitReward("Short Route Running", 1));
        uncommon.getTraitOptions().add(new TraitReward("Short Accuracy", 1));
        uncommon.getTraitOptions().add(new TraitReward("Run Block", 1));
        uncommon.getTraitOptions().add(new TraitReward("Man Coverage", 1));
        defaults.put("UNCOMMON", uncommon);
        
        // RARE Achievement Rewards
        RarityRewards rare = new RarityRewards();
        rare.setGameRestarts(0);
        rare.getTraitOptions().add(new TraitReward("Acceleration", 2));
        rare.getTraitOptions().add(new TraitReward("Break Tackle", 2));
        rare.getTraitOptions().add(new TraitReward("Medium Accuracy", 1));
        rare.getTraitOptions().add(new TraitReward("Medium Route Running", 1));
        rare.getTraitOptions().add(new TraitReward("Pass Block", 1));
        rare.getTraitOptions().add(new TraitReward("Zone Coverage", 1));
        rare.getTraitOptions().add(new TraitReward("Play Recognition", 1));
        defaults.put("RARE", rare);
        
        // EPIC Achievement Rewards
        RarityRewards epic = new RarityRewards();
        epic.setGameRestarts(1);
        epic.getTraitOptions().add(new TraitReward("Awareness", 2));
        epic.getTraitOptions().add(new TraitReward("Throw Power", 2));
        epic.getTraitOptions().add(new TraitReward("Deep Route Running", 1));
        epic.getTraitOptions().add(new TraitReward("Pass Rush Moves", 1));
        epic.getTraitOptions().add(new TraitReward("Block Shedding", 1));
        epic.getTraitOptions().add(new TraitReward("Kick Accuracy", 1));
        defaults.put("EPIC", epic);
        
        // LEGENDARY Achievement Rewards
        RarityRewards legendary = new RarityRewards();
        legendary.setGameRestarts(2);
        legendary.getTraitOptions().add(new TraitReward("Deep Accuracy", 2));
        legendary.getTraitOptions().add(new TraitReward("Spectacular Catch", 1));
        legendary.getTraitOptions().add(new TraitReward("Catch in Traffic", 1));
        legendary.getTraitOptions().add(new TraitReward("Break Sack", 1));
        legendary.getTraitOptions().add(new TraitReward("Finesse Moves", 1));
        legendary.getTraitOptions().add(new TraitReward("Press Coverage", 1));
        legendary.getTraitOptions().add(new TraitReward("Return", 1));
        defaults.put("LEGENDARY", legendary);
    }
    
    private void initializeTraitCategories() {
        // Basic Physical Traits
        traits.getBasic().addAll(Arrays.asList(
            "Speed", "Strength", "Agility", "Stamina", "Carrying", "Catching", "Tackle"
        ));
        
        // Intermediate Traits
        traits.getIntermediate().addAll(Arrays.asList(
            "Acceleration", "Awareness", "Break Tackle", "Short Accuracy", "Short Route Running",
            "Run Block", "Man Coverage", "Zone Coverage", "Toughness", "Jumping"
        ));
        
        // Advanced Traits
        traits.getAdvanced().addAll(Arrays.asList(
            "Medium Accuracy", "Medium Route Running", "Pass Block", "Play Recognition",
            "Throw Power", "Throw Under Pressure", "Pass Rush Moves", "Block Shedding"
        ));
        
        // Elite Traits
        traits.getElite().addAll(Arrays.asList(
            "Deep Accuracy", "Deep Route Running", "Break Sack", "Spectacular Catch",
            "Catch in Traffic", "Finesse Moves", "Press Coverage", "Return", "Kick Accuracy"
        ));
    }
    
    /**
     * Get rewards for a specific achievement
     */
    public List<RewardDefinition> getRewardsForAchievement(Achievement achievement) {
        String description = achievement.getDescription();
        
        // Check for specific override first
        if (specific.containsKey(description)) {
            return specific.get(description);
        }
        
        // Fall back to default rarity-based rewards
        String rarity = achievement.getRarity().name();
        RarityRewards rarityRewards = defaults.get(rarity);
        
        if (rarityRewards == null) {
            return new ArrayList<>();
        }
        
        List<RewardDefinition> rewards = new ArrayList<>();
        
        // Add game restart reward if applicable
        if (rarityRewards.getGameRestarts() > 0) {
            RewardDefinition gameRestart = new RewardDefinition();
            gameRestart.setType("GAME_RESTART");
            gameRestart.setAmount(rarityRewards.getGameRestarts());
            rewards.add(gameRestart);
        }
        
        // Add ONE random trait reward from available options (user gets one, not all)
        if (!rarityRewards.getTraitOptions().isEmpty()) {
            Random random = new Random();
            TraitReward selectedTrait = rarityRewards.getTraitOptions().get(
                random.nextInt(rarityRewards.getTraitOptions().size())
            );
            
            RewardDefinition reward = new RewardDefinition();
            reward.setType("TRAIT_BOOST");
            reward.setTraitName(selectedTrait.getName());
            reward.setAmount(selectedTrait.getBoost());
            rewards.add(reward);
        }
        
        return rewards;
    }
    
    /**
     * Check if a trait is in a specific category
     */
    public boolean isTraitInCategory(String traitName, String category) {
        switch (category.toLowerCase()) {
            case "basic": return traits.getBasic().contains(traitName);
            case "intermediate": return traits.getIntermediate().contains(traitName);
            case "advanced": return traits.getAdvanced().contains(traitName);
            case "elite": return traits.getElite().contains(traitName);
            default: return false;
        }
    }
    
    // Getters and Setters
    public Map<String, RarityRewards> getDefaults() { return defaults; }
    public void setDefaults(Map<String, RarityRewards> defaults) { this.defaults = defaults; }
    
    public Map<String, List<RewardDefinition>> getSpecific() { return specific; }
    public void setSpecific(Map<String, List<RewardDefinition>> specific) { this.specific = specific; }
    
    public TraitCategories getTraits() { return traits; }
    public void setTraits(TraitCategories traits) { this.traits = traits; }
    
    // Helper Classes
    public static class RarityRewards {
        private int gameRestarts = 0;
        private List<TraitReward> traitOptions = new ArrayList<>();
        
        public int getGameRestarts() { return gameRestarts; }
        public void setGameRestarts(int gameRestarts) { this.gameRestarts = gameRestarts; }
        
        public List<TraitReward> getTraitOptions() { return traitOptions; }
        public void setTraitOptions(List<TraitReward> traitOptions) { this.traitOptions = traitOptions; }
    }
    
    public static class TraitReward {
        private String name;
        private int boost;
        
        public TraitReward() {}
        public TraitReward(String name, int boost) {
            this.name = name;
            this.boost = boost;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getBoost() { return boost; }
        public void setBoost(int boost) { this.boost = boost; }
    }
    
    public static class RewardDefinition {
        private String type; // TRAIT_BOOST or GAME_RESTART
        private String traitName; // For TRAIT_BOOST type
        private int amount;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTraitName() { return traitName; }
        public void setTraitName(String traitName) { this.traitName = traitName; }
        
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }
    
    public static class TraitCategories {
        private List<String> basic = new ArrayList<>();
        private List<String> intermediate = new ArrayList<>();
        private List<String> advanced = new ArrayList<>();
        private List<String> elite = new ArrayList<>();
        
        public List<String> getBasic() { return basic; }
        public void setBasic(List<String> basic) { this.basic = basic; }
        
        public List<String> getIntermediate() { return intermediate; }
        public void setIntermediate(List<String> intermediate) { this.intermediate = intermediate; }
        
        public List<String> getAdvanced() { return advanced; }
        public void setAdvanced(List<String> advanced) { this.advanced = advanced; }
        
        public List<String> getElite() { return elite; }
        public void setElite(List<String> elite) { this.elite = elite; }
    }
}