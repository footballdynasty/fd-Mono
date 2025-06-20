package com.footballdynasty.repository;

import com.footballdynasty.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    
    /**
     * Find achievements by completion status
     */
    Page<Achievement> findByIsCompleted(Boolean isCompleted, Pageable pageable);
    
    /**
     * Find achievements by type
     */
    Page<Achievement> findByType(Achievement.AchievementType type, Pageable pageable);
    
    /**
     * Find achievements by type (non-paginated)
     */
    List<Achievement> findByType(Achievement.AchievementType type);
    
    /**
     * Find achievements by rarity
     */
    Page<Achievement> findByRarity(Achievement.AchievementRarity rarity, Pageable pageable);
    
    /**
     * Find achievements by type and completion status
     */
    Page<Achievement> findByTypeAndIsCompleted(Achievement.AchievementType type, Boolean isCompleted, Pageable pageable);
    
    /**
     * Find achievements by rarity and completion status
     */
    Page<Achievement> findByRarityAndIsCompleted(Achievement.AchievementRarity rarity, Boolean isCompleted, Pageable pageable);
    
    /**
     * Count completed achievements
     */
    long countByIsCompleted(Boolean isCompleted);
    
    /**
     * Count achievements by type
     */
    long countByType(Achievement.AchievementType type);
    
    /**
     * Count achievements by rarity
     */
    long countByRarity(Achievement.AchievementRarity rarity);
    
    /**
     * Find all achievements ordered by rarity (LEGENDARY first) and creation date
     */
    @Query("SELECT a FROM Achievement a ORDER BY " +
           "CASE a.rarity " +
           "WHEN 'LEGENDARY' THEN 1 " +
           "WHEN 'EPIC' THEN 2 " +
           "WHEN 'RARE' THEN 3 " +
           "WHEN 'UNCOMMON' THEN 4 " +
           "WHEN 'COMMON' THEN 5 " +
           "END, a.createdAt DESC")
    Page<Achievement> findAllOrderedByRarityAndDate(Pageable pageable);
    
    /**
     * Find achievements with custom filtering
     */
    @Query("SELECT a FROM Achievement a WHERE " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:rarity IS NULL OR a.rarity = :rarity) AND " +
           "(:completed IS NULL OR a.isCompleted = :completed) " +
           "ORDER BY " +
           "CASE a.rarity " +
           "WHEN 'LEGENDARY' THEN 1 " +
           "WHEN 'EPIC' THEN 2 " +
           "WHEN 'RARE' THEN 3 " +
           "WHEN 'UNCOMMON' THEN 4 " +
           "WHEN 'COMMON' THEN 5 " +
           "END, a.isCompleted ASC, a.createdAt DESC")
    Page<Achievement> findWithFilters(@Param("type") Achievement.AchievementType type,
                                     @Param("rarity") Achievement.AchievementRarity rarity,
                                     @Param("completed") Boolean completed,
                                     Pageable pageable);
}