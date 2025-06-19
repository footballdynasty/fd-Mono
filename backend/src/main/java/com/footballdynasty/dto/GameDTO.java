package com.footballdynasty.dto;

import com.footballdynasty.entity.Game;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class GameDTO {
    
    private UUID id;
    private String gameId;
    
    @NotNull
    private String homeTeamId;
    private String homeTeamName;
    private String homeTeamImageUrl;
    
    @NotNull
    private String awayTeamId;
    private String awayTeamName;
    private String awayTeamImageUrl;
    
    private Integer homeScore;
    private Integer awayScore;
    
    @NotNull
    private LocalDate date;
    
    private String weekId;
    private Integer weekNumber;
    private Integer year;
    
    private Integer homeTeamRank;
    private Integer awayTeamRank;
    private Game.GameStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional display fields
    private String statusDisplay;
    private String scoreDisplay;
    private Boolean isCompleted;
    private String winnerName;

    public GameDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public String getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(String homeTeamId) { this.homeTeamId = homeTeamId; }
    
    public String getHomeTeamName() { return homeTeamName; }
    public void setHomeTeamName(String homeTeamName) { this.homeTeamName = homeTeamName; }
    
    public String getHomeTeamImageUrl() { return homeTeamImageUrl; }
    public void setHomeTeamImageUrl(String homeTeamImageUrl) { this.homeTeamImageUrl = homeTeamImageUrl; }
    
    public String getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(String awayTeamId) { this.awayTeamId = awayTeamId; }
    
    public String getAwayTeamName() { return awayTeamName; }
    public void setAwayTeamName(String awayTeamName) { this.awayTeamName = awayTeamName; }
    
    public String getAwayTeamImageUrl() { return awayTeamImageUrl; }
    public void setAwayTeamImageUrl(String awayTeamImageUrl) { this.awayTeamImageUrl = awayTeamImageUrl; }
    
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getWeekId() { return weekId; }
    public void setWeekId(String weekId) { this.weekId = weekId; }
    
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getHomeTeamRank() { return homeTeamRank; }
    public void setHomeTeamRank(Integer homeTeamRank) { this.homeTeamRank = homeTeamRank; }
    
    public Integer getAwayTeamRank() { return awayTeamRank; }
    public void setAwayTeamRank(Integer awayTeamRank) { this.awayTeamRank = awayTeamRank; }
    
    public Game.GameStatus getStatus() { return status; }
    public void setStatus(Game.GameStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }
    
    public String getScoreDisplay() { return scoreDisplay; }
    public void setScoreDisplay(String scoreDisplay) { this.scoreDisplay = scoreDisplay; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
}