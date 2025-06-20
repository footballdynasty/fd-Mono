package com.footballdynasty.mapper;

import com.footballdynasty.dto.AchievementDTO;
import com.footballdynasty.entity.Achievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AchievementMapper {
    
    /**
     * Convert Achievement entity to DTO
     */
    AchievementDTO toDTO(Achievement achievement);
    
    /**
     * Convert Achievement DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Achievement toEntity(AchievementDTO achievementDTO);
    
    /**
     * Convert list of Achievement entities to DTOs
     */
    List<AchievementDTO> toDTOList(List<Achievement> achievements);
    
    /**
     * Convert list of Achievement DTOs to entities
     */
    List<Achievement> toEntityList(List<AchievementDTO> achievementDTOs);
    
    /**
     * Update existing Achievement entity from DTO
     * Ignores null values and system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(AchievementDTO dto, @MappingTarget Achievement entity);
    
    /**
     * Partial update for achievement completion
     * Used when marking an achievement as complete
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "reward", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "rarity", ignore = true)
    @Mapping(target = "icon", ignore = true)
    @Mapping(target = "color", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCompletionFromDTO(AchievementDTO dto, @MappingTarget Achievement entity);
}