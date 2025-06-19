package com.footballdynasty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "game_id", nullable = false)
    private String gameId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;
    
    @Column(name = "home_score")
    private Integer homeScore = 0;
    
    @Column(name = "away_score")
    private Integer awayScore = 0;
    
    @NotNull
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;
    
    @Column(name = "home_team_rank")
    private Integer homeTeamRank = 0;
    
    @Column(name = "away_team_rank")
    private Integer awayTeamRank = 0;
    
    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.SCHEDULED;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum GameStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Game() {}

    public Game(String gameId, Team homeTeam, Team awayTeam, LocalDate date, Week week) {
        this.gameId = gameId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.week = week;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Week getWeek() { return week; }
    public void setWeek(Week week) { this.week = week; }
    
    public Integer getHomeTeamRank() { return homeTeamRank; }
    public void setHomeTeamRank(Integer homeTeamRank) { this.homeTeamRank = homeTeamRank; }
    
    public Integer getAwayTeamRank() { return awayTeamRank; }
    public void setAwayTeamRank(Integer awayTeamRank) { this.awayTeamRank = awayTeamRank; }
    
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}