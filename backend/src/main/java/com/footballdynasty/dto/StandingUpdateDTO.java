package com.footballdynasty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;

/**
 * DTO for updating existing Standing entities.
 * Contains fields that can be updated on a standing record.
 * Team and year are not updateable once set.
 */
public class StandingUpdateDTO {
    
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

    public StandingUpdateDTO() {}

    // Getters and Setters
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