package com.footballdynasty.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public class TeamDTO {
    
    private UUID id;
    
    @NotBlank(message = "Team name is required")
    private String name;
    
    private String coach;
    private String username;
    private String conference;
    private Boolean isHuman;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for enhanced display
    private Integer currentWins;
    private Integer currentLosses;
    private Double winPercentage;
    private Integer currentRank;
    private Integer totalGames;
    
    // Conference-specific fields
    private Integer conferenceWins;
    private Integer conferenceLosses;
    private Double conferenceWinPercentage;
    private Integer conferenceRank;
    private Integer totalConferenceGames;

    public TeamDTO() {}

    public TeamDTO(String name, String coach, String conference) {
        this.name = name;
        this.coach = coach;
        this.conference = conference;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getConference() { return conference; }
    public void setConference(String conference) { this.conference = conference; }
    
    public Boolean getIsHuman() { return isHuman; }
    public void setIsHuman(Boolean isHuman) { this.isHuman = isHuman; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getCurrentWins() { return currentWins; }
    public void setCurrentWins(Integer currentWins) { this.currentWins = currentWins; }
    
    public Integer getCurrentLosses() { return currentLosses; }
    public void setCurrentLosses(Integer currentLosses) { this.currentLosses = currentLosses; }
    
    public Double getWinPercentage() { return winPercentage; }
    public void setWinPercentage(Double winPercentage) { this.winPercentage = winPercentage; }
    
    public Integer getCurrentRank() { return currentRank; }
    public void setCurrentRank(Integer currentRank) { this.currentRank = currentRank; }
    
    public Integer getTotalGames() { return totalGames; }
    public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
    
    public Integer getConferenceWins() { return conferenceWins; }
    public void setConferenceWins(Integer conferenceWins) { this.conferenceWins = conferenceWins; }
    
    public Integer getConferenceLosses() { return conferenceLosses; }
    public void setConferenceLosses(Integer conferenceLosses) { this.conferenceLosses = conferenceLosses; }
    
    public Double getConferenceWinPercentage() { return conferenceWinPercentage; }
    public void setConferenceWinPercentage(Double conferenceWinPercentage) { this.conferenceWinPercentage = conferenceWinPercentage; }
    
    public Integer getConferenceRank() { return conferenceRank; }
    public void setConferenceRank(Integer conferenceRank) { this.conferenceRank = conferenceRank; }
    
    public Integer getTotalConferenceGames() { return totalConferenceGames; }
    public void setTotalConferenceGames(Integer totalConferenceGames) { this.totalConferenceGames = totalConferenceGames; }
}