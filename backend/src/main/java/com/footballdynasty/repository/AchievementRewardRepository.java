package com.footballdynasty.repository;

import com.footballdynasty.entity.Achievement;
import com.footballdynasty.entity.AchievementReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRewardRepository extends JpaRepository<AchievementReward, UUID> {
    
    /**
     * Find all rewards for a specific achievement
     */
    List<AchievementReward> findByAchievementAndActiveTrue(Achievement achievement);
    
    /**
     * Find all rewards for a specific achievement by ID
     */
    @Query("SELECT ar FROM AchievementReward ar WHERE ar.achievement.id = :achievementId AND ar.active = true")
    List<AchievementReward> findByAchievementIdAndActiveTrue(@Param("achievementId") UUID achievementId);
    
    /**
     * Find all trait boost rewards for an achievement
     */
    @Query("SELECT ar FROM AchievementReward ar WHERE ar.achievement = :achievement AND ar.type = 'TRAIT_BOOST' AND ar.active = true")
    List<AchievementReward> findTraitRewardsByAchievement(@Param("achievement") Achievement achievement);
    
    /**
     * Find all game restart rewards for an achievement
     */
    @Query("SELECT ar FROM AchievementReward ar WHERE ar.achievement = :achievement AND ar.type = 'GAME_RESTART' AND ar.active = true")
    List<AchievementReward> findGameRestartRewardsByAchievement(@Param("achievement") Achievement achievement);
    
    /**
     * Find rewards by trait name
     */
    List<AchievementReward> findByTraitNameAndActiveTrue(String traitName);
    
    /**
     * Count total rewards for an achievement
     */
    @Query("SELECT COUNT(ar) FROM AchievementReward ar WHERE ar.achievement = :achievement AND ar.active = true")
    long countByAchievementAndActiveTrue(@Param("achievement") Achievement achievement);
    
    /**
     * Find all rewards by type
     */
    List<AchievementReward> findByTypeAndActiveTrue(AchievementReward.RewardType type);
    
    /**
     * Get total trait boost amount for a specific trait across all achievements
     */
    @Query("SELECT COALESCE(SUM(ar.boostAmount), 0) FROM AchievementReward ar WHERE ar.traitName = :traitName AND ar.type = 'TRAIT_BOOST' AND ar.active = true")
    Integer getTotalBoostForTrait(@Param("traitName") String traitName);
    
    /**
     * Get total game restarts available across all achievements
     */
    @Query("SELECT COALESCE(SUM(ar.boostAmount), 0) FROM AchievementReward ar WHERE ar.type = 'GAME_RESTART' AND ar.active = true")
    Integer getTotalGameRestarts();
    
    /**
     * Find rewards for completed achievements only
     */
    @Query("SELECT ar FROM AchievementReward ar WHERE ar.achievement.isCompleted = true AND ar.active = true")
    List<AchievementReward> findRewardsForCompletedAchievements();
    
    /**
     * Find available rewards (from incomplete achievements)
     */
    @Query("SELECT ar FROM AchievementReward ar WHERE ar.achievement.isCompleted = false AND ar.active = true")
    List<AchievementReward> findAvailableRewards();
}