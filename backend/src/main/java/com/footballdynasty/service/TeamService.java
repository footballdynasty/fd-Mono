package com.footballdynasty.service;

import com.footballdynasty.dto.TeamDTO;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.mapper.TeamMapper;
import com.footballdynasty.repository.StandingRepository;
import com.footballdynasty.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final StandingRepository standingRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository, 
                      StandingRepository standingRepository, 
                      TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.standingRepository = standingRepository;
        this.teamMapper = teamMapper;
    }

    @Transactional(readOnly = true)
    public Page<TeamDTO> getAllTeams(String search, Pageable pageable) {
        Page<Team> teams;
        if (search != null && !search.trim().isEmpty()) {
            teams = teamRepository.findBySearchTerm(search.trim(), pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }
        return teams.map(this::enrichTeamWithCurrentSeasonData);
    }

    @Transactional(readOnly = true)
    public TeamDTO getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return enrichTeamWithCurrentSeasonData(team);
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getTeamsByConference(String conference) {
        List<Team> teams = teamRepository.findByConferenceOrderByName(conference);
        return teams.stream()
            .map(this::enrichTeamWithCurrentSeasonData)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAllConferences() {
        return teamRepository.findAllConferences();
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getHumanTeams() {
        List<Team> teams = teamRepository.findByIsHumanTrue();
        return teams.stream()
            .map(this::enrichTeamWithCurrentSeasonData)
            .toList();
    }

    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = teamMapper.toEntity(teamDTO);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        Team savedTeam = teamRepository.save(team);
        return teamMapper.toDTO(savedTeam);
    }

    public TeamDTO updateTeam(UUID id, TeamDTO teamDTO) {
        Team existingTeam = teamRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        
        // Update fields
        existingTeam.setName(teamDTO.getName());
        existingTeam.setCoach(teamDTO.getCoach());
        existingTeam.setUsername(teamDTO.getUsername());
        existingTeam.setConference(teamDTO.getConference());
        existingTeam.setIsHuman(teamDTO.getIsHuman());
        existingTeam.setImageUrl(teamDTO.getImageUrl());
        existingTeam.setUpdatedAt(LocalDateTime.now());
        
        Team savedTeam = teamRepository.save(existingTeam);
        return enrichTeamWithCurrentSeasonData(savedTeam);
    }

    public void deleteTeam(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    private TeamDTO enrichTeamWithCurrentSeasonData(Team team) {
        TeamDTO dto = teamMapper.toDTO(team);
        
        // Get current season data (assuming current year)
        int currentYear = LocalDateTime.now().getYear();
        Optional<Standing> currentStanding = standingRepository.findByTeamAndYear(team, currentYear);
        
        if (currentStanding.isPresent()) {
            Standing standing = currentStanding.get();
            // Overall record
            dto.setCurrentWins(standing.getWins());
            dto.setCurrentLosses(standing.getLosses());
            dto.setWinPercentage(standing.getWinPercentage());
            dto.setCurrentRank(standing.getRank());
            dto.setTotalGames(standing.getTotalGames());
            
            // Conference record
            dto.setConferenceWins(standing.getConferenceWins());
            dto.setConferenceLosses(standing.getConferenceLosses());
            dto.setConferenceWinPercentage(standing.getConferenceWinPercentage());
            dto.setConferenceRank(standing.getConferenceRank());
            dto.setTotalConferenceGames(standing.getTotalConferenceGames());
        } else {
            // Overall record defaults
            dto.setCurrentWins(0);
            dto.setCurrentLosses(0);
            dto.setWinPercentage(0.0);
            dto.setTotalGames(0);
            
            // Conference record defaults
            dto.setConferenceWins(0);
            dto.setConferenceLosses(0);
            dto.setConferenceWinPercentage(0.0);
            dto.setTotalConferenceGames(0);
        }
        
        return dto;
    }
}