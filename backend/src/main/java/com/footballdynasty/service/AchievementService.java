package com.footballdynasty.service;

import com.footballdynasty.dto.AchievementDTO;
import com.footballdynasty.entity.Achievement;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.mapper.AchievementMapper;
import com.footballdynasty.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AchievementService {
    
    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
    
    private final AchievementRepository achievementRepository;
    private final AchievementMapper achievementMapper;
    
    @Autowired
    public AchievementService(AchievementRepository achievementRepository, AchievementMapper achievementMapper) {
        this.achievementRepository = achievementRepository;
        this.achievementMapper = achievementMapper;
    }
    
    /**
     * Get all achievements with optional filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<AchievementDTO> getAllAchievements(int page, int size, String type, String rarity, Boolean completed) {
        logger.debug("Fetching achievements with string filters - type: {}, rarity: {}, completed: {}", type, rarity, completed);
        
        Achievement.AchievementType typeEnum = null;
        Achievement.AchievementRarity rarityEnum = null;
        
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = Achievement.AchievementType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid achievement type: {}", type);
            }
        }
        
        if (rarity != null && !rarity.trim().isEmpty()) {
            try {
                rarityEnum = Achievement.AchievementRarity.valueOf(rarity.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid achievement rarity: {}", rarity);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return getAllAchievements(typeEnum, rarityEnum, completed, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AchievementDTO> getAllAchievements(Achievement.AchievementType type, 
                                                  Achievement.AchievementRarity rarity,
                                                  Boolean completed, 
                                                  Pageable pageable) {
        logger.debug("Fetching achievements with filters - type: {}, rarity: {}, completed: {}", type, rarity, completed);
        
        Page<Achievement> achievements;
        
        if (type != null || rarity != null || completed != null) {
            achievements = achievementRepository.findWithFilters(type, rarity, completed, pageable);
        } else {
            achievements = achievementRepository.findAllOrderedByRarityAndDate(pageable);
        }
        
        logger.debug("Found {} achievements", achievements.getTotalElements());
        return achievements.map(achievementMapper::toDTO);
    }
    
    /**
     * Get achievement by ID
     */
    @Transactional(readOnly = true)
    public AchievementDTO getAchievementById(UUID id) {
        logger.debug("Fetching achievement with ID: {}", id);
        
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        return achievementMapper.toDTO(achievement);
    }
    
    /**
     * Get achievements by type (non-paginated)
     */
    @Transactional(readOnly = true)
    public List<AchievementDTO> getAchievementsByType(Achievement.AchievementType type) {
        logger.debug("Fetching achievements by type: {}", type);
        
        List<Achievement> achievements = achievementRepository.findByType(type);
        logger.debug("Found {} achievements of type {}", achievements.size(), type);
        
        return achievementMapper.toDTOList(achievements);
    }
    
    /**
     * Create new achievement
     */
    public AchievementDTO createAchievement(AchievementDTO achievementDTO) {
        logger.info("Creating new achievement: {}", achievementDTO.getDescription());
        
        Achievement achievement = achievementMapper.toEntity(achievementDTO);
        achievement.setIsCompleted(false); // Ensure new achievements start as incomplete
        
        Achievement savedAchievement = achievementRepository.save(achievement);
        logger.info("Created achievement with ID: {}", savedAchievement.getId());
        
        return achievementMapper.toDTO(savedAchievement);
    }
    
    /**
     * Update existing achievement
     */
    public AchievementDTO updateAchievement(UUID id, AchievementDTO achievementDTO) {
        logger.info("Updating achievement with ID: {}", id);
        
        Achievement existingAchievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        achievementMapper.updateEntityFromDTO(achievementDTO, existingAchievement);
        
        Achievement updatedAchievement = achievementRepository.save(existingAchievement);
        logger.info("Updated achievement: {}", updatedAchievement.getDescription());
        
        return achievementMapper.toDTO(updatedAchievement);
    }
    
    /**
     * Mark achievement as completed
     */
    public AchievementDTO completeAchievement(UUID id) {
        logger.info("Completing achievement with ID: {}", id);
        
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        if (Boolean.TRUE.equals(achievement.getIsCompleted())) {
            logger.warn("Achievement {} is already completed", id);
            return achievementMapper.toDTO(achievement);
        }
        
        achievement.setIsCompleted(true);
        achievement.setDateCompleted(Instant.now().toEpochMilli());
        
        Achievement completedAchievement = achievementRepository.save(achievement);
        logger.info("Achievement completed: {} - {}", completedAchievement.getDescription(), completedAchievement.getReward());
        
        return achievementMapper.toDTO(completedAchievement);
    }
    
    /**
     * Reset achievement completion (for testing/admin purposes)
     */
    public AchievementDTO resetAchievement(UUID id) {
        logger.info("Resetting achievement completion for ID: {}", id);
        
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        achievement.setIsCompleted(false);
        achievement.setDateCompleted(null);
        
        Achievement resetAchievement = achievementRepository.save(achievement);
        logger.info("Achievement reset: {}", resetAchievement.getDescription());
        
        return achievementMapper.toDTO(resetAchievement);
    }
    
    /**
     * Mark achievement as not completed (admin function)
     */
    public AchievementDTO uncompleteAchievement(UUID id) {
        logger.info("Marking achievement as not completed with ID: {}", id);
        
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        achievement.setIsCompleted(false);
        achievement.setDateCompleted(null);
        
        Achievement savedAchievement = achievementRepository.save(achievement);
        logger.info("Achievement marked as not completed: {}", achievement.getDescription());
        
        return achievementMapper.toDTO(savedAchievement);
    }
    
    /**
     * Delete achievement
     */
    public void deleteAchievement(UUID id) {
        logger.info("Deleting achievement with ID: {}", id);
        
        Achievement achievement = achievementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
        
        achievementRepository.delete(achievement);
        logger.info("Deleted achievement: {}", achievement.getDescription());
    }
    
    /**
     * Get achievement statistics for admin API
     */
    @Transactional(readOnly = true)
    public AchievementStatsDTO getAchievementStatistics() {
        return getAchievementStats();
    }
    
    /**
     * Get achievement statistics
     */
    @Transactional(readOnly = true)
    public AchievementStatsDTO getAchievementStats() {
        logger.debug("Calculating achievement statistics");
        
        long totalAchievements = achievementRepository.count();
        long completedAchievements = achievementRepository.countByIsCompleted(true);
        
        AchievementStatsDTO stats = new AchievementStatsDTO();
        stats.setTotalAchievements(totalAchievements);
        stats.setCompletedAchievements(completedAchievements);
        stats.setCompletionPercentage(totalAchievements > 0 ? 
            (double) completedAchievements / totalAchievements * 100.0 : 0.0);
        
        // Count by type
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            long typeCount = achievementRepository.countByType(type);
            stats.getCountByType().put(type.name(), typeCount);
        }
        
        // Count by rarity
        for (Achievement.AchievementRarity rarity : Achievement.AchievementRarity.values()) {
            long rarityCount = achievementRepository.countByRarity(rarity);
            stats.getCountByRarity().put(rarity.name(), rarityCount);
        }
        
        logger.debug("Achievement stats: {} total, {} completed ({:.1f}%)", 
            totalAchievements, completedAchievements, stats.getCompletionPercentage());
        
        return stats;
    }
    
    /**
     * Auto-complete achievements based on game events
     * This method can be called by other services when game events occur
     */
    public void checkAndCompleteAchievements(String eventType, Object eventData) {
        logger.debug("Checking achievements for event type: {}", eventType);
        
        // TODO: Implement achievement tracking logic based on game events
        // For example:
        // - Win achievements when a team reaches certain win counts
        // - Season achievements when certain milestones are reached
        // - Championship achievements when teams win championships
        // - Statistics achievements when certain statistical thresholds are met
        
        // This would be expanded based on specific achievement requirements
        switch (eventType) {
            case "GAME_COMPLETED":
                // Check for win-based achievements
                break;
            case "SEASON_ENDED":
                // Check for season-based achievements
                break;
            case "CHAMPIONSHIP_WON":
                // Check for championship achievements
                break;
            default:
                logger.debug("No achievement logic implemented for event type: {}", eventType);
        }
    }
    
    /**
     * DTO for achievement statistics
     */
    public static class AchievementStatsDTO {
        private long totalAchievements;
        private long completedAchievements;
        private double completionPercentage;
        private java.util.Map<String, Long> countByType = new java.util.HashMap<>();
        private java.util.Map<String, Long> countByRarity = new java.util.HashMap<>();
        
        // Getters and setters
        public long getTotalAchievements() { return totalAchievements; }
        public void setTotalAchievements(long totalAchievements) { this.totalAchievements = totalAchievements; }
        
        public long getCompletedAchievements() { return completedAchievements; }
        public void setCompletedAchievements(long completedAchievements) { this.completedAchievements = completedAchievements; }
        
        public double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
        
        public java.util.Map<String, Long> getCountByType() { return countByType; }
        public void setCountByType(java.util.Map<String, Long> countByType) { this.countByType = countByType; }
        
        public java.util.Map<String, Long> getCountByRarity() { return countByRarity; }
        public void setCountByRarity(java.util.Map<String, Long> countByRarity) { this.countByRarity = countByRarity; }
    }
}