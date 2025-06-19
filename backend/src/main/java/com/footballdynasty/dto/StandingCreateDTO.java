package com.footballdynasty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating new Standing entities.
 * Contains required fields for creating a standing record.
 */
public class StandingCreateDTO {
    
    @NotNull(message = "Team ID is required")
    @JsonProperty("team_id")
    private UUID teamId;
    
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    private Integer year;
    
    @Min(value = 0, message = "Wins cannot be negative")
    private Integer wins = 0;
    
    @Min(value = 0, message = "Losses cannot be negative")
    private Integer losses = 0;
    
    @JsonProperty("conference_wins")
    @Min(value = 0, message = "Conference wins cannot be negative")
    private Integer conferenceWins = 0;
    
    @JsonProperty("conference_losses")
    @Min(value = 0, message = "Conference losses cannot be negative")
    private Integer conferenceLosses = 0;
    
    private Integer rank;
    
    @JsonProperty("conference_rank")
    private Integer conferenceRank;
    
    @JsonProperty("receiving_votes")
    private Integer receivingVotes = 0;

    public StandingCreateDTO() {}

    public StandingCreateDTO(UUID teamId, Integer year) {
        this.teamId = teamId;
        this.year = year;
    }

    // Getters and Setters
    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
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
}