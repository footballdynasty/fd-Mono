package com.footballdynasty.dto;

public class AuthResponse {
    
    private UserDTO user;
    private String token;
    private TeamDTO selectedTeam;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(UserDTO user, String token) {
        this.user = user;
        this.token = token;
    }
    
    public AuthResponse(UserDTO user, String token, TeamDTO selectedTeam) {
        this.user = user;
        this.token = token;
        this.selectedTeam = selectedTeam;
    }
    
    // Getters and Setters
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public TeamDTO getSelectedTeam() {
        return selectedTeam;
    }
    
    public void setSelectedTeam(TeamDTO selectedTeam) {
        this.selectedTeam = selectedTeam;
    }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "user=" + user +
                ", token='[PROTECTED]'" +
                ", selectedTeam=" + selectedTeam +
                '}';
    }
}