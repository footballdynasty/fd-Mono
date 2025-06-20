package com.footballdynasty.controller;

import com.footballdynasty.dto.UserDTO;
import com.footballdynasty.entity.User;
import com.footballdynasty.entity.Role;
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
import com.footballdynasty.service.AchievementService;
import com.footballdynasty.service.AchievementRewardService;
import com.footballdynasty.service.InboxService;
import com.footballdynasty.config.AppConfig;
import com.footballdynasty.dto.AchievementDTO;
import com.footballdynasty.dto.AchievementRewardDTO;
import com.footballdynasty.dto.AchievementRequestDTO;
import com.footballdynasty.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.sentry.Sentry;

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
    private final AchievementService achievementService;
    private final AchievementRewardService rewardService;
    private final InboxService inboxService;
    
    @Autowired
    public AdminController(UserRepository userRepository, UserMapper userMapper, 
                          MockDataService mockDataService, AppConfig appConfig,
                          GameRepository gameRepository, TeamRepository teamRepository,
                          WeekRepository weekRepository, StandingRepository standingRepository,
                          AchievementService achievementService, AchievementRewardService rewardService,
                          InboxService inboxService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mockDataService = mockDataService;
        this.appConfig = appConfig;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.weekRepository = weekRepository;
        this.standingRepository = standingRepository;
        this.achievementService = achievementService;
        this.rewardService = rewardService;
        this.inboxService = inboxService;
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
            return ResponseUtil.createInternalServerError("Failed to retrieve users: " + e.getMessage());
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
                return ResponseUtil.createNotFoundError("User not found with identifier: " + identifier);
            }
        } catch (Exception e) {
            logger.error("Error retrieving user {}: {}", identifier, e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve user: " + e.getMessage());
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
    
    @PostMapping("/sentry/test-exception")
    @Operation(summary = "Test Sentry exception capture", description = "Create a test exception to verify Sentry integration")
    @ApiResponse(responseCode = "200", description = "Test exception sent to Sentry successfully")
    public ResponseEntity<?> testSentryException() {
        logger.info("SENTRY_TEST: Testing Sentry exception capture");
        
        try {
            // Create a test exception with context
            Exception testException = new Exception("Test exception from Spring Boot backend - This is intentional for Sentry testing");
            
            // Add additional context
            Sentry.configureScope(scope -> {
                scope.setTag("test_type", "backend_exception");
                scope.setTag("endpoint", "/admin/sentry/test-exception");
                scope.setLevel(io.sentry.SentryLevel.ERROR);
                scope.setExtra("test_timestamp", String.valueOf(System.currentTimeMillis()));
                scope.setExtra("environment", "testing");
                scope.setExtra("user_triggered", "true");
            });
            
            // Capture the exception
            io.sentry.protocol.SentryId eventId = Sentry.captureException(testException);
            
            logger.info("SENTRY_EVENT_CAPTURED: Event ID: {}", eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test exception sent to Sentry successfully");
            response.put("eventId", eventId.toString());
            response.put("timestamp", System.currentTimeMillis());
            response.put("sentryConfigured", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("SENTRY_TEST_ERROR: Failed to send test exception to Sentry - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to send test exception to Sentry: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/sentry/test-message")
    @Operation(summary = "Test Sentry message capture", description = "Send a test message to Sentry")
    @ApiResponse(responseCode = "200", description = "Test message sent to Sentry successfully")
    public ResponseEntity<?> testSentryMessage() {
        logger.info("SENTRY_TEST: Testing Sentry message capture");
        
        try {
            // Configure scope for the message
            Sentry.configureScope(scope -> {
                scope.setTag("test_type", "backend_message");
                scope.setTag("endpoint", "/admin/sentry/test-message");
                scope.setLevel(io.sentry.SentryLevel.INFO);
                scope.setExtra("test_timestamp", String.valueOf(System.currentTimeMillis()));
                scope.setExtra("environment", "testing");
                scope.setExtra("user_triggered", "true");
            });
            
            // Send a test message
            io.sentry.protocol.SentryId eventId = Sentry.captureMessage("Test message from Spring Boot backend - Sentry integration working correctly");
            
            logger.info("SENTRY_MESSAGE_CAPTURED: Event ID: {}", eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test message sent to Sentry successfully");
            response.put("eventId", eventId.toString());
            response.put("timestamp", System.currentTimeMillis());
            response.put("sentryConfigured", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("SENTRY_MESSAGE_ERROR: Failed to send test message to Sentry - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to send test message to Sentry: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/sentry/test-span")
    @Operation(summary = "Test Sentry span/performance tracking", description = "Create a test span to verify Sentry performance monitoring")
    @ApiResponse(responseCode = "200", description = "Test span completed successfully")
    public ResponseEntity<?> testSentrySpan() {
        logger.info("SENTRY_TEST: Testing Sentry span/performance tracking");
        
        try {
            // Start a transaction for performance monitoring
            io.sentry.ITransaction transaction = Sentry.startTransaction("test-transaction", "backend-span-test");
            
            try {
                // Set transaction data
                transaction.setTag("test_type", "backend_span");
                transaction.setTag("endpoint", "/admin/sentry/test-span");
                transaction.setData("user_triggered", "true");
                transaction.setData("test_timestamp", String.valueOf(System.currentTimeMillis()));
                
                // Simulate some processing time
                Thread.sleep(100); // 100ms of simulated work
                
                logger.info("SENTRY_SPAN_COMPLETED: Span processing finished");
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Test span completed successfully");
                response.put("transactionId", transaction.getEventId() != null ? transaction.getEventId().toString() : "unknown");
                response.put("timestamp", System.currentTimeMillis());
                response.put("processingTime", "100ms (simulated)");
                response.put("sentryConfigured", true);
                
                return ResponseEntity.ok(response);
                
            } finally {
                // Finish the transaction
                transaction.finish();
            }
            
        } catch (Exception e) {
            logger.error("SENTRY_SPAN_ERROR: Failed to complete test span - {}", e.getMessage(), e);
            Sentry.captureException(e);
            
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to complete test span: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/sentry/status")
    @Operation(summary = "Get Sentry configuration status", description = "Check if Sentry is properly configured and working")
    @ApiResponse(responseCode = "200", description = "Sentry status retrieved successfully")
    public ResponseEntity<?> getSentryStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Check if Sentry is initialized
            boolean sentryInitialized = Sentry.isEnabled();
            
            response.put("sentryEnabled", sentryInitialized);
            response.put("sentryConfigured", sentryInitialized);
            response.put("timestamp", System.currentTimeMillis());
            
            if (sentryInitialized) {
                response.put("message", "Sentry is properly configured and enabled");
                response.put("status", "OK");
                
                // Add some configuration details (without sensitive info)
                response.put("environment", "backend-spring-boot");
                response.put("platform", "java");
            } else {
                response.put("message", "Sentry is not enabled or not configured");
                response.put("status", "NOT_CONFIGURED");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("SENTRY_STATUS_ERROR: Failed to get Sentry status - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get Sentry status: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // ====================================
    // ACHIEVEMENT MANAGEMENT ENDPOINTS
    // ====================================
    
    @GetMapping("/achievements")
    @Operation(summary = "Get all achievements", description = "Retrieve all achievements with optional filtering and pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievements retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllAchievements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) Boolean completed) {
        try {
            logger.info("ADMIN_ACHIEVEMENTS: Getting all achievements - page: {}, size: {}, type: {}, rarity: {}, completed: {}", 
                       page, size, type, rarity, completed);
            
            var achievements = achievementService.getAllAchievements(page, size, type, rarity, completed);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievements", achievements.getContent());
            response.put("page", achievements.getNumber());
            response.put("size", achievements.getSize());
            response.put("totalElements", achievements.getTotalElements());
            response.put("totalPages", achievements.getTotalPages());
            response.put("isFirst", achievements.isFirst());
            response.put("isLast", achievements.isLast());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENTS_ERROR: Failed to get achievements - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve achievements: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/achievements/{id}")
    @Operation(summary = "Get achievement by ID", description = "Retrieve a specific achievement by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement found"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAchievementById(@PathVariable UUID id) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_GET: Getting achievement by ID: {}", id);
            
            AchievementDTO achievement = achievementService.getAchievementById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievement", achievement);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_GET_ERROR: Failed to get achievement {} - {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/achievements")
    @Operation(summary = "Create achievement", description = "Create a new achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Achievement created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createAchievement(@Valid @RequestBody AchievementDTO achievementDTO) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_CREATE: Creating new achievement: {}", achievementDTO.getDescription());
            
            AchievementDTO createdAchievement = achievementService.createAchievement(achievementDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievement", createdAchievement);
            response.put("message", "Achievement created successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_CREATE_ERROR: Failed to create achievement - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/achievements/{id}")
    @Operation(summary = "Update achievement", description = "Update an existing achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement updated successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateAchievement(@PathVariable UUID id, @Valid @RequestBody AchievementDTO achievementDTO) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_UPDATE: Updating achievement: {}", id);
            
            AchievementDTO updatedAchievement = achievementService.updateAchievement(id, achievementDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievement", updatedAchievement);
            response.put("message", "Achievement updated successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_UPDATE_ERROR: Failed to update achievement {} - {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/achievements/{id}")
    @Operation(summary = "Delete achievement", description = "Delete an achievement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteAchievement(@PathVariable UUID id) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_DELETE: Deleting achievement: {}", id);
            
            achievementService.deleteAchievement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Achievement deleted successfully");
            response.put("achievementId", id);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_DELETE_ERROR: Failed to delete achievement {} - {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/achievements/{id}/complete")
    @Operation(summary = "Complete achievement", description = "Mark an achievement as completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement completed successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> completeAchievement(@PathVariable UUID id) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_COMPLETE: Marking achievement as completed: {}", id);
            
            AchievementDTO completedAchievement = achievementService.completeAchievement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievement", completedAchievement);
            response.put("message", "Achievement marked as completed");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_COMPLETE_ERROR: Failed to complete achievement {} - {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to complete achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/achievements/{id}/complete")
    @Operation(summary = "Uncomplete achievement", description = "Mark an achievement as not completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Achievement uncompleted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> uncompleteAchievement(@PathVariable UUID id) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_UNCOMPLETE: Marking achievement as not completed: {}", id);
            
            AchievementDTO uncompletedAchievement = achievementService.uncompleteAchievement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievement", uncompletedAchievement);
            response.put("message", "Achievement marked as not completed");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_UNCOMPLETE_ERROR: Failed to uncomplete achievement {} - {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to uncomplete achievement: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/achievements/stats")
    @Operation(summary = "Get achievement statistics", description = "Get comprehensive achievement statistics")
    @ApiResponse(responseCode = "200", description = "Achievement statistics retrieved successfully")
    public ResponseEntity<?> getAchievementStats() {
        try {
            logger.info("ADMIN_ACHIEVEMENT_STATS: Getting achievement statistics");
            
            var stats = achievementService.getAchievementStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("stats", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_STATS_ERROR: Failed to get achievement stats - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve achievement statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/achievements/bulk-create")
    @Operation(summary = "Bulk create achievements", description = "Create multiple achievements at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Achievements created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> bulkCreateAchievements(@Valid @RequestBody List<AchievementDTO> achievementDTOs) {
        try {
            logger.info("ADMIN_ACHIEVEMENT_BULK_CREATE: Creating {} achievements", achievementDTOs.size());
            
            List<AchievementDTO> createdAchievements = new ArrayList<>();
            for (AchievementDTO achievementDTO : achievementDTOs) {
                try {
                    AchievementDTO created = achievementService.createAchievement(achievementDTO);
                    createdAchievements.add(created);
                } catch (Exception e) {
                    logger.warn("Failed to create achievement '{}': {}", achievementDTO.getDescription(), e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("achievements", createdAchievements);
            response.put("totalRequested", achievementDTOs.size());
            response.put("totalCreated", createdAchievements.size());
            response.put("message", String.format("Successfully created %d out of %d achievements", 
                                                 createdAchievements.size(), achievementDTOs.size()));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("ADMIN_ACHIEVEMENT_BULK_CREATE_ERROR: Failed to bulk create achievements - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create achievements: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // ====================================
    // ACHIEVEMENT REWARD MANAGEMENT ENDPOINTS
    // ====================================
    
    @GetMapping("/achievements/{achievementId}/rewards")
    @Operation(summary = "Get rewards for achievement", description = "Get all rewards for a specific achievement")
    @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully")
    public ResponseEntity<?> getRewardsForAchievement(@PathVariable UUID achievementId) {
        try {
            logger.info("ADMIN_REWARDS_GET: Getting rewards for achievement: {}", achievementId);
            
            var rewards = rewardService.getRewardsForAchievement(achievementId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("rewards", rewards);
            response.put("count", rewards.size());
            response.put("achievementId", achievementId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_GET_ERROR: Failed to get rewards for achievement {} - {}", achievementId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve rewards: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/achievements/{achievementId}/rewards")
    @Operation(summary = "Create reward for achievement", description = "Create a custom reward for an achievement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reward created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createReward(@PathVariable UUID achievementId, @Valid @RequestBody AchievementRewardDTO rewardDTO) {
        try {
            logger.info("ADMIN_REWARD_CREATE: Creating reward for achievement: {}", achievementId);
            
            AchievementRewardDTO createdReward = rewardService.createReward(achievementId, rewardDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reward", createdReward);
            response.put("message", "Reward created successfully");
            response.put("achievementId", achievementId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARD_CREATE_ERROR: Failed to create reward for achievement {} - {}", achievementId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create reward: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/rewards/{rewardId}")
    @Operation(summary = "Update reward", description = "Update an existing reward")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reward updated successfully"),
        @ApiResponse(responseCode = "404", description = "Reward not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateReward(@PathVariable UUID rewardId, @Valid @RequestBody AchievementRewardDTO rewardDTO) {
        try {
            logger.info("ADMIN_REWARD_UPDATE: Updating reward: {}", rewardId);
            
            AchievementRewardDTO updatedReward = rewardService.updateReward(rewardId, rewardDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reward", updatedReward);
            response.put("message", "Reward updated successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARD_UPDATE_ERROR: Failed to update reward {} - {}", rewardId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update reward: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/rewards/{rewardId}")
    @Operation(summary = "Delete reward", description = "Delete a reward (soft delete)")
    @ApiResponse(responseCode = "200", description = "Reward deleted successfully")
    public ResponseEntity<?> deleteReward(@PathVariable UUID rewardId) {
        try {
            logger.info("ADMIN_REWARD_DELETE: Deleting reward: {}", rewardId);
            
            rewardService.deleteReward(rewardId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reward deleted successfully");
            response.put("rewardId", rewardId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARD_DELETE_ERROR: Failed to delete reward {} - {}", rewardId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete reward: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/rewards/initialize")
    @Operation(summary = "Initialize default rewards", description = "Initialize default rewards for all achievements based on configuration")
    @ApiResponse(responseCode = "200", description = "Default rewards initialized successfully")
    public ResponseEntity<?> initializeDefaultRewards() {
        try {
            logger.info("ADMIN_REWARDS_INIT: Initializing default rewards");
            
            rewardService.initializeDefaultRewards();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Default rewards initialized successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_INIT_ERROR: Failed to initialize default rewards - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to initialize default rewards: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/rewards/statistics")
    @Operation(summary = "Get reward statistics", description = "Get comprehensive reward system statistics")
    @ApiResponse(responseCode = "200", description = "Reward statistics retrieved successfully")
    public ResponseEntity<?> getRewardStatistics() {
        try {
            logger.info("ADMIN_REWARDS_STATS: Getting reward statistics");
            
            var stats = rewardService.getRewardStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("statistics", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_STATS_ERROR: Failed to get reward statistics - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve reward statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/rewards/trait-options")
    @Operation(summary = "Get trait options", description = "Get all available trait options organized by category")
    @ApiResponse(responseCode = "200", description = "Trait options retrieved successfully")
    public ResponseEntity<?> getTraitOptions() {
        try {
            logger.info("ADMIN_TRAITS: Getting trait options");
            
            var traitOptions = rewardService.getTraitOptions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("traitOptions", traitOptions);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_TRAITS_ERROR: Failed to get trait options - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve trait options: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/achievements/{achievementId}/apply-rewards")
    @Operation(summary = "Apply rewards for completed achievement", description = "Apply all rewards when an achievement is completed")
    @ApiResponse(responseCode = "200", description = "Rewards applied successfully")
    public ResponseEntity<?> applyRewards(@PathVariable UUID achievementId) {
        try {
            logger.info("ADMIN_REWARDS_APPLY: Applying rewards for achievement: {}", achievementId);
            
            var appliedRewards = rewardService.applyRewardsForCompletion(achievementId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("appliedRewards", appliedRewards);
            response.put("count", appliedRewards.size());
            response.put("message", "Rewards applied successfully");
            response.put("achievementId", achievementId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_APPLY_ERROR: Failed to apply rewards for achievement {} - {}", achievementId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to apply rewards: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/rewards/clear")
    @Operation(summary = "Clear all achievement rewards", description = "Delete all existing achievement rewards (admin only)")
    public ResponseEntity<?> clearAllRewards() {
        try {
            logger.info("ADMIN_REWARDS_CLEAR: Clearing all achievement rewards");
            
            rewardService.clearAllRewards();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All achievement rewards cleared successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_CLEAR_ERROR: Failed to clear rewards - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to clear rewards: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/rewards/reset")
    @Operation(summary = "Clear and reinitialize achievement rewards", description = "Clear all existing rewards and reinitialize with current configuration")
    public ResponseEntity<?> resetRewards() {
        try {
            logger.info("ADMIN_REWARDS_RESET: Clearing and reinitializing all achievement rewards");
            
            // Clear existing rewards
            rewardService.clearAllRewards();
            logger.info("ADMIN_REWARDS_RESET: Cleared existing rewards");
            
            // Reinitialize with current configuration
            rewardService.initializeDefaultRewards();
            logger.info("ADMIN_REWARDS_RESET: Reinitialized rewards with current configuration");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Achievement rewards reset and reinitialized successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_REWARDS_RESET_ERROR: Failed to reset rewards - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to reset rewards: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // ===================== INBOX MANAGEMENT ENDPOINTS =====================
    
    @GetMapping("/inbox/requests")
    @Operation(summary = "Get pending achievement requests", description = "Retrieve all pending achievement completion requests for admin review")
    public ResponseEntity<?> getPendingRequests() {
        try {
            logger.info("ADMIN_INBOX_REQUESTS: Getting all pending achievement requests");
            
            var requests = inboxService.getAllPendingRequests();
            var requestDTOs = requests.stream()
                    .map(this::convertRequestToDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("requests", requestDTOs);
            response.put("count", requestDTOs.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_INBOX_REQUESTS_ERROR: Failed to get pending requests - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve requests: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/inbox/requests/{requestId}/approve")
    @Operation(summary = "Approve achievement request", description = "Approve a pending achievement completion request")
    public ResponseEntity<?> approveRequest(@PathVariable UUID requestId, @RequestBody Map<String, String> requestBody) {
        try {
            String adminNotes = requestBody.getOrDefault("adminNotes", "");
            String adminUserId = "admin"; // TODO: Get from security context
            
            logger.info("ADMIN_INBOX_APPROVE: Approving request {} by admin {}", requestId, adminUserId);
            
            var approvedRequest = inboxService.approveRequest(requestId, adminUserId, adminNotes);
            var requestDTO = convertRequestToDTO(approvedRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("request", requestDTO);
            response.put("message", "Achievement request approved successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_INBOX_APPROVE_ERROR: Failed to approve request {} - {}", requestId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to approve request: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/inbox/requests/{requestId}/reject")
    @Operation(summary = "Reject achievement request", description = "Reject a pending achievement completion request")
    public ResponseEntity<?> rejectRequest(@PathVariable UUID requestId, @RequestBody Map<String, String> requestBody) {
        try {
            String adminNotes = requestBody.getOrDefault("adminNotes", "");
            String adminUserId = "admin"; // TODO: Get from security context
            
            logger.info("ADMIN_INBOX_REJECT: Rejecting request {} by admin {}", requestId, adminUserId);
            
            var rejectedRequest = inboxService.rejectRequest(requestId, adminUserId, adminNotes);
            var requestDTO = convertRequestToDTO(rejectedRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("request", requestDTO);
            response.put("message", "Achievement request rejected");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_INBOX_REJECT_ERROR: Failed to reject request {} - {}", requestId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to reject request: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/inbox/statistics")
    @Operation(summary = "Get inbox statistics", description = "Get statistics about achievement requests")
    public ResponseEntity<?> getInboxStatistics() {
        try {
            logger.info("ADMIN_INBOX_STATS: Getting inbox statistics");
            
            var stats = inboxService.getInboxStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("statistics", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("ADMIN_INBOX_STATS_ERROR: Failed to get inbox statistics - {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    private AchievementRequestDTO convertRequestToDTO(com.footballdynasty.entity.AchievementRequest request) {
        AchievementRequestDTO dto = new AchievementRequestDTO();
        dto.setId(request.getId());
        dto.setAchievementId(request.getAchievement().getId());
        dto.setAchievementDescription(request.getAchievement().getDescription());
        dto.setAchievementRarity(request.getAchievement().getRarity().toString());
        dto.setUserId(request.getUserId());
        dto.setUserDisplayName(request.getUserDisplayName());
        dto.setTeamId(request.getTeamId());
        dto.setTeamName(request.getTeamName());
        dto.setStatus(request.getStatus());
        dto.setRequestReason(request.getRequestReason());
        dto.setAdminNotes(request.getAdminNotes());
        dto.setReviewedBy(request.getReviewedBy());
        dto.setReviewedAt(request.getReviewedAt());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.generateDisplayInfo();
        
        return dto;
    }
    
    @PostMapping("/users/{username}/promote")
    @Operation(summary = "Promote user to commissioner", description = "Promote a user to commissioner role for testing purposes")
    public ResponseEntity<?> promoteUserToCommissioner(@PathVariable String username) {
        try {
            logger.info("ADMIN_PROMOTE_USER: Promoting user {} to commissioner", username);
            
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found: " + username);
                return ResponseEntity.notFound().build();
            }
            
            User user = userOptional.get();
            user.addRole(Role.COMMISSIONER);
            user = userRepository.save(user);
            
            UserDTO userDTO = userMapper.toDTO(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDTO);
            response.put("message", "User " + username + " promoted to commissioner");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("ADMIN_PROMOTE_USER_SUCCESS: User {} promoted to commissioner", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ADMIN_PROMOTE_USER_ERROR: Failed to promote user {} - {}", username, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to promote user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}