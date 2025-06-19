package com.footballdynasty.controller;

import com.footballdynasty.dto.UserDTO;
import com.footballdynasty.entity.User;
import com.footballdynasty.entity.Game;
import com.footballdynasty.entity.Team;
import com.footballdynasty.entity.Week;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.mapper.UserMapper;
import com.footballdynasty.repository.UserRepository;
import com.footballdynasty.repository.GameRepository;
import com.footballdynasty.repository.TeamRepository;
import com.footballdynasty.repository.WeekRepository;
import com.footballdynasty.repository.StandingRepository;
import com.footballdynasty.service.MockDataService;
import com.footballdynasty.config.AppConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MockDataService mockDataService;
    private final AppConfig appConfig;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final WeekRepository weekRepository;
    private final StandingRepository standingRepository;
    
    @Autowired
    public AdminController(UserRepository userRepository, UserMapper userMapper, 
                          MockDataService mockDataService, AppConfig appConfig,
                          GameRepository gameRepository, TeamRepository teamRepository,
                          WeekRepository weekRepository, StandingRepository standingRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mockDataService = mockDataService;
        this.appConfig = appConfig;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.weekRepository = weekRepository;
        this.standingRepository = standingRepository;
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve all users from the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", userDTOs);
            response.put("count", userDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve users: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/users/{identifier}")
    @Operation(summary = "Get user by ID or username", description = "Retrieve a specific user by ID or username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserByIdentifier(@PathVariable String identifier) {
        try {
            Optional<User> userOpt;
            
            // Try to parse as Long (ID) first
            try {
                Long id = Long.parseLong(identifier);
                userOpt = userRepository.findByIdWithSelectedTeam(id);
            } catch (NumberFormatException e) {
                // If not a number, treat as username
                userOpt = userRepository.findByUsernameWithSelectedTeam(identifier);
            }
            
            if (userOpt.isPresent()) {
                UserDTO userDTO = userMapper.toDTO(userOpt.get());
                
                Map<String, Object> response = new HashMap<>();
                response.put("user", userDTO);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found with identifier: " + identifier);
                return ResponseEntity.status(404).body(error);
            }
        } catch (Exception e) {
            logger.error("Error retrieving user {}: {}", identifier, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/users/search")
    @Operation(summary = "Search users", description = "Search users by username or email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        try {
            if (username == null && email == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Either username or email parameter is required");
                return ResponseEntity.status(400).body(error);
            }
            
            Optional<User> userOpt = Optional.empty();
            
            if (username != null && email != null) {
                userOpt = userRepository.findByUsernameOrEmail(username, email);
            } else if (username != null) {
                userOpt = userRepository.findByUsernameWithSelectedTeam(username);
            } else if (email != null) {
                userOpt = userRepository.findByEmail(email);
            }
            
            Map<String, Object> response = new HashMap<>();
            if (userOpt.isPresent()) {
                UserDTO userDTO = userMapper.toDTO(userOpt.get());
                response.put("user", userDTO);
                response.put("found", true);
            } else {
                response.put("user", null);
                response.put("found", false);
                response.put("message", "No user found with the provided criteria");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to search users: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/users/credentials/{username}")
    @Operation(summary = "Get user credentials info", description = "Get basic credential information for a user (for development/debugging)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credentials info retrieved"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserCredentials(@PathVariable String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
                response.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
                response.put("isActive", user.getIsActive());
                response.put("roles", user.getRoles());
                response.put("createdAt", user.getCreatedAt());
                response.put("updatedAt", user.getUpdatedAt());
                response.put("selectedTeamId", user.getSelectedTeamId());
                
                // DO NOT return the actual password hash for security
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found: " + username);
                return ResponseEntity.status(404).body(error);
            }
        } catch (Exception e) {
            logger.error("Error retrieving credentials for user {}: {}", username, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve user credentials: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get database stats", description = "Get basic database statistics")
    @ApiResponse(responseCode = "200", description = "Stats retrieved successfully")
    public ResponseEntity<?> getStats() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = ((List<User>) userRepository.findAllActiveUsers()).size();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", totalUsers);
            response.put("activeUsers", activeUsers);
            response.put("inactiveUsers", totalUsers - activeUsers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving stats: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve stats: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/debug")
    @Operation(summary = "Debug info", description = "Get debug information and available endpoints")
    @ApiResponse(responseCode = "200", description = "Debug info retrieved successfully")
    public ResponseEntity<?> getDebugInfo() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // System info
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("timestamp", System.currentTimeMillis());
            systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
            systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
            
            // Available endpoints
            List<Map<String, String>> endpoints = new ArrayList<>();
            
            endpoints.add(Map.of(
                "path", "/api/v2/admin/users",
                "method", "GET",
                "description", "Get all users"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/admin/users/{id_or_username}",
                "method", "GET", 
                "description", "Get user by ID or username"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/admin/users/search?username=X&email=Y",
                "method", "GET",
                "description", "Search users by username or email"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/admin/users/credentials/{username}",
                "method", "GET",
                "description", "Get user credentials info (safe)"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/teams",
                "method", "GET",
                "description", "Get all teams with pagination"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/teams/conferences",
                "method", "GET",
                "description", "Get all conferences"
            ));
            
            endpoints.add(Map.of(
                "path", "/api/v2/auth/test",
                "method", "GET",
                "description", "Test backend connectivity"
            ));
            
            // Quick stats
            long totalUsers = userRepository.count();
            Map<String, Object> quickStats = new HashMap<>();
            quickStats.put("totalUsers", totalUsers);
            quickStats.put("activeUsers", ((List<User>) userRepository.findAllActiveUsers()).size());
            
            response.put("systemInfo", systemInfo);
            response.put("availableEndpoints", endpoints);
            response.put("quickStats", quickStats);
            response.put("message", "Debug info retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving debug info: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve debug info: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/mock-data/create")
    @Operation(summary = "Create mock data", description = "Manually create mock data for testing")
    @ApiResponse(responseCode = "200", description = "Mock data created successfully")
    public ResponseEntity<?> createMockData() {
        logger.info("ENDPOINT_ENTRY: POST /admin/mock-data/create");
        
        try {
            if (!appConfig.isMockDataEnabled()) {
                logger.warn("MOCK_DATA_DISABLED: Mock data creation attempted but not enabled");
                Map<String, String> error = new HashMap<>();
                error.put("message", "Mock data is not enabled. Set CURRENT_ENVIRONMENT=testing or MOCK_DATA_ENABLED=true");
                return ResponseEntity.status(400).body(error);
            }
            
            logger.info("MOCK_DATA_TRIGGER: Manually triggering mock data creation");
            long startTime = System.currentTimeMillis();
            
            mockDataService.recreateMockData();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("MOCK_DATA_COMPLETE: Mock data creation completed in {}ms", duration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mock data created successfully");
            response.put("duration", duration + "ms");
            response.put("environment", appConfig.getEnvironment());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("PROCESSING_ERROR: Failed to create mock data - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create mock data: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
            
        } finally {
            logger.info("ENDPOINT_EXIT: POST /admin/mock-data/create");
        }
    }
    
    @GetMapping("/mock-data/analyze")
    @Operation(summary = "Analyze game distribution", description = "Analyze current game distribution per team")
    @ApiResponse(responseCode = "200", description = "Game analysis completed successfully")
    public ResponseEntity<?> analyzeGameDistribution() {
        logger.info("ENDPOINT_ENTRY: GET /admin/mock-data/analyze");
        
        try {
            if (!appConfig.isMockDataEnabled()) {
                logger.warn("ANALYSIS_DISABLED: Game analysis attempted but mock data not enabled");
                Map<String, String> error = new HashMap<>();
                error.put("message", "Analysis endpoints require mock data to be enabled. Set CURRENT_ENVIRONMENT=testing or MOCK_DATA_ENABLED=true");
                return ResponseEntity.status(400).body(error);
            }
            
            logger.info("ANALYSIS_TRIGGER: Triggering game distribution analysis");
            long startTime = System.currentTimeMillis();
            
            mockDataService.analyzeGameDistribution();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("ANALYSIS_COMPLETE: Game analysis completed in {}ms", duration);
            
            // Get actual stats for response
            List<Team> allTeams = teamRepository.findAll();
            List<Game> allGames = gameRepository.findByYearOrderByWeekAndDate(LocalDate.now().getYear());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Game analysis completed successfully - check server logs for detailed results");
            response.put("duration", duration + "ms");
            response.put("totalTeams", allTeams.size());
            response.put("totalGames", allGames.size());
            response.put("averageGamesPerTeam", allTeams.isEmpty() ? 0 : (double) allGames.size() / allTeams.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ANALYSIS_ERROR: Failed to analyze game distribution - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to analyze game distribution: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
            
        } finally {
            logger.info("ENDPOINT_EXIT: GET /admin/mock-data/analyze");
        }
    }
    
    @PostMapping("/mock-data/clear")
    @Operation(summary = "Clear mock data", description = "Clear all mock data from the database without recreating it")
    @ApiResponse(responseCode = "200", description = "Mock data cleared successfully")
    public ResponseEntity<?> clearMockData() {
        logger.info("ENDPOINT_ENTRY: POST /admin/mock-data/clear");
        
        try {
            logger.info("CLEAR_MOCK_DATA: Manually clearing all mock data");
            long startTime = System.currentTimeMillis();
            
            // Clear data directly using repositories
            int currentYear = java.time.LocalDate.now().getYear();
            
            // Delete standings for current year
            List<Standing> standings = standingRepository.findByYearOrderByWinsDescLossesAsc(currentYear);
            standingRepository.deleteAll(standings);
            logger.info("CLEAR_STANDINGS: Deleted {} standings for year {}", standings.size(), currentYear);
            
            // Delete games for current year
            List<Game> games = gameRepository.findByYearOrderByWeekAndDate(currentYear);
            gameRepository.deleteAll(games);
            logger.info("CLEAR_GAMES: Deleted {} games for year {}", games.size(), currentYear);
            
            // Delete weeks for current year
            List<Week> weeks = weekRepository.findByYearOrderByWeekNumber(currentYear);
            weekRepository.deleteAll(weeks);
            logger.info("CLEAR_WEEKS: Deleted {} weeks for year {}", weeks.size(), currentYear);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("CLEAR_COMPLETE: Mock data clearing completed in {}ms", duration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mock data cleared successfully");
            response.put("duration", duration + "ms");
            response.put("standingsDeleted", standings.size());
            response.put("gamesDeleted", games.size());
            response.put("weeksDeleted", weeks.size());
            response.put("year", currentYear);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("CLEAR_ERROR: Failed to clear mock data - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to clear mock data: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
            
        } finally {
            logger.info("ENDPOINT_EXIT: POST /admin/mock-data/clear");
        }
    }

    @PostMapping("/mock-data/recalculate-rankings")
    @Operation(summary = "Recalculate overall rankings", description = "Recalculate overall rankings for existing standings data")
    @ApiResponse(responseCode = "200", description = "Overall rankings recalculated successfully")
    public ResponseEntity<?> recalculateOverallRankings() {
        logger.info("ENDPOINT_ENTRY: POST /admin/mock-data/recalculate-rankings");
        
        try {
            if (!appConfig.isMockDataEnabled()) {
                logger.warn("RANKINGS_DISABLED: Rankings recalculation attempted but mock data not enabled");
                Map<String, String> error = new HashMap<>();
                error.put("message", "Rankings endpoints require mock data to be enabled. Set CURRENT_ENVIRONMENT=testing or MOCK_DATA_ENABLED=true");
                return ResponseEntity.status(400).body(error);
            }
            
            logger.info("RANKINGS_TRIGGER: Manually triggering overall rankings recalculation");
            long startTime = System.currentTimeMillis();
            
            mockDataService.recalculateOverallRankings();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("RANKINGS_COMPLETE: Overall rankings recalculation completed in {}ms", duration);
            
            // Get some stats for the response
            List<Standing> topRankedTeams = standingRepository.findByYearOrderByWinsDescLossesAsc(LocalDate.now().getYear())
                .stream()
                .filter(standing -> standing.getRank() != null && standing.getRank() <= 10)
                .limit(10)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Overall rankings recalculated successfully");
            response.put("duration", duration + "ms");
            response.put("environment", appConfig.getEnvironment());
            response.put("year", LocalDate.now().getYear());
            response.put("topTeamsWithRanks", topRankedTeams.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("RANKINGS_ERROR: Failed to recalculate overall rankings - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to recalculate overall rankings: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
            
        } finally {
            logger.info("ENDPOINT_EXIT: POST /admin/mock-data/recalculate-rankings");
        }
    }
    
    @GetMapping("/environment")
    @Operation(summary = "Get environment info", description = "Get current environment configuration")
    @ApiResponse(responseCode = "200", description = "Environment info retrieved successfully")
    public ResponseEntity<?> getEnvironmentInfo() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("environment", appConfig.getEnvironment());
            response.put("mockDataEnabled", appConfig.isMockDataEnabled());
            response.put("isTestingEnvironment", appConfig.isTestingEnvironment());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving environment info: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve environment info: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/debug/standings-test")
    @Operation(summary = "Test standings database access", description = "Test basic standings repository access")
    @ApiResponse(responseCode = "200", description = "Standings test successful")
    public ResponseEntity<?> testStandingsAccess() {
        logger.info("DEBUG_STANDINGS: Testing standings repository access");
        
        try {
            // Try basic operations
            long standingsCount = standingRepository.count();
            logger.info("STANDINGS_COUNT: Found {} standing records", standingsCount);
            
            List<Team> allTeams = teamRepository.findAll();
            logger.info("TEAMS_COUNT: Found {} teams", allTeams.size());
            
            if (!allTeams.isEmpty()) {
                Team firstTeam = allTeams.get(0);
                logger.info("FIRST_TEAM: Testing with team '{}'", firstTeam.getName());
                
                Optional<Standing> standing = standingRepository.findByTeamAndYear(firstTeam, 2025);
                logger.info("STANDING_LOOKUP: Found standing for team '{}': {}", 
                    firstTeam.getName(), standing.isPresent());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Standings test completed successfully");
            response.put("standingsCount", standingsCount);
            response.put("teamsCount", allTeams.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ERROR: Standings test failed - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Standings test failed: " + e.getMessage());
            error.put("errorClass", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/debug/standings-simple-test")
    @Operation(summary = "Test simple standings query", description = "Test basic standings queries")
    @ApiResponse(responseCode = "200", description = "Simple test successful")
    public ResponseEntity<?> testSimpleStandings() {
        logger.info("DEBUG_SIMPLE: Testing simple standings queries");
        
        try {
            // Test 1: Get all standings
            long totalStandings = standingRepository.count();
            logger.info("TOTAL_STANDINGS: {}", totalStandings);
            
            // Test 2: Get standings by year
            List<Standing> standings2024 = standingRepository.findByYearOrderByWinsDescLossesAsc(2024);
            logger.info("STANDINGS_2024: {}", standings2024.size());
            
            List<Standing> standings2025 = standingRepository.findByYearOrderByWinsDescLossesAsc(2025);
            logger.info("STANDINGS_2025: {}", standings2025.size());
            
            // Test 3: Get one team's standing safely
            List<Team> allTeams = teamRepository.findAll();
            if (!allTeams.isEmpty()) {
                Team firstTeam = allTeams.get(0);
                Optional<Standing> teamStanding2025 = standingRepository.findByTeamAndYear(firstTeam, 2025);
                logger.info("TEAM_STANDING_2025: exists={}", teamStanding2025.isPresent());
                
                Optional<Standing> teamStanding2024 = standingRepository.findByTeamAndYear(firstTeam, 2024);
                logger.info("TEAM_STANDING_2024: exists={}", teamStanding2024.isPresent());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Simple standings test completed");
            response.put("totalStandings", totalStandings);
            response.put("standings2024", standings2024.size());
            response.put("standings2025", standings2025.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("SIMPLE_TEST_ERROR: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Simple test failed: " + e.getMessage());
            error.put("errorClass", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/debug/standings-query-test")
    @Operation(summary = "Test specific standings query", description = "Test the problematic standings query")
    @ApiResponse(responseCode = "200", description = "Query test successful")
    public ResponseEntity<?> testStandingsQuery() {
        logger.info("DEBUG_QUERY: Testing specific standings query that's causing 500 error");
        
        try {
            // Get a conference and teams to test with
            List<String> conferences = teamRepository.findAllConferences();
            logger.info("CONFERENCES_FOUND: {}", conferences.size());
            
            if (conferences.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "No conferences found"));
            }
            
            String testConference = conferences.get(0); // Use first conference
            logger.info("TESTING_CONFERENCE: {}", testConference);
            
            List<Team> conferenceTeams = teamRepository.findByConferenceOrderByName(testConference);
            logger.info("TEAMS_FOUND: {} teams in conference {}", conferenceTeams.size(), testConference);
            
            if (conferenceTeams.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "No teams found in conference " + testConference));
            }
            
            List<UUID> teamIds = conferenceTeams.stream().map(Team::getId).limit(3).collect(Collectors.toList());
            logger.info("TESTING_WITH_TEAM_IDS: {} team IDs", teamIds.size());
            
            // Test the problematic query
            try {
                List<Standing> standings = standingRepository.findByTeamsAndYearOrderByConferenceRank(teamIds, 2025);
                logger.info("QUERY_SUCCESS: Found {} standings", standings.size());
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Query test successful");
                response.put("conference", testConference);
                response.put("teamsChecked", teamIds.size());
                response.put("standingsFound", standings.size());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("QUERY_FAILED: The specific query failed - {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Query failed: " + e.getMessage());
                error.put("errorClass", e.getClass().getSimpleName());
                error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "No cause");
                return ResponseEntity.status(500).body(error);
            }
            
        } catch (Exception e) {
            logger.error("TEST_ERROR: Overall test failed - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Test failed: " + e.getMessage());
            error.put("errorClass", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/debug/test")
    @Operation(summary = "Test debug endpoint", description = "Simple test endpoint to verify debug routing works")
    @ApiResponse(responseCode = "200", description = "Test successful")
    public ResponseEntity<?> testDebugEndpoint() {
        logger.info("DEBUG_TEST: Test endpoint called successfully");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug endpoint test successful");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/debug/create-conference-game")
    @Operation(summary = "Create debug conference game", description = "Create a new conference game between two teams in the same conference")
    @ApiResponse(responseCode = "200", description = "Conference game created successfully")
    public ResponseEntity<?> createConferenceGame(
            @RequestParam String homeTeamName,
            @RequestParam String awayTeamName,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String gameDate) {
        
        logger.info("ENDPOINT_ENTRY: POST /admin/debug/create-conference-game - home='{}', away='{}'", homeTeamName, awayTeamName);
        
        try {
            if (!appConfig.isMockDataEnabled()) {
                logger.warn("DEBUG_DISABLED: Debug game creation attempted but mock data not enabled");
                Map<String, String> error = new HashMap<>();
                error.put("message", "Debug endpoints require mock data to be enabled. Set CURRENT_ENVIRONMENT=testing or MOCK_DATA_ENABLED=true");
                return ResponseEntity.status(400).body(error);
            }
            
            // Default values
            Integer gameYear = year != null ? year : LocalDate.now().getYear();
            Integer gameWeek = weekNumber != null ? weekNumber : 10; // Default to week 10
            LocalDate date = gameDate != null ? LocalDate.parse(gameDate) : LocalDate.now().plusDays(7);
            
            // Find teams
            Optional<Team> homeTeamOpt = teamRepository.findByName(homeTeamName);
            Optional<Team> awayTeamOpt = teamRepository.findByName(awayTeamName);
            
            if (homeTeamOpt.isEmpty() || awayTeamOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "One or both teams not found. Home: " + homeTeamName + ", Away: " + awayTeamName);
                return ResponseEntity.status(400).body(error);
            }
            
            Team homeTeam = homeTeamOpt.get();
            Team awayTeam = awayTeamOpt.get();
            
            // Validate teams are in the same conference
            if (homeTeam.getConference() == null || awayTeam.getConference() == null || 
                !homeTeam.getConference().equals(awayTeam.getConference())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Teams must be in the same conference for a conference game. Home conference: " + 
                         homeTeam.getConference() + ", Away conference: " + awayTeam.getConference());
                return ResponseEntity.status(400).body(error);
            }
            
            // Find or create week
            Week week = weekRepository.findByYearAndWeekNumber(gameYear, gameWeek)
                .orElseGet(() -> {
                    Week newWeek = new Week();
                    newWeek.setYear(gameYear);
                    newWeek.setWeekNumber(gameWeek);
                    return weekRepository.save(newWeek);
                });
            
            // Create game
            String gameId = String.format("DEBUG_CONF_%s_%s_W%d", 
                homeTeam.getName().replaceAll("\\s+", ""), 
                awayTeam.getName().replaceAll("\\s+", ""), 
                gameWeek);
            
            Game game = new Game(gameId, homeTeam, awayTeam, date, week);
            game.setStatus(Game.GameStatus.SCHEDULED);
            
            Game savedGame = gameRepository.save(game);
            logger.info("CONFERENCE_GAME_CREATED: gameId='{}', home='{}', away='{}', conference='{}'", 
                savedGame.getGameId(), homeTeam.getName(), awayTeam.getName(), homeTeam.getConference());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Conference game created successfully");
            response.put("gameId", savedGame.getGameId());
            response.put("homeTeam", homeTeam.getName());
            response.put("awayTeam", awayTeam.getName());
            response.put("conference", homeTeam.getConference());
            response.put("date", date.toString());
            response.put("week", gameWeek);
            response.put("year", gameYear);
            response.put("status", savedGame.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create conference game - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create conference game: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } finally {
            logger.info("ENDPOINT_EXIT: POST /admin/debug/create-conference-game");
        }
    }
    
    @PostMapping("/debug/create-non-conference-game")
    @Operation(summary = "Create debug non-conference game", description = "Create a new non-conference game between teams from different conferences")
    @ApiResponse(responseCode = "200", description = "Non-conference game created successfully")
    public ResponseEntity<?> createNonConferenceGame(
            @RequestParam String homeTeamName,
            @RequestParam String awayTeamName,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String gameDate) {
        
        logger.info("ENDPOINT_ENTRY: POST /admin/debug/create-non-conference-game - home='{}', away='{}'", homeTeamName, awayTeamName);
        
        try {
            if (!appConfig.isMockDataEnabled()) {
                logger.warn("DEBUG_DISABLED: Debug game creation attempted but mock data not enabled");
                Map<String, String> error = new HashMap<>();
                error.put("message", "Debug endpoints require mock data to be enabled. Set CURRENT_ENVIRONMENT=testing or MOCK_DATA_ENABLED=true");
                return ResponseEntity.status(400).body(error);
            }
            
            // Default values
            Integer gameYear = year != null ? year : LocalDate.now().getYear();
            Integer gameWeek = weekNumber != null ? weekNumber : 10; // Default to week 10
            LocalDate date = gameDate != null ? LocalDate.parse(gameDate) : LocalDate.now().plusDays(7);
            
            // Find teams
            Optional<Team> homeTeamOpt = teamRepository.findByName(homeTeamName);
            Optional<Team> awayTeamOpt = teamRepository.findByName(awayTeamName);
            
            if (homeTeamOpt.isEmpty() || awayTeamOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "One or both teams not found. Home: " + homeTeamName + ", Away: " + awayTeamName);
                return ResponseEntity.status(400).body(error);
            }
            
            Team homeTeam = homeTeamOpt.get();
            Team awayTeam = awayTeamOpt.get();
            
            // Validate teams are in different conferences (or one is independent)
            if (homeTeam.getConference() != null && awayTeam.getConference() != null && 
                homeTeam.getConference().equals(awayTeam.getConference())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Teams must be in different conferences for a non-conference game. Both teams are in: " + homeTeam.getConference());
                return ResponseEntity.status(400).body(error);
            }
            
            // Find or create week
            Week week = weekRepository.findByYearAndWeekNumber(gameYear, gameWeek)
                .orElseGet(() -> {
                    Week newWeek = new Week();
                    newWeek.setYear(gameYear);
                    newWeek.setWeekNumber(gameWeek);
                    return weekRepository.save(newWeek);
                });
            
            // Create game
            String gameId = String.format("DEBUG_NONCONF_%s_%s_W%d", 
                homeTeam.getName().replaceAll("\\s+", ""), 
                awayTeam.getName().replaceAll("\\s+", ""), 
                gameWeek);
            
            Game game = new Game(gameId, homeTeam, awayTeam, date, week);
            game.setStatus(Game.GameStatus.SCHEDULED);
            
            Game savedGame = gameRepository.save(game);
            logger.info("NON_CONFERENCE_GAME_CREATED: gameId='{}', home='{}' ({}), away='{}' ({})", 
                savedGame.getGameId(), homeTeam.getName(), homeTeam.getConference(), 
                awayTeam.getName(), awayTeam.getConference());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Non-conference game created successfully");
            response.put("gameId", savedGame.getGameId());
            response.put("homeTeam", homeTeam.getName());
            response.put("homeConference", homeTeam.getConference());
            response.put("awayTeam", awayTeam.getName());
            response.put("awayConference", awayTeam.getConference());
            response.put("date", date.toString());
            response.put("week", gameWeek);
            response.put("year", gameYear);
            response.put("status", savedGame.getStatus());
            response.put("isNonConference", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create non-conference game - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create non-conference game: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } finally {
            logger.info("ENDPOINT_EXIT: POST /admin/debug/create-non-conference-game");
        }
    }
}