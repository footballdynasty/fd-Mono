package com.footballdynasty.service;

import com.footballdynasty.dto.*;
import com.footballdynasty.entity.Team;
import com.footballdynasty.entity.User;
import com.footballdynasty.exception.ResourceNotFoundException;
import com.footballdynasty.mapper.TeamMapper;
import com.footballdynasty.mapper.UserMapper;
import com.footballdynasty.repository.TeamRepository;
import com.footballdynasty.repository.UserRepository;
import com.footballdynasty.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    
    @Autowired
    public AuthService(UserRepository userRepository, 
                      TeamRepository teamRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager,
                      UserMapper userMapper,
                      TeamMapper teamMapper) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.teamMapper = teamMapper;
    }
    
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Find user with selected team
            User user = userRepository.findByUsernameWithSelectedTeam(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Generate JWT token with user info
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("roles", user.getRoles());
            
            String token = jwtUtil.generateToken(user.getUsername(), claims);
            
            // Convert to DTOs
            UserDTO userDTO = userMapper.toDTO(user);
            TeamDTO selectedTeamDTO = user.getSelectedTeam() != null ? 
                teamMapper.toDTO(user.getSelectedTeam()) : null;
            
            return new AuthResponse(userDTO, token, selectedTeamDTO);
            
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
    
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists (only if email is provided)
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty() 
            && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        // Save user
        user = userRepository.save(user);
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles());
        
        String token = jwtUtil.generateToken(user.getUsername(), claims);
        
        // Convert to DTO
        UserDTO userDTO = userMapper.toDTO(user);
        
        return new AuthResponse(userDTO, token);
    }
    
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsernameWithSelectedTeam(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toDTO(user);
    }
    
    public UserDTO selectTeam(String username, UUID teamId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Find and mark the team as human-controlled
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        
        team.setIsHuman(true);
        teamRepository.save(team);
        
        user.setSelectedTeamId(teamId);
        user = userRepository.save(user);
        
        // Fetch user with selected team
        user = userRepository.findByUsernameWithSelectedTeam(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toDTO(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}