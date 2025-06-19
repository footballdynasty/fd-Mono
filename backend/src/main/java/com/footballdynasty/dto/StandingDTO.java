package com.footballdynasty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Standing entity representing team standings data for API responses.
 * Contains all standing information including team reference, win/loss records,
 * rankings, and calculated statistics.
 */
public class StandingDTO {
    
    private UUID id;
    
    @NotNull(message = "Team information is required")
    private TeamDTO team;
    
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    private Integer year;
    
    @Min(value = 0, message = "Wins cannot be negative")
    private Integer wins;
    
    @Min(value = 0, message = "Losses cannot be negative")
    private Integer losses;
    
    @JsonProperty("conference_wins")
    @Min(value = 0, message = "Conference wins cannot be negative")
    private Integer conferenceWins;
    
    @JsonProperty("conference_losses")
    @Min(value = 0, message = "Conference losses cannot be negative")
    private Integer conferenceLosses;
    
    private Integer rank;
    
    @JsonProperty("conference_rank")
    private Integer conferenceRank;
    
    @JsonProperty("receiving_votes")
    private Integer receivingVotes;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    // Calculated fields (read-only)
    @JsonProperty("win_percentage")
    private Double winPercentage;
    
    @JsonProperty("total_games")
    private Integer totalGames;
    
    @JsonProperty("conference_win_percentage")
    private Double conferenceWinPercentage;
    
    @JsonProperty("total_conference_games")
    private Integer totalConferenceGames;

    public StandingDTO() {}

    public StandingDTO(TeamDTO team, Integer year) {
        this.team = team;
        this.year = year;
        this.wins = 0;
        this.losses = 0;
        this.conferenceWins = 0;
        this.conferenceLosses = 0;
        this.receivingVotes = 0;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TeamDTO getTeam() {
        return team;
    }

    public void setTeam(TeamDTO team) {
        this.team = team;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public Integer getConferenceWins() {
        return conferenceWins;
    }

    public void setConferenceWins(Integer conferenceWins) {
        this.conferenceWins = conferenceWins;
    }

    public Integer getConferenceLosses() {
        return conferenceLosses;
    }

    public void setConferenceLosses(Integer conferenceLosses) {
        this.conferenceLosses = conferenceLosses;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getConferenceRank() {
        return conferenceRank;
    }

    public void setConferenceRank(Integer conferenceRank) {
        this.conferenceRank = conferenceRank;
    }

    public Integer getReceivingVotes() {
        return receivingVotes;
    }

    public void setReceivingVotes(Integer receivingVotes) {
        this.receivingVotes = receivingVotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(Double winPercentage) {
        this.winPercentage = winPercentage;
    }

    public Integer getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(Integer totalGames) {
        this.totalGames = totalGames;
    }

    public Double getConferenceWinPercentage() {
        return conferenceWinPercentage;
    }

    public void setConferenceWinPercentage(Double conferenceWinPercentage) {
        this.conferenceWinPercentage = conferenceWinPercentage;
    }

    public Integer getTotalConferenceGames() {
        return totalConferenceGames;
    }

    public void setTotalConferenceGames(Integer totalConferenceGames) {
        this.totalConferenceGames = totalConferenceGames;
    }
}