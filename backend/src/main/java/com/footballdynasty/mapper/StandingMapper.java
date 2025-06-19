package com.footballdynasty.mapper;

import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Standing entity and its DTOs.
 * Handles conversion between entity and various DTO types for different operations.
 */
@Mapper(componentModel = "spring", uses = {TeamMapper.class})
public interface StandingMapper {
    
    /**
     * Convert Standing entity to StandingDTO.
     * Includes calculated fields like win percentage and total games.
     */
    @Mapping(target = "winPercentage", source = "winPercentage")
    @Mapping(target = "totalGames", source = "totalGames")
    @Mapping(target = "conferenceWinPercentage", source = "conferenceWinPercentage")
    @Mapping(target = "totalConferenceGames", source = "totalConferenceGames")
    StandingDTO toDTO(Standing standing);
    
    /**
     * Convert StandingCreateDTO to Standing entity.
     * Requires a separate Team entity to be provided for the relationship.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", source = "team")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "winPercentage", ignore = true)
    @Mapping(target = "totalGames", ignore = true)
    @Mapping(target = "conferenceWinPercentage", ignore = true)
    @Mapping(target = "totalConferenceGames", ignore = true)
    Standing toEntity(StandingCreateDTO createDTO, Team team);
    
    /**
     * Update existing Standing entity with values from StandingUpdateDTO.
     * Only updates non-null fields from the DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "winPercentage", ignore = true)
    @Mapping(target = "totalGames", ignore = true)
    @Mapping(target = "conferenceWinPercentage", ignore = true)
    @Mapping(target = "totalConferenceGames", ignore = true)
    void updateEntity(StandingUpdateDTO updateDTO, @MappingTarget Standing standing);
    
    /**
     * Create a basic Standing entity with only team and year set.
     * Used for initial creation before setting other values.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wins", constant = "0")
    @Mapping(target = "losses", constant = "0")
    @Mapping(target = "conferenceWins", constant = "0")
    @Mapping(target = "conferenceLosses", constant = "0")
    @Mapping(target = "rank", ignore = true)
    @Mapping(target = "conferenceRank", ignore = true)
    @Mapping(target = "receivingVotes", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "winPercentage", ignore = true)
    @Mapping(target = "totalGames", ignore = true)
    @Mapping(target = "conferenceWinPercentage", ignore = true)
    @Mapping(target = "totalConferenceGames", ignore = true)
    Standing createEntity(Team team, Integer year);
}