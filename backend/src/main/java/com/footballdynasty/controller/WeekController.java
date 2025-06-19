package com.footballdynasty.controller;

import com.footballdynasty.entity.Week;
import com.footballdynasty.repository.WeekRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/weeks")
@Tag(name = "Weeks", description = "Week management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WeekController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeekController.class);
    private final WeekRepository weekRepository;
    
    @Autowired
    public WeekController(WeekRepository weekRepository) {
        this.weekRepository = weekRepository;
    }
    
    @GetMapping("/current")
    @Operation(summary = "Get current week", description = "Get the current week information based on system logic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current week retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No current week found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCurrentWeek() {
        try {
            int currentYear = LocalDate.now().getYear();
            logger.info("GET /weeks/current - retrieving current week for year: {}", currentYear);
            
            // Get all weeks for current year
            List<Week> weeks = weekRepository.findByYearOrderByWeekNumber(currentYear);
            
            if (weeks.isEmpty()) {
                logger.warn("No weeks found for year: {}", currentYear);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "No weeks found for current year");
                response.put("year", currentYear);
                response.put("currentWeek", null);
                response.put("totalWeeks", 0);
                response.put("seasonProgress", 0.0);
                return ResponseEntity.ok(response);
            }
            
            // Determine current week (Week 9 is current for mock data logic)
            // In production, this could be more sophisticated based on dates
            Week currentWeek = null;
            int currentWeekNumber = 9; // Default to week 9 as per mock data logic
            
            for (Week week : weeks) {
                if (week.getWeekNumber() == currentWeekNumber) {
                    currentWeek = week;
                    break;
                }
            }
            
            // If week 9 doesn't exist, use the last week
            if (currentWeek == null && !weeks.isEmpty()) {
                currentWeek = weeks.get(weeks.size() - 1);
                currentWeekNumber = currentWeek.getWeekNumber();
            }
            
            // Calculate season progress
            int totalWeeks = weeks.size();
            double seasonProgress = totalWeeks > 0 ? (double) currentWeekNumber / totalWeeks : 0.0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", currentYear);
            response.put("currentWeek", currentWeekNumber);
            response.put("totalWeeks", totalWeeks);
            response.put("seasonProgress", Math.min(seasonProgress, 1.0)); // Cap at 100%
            response.put("weekId", currentWeek != null ? currentWeek.getId() : null);
            
            logger.info("Current week retrieved: week {} of {} ({}% progress)", 
                currentWeekNumber, totalWeeks, Math.round(seasonProgress * 100));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving current week: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve current week: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/{year}")
    @Operation(summary = "Get weeks by year", description = "Get all weeks for a specific year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Weeks retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getWeeksByYear(@PathVariable Integer year) {
        try {
            logger.info("GET /weeks/{} - retrieving weeks for year: {}", year, year);
            
            List<Week> weeks = weekRepository.findByYearOrderByWeekNumber(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("weeks", weeks);
            response.put("totalWeeks", weeks.size());
            
            logger.info("Retrieved {} weeks for year {}", weeks.size(), year);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving weeks for year {}: {}", year, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve weeks: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/{year}/{weekNumber}")
    @Operation(summary = "Get specific week", description = "Get a specific week by year and week number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Week retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Week not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getWeek(@PathVariable Integer year, @PathVariable Integer weekNumber) {
        try {
            logger.info("GET /weeks/{}/{} - retrieving week {} for year {}", year, weekNumber, weekNumber, year);
            
            Optional<Week> weekOpt = weekRepository.findByYearAndWeekNumber(year, weekNumber);
            
            if (weekOpt.isPresent()) {
                Week week = weekOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("week", week);
                response.put("year", year);
                response.put("weekNumber", weekNumber);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Week " + weekNumber + " not found for year " + year);
                return ResponseEntity.status(404).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving week {} for year {}: {}", weekNumber, year, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve week: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}