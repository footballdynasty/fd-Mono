package com.footballdynasty.controller;

import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import com.footballdynasty.service.StandingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Managing Team Standings.
 * Provides endpoints for CRUD operations, filtering, and standings calculations.
 * 
 * Base URL: /api/v2/standings (configured via context-path)
 */
@RestController
@RequestMapping("/standings")
@Tag(name = "Standings", description = "Team standings management endpoints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class StandingController {
    
    private static final Logger logger = LoggerFactory.getLogger(StandingController.class);
    
    private final StandingService standingService;
    
    public StandingController(StandingService standingService) {
        this.standingService = standingService;
    }
    
    @GetMapping
    @Operation(summary = "Get all standings", 
               description = "Retrieve all standings with optional filtering by year and conference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved standings"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<StandingDTO>> getAllStandings(
            @Parameter(description = "Filter by year (e.g., 2024)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by conference (e.g., 'ACC', 'SEC')")
            @RequestParam(required = false) String conference,
            Pageable pageable) {
        
        logger.info("GET /standings - year: {}, conference: {}, page: {}", 
            year, conference, pageable.getPageNumber());
        
        try {
            Page<StandingDTO> standings;
            
            if (year != null || (conference != null && !conference.trim().isEmpty())) {
                standings = standingService.findByYearAndConference(year, conference, pageable);
            } else {
                standings = standingService.findAll(pageable);
            }
            
            logger.debug("Retrieved {} standings (total: {})", 
                standings.getNumberOfElements(), standings.getTotalElements());
            
            return ResponseEntity.ok(standings);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve standings - year: {}, conference: {} - {}", 
                year, conference, e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get standing by ID", description = "Retrieve a specific standing by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved standing"),
        @ApiResponse(responseCode = "404", description = "Standing not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandingDTO> getStandingById(
            @Parameter(description = "Standing ID") @PathVariable UUID id) {
        
        logger.info("GET /standings/{}", id);
        
        StandingDTO standing = standingService.findById(id);
        logger.debug("Retrieved standing for team ID: {}", 
            standing.getTeam() != null ? standing.getTeam().getId() : "unknown");
        
        return ResponseEntity.ok(standing);
    }
    
    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get standings by team", 
               description = "Retrieve all standings for a specific team across years")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved team standings"),
        @ApiResponse(responseCode = "404", description = "Team not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<StandingDTO>> getStandingsByTeam(
            @Parameter(description = "Team ID") @PathVariable UUID teamId,
            Pageable pageable) {
        
        logger.info("GET /standings/team/{} - page: {}", teamId, pageable.getPageNumber());
        
        Page<StandingDTO> standings = standingService.findByTeam(teamId, pageable);
        logger.debug("Retrieved {} standings for team {}", 
            standings.getNumberOfElements(), teamId);
        
        return ResponseEntity.ok(standings);
    }
    
    @GetMapping("/team/{teamId}/year/{year}")
    @Operation(summary = "Get standing by team and year", 
               description = "Retrieve specific standing for a team in a specific year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved standing"),
        @ApiResponse(responseCode = "404", description = "Standing not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandingDTO> getStandingByTeamAndYear(
            @Parameter(description = "Team ID") @PathVariable UUID teamId,
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        logger.info("GET /standings/team/{}/year/{}", teamId, year);
        
        StandingDTO standing = standingService.findByTeamAndYear(teamId, year);
        logger.debug("Retrieved standing for team {} in year {}", teamId, year);
        
        return ResponseEntity.ok(standing);
    }
    
    @GetMapping("/conference/{conference}/year/{year}")
    @Operation(summary = "Get conference standings", 
               description = "Retrieve standings for a specific conference and year, ordered by rank")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved conference standings"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StandingDTO>> getConferenceStandings(
            @Parameter(description = "Conference name") @PathVariable String conference,
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        logger.info("GET /standings/conference/{}/year/{}", conference, year);
        
        List<StandingDTO> standings = standingService.findByConferenceAndYear(conference, year);
        logger.debug("Retrieved {} standings for conference '{}' in year {}", 
            standings.size(), conference, year);
        
        return ResponseEntity.ok(standings);
    }
    
    @GetMapping("/ranked/year/{year}")
    @Operation(summary = "Get top ranked teams", 
               description = "Retrieve top ranked teams for a specific year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved ranked teams"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StandingDTO>> getTopRankedTeams(
            @Parameter(description = "Year") @PathVariable Integer year,
            @Parameter(description = "Maximum number of teams to return (default: 25)")
            @RequestParam(defaultValue = "25") int limit) {
        
        logger.info("GET /standings/ranked/year/{} - limit: {}", year, limit);
        
        List<StandingDTO> standings = standingService.findTopRankedByYear(year, limit);
        logger.debug("Retrieved {} top ranked teams for year {}", standings.size(), year);
        
        return ResponseEntity.ok(standings);
    }
    
    @GetMapping("/votes/year/{year}")
    @Operation(summary = "Get teams receiving votes", 
               description = "Retrieve teams receiving votes for a specific year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved teams receiving votes"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StandingDTO>> getTeamsReceivingVotes(
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        logger.info("GET /standings/votes/year/{}", year);
        
        List<StandingDTO> standings = standingService.findTeamsReceivingVotesByYear(year);
        logger.debug("Retrieved {} teams receiving votes for year {}", standings.size(), year);
        
        return ResponseEntity.ok(standings);
    }
    
    @PostMapping
    @Operation(summary = "Create new standing", description = "Create a new standing record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created standing"),
        @ApiResponse(responseCode = "400", description = "Invalid standing data"),
        @ApiResponse(responseCode = "409", description = "Standing already exists for team and year"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandingDTO> createStanding(
            @Valid @RequestBody StandingCreateDTO createDTO) {
        
        logger.info("POST /standings - teamId: {}, year: {}", 
            createDTO.getTeamId(), createDTO.getYear());
        
        StandingDTO standing = standingService.create(createDTO);
        logger.info("Created standing with ID: {}", standing.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(standing);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update standing", description = "Update an existing standing record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated standing"),
        @ApiResponse(responseCode = "404", description = "Standing not found"),
        @ApiResponse(responseCode = "400", description = "Invalid standing data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandingDTO> updateStanding(
            @Parameter(description = "Standing ID") @PathVariable UUID id,
            @Valid @RequestBody StandingUpdateDTO updateDTO) {
        
        logger.info("PUT /standings/{}", id);
        
        StandingDTO standing = standingService.update(id, updateDTO);
        logger.info("Updated standing with ID: {}", standing.getId());
        
        return ResponseEntity.ok(standing);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete standing", description = "Delete a standing record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted standing"),
        @ApiResponse(responseCode = "404", description = "Standing not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteStanding(
            @Parameter(description = "Standing ID") @PathVariable UUID id) {
        
        logger.info("DELETE /standings/{}", id);
        
        standingService.delete(id);
        logger.info("Deleted standing with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/calculate/{year}")
    @Operation(summary = "Calculate standings", 
               description = "Trigger standings calculation for all conferences in a specific year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated standings"),
        @ApiResponse(responseCode = "400", description = "Invalid year"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> calculateStandings(
            @Parameter(description = "Year to calculate standings for") @PathVariable Integer year) {
        
        logger.info("POST /standings/calculate/{}", year);
        
        try {
            standingService.calculateStandings(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Standings calculation completed successfully");
            response.put("year", year);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Completed standings calculation for year {}", year);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to calculate standings for year {} - {}", year, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to calculate standings: " + e.getMessage());
            errorResponse.put("year", year);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/calculate/conference/{conference}/year/{year}")
    @Operation(summary = "Calculate conference standings", 
               description = "Trigger standings calculation for a specific conference and year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated conference standings"),
        @ApiResponse(responseCode = "400", description = "Invalid conference or year"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> calculateConferenceStandings(
            @Parameter(description = "Conference name") @PathVariable String conference,
            @Parameter(description = "Year to calculate standings for") @PathVariable Integer year) {
        
        logger.info("POST /standings/calculate/conference/{}/year/{}", conference, year);
        
        try {
            standingService.calculateConferenceStandings(conference, year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Conference standings calculation completed successfully");
            response.put("conference", conference);
            response.put("year", year);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Completed standings calculation for conference '{}' in year {}", conference, year);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to calculate standings for conference '{}', year {} - {}", 
                conference, year, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to calculate conference standings: " + e.getMessage());
            errorResponse.put("conference", conference);
            errorResponse.put("year", year);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}