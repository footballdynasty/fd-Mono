package com.footballdynasty.controller;

import com.footballdynasty.entity.Standing;
import com.footballdynasty.service.ConferenceStandingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conference-standings")
@CrossOrigin(origins = "*")
public class ConferenceStandingsController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConferenceStandingsController.class);
    
    private final ConferenceStandingsService conferenceStandingsService;
    
    @Autowired
    public ConferenceStandingsController(ConferenceStandingsService conferenceStandingsService) {
        this.conferenceStandingsService = conferenceStandingsService;
    }
    
    /**
     * Calculate conference standings for a specific year
     */
    @PostMapping("/calculate/{year}")
    public ResponseEntity<String> calculateConferenceStandings(@PathVariable Integer year) {
        logger.info("ENDPOINT_ENTRY: POST /calculate/{} - calculateConferenceStandings", year);
        
        try {
            // Input validation
            if (year == null) {
                logger.error("VALIDATION_ERROR: year path variable is null");
                return ResponseEntity.badRequest().body("Year parameter is required");
            }
            
            if (year < 1900 || year > 2100) {
                logger.error("VALIDATION_ERROR: invalid year={} - must be between 1900 and 2100", year);
                return ResponseEntity.badRequest().body("Year must be between 1900 and 2100");
            }
            
            logger.info("REQUEST_PROCESSING: Starting conference standings calculation for year={}", year);
            
            // Delegate to service
            logger.info("SERVICE_CALL: Calling conferenceStandingsService.calculateConferenceStandings({})", year);
            long startTime = System.currentTimeMillis();
            
            try {
                conferenceStandingsService.calculateConferenceStandings(year);
                logger.info("SERVICE_SUCCESS: calculateConferenceStandings completed successfully");
            } catch (Exception e) {
                logger.error("SERVICE_ERROR: calculateConferenceStandings failed - {}", e.getMessage(), e);
                throw e;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("SERVICE_COMPLETED: Conference standings calculation completed in {}ms for year={}", duration, year);
            
            String successMessage = "Conference standings calculated successfully for year " + year;
            logger.info("RESPONSE_SUCCESS: {}", successMessage);
            
            return ResponseEntity.ok(successMessage);
            
        } catch (IllegalArgumentException e) {
            logger.error("VALIDATION_ERROR: Invalid input for year={} - {}", year, e.getMessage());
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Error calculating conference standings for year={} - {}", year, e.getMessage(), e);
            String errorMessage = "Error calculating conference standings: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
            
        } finally {
            logger.info("ENDPOINT_EXIT: POST /calculate/{} - calculateConferenceStandings", year);
        }
    }
    
    /**
     * Calculate conference standings for a specific conference and year
     */
    @PostMapping("/calculate/{conference}/{year}")
    public ResponseEntity<String> calculateConferenceStandingsForConference(
            @PathVariable String conference, 
            @PathVariable Integer year) {
        logger.info("ENDPOINT_ENTRY: POST /calculate/{}/{} - calculateConferenceStandingsForConference", conference, year);
        
        try {
            // Input validation
            if (conference == null || conference.trim().isEmpty()) {
                logger.error("VALIDATION_ERROR: conference path variable is null or empty");
                return ResponseEntity.badRequest().body("Conference parameter is required");
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: year path variable is null");
                return ResponseEntity.badRequest().body("Year parameter is required");
            }
            
            if (year < 1900 || year > 2100) {
                logger.error("VALIDATION_ERROR: invalid year={} - must be between 1900 and 2100", year);
                return ResponseEntity.badRequest().body("Year must be between 1900 and 2100");
            }
            
            logger.info("REQUEST_PROCESSING: Starting standings calculation for conference='{}', year={}", conference, year);
            
            // Delegate to service
            logger.debug("SERVICE_CALL: Calling conferenceStandingsService.calculateConferenceStandingsForConference('{}', {})", 
                conference, year);
            long startTime = System.currentTimeMillis();
            
            conferenceStandingsService.calculateConferenceStandingsForConference(conference, year);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("SERVICE_COMPLETED: Conference standings calculation completed in {}ms for conference='{}', year={}", 
                duration, conference, year);
            
            String successMessage = "Conference standings calculated successfully for " + conference + " in " + year;
            logger.info("RESPONSE_SUCCESS: {}", successMessage);
            
            return ResponseEntity.ok(successMessage);
            
        } catch (IllegalArgumentException e) {
            logger.error("VALIDATION_ERROR: Invalid input for conference='{}', year={} - {}", conference, year, e.getMessage());
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Error calculating standings for conference='{}', year={} - {}", 
                conference, year, e.getMessage(), e);
            String errorMessage = "Error calculating conference standings: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
            
        } finally {
            logger.info("ENDPOINT_EXIT: POST /calculate/{}/{} - calculateConferenceStandingsForConference", conference, year);
        }
    }
    
    /**
     * Get conference standings for a specific conference and year
     */
    @GetMapping("/{conference}/{year}")
    public ResponseEntity<List<Standing>> getConferenceStandings(
            @PathVariable String conference, 
            @PathVariable Integer year) {
        logger.info("ENDPOINT_ENTRY: GET /{}/{} - getConferenceStandings", conference, year);
        
        try {
            // Input validation
            if (conference == null || conference.trim().isEmpty()) {
                logger.error("VALIDATION_ERROR: conference path variable is null or empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: year path variable is null");
                return ResponseEntity.badRequest().build();
            }
            
            if (year < 1900 || year > 2100) {
                logger.error("VALIDATION_ERROR: invalid year={} - must be between 1900 and 2100", year);
                return ResponseEntity.badRequest().build();
            }
            
            logger.info("REQUEST_PROCESSING: Fetching standings for conference='{}', year={}", conference, year);
            
            // Delegate to service
            logger.debug("SERVICE_CALL: Calling conferenceStandingsService.getConferenceStandings('{}', {})", 
                conference, year);
            long startTime = System.currentTimeMillis();
            
            List<Standing> standings = conferenceStandingsService.getConferenceStandings(conference, year);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("SERVICE_COMPLETED: Retrieved {} standings in {}ms for conference='{}', year={}", 
                standings.size(), duration, conference, year);
            
            if (standings.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No standings found for conference='{}', year={}", conference, year);
            }
            
            logger.info("RESPONSE_SUCCESS: Returning {} standings for conference='{}', year={}", 
                standings.size(), conference, year);
            
            return ResponseEntity.ok(standings);
            
        } catch (IllegalArgumentException e) {
            logger.error("VALIDATION_ERROR: Invalid input for conference='{}', year={} - {}", conference, year, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Error retrieving standings for conference='{}', year={} - {}", 
                conference, year, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
            
        } finally {
            logger.info("ENDPOINT_EXIT: GET /{}/{} - getConferenceStandings", conference, year);
        }
    }
    
    /**
     * Get all conference standings for a specific year, grouped by conference
     */
    @GetMapping("/all/{year}")
    public ResponseEntity<Map<String, List<Standing>>> getAllConferenceStandings(@PathVariable Integer year) {
        logger.info("ENDPOINT_ENTRY: GET /all/{} - getAllConferenceStandings", year);
        
        try {
            // Input validation
            if (year == null) {
                logger.error("VALIDATION_ERROR: year path variable is null");
                return ResponseEntity.badRequest().build();
            }
            
            if (year < 1900 || year > 2100) {
                logger.error("VALIDATION_ERROR: invalid year={} - must be between 1900 and 2100", year);
                return ResponseEntity.badRequest().build();
            }
            
            logger.info("REQUEST_PROCESSING: Fetching all conference standings for year={}", year);
            
            // Delegate to service
            logger.info("SERVICE_CALL: Calling conferenceStandingsService.getAllConferenceStandings({})", year);
            long startTime = System.currentTimeMillis();
            
            Map<String, List<Standing>> standings;
            try {
                standings = conferenceStandingsService.getAllConferenceStandings(year);
                logger.info("SERVICE_SUCCESS: getAllConferenceStandings completed successfully");
            } catch (Exception e) {
                logger.error("SERVICE_ERROR: getAllConferenceStandings failed - {}", e.getMessage(), e);
                throw e;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            int totalStandings = standings.values().stream().mapToInt(List::size).sum();
            
            logger.info("SERVICE_COMPLETED: Retrieved {} conferences with {} total standings in {}ms for year={}", 
                standings.size(), totalStandings, duration, year);
            
            if (standings.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No conference standings found for year={}", year);
            } else {
                // Log conference summary
                standings.forEach((conf, standingsList) -> 
                    logger.debug("CONFERENCE_SUMMARY: conference='{}', standings={}", conf, standingsList.size()));
            }
            
            logger.info("RESPONSE_SUCCESS: Returning {} conferences with {} total standings for year={}", 
                standings.size(), totalStandings, year);
            
            return ResponseEntity.ok(standings);
            
        } catch (IllegalArgumentException e) {
            logger.error("VALIDATION_ERROR: Invalid input for year={} - {}", year, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Error retrieving all conference standings for year={} - {}", 
                year, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
            
        } finally {
            logger.info("ENDPOINT_EXIT: GET /all/{} - getAllConferenceStandings", year);
        }
    }
}