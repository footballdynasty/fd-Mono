package com.footballdynasty.controller;

import com.footballdynasty.dto.AchievementDTO;
import com.footballdynasty.entity.Achievement;
import com.footballdynasty.service.AchievementService;
import com.footballdynasty.service.InboxService;
import com.footballdynasty.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/achievements")
@Tag(name = "Achievement Management", description = "APIs for managing football dynasty achievements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AchievementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AchievementController.class);
    
    private final AchievementService achievementService;
    private final InboxService inboxService;
    private final PermissionService permissionService;
    
    @Autowired
    public AchievementController(AchievementService achievementService, InboxService inboxService, PermissionService permissionService) {
        this.achievementService = achievementService;
        this.inboxService = inboxService;
        this.permissionService = permissionService;
    }
    
    @Operation(summary = "Get all achievements with optional filtering and pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved achievements"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    public ResponseEntity<Page<AchievementDTO>> getAllAchievements(
            @Parameter(description = "Filter by achievement type") 
            @RequestParam(required = false) Achievement.AchievementType type,
            
            @Parameter(description = "Filter by achievement rarity") 
            @RequestParam(required = false) Achievement.AchievementRarity rarity,
            
            @Parameter(description = "Filter by completion status") 
            @RequestParam(required = false) Boolean completed,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("GET /achievements - type: {}, rarity: {}, completed: {}, page: {}, size: {}", 
            type, rarity, completed, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AchievementDTO> achievements = achievementService.getAllAchievements(type, rarity, completed, pageable);
        
        logger.debug("Retrieved {} achievements", achievements.getTotalElements());
        return ResponseEntity.ok(achievements);
    }
    
    @Operation(summary = "Get achievement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved achievement"),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AchievementDTO> getAchievementById(
            @Parameter(description = "Achievement ID") 
            @PathVariable UUID id) {
        
        logger.debug("GET /achievements/{}", id);
        
        AchievementDTO achievement = achievementService.getAchievementById(id);
        return ResponseEntity.ok(achievement);
    }
    
    @Operation(summary = "Get achievements by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved achievements"),
        @ApiResponse(responseCode = "400", description = "Invalid achievement type")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AchievementDTO>> getAchievementsByType(
            @Parameter(description = "Achievement type") 
            @PathVariable Achievement.AchievementType type) {
        
        logger.debug("GET /achievements/type/{}", type);
        
        List<AchievementDTO> achievements = achievementService.getAchievementsByType(type);
        
        logger.debug("Retrieved {} achievements of type {}", achievements.size(), type);
        return ResponseEntity.ok(achievements);
    }
    
    @Operation(summary = "Create new achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Achievement created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid achievement data")
    })
    @PostMapping
    public ResponseEntity<AchievementDTO> createAchievement(
            @Parameter(description = "Achievement data") 
            @Valid @RequestBody AchievementDTO achievementDTO) {
        
        logger.info("POST /achievements - creating: {}", achievementDTO.getDescription());
        
        AchievementDTO createdAchievement = achievementService.createAchievement(achievementDTO);
        
        logger.info("Created achievement with ID: {}", createdAchievement.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAchievement);
    }
    
    @Operation(summary = "Update existing achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement updated successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid achievement data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AchievementDTO> updateAchievement(
            @Parameter(description = "Achievement ID") 
            @PathVariable UUID id,
            
            @Parameter(description = "Updated achievement data") 
            @Valid @RequestBody AchievementDTO achievementDTO) {
        
        logger.info("PUT /achievements/{} - updating", id);
        
        AchievementDTO updatedAchievement = achievementService.updateAchievement(id, achievementDTO);
        
        logger.info("Updated achievement: {}", updatedAchievement.getDescription());
        return ResponseEntity.ok(updatedAchievement);
    }
    
    @Operation(summary = "Submit achievement completion request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement request submitted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request or duplicate request")
    })
    @PostMapping("/submit-request")
    public ResponseEntity<?> submitAchievementRequest(@RequestBody Map<String, String> requestBody) {
        
        logger.info("POST /achievements/submit-request");
        
        try {
            // Extract request data
            String achievementIdStr = requestBody.get("achievementId");
            if (achievementIdStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "achievementId is required"));
            }
            
            UUID achievementId = UUID.fromString(achievementIdStr);
            String userId = requestBody.getOrDefault("userId", "user");
            String userDisplayName = requestBody.getOrDefault("userDisplayName", "Anonymous User");
            String teamId = requestBody.getOrDefault("teamId", "");
            String teamName = requestBody.getOrDefault("teamName", "");
            String requestReason = requestBody.getOrDefault("requestReason", "Achievement completed");
            
            // Submit request (regular users only - this endpoint is for non-admin requests)
            logger.info("Regular user submitting achievement request: {} by user: {}", achievementId, userId);
            var request = inboxService.submitAchievementRequest(achievementId, userId, userDisplayName, teamId, teamName, requestReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", request.getId());
            response.put("status", "pending");
            response.put("message", "Achievement completion request submitted for admin review");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            logger.warn("Achievement completion request failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to process achievement completion request: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to process achievement request: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @Operation(summary = "Request achievement completion")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement request submitted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request or duplicate request")
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> requestAchievementCompletion(
            @Parameter(description = "Achievement ID") 
            @PathVariable UUID id,
            @RequestBody Map<String, String> requestBody) {
        
        logger.info("PATCH /achievements/{}/complete", id);
        
        // Extract user information from request body
        String userId = requestBody.getOrDefault("userId", "user");
        String userDisplayName = requestBody.getOrDefault("userDisplayName", "Anonymous User");
        String teamId = requestBody.getOrDefault("teamId", "");
        String teamName = requestBody.getOrDefault("teamName", "");
        String requestReason = requestBody.getOrDefault("requestReason", "Achievement completed");
        
        // Check if current user is actually a commissioner (don't trust frontend)
        boolean isCommissioner = permissionService.isCurrentUserCommissioner();
        
        try {
            if (isCommissioner) {
                // Commissioner users can complete achievements directly
                logger.info("Commissioner user completing achievement directly: {}", id);
                Long userLong = Long.parseLong(userId);
                AchievementDTO completedAchievement = achievementService.completeAchievement(id, userLong);
                
                Map<String, Object> response = new HashMap<>();
                response.put("achievement", completedAchievement);
                response.put("status", "completed");
                response.put("message", "Achievement completed successfully");
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            } else {
                // Regular users submit requests for approval
                logger.info("Regular user submitting achievement request: {} by user: {}", id, userId);
                var request = inboxService.submitAchievementRequest(id, userId, userDisplayName, teamId, teamName, requestReason);
                
                Map<String, Object> response = new HashMap<>();
                response.put("requestId", request.getId());
                response.put("status", "pending");
                response.put("message", "Achievement completion request submitted for admin review");
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            }
        } catch (IllegalStateException e) {
            logger.warn("Achievement completion request failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to process achievement completion request for {}: {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to process achievement request: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @Operation(summary = "Reset achievement completion (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement reset successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    @PatchMapping("/{id}/reset")
    public ResponseEntity<AchievementDTO> resetAchievement(
            @Parameter(description = "Achievement ID") 
            @PathVariable UUID id) {
        
        logger.info("PATCH /achievements/{}/reset", id);
        
        AchievementDTO resetAchievement = achievementService.resetAchievement(id);
        
        logger.info("Reset achievement: {}", resetAchievement.getDescription());
        return ResponseEntity.ok(resetAchievement);
    }
    
    @Operation(summary = "Delete achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Achievement deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAchievement(
            @Parameter(description = "Achievement ID") 
            @PathVariable UUID id) {
        
        logger.info("DELETE /achievements/{}", id);
        
        achievementService.deleteAchievement(id);
        
        logger.info("Deleted achievement with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get achievement statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved achievement statistics")
    })
    @GetMapping("/stats")
    public ResponseEntity<AchievementService.AchievementStatsDTO> getAchievementStats() {
        logger.debug("GET /achievements/stats");
        
        AchievementService.AchievementStatsDTO stats = achievementService.getAchievementStats();
        
        logger.debug("Retrieved achievement stats: {} total, {} completed", 
            stats.getTotalAchievements(), stats.getCompletedAchievements());
        
        return ResponseEntity.ok(stats);
    }
}