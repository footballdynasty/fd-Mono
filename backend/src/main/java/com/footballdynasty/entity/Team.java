package com.footballdynasty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    @NotBlank(message = "Team name is required")
    private String name;
    
    private String coach;
    
    private String username;
    
    private String conference;
    
    @Column(name = "is_human")
    private Boolean isHuman = false;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Standing> standings;
    
    @OneToMany(mappedBy = "homeTeam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Game> homeGames;
    
    @OneToMany(mappedBy = "awayTeam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Game> awayGames;

    public Team() {}

    public Team(String name, String coach, String conference) {
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
    
    public List<Standing> getStandings() { return standings; }
    public void setStandings(List<Standing> standings) { this.standings = standings; }
    
    public List<Game> getHomeGames() { return homeGames; }
    public void setHomeGames(List<Game> homeGames) { this.homeGames = homeGames; }
    
    public List<Game> getAwayGames() { return awayGames; }
    public void setAwayGames(List<Game> awayGames) { this.awayGames = awayGames; }
}