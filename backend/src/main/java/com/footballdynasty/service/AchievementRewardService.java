package com.footballdynasty.service;

import com.footballdynasty.config.AchievementRewardConfig;
import com.footballdynasty.dto.AchievementRewardDTO;
import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementReward;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.repository.AchievementRepository;
import com.footballdynasty.repository.AchievementRewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AchievementRewardService {
    
    private static final Logger logger = LoggerFactory.getLogger(AchievementRewardService.class);
    
    private final AchievementRewardRepository rewardRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementRewardConfig rewardConfig;
    
    @Autowired
    public AchievementRewardService(AchievementRewardRepository rewardRepository,
                                   AchievementRepository achievementRepository,
                                   AchievementRewardConfig rewardConfig) {
        this.rewardRepository = rewardRepository;
        this.achievementRepository = achievementRepository;
        this.rewardConfig = rewardConfig;
    }
    
    /**
     * Clear all existing achievement rewards
     */
    @Transactional
    public void clearAllRewards() {
        logger.info("Clearing all existing achievement rewards");
        
        long rewardsDeleted = rewardRepository.count();
        rewardRepository.deleteAll();
        
        logger.info("Cleared {} achievement rewards", rewardsDeleted);
    }
    
    /**
     * Initialize default rewards for all achievements based on configuration
     */
    @Transactional
    public void initializeDefaultRewards() {
        logger.info("Initializing default rewards for all achievements");
        
        List<Achievement> achievements = achievementRepository.findAll();
        int rewardsCreated = 0;
        
        for (Achievement achievement : achievements) {
            // Skip if rewards already exist
            if (rewardRepository.countByAchievementAndActiveTrue(achievement) > 0) {
                continue;
            }
            
            List<AchievementRewardConfig.RewardDefinition> rewardDefs = rewardConfig.getRewardsForAchievement(achievement);
            
            for (AchievementRewardConfig.RewardDefinition rewardDef : rewardDefs) {
                AchievementReward reward = new AchievementReward();
                reward.setAchievement(achievement);
                reward.setType(AchievementReward.RewardType.valueOf(rewardDef.getType()));
                reward.setBoostAmount(rewardDef.getAmount());
                
                if ("TRAIT_BOOST".equals(rewardDef.getType())) {
                    reward.setTraitName(rewardDef.getTraitName());
                }
                
                rewardRepository.save(reward);
                rewardsCreated++;
            }
        }
        
        logger.info("Created {} default rewards for {} achievements", rewardsCreated, achievements.size());
    }
    
    /**
     * Get all rewards for a specific achievement
     */
    @Transactional(readOnly = true)
    public List<AchievementRewardDTO> getRewardsForAchievement(UUID achievementId) {
        logger.debug("Getting rewards for achievement: {}", achievementId);
        
        List<AchievementReward> rewards = rewardRepository.findByAchievementIdAndActiveTrue(achievementId);
        return rewards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a custom reward for an achievement
     */
    public AchievementRewardDTO createReward(UUID achievementId, AchievementRewardDTO rewardDTO) {
        logger.info("Creating custom reward for achievement: {}", achievementId);
        
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + achievementId));
        
        AchievementReward reward = new AchievementReward();
        reward.setAchievement(achievement);
        reward.setType(rewardDTO.getType());
        reward.setTraitName(rewardDTO.getTraitName());
        reward.setBoostAmount(rewardDTO.getBoostAmount());
        reward.setActive(true);
        
        AchievementReward savedReward = rewardRepository.save(reward);
        logger.info("Created reward: {} +{} for achievement: {}", 
                   savedReward.getTraitName(), savedReward.getBoostAmount(), achievement.getDescription());
        
        return convertToDTO(savedReward);
    }
    
    /**
     * Update an existing reward
     */
    public AchievementRewardDTO updateReward(UUID rewardId, AchievementRewardDTO rewardDTO) {
        logger.info("Updating reward: {}", rewardId);
        
        AchievementReward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found with ID: " + rewardId));
        
        reward.setType(rewardDTO.getType());
        reward.setTraitName(rewardDTO.getTraitName());
        reward.setBoostAmount(rewardDTO.getBoostAmount());
        reward.setActive(rewardDTO.getActive());
        
        AchievementReward savedReward = rewardRepository.save(reward);
        return convertToDTO(savedReward);
    }
    
    /**
     * Delete a reward (soft delete)
     */
    public void deleteReward(UUID rewardId) {
        logger.info("Deleting reward: {}", rewardId);
        
        AchievementReward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found with ID: " + rewardId));
        
        reward.setActive(false);
        rewardRepository.save(reward);
    }
    
    /**
     * Get reward summary statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRewardStatistics() {
        logger.debug("Calculating reward statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total rewards
        long totalRewards = rewardRepository.count();
        long activeRewards = rewardRepository.findByTypeAndActiveTrue(AchievementReward.RewardType.TRAIT_BOOST).size() +
                           rewardRepository.findByTypeAndActiveTrue(AchievementReward.RewardType.GAME_RESTART).size();
        
        stats.put("totalRewards", totalRewards);
        stats.put("activeRewards", activeRewards);
        
        // Rewards by type
        long traitRewards = rewardRepository.findByTypeAndActiveTrue(AchievementReward.RewardType.TRAIT_BOOST).size();
        long gameRestartRewards = rewardRepository.findByTypeAndActiveTrue(AchievementReward.RewardType.GAME_RESTART).size();
        
        stats.put("traitRewards", traitRewards);
        stats.put("gameRestartRewards", gameRestartRewards);
        
        // Total potential boosts
        Integer totalGameRestarts = rewardRepository.getTotalGameRestarts();
        stats.put("totalGameRestarts", totalGameRestarts != null ? totalGameRestarts : 0);
        
        // Available vs earned rewards
        long availableRewards = rewardRepository.findAvailableRewards().size();
        long earnedRewards = rewardRepository.findRewardsForCompletedAchievements().size();
        
        stats.put("availableRewards", availableRewards);
        stats.put("earnedRewards", earnedRewards);
        
        // Most common traits
        Map<String, Long> traitCounts = rewardRepository.findByTypeAndActiveTrue(AchievementReward.RewardType.TRAIT_BOOST)
                .stream()
                .filter(r -> r.getTraitName() != null)
                .collect(Collectors.groupingBy(AchievementReward::getTraitName, Collectors.counting()));
        
        stats.put("traitDistribution", traitCounts);
        
        return stats;
    }
    
    /**
     * Get all available trait options with their categories
     */
    public Map<String, Object> getTraitOptions() {
        Map<String, Object> options = new HashMap<>();
        
        // Get trait categories from config
        options.put("basic", rewardConfig.getTraits().getBasic());
        options.put("intermediate", rewardConfig.getTraits().getIntermediate());
        options.put("advanced", rewardConfig.getTraits().getAdvanced());
        options.put("elite", rewardConfig.getTraits().getElite());
        
        // Get all available traits from enum
        List<String> allTraits = Arrays.stream(AchievementReward.TraitType.values())
                .map(AchievementReward.TraitType::getDisplayName)
                .sorted()
                .collect(Collectors.toList());
        
        options.put("all", allTraits);
        
        return options;
    }
    
    /**
     * Apply rewards when an achievement is completed
     */
    public List<AchievementRewardDTO> applyRewardsForCompletion(UUID achievementId) {
        logger.info("Applying rewards for completed achievement: {}", achievementId);
        
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + achievementId));
        
        if (!achievement.getIsCompleted()) {
            throw new IllegalStateException("Cannot apply rewards for incomplete achievement");
        }
        
        List<AchievementReward> rewards = rewardRepository.findByAchievementAndActiveTrue(achievement);
        
        // Log reward application
        for (AchievementReward reward : rewards) {
            if (reward.getType() == AchievementReward.RewardType.TRAIT_BOOST) {
                logger.info("REWARD_APPLIED: {} +{} from achievement '{}'", 
                           reward.getTraitName(), reward.getBoostAmount(), achievement.getDescription());
            } else {
                logger.info("REWARD_APPLIED: +{} Game Restart(s) from achievement '{}'", 
                           reward.getBoostAmount(), achievement.getDescription());
            }
        }
        
        return rewards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert entity to DTO
     */
    private AchievementRewardDTO convertToDTO(AchievementReward reward) {
        AchievementRewardDTO dto = new AchievementRewardDTO();
        dto.setId(reward.getId());
        dto.setBoostAmount(reward.getBoostAmount());
        dto.setTraitName(reward.getTraitName());
        dto.setType(reward.getType()); // Set type after boostAmount to avoid null pointer
        dto.setActive(reward.getActive());
        dto.setCreatedAt(reward.getCreatedAt());
        dto.setUpdatedAt(reward.getUpdatedAt());
        
        return dto;
    }
}