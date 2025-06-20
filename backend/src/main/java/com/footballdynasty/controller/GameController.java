package com.footballdynasty.controller;

import com.footballdynasty.dto.GameDTO;
import com.footballdynasty.entity.Game;
import com.footballdynasty.mapper.GameMapper;
import com.footballdynasty.repository.GameRepository;
import com.footballdynasty.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
@Tag(name = "Games", description = "CFB game management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    
    @Autowired
    public GameController(GameRepository gameRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
    }
    
    @GetMapping
    @Operation(summary = "Get all games", description = "Retrieve all games with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer year) {
        
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games - page={}, size={}, year={}", page, size, year);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
            Page<Game> gamesPage;
            
            if (year != null) {
                gamesPage = gameRepository.findByYearOrderByDateDesc(year, pageable);
                logger.debug("GAMES_BY_YEAR: Found {} games for year {}", gamesPage.getTotalElements(), year);
            } else {
                gamesPage = gameRepository.findAllOrderByDateDesc(pageable);
                logger.debug("GAMES_ALL: Found {} total games", gamesPage.getTotalElements());
            }
            
            List<GameDTO> gameDTOs = gamesPage.getContent().stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
            
            Map<String, Object> response = ResponseUtil.createPaginatedResponse(
                gameDTOs,
                gamesPage.getTotalElements(),
                gamesPage.getTotalPages(),
                page,
                size,
                gamesPage.hasNext(),
                gamesPage.hasPrevious()
            );
            
            logger.debug("GAMES_RESPONSE: Returning {} games", gameDTOs.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve games - {}", e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve games: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get game by ID", description = "Retrieve a specific game by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game found"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGameById(@PathVariable String id) {
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games/{}", id);
            
            Optional<Game> gameOpt = gameRepository.findById(id);
            
            if (gameOpt.isPresent()) {
                GameDTO gameDTO = gameMapper.toDTO(gameOpt.get());
                logger.debug("GAME_FOUND: {} vs {}", gameDTO.getHomeTeamName(), gameDTO.getAwayTeamName());
                return ResponseEntity.ok(gameDTO);
            } else {
                logger.warn("GAME_NOT_FOUND: Game with ID {} not found", id);
                return ResponseUtil.createNotFoundError("Game not found with ID: " + id);
            }
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve game {} - {}", id, e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve game: " + e.getMessage());
        }
    }
    
    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get games by team", description = "Retrieve all games for a specific team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team games retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGamesByTeam(
            @PathVariable String teamId,
            @RequestParam(required = false) Integer year) {
        
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games/team/{} - year={}", teamId, year);
            
            // Convert string teamId to UUID
            UUID teamUuid = UUID.fromString(teamId);
            
            List<Game> games;
            int currentYear = year != null ? year : LocalDate.now().getYear();
            
            games = gameRepository.findByTeamAndYearOrderByDateDesc(teamUuid, currentYear);
            logger.debug("TEAM_GAMES: Found {} games for team {} in year {}", games.size(), teamId, currentYear);
            
            List<GameDTO> gameDTOs = games.stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
            
            logger.debug("TEAM_GAMES_RESPONSE: Returning {} games for team", gameDTOs.size());
            return ResponseEntity.ok(gameDTOs);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve games for team {} - {}", teamId, e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve team games: " + e.getMessage());
        }
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming games", description = "Retrieve upcoming scheduled games")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming games retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUpcomingGames(@RequestParam(required = false) String teamId) {
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games/upcoming - teamId={}", teamId);
            
            List<Game> games;
            LocalDate today = LocalDate.now();
            
            if (teamId != null && !teamId.trim().isEmpty()) {
                UUID teamUuid = UUID.fromString(teamId);
                games = gameRepository.findUpcomingGamesByTeam(teamUuid, today);
                logger.debug("UPCOMING_TEAM_GAMES: Found {} upcoming games for team {}", games.size(), teamId);
            } else {
                games = gameRepository.findUpcomingGames(today);
                logger.debug("UPCOMING_ALL_GAMES: Found {} upcoming games", games.size());
            }
            
            List<GameDTO> gameDTOs = games.stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
            
            logger.debug("UPCOMING_GAMES_RESPONSE: Returning {} upcoming games", gameDTOs.size());
            return ResponseEntity.ok(gameDTOs);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve upcoming games - {}", e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve upcoming games: " + e.getMessage());
        }
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent completed games", description = "Retrieve recently completed games")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent games retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getRecentGames(
            @RequestParam(required = false) String teamId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games/recent - teamId={}, limit={}", teamId, limit);
            
            List<Game> games;
            LocalDate today = LocalDate.now();
            Pageable limitPageable = PageRequest.of(0, limit);
            
            if (teamId != null && !teamId.trim().isEmpty()) {
                UUID teamUuid = UUID.fromString(teamId);
                games = gameRepository.findRecentCompletedGamesByTeam(teamUuid, today)
                    .stream().limit(limit).collect(Collectors.toList());
                logger.debug("RECENT_TEAM_GAMES: Found {} recent games for team {}", games.size(), teamId);
            } else {
                games = gameRepository.findRecentCompletedGames(today)
                    .stream().limit(limit).collect(Collectors.toList());
                logger.debug("RECENT_ALL_GAMES: Found {} recent games", games.size());
            }
            
            List<GameDTO> gameDTOs = games.stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
            
            logger.debug("RECENT_GAMES_RESPONSE: Returning {} recent games", gameDTOs.size());
            return ResponseEntity.ok(gameDTOs);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve recent games - {}", e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve recent games: " + e.getMessage());
        }
    }
    
    @GetMapping("/week/{weekId}")
    @Operation(summary = "Get games by week", description = "Retrieve all games for a specific week")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Week games retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGamesByWeek(@PathVariable String weekId) {
        try {
            logger.debug("ENDPOINT_ENTRY: GET /games/week/{}", weekId);
            
            // Convert string weekId to UUID
            UUID weekUuid = UUID.fromString(weekId);
            
            List<Game> games = gameRepository.findByWeekIdOrderByDate(weekUuid);
            logger.debug("WEEK_GAMES: Found {} games for week {}", games.size(), weekId);
            
            List<GameDTO> gameDTOs = games.stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
            
            logger.debug("WEEK_GAMES_RESPONSE: Returning {} games for week", gameDTOs.size());
            return ResponseEntity.ok(gameDTOs);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to retrieve games for week {} - {}", weekId, e.getMessage(), e);
            return ResponseUtil.createInternalServerError("Failed to retrieve week games: " + e.getMessage());
        }
    }
}