package com.footballdynasty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "standings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "year"})
})
public class Standing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @NotNull
    @Min(value = 1900, message = "Year must be valid")
    private Integer year;
    
    @Min(value = 0, message = "Wins cannot be negative")
    private Integer wins = 0;
    
    @Min(value = 0, message = "Losses cannot be negative")
    private Integer losses = 0;
    
    // Conference-specific standings
    @Column(name = "conference_wins")
    @Min(value = 0, message = "Conference wins cannot be negative")
    private Integer conferenceWins = 0;
    
    @Column(name = "conference_losses")
    @Min(value = 0, message = "Conference losses cannot be negative")
    private Integer conferenceLosses = 0;
    
    private Integer rank;
    
    @Column(name = "conference_rank")
    private Integer conferenceRank;
    
    @Column(name = "receiving_votes")
    private Integer receivingVotes = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Standing() {}

    public Standing(Team team, Integer year) {
        this.team = team;
        this.year = year;
    }

    // Calculated properties
    public Double getWinPercentage() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) wins / totalGames : 0.0;
    }

    public Integer getTotalGames() {
        return wins + losses;
    }
    
    public Double getConferenceWinPercentage() {
        int totalConferenceGames = conferenceWins + conferenceLosses;
        return totalConferenceGames > 0 ? (double) conferenceWins / totalConferenceGames : 0.0;
    }

    public Integer getTotalConferenceGames() {
        return conferenceWins + conferenceLosses;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getWins() { return wins; }
    public void setWins(Integer wins) { this.wins = wins; }
    
    public Integer getLosses() { return losses; }
    public void setLosses(Integer losses) { this.losses = losses; }
    
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    
    public Integer getConferenceWins() { return conferenceWins; }
    public void setConferenceWins(Integer conferenceWins) { this.conferenceWins = conferenceWins; }
    
    public Integer getConferenceLosses() { return conferenceLosses; }
    public void setConferenceLosses(Integer conferenceLosses) { this.conferenceLosses = conferenceLosses; }
    
    public Integer getConferenceRank() { return conferenceRank; }
    public void setConferenceRank(Integer conferenceRank) { this.conferenceRank = conferenceRank; }
    
    public Integer getReceivingVotes() { return receivingVotes; }
    public void setReceivingVotes(Integer receivingVotes) { this.receivingVotes = receivingVotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}