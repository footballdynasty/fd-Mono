package com.footballdynasty.service;

import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.mapper.StandingMapper;
import com.footballdynasty.repository.StandingRepository;
import com.footballdynasty.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of StandingService interface.
 * Provides business logic for Standing entity CRUD operations and calculations.
 */
@Service
@Transactional
public class StandingServiceImpl implements StandingService {
    
    private static final Logger logger = LoggerFactory.getLogger(StandingServiceImpl.class);
    
    private final StandingRepository standingRepository;
    private final TeamRepository teamRepository;
    private final StandingMapper standingMapper;
    private final ConferenceStandingsService conferenceStandingsService;
    
    public StandingServiceImpl(StandingRepository standingRepository,
                             TeamRepository teamRepository,
                             StandingMapper standingMapper,
                             ConferenceStandingsService conferenceStandingsService) {
        this.standingRepository = standingRepository;
        this.teamRepository = teamRepository;
        this.standingMapper = standingMapper;
        this.conferenceStandingsService = conferenceStandingsService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<StandingDTO> findAll(Pageable pageable) {
        logger.info("Finding all standings with pagination: {}", pageable);
        
        // Show only top 25 ranked teams from current year by default (national rankings)
        int currentYear = LocalDateTime.now().getYear();
        Pageable rankedPageable = PageRequest.of(
            pageable.getPageNumber(),
            Math.min(pageable.getPageSize(), 25),
            pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.ASC, "rank")
        );
        
        Page<Standing> standings = standingRepository.findRankedTeamsByYear(currentYear, rankedPageable);
        logger.debug("Found {} top 25 ranked teams for current year {}", standings.getTotalElements(), currentYear);
        
        return standings.map(standingMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<StandingDTO> findByYearAndConference(Integer year, String conference, Pageable pageable) {
        logger.info("Finding standings by year: {} and conference: {}", year, conference);
        
        Page<Standing> standings;
        
        if (conference != null && !conference.trim().isEmpty()) {
            // Filter by conference - show all teams in that conference
            if (year != null) {
                // Filter by both year and conference
                standings = standingRepository.findByConferenceAndYearOrderByRecord(conference, year, pageable);
                logger.debug("Found {} standings for conference '{}' in year {}", 
                    standings.getTotalElements(), conference, year);
            } else {
                // Filter by conference only (current year assumed)
                int currentYear = LocalDateTime.now().getYear();
                standings = standingRepository.findByConferenceAndYearOrderByRecord(conference, currentYear, pageable);
                logger.debug("Found {} standings for conference '{}' in current year {}", 
                    standings.getTotalElements(), conference, currentYear);
            }
        } else {
            // No conference filter - show only top 25 ranked teams (national rankings)
            if (year != null) {
                // Filter by year only, limit to top 25 ranked teams
                Pageable rankedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    Math.min(pageable.getPageSize(), 25),
                    Sort.by(Sort.Direction.ASC, "rank")
                );
                standings = standingRepository.findRankedTeamsByYear(year, rankedPageable);
                logger.debug("Found {} top 25 ranked teams for year {}", standings.getTotalElements(), year);
            } else {
                // No filters, return top 25 ranked teams from current year
                int currentYear = LocalDateTime.now().getYear();
                Pageable rankedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    Math.min(pageable.getPageSize(), 25),
                    Sort.by(Sort.Direction.ASC, "rank")
                );
                standings = standingRepository.findRankedTeamsByYear(currentYear, rankedPageable);
                logger.debug("Found {} top 25 ranked teams for current year {}", standings.getTotalElements(), currentYear);
            }
        }
        
        return standings.map(standingMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<StandingDTO> findByTeam(UUID teamId, Pageable pageable) {
        logger.info("Finding standings for team: {}", teamId);
        
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            logger.debug("Team not found with id: {}, returning empty page", teamId);
            return Page.empty(pageable);
        }
        
        // Sort by year descending to show most recent first
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "year")
        );
        
        Page<Standing> standings = standingRepository.findByTeamOrderByYearDesc(team, sortedPageable);
        logger.debug("Found {} standings for team '{}'", standings.getTotalElements(), team.getName());
        
        return standings.map(standingMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StandingDTO findByTeamAndYear(UUID teamId, Integer year) {
        logger.info("Finding standing for team: {} and year: {}", teamId, year);
        
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        Standing standing = standingRepository.findByTeamAndYear(team, year)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Standing not found for team '" + team.getName() + "' in year " + year));
        
        logger.debug("Found standing for team '{}' in year {}", team.getName(), year);
        return standingMapper.toDTO(standing);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StandingDTO> findByConferenceAndYear(String conference, Integer year) {
        logger.info("Finding standings for conference: '{}' and year: {}", conference, year);
        
        List<Standing> standings = standingRepository.findByConferenceAndYearOrderByRecord(conference, year);
        logger.debug("Found {} standings for conference '{}' in year {}", 
            standings.size(), conference, year);
        
        return standings.stream()
            .map(standingMapper::toDTO)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StandingDTO> findTopRankedByYear(Integer year, int limit) {
        logger.info("Finding top {} ranked teams for year: {}", limit, year);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "rank"));
        Page<Standing> standingsPage = standingRepository.findRankedTeamsByYear(year, pageable);
        List<Standing> standings = standingsPage.getContent();
        
        logger.debug("Found {} top ranked teams for year {}", standings.size(), year);
        
        return standings.stream()
            .map(standingMapper::toDTO)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StandingDTO> findTeamsReceivingVotesByYear(Integer year) {
        logger.info("Finding teams receiving votes for year: {}", year);
        
        List<Standing> standings = standingRepository.findTeamsReceivingVotesByYear(year);
        logger.debug("Found {} teams receiving votes for year {}", standings.size(), year);
        
        return standings.stream()
            .map(standingMapper::toDTO)
            .toList();
    }
    
    @Override
    public StandingDTO create(StandingCreateDTO createDTO) {
        logger.info("Creating new standing for team: {} and year: {}", createDTO.getTeamId(), createDTO.getYear());
        
        Team team = teamRepository.findById(createDTO.getTeamId())
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + createDTO.getTeamId()));
        
        // Check if standing already exists for this team and year
        if (standingRepository.findByTeamAndYear(team, createDTO.getYear()).isPresent()) {
            throw new IllegalArgumentException(
                "Standing already exists for team '" + team.getName() + "' in year " + createDTO.getYear());
        }
        
        Standing standing = standingMapper.toEntity(createDTO, team);
        Standing savedStanding = standingRepository.save(standing);
        
        logger.info("Created standing with id: {} for team '{}' in year {}", 
            savedStanding.getId(), team.getName(), createDTO.getYear());
        
        return standingMapper.toDTO(savedStanding);
    }
    
    @Override
    public StandingDTO update(UUID standingId, StandingUpdateDTO updateDTO) {
        logger.info("Updating standing: {}", standingId);
        
        Standing standing = standingRepository.findById(standingId)
            .orElseThrow(() -> new ResourceNotFoundException("Standing not found with id: " + standingId));
        
        String teamName = standing.getTeam() != null ? standing.getTeam().getName() : "Unknown";
        logger.debug("Updating standing for team '{}' in year {}", teamName, standing.getYear());
        
        standingMapper.updateEntity(updateDTO, standing);
        Standing savedStanding = standingRepository.save(standing);
        
        logger.info("Updated standing for team '{}' in year {}", teamName, standing.getYear());
        
        return standingMapper.toDTO(savedStanding);
    }
    
    @Override
    public void delete(UUID standingId) {
        logger.info("Deleting standing: {}", standingId);
        
        Standing standing = standingRepository.findById(standingId)
            .orElseThrow(() -> new ResourceNotFoundException("Standing not found with id: " + standingId));
        
        String teamName = standing.getTeam() != null ? standing.getTeam().getName() : "Unknown";
        logger.debug("Deleting standing for team '{}' in year {}", teamName, standing.getYear());
        
        standingRepository.delete(standing);
        
        logger.info("Deleted standing for team '{}' in year {}", teamName, standing.getYear());
    }
    
    @Override
    public void calculateStandings(Integer year) {
        logger.info("Calculating standings for all conferences in year: {}", year);
        
        conferenceStandingsService.calculateConferenceStandings(year);
        
        logger.info("Completed standings calculation for year {}", year);
    }
    
    @Override
    public void calculateConferenceStandings(String conference, Integer year) {
        logger.info("Calculating standings for conference: '{}' in year: {}", conference, year);
        
        conferenceStandingsService.calculateConferenceStandingsForConference(conference, year);
        
        logger.info("Completed standings calculation for conference '{}' in year {}", conference, year);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StandingDTO findById(UUID standingId) {
        logger.info("Finding standing by id: {}", standingId);
        
        Standing standing = standingRepository.findById(standingId)
            .orElseThrow(() -> new ResourceNotFoundException("Standing not found with id: " + standingId));
        
        String teamName = standing.getTeam() != null ? standing.getTeam().getName() : "Unknown";
        logger.debug("Found standing for team '{}' in year {}", teamName, standing.getYear());
        
        return standingMapper.toDTO(standing);
    }
}