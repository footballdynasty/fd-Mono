package com.footballdynasty.service;

import com.footballdynasty.entity.Role;
import com.footballdynasty.entity.User;
import com.footballdynasty.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public PermissionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Check if the current authenticated user is a commissioner
     */
    public boolean isCurrentUserCommissioner() {
        return getCurrentUser()
            .map(user -> user.hasRole(Role.COMMISSIONER))
            .orElse(false);
    }
    
    /**
     * Check if a specific user (by username) is a commissioner
     */
    public boolean isUserCommissioner(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.findByUsername(username)
            .map(user -> user.hasRole(Role.COMMISSIONER))
            .orElse(false);
    }
    
    /**
     * Get the current authenticated user
     */
    public Optional<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                String username = authentication.getName();
                return userRepository.findByUsername(username);
            }
        } catch (Exception e) {
            logger.error("Error getting current user from security context", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated username
     */
    public Optional<String> getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                return Optional.of(authentication.getName());
            }
        } catch (Exception e) {
            logger.error("Error getting current username from security context", e);
        }
        
        return Optional.empty();
    }
}