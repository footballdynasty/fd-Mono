package com.footballdynasty.controller;

import com.footballdynasty.dto.*;
import com.footballdynasty.service.AuthService;
import com.footballdynasty.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for username: {}", loginRequest.getUsername());
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            logger.info("Login successful for username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            logger.error("Login failed for username: {}, error: {}", loginRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create new user account and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or user already exists"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration attempt for username: {}, email: {}", registerRequest.getUsername(), registerRequest.getEmail());
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            logger.info("Registration successful for username: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed for username: {}, error: {}", registerRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            logger.error("Registration failed for username: {}, error: {}", registerRequest.getUsername(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        try {
            UserDTO user = authService.getCurrentUser(userPrincipal.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/select-team")
    @Operation(summary = "Select team", description = "Set the user's selected team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team selected successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User or team not found")
    })
    public ResponseEntity<?> selectTeam(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            @RequestBody Map<String, UUID> request) {
        try {
            UUID teamId = request.get("teamId");
            if (teamId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Team ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            UserDTO user = authService.selectTeam(userPrincipal.getUsername(), teamId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout current user (client-side token removal)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully logged out")
    })
    public ResponseEntity<?> logout() {
        // Since JWT is stateless, logout is handled client-side by removing the token
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate current JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid")
    })
    public ResponseEntity<?> validateToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", authentication.getName());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Public test endpoint to verify backend is working")
    @ApiResponse(responseCode = "200", description = "Backend is working")
    public ResponseEntity<?> testEndpoint() {
        logger.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Backend is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}