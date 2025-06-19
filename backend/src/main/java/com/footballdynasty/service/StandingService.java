package com.footballdynasty.service;

import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Standing entity operations.
 * Provides business logic for CRUD operations, standings calculations,
 * and various filtering and sorting capabilities.
 */
public interface StandingService {
    
    /**
     * Get all standings with pagination support.
     * 
     * @param pageable pagination information
     * @return paginated list of standings
     */
    Page<StandingDTO> findAll(Pageable pageable);
    
    /**
     * Get standings filtered by year and conference with pagination.
     * 
     * @param year the year to filter by (optional)
     * @param conference the conference to filter by (optional)
     * @param pageable pagination information
     * @return paginated list of filtered standings
     */
    Page<StandingDTO> findByYearAndConference(Integer year, String conference, Pageable pageable);
    
    /**
     * Get standings for a specific team across years.
     * 
     * @param teamId the team ID
     * @param pageable pagination information
     * @return paginated list of team's standings by year
     */
    Page<StandingDTO> findByTeam(UUID teamId, Pageable pageable);
    
    /**
     * Get standing for a specific team and year.
     * 
     * @param teamId the team ID
     * @param year the year
     * @return the standing DTO if found
     * @throws com.footballdynasty.exception.ResourceNotFoundException if not found
     */
    StandingDTO findByTeamAndYear(UUID teamId, Integer year);
    
    /**
     * Get standings by a specific conference and year, ordered by rank.
     * 
     * @param conference the conference name
     * @param year the year
     * @return list of standings ordered by conference rank
     */
    List<StandingDTO> findByConferenceAndYear(String conference, Integer year);
    
    /**
     * Get top ranked teams for a year.
     * 
     * @param year the year
     * @param limit maximum number of teams to return
     * @return list of top ranked teams
     */
    List<StandingDTO> findTopRankedByYear(Integer year, int limit);
    
    /**
     * Get teams receiving votes for a year.
     * 
     * @param year the year
     * @return list of teams receiving votes, ordered by votes received
     */
    List<StandingDTO> findTeamsReceivingVotesByYear(Integer year);
    
    /**
     * Create a new standing record.
     * 
     * @param createDTO the standing creation data
     * @return the created standing DTO
     */
    StandingDTO create(StandingCreateDTO createDTO);
    
    /**
     * Update an existing standing record.
     * 
     * @param standingId the standing ID
     * @param updateDTO the standing update data
     * @return the updated standing DTO
     * @throws com.footballdynasty.exception.ResourceNotFoundException if standing not found
     */
    StandingDTO update(UUID standingId, StandingUpdateDTO updateDTO);
    
    /**
     * Delete a standing record.
     * 
     * @param standingId the standing ID
     * @throws com.footballdynasty.exception.ResourceNotFoundException if standing not found
     */
    void delete(UUID standingId);
    
    /**
     * Calculate and update standings for all conferences in a specific year.
     * This method delegates to the existing ConferenceStandingsService.
     * 
     * @param year the year to calculate standings for
     */
    void calculateStandings(Integer year);
    
    /**
     * Calculate and update standings for a specific conference and year.
     * This method delegates to the existing ConferenceStandingsService.
     * 
     * @param conference the conference to calculate standings for
     * @param year the year to calculate standings for
     */
    void calculateConferenceStandings(String conference, Integer year);
    
    /**
     * Get a standing by its ID.
     * 
     * @param standingId the standing ID
     * @return the standing DTO
     * @throws com.footballdynasty.exception.ResourceNotFoundException if not found
     */
    StandingDTO findById(UUID standingId);
}