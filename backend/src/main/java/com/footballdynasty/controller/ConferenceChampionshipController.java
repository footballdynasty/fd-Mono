package com.footballdynasty.controller;

import com.footballdynasty.service.ConferenceChampionshipService;
import com.footballdynasty.service.ConferenceChampionshipService.ConferenceChampionshipBid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/conference-championship")
@CrossOrigin(origins = "*")
public class ConferenceChampionshipController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConferenceChampionshipController.class);
    
    private final ConferenceChampionshipService conferenceChampionshipService;
    
    @Autowired
    public ConferenceChampionshipController(ConferenceChampionshipService conferenceChampionshipService) {
        this.conferenceChampionshipService = conferenceChampionshipService;
    }
    
    /**
     * Get conference championship bid analysis for a specific team
     */
    @GetMapping("/bid/{teamId}/{year}")
    public ResponseEntity<ConferenceChampionshipBid> getChampionshipBid(
            @PathVariable UUID teamId,
            @PathVariable Integer year) {
        logger.info("ENDPOINT_ENTRY: GET /bid/{}/{} - getChampionshipBid", teamId, year);
        
        try {
            // Input validation
            if (teamId == null) {
                logger.error("VALIDATION_ERROR: teamId path variable is null");
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
            
            logger.info("REQUEST_PROCESSING: Getting championship bid for teamId={}, year={}", teamId, year);
            
            // Delegate to service
            logger.debug("SERVICE_CALL: Calling conferenceChampionshipService.getChampionshipBid({}, {})", teamId, year);
            long startTime = System.currentTimeMillis();
            
            ConferenceChampionshipBid bid = conferenceChampionshipService.getChampionshipBid(teamId, year);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("SERVICE_COMPLETED: Retrieved championship bid in {}ms for team='{}', conference='{}'", 
                duration, bid.getTeamName(), bid.getConference());
            
            logger.info("BID_SUMMARY: team='{}', rank={}, canWin={}, gamesNeeded={}, analysis='{}'", 
                bid.getTeamName(), bid.getCurrentRank(), bid.isCanStillWinConference(), 
                bid.getGamesNeededToClinch(), bid.getAnalysis());
            
            logger.info("RESPONSE_SUCCESS: Returning championship bid for team='{}'", bid.getTeamName());
            
            return ResponseEntity.ok(bid);
            
        } catch (IllegalArgumentException e) {
            logger.error("VALIDATION_ERROR: Invalid input for teamId={}, year={} - {}", teamId, year, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (RuntimeException e) {
            logger.error("BUSINESS_ERROR: Business logic error for teamId={}, year={} - {}", teamId, year, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Error getting championship bid for teamId={}, year={} - {}", 
                teamId, year, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
            
        } finally {
            logger.info("ENDPOINT_EXIT: GET /bid/{}/{} - getChampionshipBid", teamId, year);
        }
    }
}