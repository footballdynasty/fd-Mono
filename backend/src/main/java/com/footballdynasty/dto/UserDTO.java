package com.footballdynasty.dto;

import com.footballdynasty.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private UUID selectedTeamId;
    private TeamDTO selectedTeam;
    private Set<Role> roles;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserDTO() {}
    
    public UserDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UUID getSelectedTeamId() {
        return selectedTeamId;
    }
    
    public void setSelectedTeamId(UUID selectedTeamId) {
        this.selectedTeamId = selectedTeamId;
    }
    
    public TeamDTO getSelectedTeam() {
        return selectedTeam;
    }
    
    public void setSelectedTeam(TeamDTO selectedTeam) {
        this.selectedTeam = selectedTeam;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", selectedTeamId=" + selectedTeamId +
                ", roles=" + roles +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}