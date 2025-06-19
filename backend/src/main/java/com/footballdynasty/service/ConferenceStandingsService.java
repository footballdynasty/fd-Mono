package com.footballdynasty.service;

import com.footballdynasty.entity.Game;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import com.footballdynasty.repository.GameRepository;
import com.footballdynasty.repository.StandingRepository;
import com.footballdynasty.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConferenceStandingsService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConferenceStandingsService.class);
    
    private final GameRepository gameRepository;
    private final StandingRepository standingRepository;
    private final TeamRepository teamRepository;
    
    @Autowired
    public ConferenceStandingsService(GameRepository gameRepository, 
                                    StandingRepository standingRepository,
                                    TeamRepository teamRepository) {
        this.gameRepository = gameRepository;
        this.standingRepository = standingRepository;
        this.teamRepository = teamRepository;
    }
    
    /**
     * Calculate and update conference standings for a specific year
     */
    public void calculateConferenceStandings(Integer year) {
        logger.info("METHOD_ENTRY: calculateConferenceStandings - year={}", year);
        
        try {
            if (year == null) {
                logger.error("VALIDATION_ERROR: calculateConferenceStandings - year parameter is null");
                throw new IllegalArgumentException("Year parameter cannot be null");
            }
            
            if (year < 1900 || year > 2100) {
                logger.error("VALIDATION_ERROR: calculateConferenceStandings - invalid year={}", year);
                throw new IllegalArgumentException("Year must be between 1900 and 2100");
            }
            
            logger.info("BUSINESS_LOGIC: Starting conference standings calculation for year={}", year);
            
            // Get all conferences
            logger.debug("DATA_ACCESS: Fetching all conferences from database");
            List<String> conferences = teamRepository.findAllConferences();
            logger.info("DATA_RESULT: Found {} conferences for processing", conferences.size());
            
            if (conferences.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No conferences found in database for year={}", year);
                return;
            }
            
            int processedConferences = 0;
            int skippedConferences = 0;
            
            for (String conference : conferences) {
                logger.debug("LOOP_ITERATION: Processing conference='{}' for year={}", conference, year);
                
                if (conference == null || conference.trim().isEmpty()) {
                    logger.warn("VALIDATION_WARNING: Skipping null or empty conference name");
                    skippedConferences++;
                    continue;
                }
                
                try {
                    logger.info("DELEGATE_CALL: Calculating standings for conference='{}', year={}", conference, year);
                    calculateConferenceStandingsForConference(conference, year);
                    processedConferences++;
                    logger.debug("SUCCESS: Completed standings calculation for conference='{}'", conference);
                } catch (Exception e) {
                    logger.error("ERROR: Failed to calculate standings for conference='{}', year={} - {}", 
                        conference, year, e.getMessage(), e);
                    // Continue processing other conferences
                }
            }
            
            logger.info("SUMMARY: Conference standings calculation completed - year={}, processed={}, skipped={}", 
                year, processedConferences, skippedConferences);
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: calculateConferenceStandings failed for year={} - {}", year, e.getMessage(), e);
            throw e;
        }
        
        logger.info("METHOD_EXIT: calculateConferenceStandings - year={}", year);
    }
    
    /**
     * Calculate conference standings for a specific conference and year
     */
    public void calculateConferenceStandingsForConference(String conference, Integer year) {
        logger.info("METHOD_ENTRY: calculateConferenceStandingsForConference - conference='{}', year={}", conference, year);
        
        try {
            // Input validation
            if (conference == null || conference.trim().isEmpty()) {
                logger.error("VALIDATION_ERROR: conference parameter is null or empty");
                throw new IllegalArgumentException("Conference parameter cannot be null or empty");
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: year parameter is null");
                throw new IllegalArgumentException("Year parameter cannot be null");
            }
            
            logger.info("BUSINESS_LOGIC: Starting standings calculation for conference='{}', year={}", conference, year);
            
            // Get all teams in the conference
            logger.debug("DATA_ACCESS: Fetching teams for conference='{}'", conference);
            List<Team> conferenceTeams = teamRepository.findByConferenceOrderByName(conference);
            logger.info("DATA_RESULT: Found {} teams in conference='{}'", conferenceTeams.size(), conference);
            
            if (conferenceTeams.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No teams found for conference='{}' - skipping calculation", conference);
                return;
            }
            
            // Log team details
            logger.debug("TEAM_LIST: Teams in conference='{}': {}", conference, 
                conferenceTeams.stream().map(Team::getName).collect(Collectors.toList()));
            
            // Get all completed games involving these teams for the year
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            logger.debug("DATE_RANGE: Searching for games between {} and {}", yearStart, yearEnd);
            
            List<UUID> teamIds = conferenceTeams.stream().map(Team::getId).collect(Collectors.toList());
            logger.debug("DATA_ACCESS: Fetching completed games for {} teams in date range", teamIds.size());
            
            List<Game> completedGames = gameRepository.findCompletedGamesByTeamsAndDateRange(teamIds, yearStart, yearEnd);
            logger.info("DATA_RESULT: Found {} completed games for conference='{}' in year={}", 
                completedGames.size(), conference, year);
            
            if (completedGames.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No completed games found for conference='{}' in year={}", conference, year);
            }
            
            // Initialize standings data for each team
            logger.debug("INITIALIZATION: Creating standings data structures for {} teams", conferenceTeams.size());
            Map<UUID, StandingData> standingsData = new HashMap<>();
            
            for (Team team : conferenceTeams) {
                StandingData data = new StandingData(team);
                standingsData.put(team.getId(), data);
                logger.trace("INIT_TEAM: Initialized standings for team='{}' (id={})", team.getName(), team.getId());
            }
            
            // Process each completed game
            logger.info("GAME_PROCESSING: Processing {} completed games", completedGames.size());
            int gamesProcessed = 0;
            int conferenceGamesFound = 0;
            
            for (Game game : completedGames) {
                logger.trace("GAME_PROCESS: Processing game id={}, home='{}' vs away='{}'", 
                    game.getId(), 
                    game.getHomeTeam() != null ? game.getHomeTeam().getName() : "null",
                    game.getAwayTeam() != null ? game.getAwayTeam().getName() : "null");
                
                boolean wasConferenceGame = processGame(game, standingsData);
                gamesProcessed++;
                
                if (wasConferenceGame) {
                    conferenceGamesFound++;
                }
            }
            
            logger.info("PROCESSING_SUMMARY: Processed {} total games, {} conference games for conference='{}'", 
                gamesProcessed, conferenceGamesFound, conference);
            
            // Debug specific teams and check for problematic patterns
            StandingData floridaData = standingsData.values().stream()
                .filter(data -> data.team.getName().contains("Florida"))
                .findFirst().orElse(null);
            if (floridaData != null) {
                logger.info("FLORIDA_PROCESSING: Found Florida with Overall={}-{}, Conference={}-{}", 
                    floridaData.wins, floridaData.losses, floridaData.conferenceWins, floridaData.conferenceLosses);
            }
            
            // Check for suspicious pattern: all teams having identical conference and overall records
            long teamsWithIdenticalRecords = standingsData.values().stream()
                .filter(data -> data.wins == data.conferenceWins && data.losses == data.conferenceLosses)
                .count();
            
            if (teamsWithIdenticalRecords > standingsData.size() * 0.8) { // More than 80% of teams
                logger.warn("SUSPICIOUS_PATTERN: {}/{} teams have identical conference and overall records - possible all-conference-games issue", 
                    teamsWithIdenticalRecords, standingsData.size());
                
                // Log a few examples
                standingsData.values().stream()
                    .filter(data -> data.wins == data.conferenceWins && data.losses == data.conferenceLosses)
                    .limit(3)
                    .forEach(data -> logger.warn("IDENTICAL_RECORD_EXAMPLE: {} ({}) - Overall:{}-{}, Conference:{}-{}", 
                        data.team.getName(), data.team.getConference(),
                        data.wins, data.losses, data.conferenceWins, data.conferenceLosses));
            }
            
            // Convert to list and sort by conference record
            logger.debug("SORTING: Converting and sorting standings data");
            List<StandingData> sortedStandings = standingsData.values().stream()
                .sorted(this::compareTeamsByConferenceRecord)
                .collect(Collectors.toList());
            
            logger.info("RANKING: Sorted {} teams by conference record", sortedStandings.size());
            
            // Log the sorted standings for debugging
            for (int i = 0; i < sortedStandings.size(); i++) {
                StandingData data = sortedStandings.get(i);
                logger.debug("RANK_{}: team='{}', confRecord={}-{} ({:.3f}), overallRecord={}-{} ({:.3f})", 
                    i + 1, data.team.getName(), 
                    data.conferenceWins, data.conferenceLosses, data.getConferenceWinPercentage(),
                    data.wins, data.losses, data.getOverallWinPercentage());
            }
            
            // Assign conference ranks and save to database
            logger.info("DATABASE_SAVE: Saving standings for {} teams", sortedStandings.size());
            int savedCount = 0;
            
            for (int i = 0; i < sortedStandings.size(); i++) {
                StandingData data = sortedStandings.get(i);
                data.conferenceRank = i + 1;
                
                logger.debug("SAVE_TEAM: Saving standings for team='{}', rank={}", data.team.getName(), data.conferenceRank);
                
                try {
                    saveOrUpdateStanding(data, year);
                    savedCount++;
                    logger.trace("SAVE_SUCCESS: Saved standings for team='{}'", data.team.getName());
                } catch (Exception e) {
                    logger.error("SAVE_ERROR: Failed to save standings for team='{}' - {}", 
                        data.team.getName(), e.getMessage(), e);
                }
            }
            
            logger.info("SAVE_SUMMARY: Successfully saved standings for {}/{} teams in conference='{}'", 
                savedCount, sortedStandings.size(), conference);
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: calculateConferenceStandingsForConference failed for conference='{}', year={} - {}", 
                conference, year, e.getMessage(), e);
            throw e;
        }
        
        logger.info("METHOD_EXIT: calculateConferenceStandingsForConference - conference='{}', year={}", conference, year);
    }
    
    /**
     * Process a single game and update standings data
     */
    private boolean processGame(Game game, Map<UUID, StandingData> standingsData) {
        logger.trace("METHOD_ENTRY: processGame - gameId={}", game.getId());
        
        try {
            // Validate game input
            if (game == null) {
                logger.warn("VALIDATION_WARNING: processGame called with null game");
                return false;
            }
            
            Team homeTeam = game.getHomeTeam();
            Team awayTeam = game.getAwayTeam();
            
            logger.trace("GAME_DETAILS: gameId={}, homeTeam='{}', awayTeam='{}', status={}, homeScore={}, awayScore={}", 
                game.getId(), 
                homeTeam != null ? homeTeam.getName() : "null",
                awayTeam != null ? awayTeam.getName() : "null",
                game.getStatus(),
                game.getHomeScore(),
                game.getAwayScore());
            
            // Check if game is completed
            if (game.getStatus() != Game.GameStatus.COMPLETED) {
                logger.debug("GAME_SKIP: Game not completed - gameId={}, status={}", game.getId(), game.getStatus());
                return false;
            }
            
            // Validate teams
            if (homeTeam == null || awayTeam == null) {
                logger.warn("VALIDATION_WARNING: Game has null teams - gameId={}, homeTeam={}, awayTeam={}", 
                    game.getId(), homeTeam != null ? homeTeam.getName() : "null", awayTeam != null ? awayTeam.getName() : "null");
                return false;
            }
            
            // Get standings data for both teams
            StandingData homeData = standingsData.get(homeTeam.getId());
            StandingData awayData = standingsData.get(awayTeam.getId());
            
            logger.trace("STANDINGS_LOOKUP: homeTeam='{}' found={}, awayTeam='{}' found={}", 
                homeTeam.getName(), homeData != null, awayTeam.getName(), awayData != null);
            
            // Process the game if at least one team is in this conference
            // This allows us to track non-conference games in overall record
            if (homeData == null && awayData == null) {
                logger.debug("GAME_SKIP: Neither team is in this conference - gameId={}", game.getId());
                return false; // Neither team is in this conference
            }
            
            // Validate scores
            Integer homeScore = game.getHomeScore();
            Integer awayScore = game.getAwayScore();
            
            if (homeScore == null || awayScore == null) {
                logger.warn("VALIDATION_WARNING: Game has null scores - gameId={}, homeScore={}, awayScore={}", 
                    game.getId(), homeScore, awayScore);
                return false;
            }
            
            // Determine winner and update overall record
            boolean homeTeamWon = homeScore > awayScore;
            logger.debug("GAME_RESULT: gameId={}, homeTeam='{}' score={}, awayTeam='{}' score={}, winner='{}'", 
                game.getId(), homeTeam.getName(), homeScore, awayTeam.getName(), awayScore, 
                homeTeamWon ? homeTeam.getName() : awayTeam.getName());
            
            // Update overall record for teams in this conference
            if (homeTeamWon) {
                // Home team wins
                if (homeData != null) {
                    homeData.wins++;
                    logger.trace("OVERALL_UPDATE: homeTeam='{}' wins++ (now {}-{})", 
                        homeTeam.getName(), homeData.wins, homeData.losses);
                }
                if (awayData != null) {
                    awayData.losses++;
                    logger.trace("OVERALL_UPDATE: awayTeam='{}' losses++ (now {}-{})", 
                        awayTeam.getName(), awayData.wins, awayData.losses);
                }
            } else {
                // Away team wins
                if (awayData != null) {
                    awayData.wins++;
                    logger.trace("OVERALL_UPDATE: awayTeam='{}' wins++ (now {}-{})", 
                        awayTeam.getName(), awayData.wins, awayData.losses);
                }
                if (homeData != null) {
                    homeData.losses++;
                    logger.trace("OVERALL_UPDATE: homeTeam='{}' losses++ (now {}-{})", 
                        homeTeam.getName(), homeData.wins, homeData.losses);
                }
            }
            
            // Check if this is a conference game (both teams in same conference)
            boolean isConferenceGame = isConferenceGame(homeTeam, awayTeam);
            logger.info("CONFERENCE_CHECK: gameId={}, home='{}' conf='{}', away='{}' conf='{}', isConferenceGame={}", 
                game.getId(), homeTeam.getName(), homeTeam.getConference(), awayTeam.getName(), awayTeam.getConference(), isConferenceGame);
            
            if (isConferenceGame) {
                // Update conference record (only if both teams are in this conference)
                if (homeTeamWon) {
                    // Home team wins conference game
                    if (homeData != null) {
                        homeData.conferenceWins++;
                        logger.debug("CONFERENCE_UPDATE: homeTeam='{}' confWins++ (now {}-{})", 
                            homeTeam.getName(), homeData.conferenceWins, homeData.conferenceLosses);
                    }
                    if (awayData != null) {
                        awayData.conferenceLosses++;
                        logger.debug("CONFERENCE_UPDATE: awayTeam='{}' confLosses++ (now {}-{})", 
                            awayTeam.getName(), awayData.conferenceWins, awayData.conferenceLosses);
                    }
                } else {
                    // Away team wins conference game
                    if (awayData != null) {
                        awayData.conferenceWins++;
                        logger.debug("CONFERENCE_UPDATE: awayTeam='{}' confWins++ (now {}-{})", 
                            awayTeam.getName(), awayData.conferenceWins, awayData.conferenceLosses);
                    }
                    if (homeData != null) {
                        homeData.conferenceLosses++;
                        logger.debug("CONFERENCE_UPDATE: homeTeam='{}' confLosses++ (now {}-{})", 
                            homeTeam.getName(), homeData.conferenceWins, homeData.conferenceLosses);
                    }
                }
            }
            
            logger.trace("METHOD_EXIT: processGame - gameId={}, isConferenceGame={}", game.getId(), isConferenceGame);
            return isConferenceGame;
            
        } catch (Exception e) {
            logger.error("ERROR: processGame failed for gameId={} - {}", 
                game != null ? game.getId() : "null", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if a game is a conference game (both teams in same conference)
     */
    private boolean isConferenceGame(Team homeTeam, Team awayTeam) {
        logger.trace("METHOD_ENTRY: isConferenceGame - homeTeam='{}', awayTeam='{}'", 
            homeTeam != null ? homeTeam.getName() : "null", 
            awayTeam != null ? awayTeam.getName() : "null");
        
        try {
            if (homeTeam == null || awayTeam == null) {
                logger.debug("CONFERENCE_CHECK: One or both teams null - returning false");
                return false;
            }
            
            String homeConference = homeTeam.getConference();
            String awayConference = awayTeam.getConference();
            
            logger.trace("CONFERENCE_DETAILS: homeTeam='{}' conf='{}', awayTeam='{}' conf='{}'", 
                homeTeam.getName(), homeConference, awayTeam.getName(), awayConference);
            
            boolean isConferenceGame = homeConference != null && 
                                     awayConference != null && 
                                     homeConference.equals(awayConference);
            
            logger.debug("CONFERENCE_RESULT: homeConf='{}', awayConf='{}', isConferenceGame={}", 
                homeConference, awayConference, isConferenceGame);
            
            return isConferenceGame;
            
        } catch (Exception e) {
            logger.error("ERROR: isConferenceGame failed for homeTeam='{}', awayTeam='{}' - {}", 
                homeTeam != null ? homeTeam.getName() : "null",
                awayTeam != null ? awayTeam.getName() : "null", 
                e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Compare teams by their conference record for ranking
     */
    private int compareTeamsByConferenceRecord(StandingData a, StandingData b) {
        logger.trace("METHOD_ENTRY: compareTeamsByConferenceRecord - teamA='{}', teamB='{}'", 
            a.team.getName(), b.team.getName());
        
        try {
            // First, compare by conference win percentage
            double aConferencePct = a.getConferenceWinPercentage();
            double bConferencePct = b.getConferenceWinPercentage();
            
            logger.trace("COMPARE_CONF_PCT: teamA='{}' confPct={:.3f}, teamB='{}' confPct={:.3f}", 
                a.team.getName(), aConferencePct, b.team.getName(), bConferencePct);
            
            if (aConferencePct != bConferencePct) {
                int result = Double.compare(bConferencePct, aConferencePct); // Higher percentage first
                logger.debug("COMPARE_RESULT: Conference percentage determines order - teamA='{}' vs teamB='{}', result={}", 
                    a.team.getName(), b.team.getName(), result);
                return result;
            }
            
            logger.debug("TIEBREAKER: Conference percentages tied - checking overall record");
            
            // If tied, compare by overall win percentage
            double aOverallPct = a.getOverallWinPercentage();
            double bOverallPct = b.getOverallWinPercentage();
            
            logger.trace("COMPARE_OVERALL_PCT: teamA='{}' overallPct={:.3f}, teamB='{}' overallPct={:.3f}", 
                a.team.getName(), aOverallPct, b.team.getName(), bOverallPct);
            
            if (aOverallPct != bOverallPct) {
                int result = Double.compare(bOverallPct, aOverallPct); // Higher percentage first
                logger.debug("COMPARE_RESULT: Overall percentage determines order - teamA='{}' vs teamB='{}', result={}", 
                    a.team.getName(), b.team.getName(), result);
                return result;
            }
            
            logger.debug("TIEBREAKER: Overall percentages tied - using team name for consistency");
            
            // If still tied, compare by team name for consistency
            int result = a.team.getName().compareTo(b.team.getName());
            logger.debug("COMPARE_RESULT: Team name determines order - teamA='{}' vs teamB='{}', result={}", 
                a.team.getName(), b.team.getName(), result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("ERROR: compareTeamsByConferenceRecord failed for teamA='{}', teamB='{}' - {}", 
                a.team.getName(), b.team.getName(), e.getMessage(), e);
            return 0; // Return equal if comparison fails
        }
    }
    
    /**
     * Save or update a Standing record in the database
     */
    private void saveOrUpdateStanding(StandingData data, Integer year) {
        logger.trace("METHOD_ENTRY: saveOrUpdateStanding - team='{}', year={}", data.team.getName(), year);
        
        try {
            // Validate inputs
            if (data == null || data.team == null) {
                logger.error("VALIDATION_ERROR: saveOrUpdateStanding called with null data or team");
                throw new IllegalArgumentException("StandingData and team cannot be null");
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: saveOrUpdateStanding called with null year");
                throw new IllegalArgumentException("Year cannot be null");
            }
            
            logger.debug("DATABASE_LOOKUP: Searching for existing standing - team='{}', year={}", data.team.getName(), year);
            
            Optional<Standing> existingOpt = standingRepository.findByTeamAndYear(data.team, year);
            boolean isUpdate = existingOpt.isPresent();
            
            logger.debug("EXISTING_CHECK: team='{}', year={}, existingFound={}", 
                data.team.getName(), year, isUpdate);
            
            Standing standing;
            if (isUpdate) {
                standing = existingOpt.get();
                logger.debug("UPDATE_MODE: Updating existing standing for team='{}', standingId={}", 
                    data.team.getName(), standing.getId());
            } else {
                standing = new Standing(data.team, year);
                logger.debug("CREATE_MODE: Creating new standing for team='{}', year={}", 
                    data.team.getName(), year);
            }
            
            // Log before values (for updates)
            if (isUpdate) {
                logger.debug("BEFORE_UPDATE: team='{}', wins={}, losses={}, confWins={}, confLosses={}, confRank={}", 
                    data.team.getName(), standing.getWins(), standing.getLosses(), 
                    standing.getConferenceWins(), standing.getConferenceLosses(), standing.getConferenceRank());
            }
            
            // Update all fields
            standing.setWins(data.wins);
            standing.setLosses(data.losses);
            standing.setConferenceWins(data.conferenceWins);
            standing.setConferenceLosses(data.conferenceLosses);
            standing.setConferenceRank(data.conferenceRank);
            
            logger.debug("FIELD_UPDATE: team='{}', setting wins={}, losses={}, confWins={}, confLosses={}, confRank={}", 
                data.team.getName(), data.wins, data.losses, 
                data.conferenceWins, data.conferenceLosses, data.conferenceRank);
            
            // Save to database
            logger.debug("DATABASE_SAVE: Saving standing for team='{}'", data.team.getName());
            Standing savedStanding = standingRepository.save(standing);
            
            if (savedStanding == null) {
                logger.error("SAVE_ERROR: standingRepository.save returned null for team='{}'", data.team.getName());
                throw new RuntimeException("Failed to save standing - repository returned null");
            }
            
            logger.info("SAVE_SUCCESS: {} standing for team='{}' - Overall: {}-{} ({:.3f}), Conference: {}-{} ({:.3f}), Rank: {}", 
                isUpdate ? "Updated" : "Created",
                data.team.getName(), 
                data.wins, data.losses, data.getOverallWinPercentage(),
                data.conferenceWins, data.conferenceLosses, data.getConferenceWinPercentage(),
                data.conferenceRank);
            
            // Special logging for Florida to debug the issue
            if (data.team.getName().contains("Florida")) {
                logger.info("FLORIDA_DEBUG: Overall={}-{}, Conference={}-{}, Should be different if has non-conf games!", 
                    data.wins, data.losses, data.conferenceWins, data.conferenceLosses);
            }
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: saveOrUpdateStanding failed for team='{}', year={} - {}", 
                data.team != null ? data.team.getName() : "null", year, e.getMessage(), e);
            throw e;
        }
        
        logger.trace("METHOD_EXIT: saveOrUpdateStanding - team='{}', year={}", data.team.getName(), year);
    }
    
    /**
     * Get conference standings for a specific conference and year
     */
    public List<Standing> getConferenceStandings(String conference, Integer year) {
        logger.info("METHOD_ENTRY: getConferenceStandings - conference='{}', year={}", conference, year);
        
        try {
            // Validate inputs
            if (conference == null || conference.trim().isEmpty()) {
                logger.error("VALIDATION_ERROR: conference parameter is null or empty");
                throw new IllegalArgumentException("Conference parameter cannot be null or empty");
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: year parameter is null");
                throw new IllegalArgumentException("Year parameter cannot be null");
            }
            
            logger.debug("DATA_ACCESS: Fetching teams for conference='{}'", conference);
            List<Team> conferenceTeams = teamRepository.findByConferenceOrderByName(conference);
            logger.info("DATA_RESULT: Found {} teams in conference='{}'", conferenceTeams.size(), conference);
            
            if (conferenceTeams.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No teams found for conference='{}' - returning empty list", conference);
                return new ArrayList<>();
            }
            
            List<UUID> teamIds = conferenceTeams.stream().map(Team::getId).collect(Collectors.toList());
            logger.debug("DATABASE_QUERY: Fetching standings for {} teams in conference='{}', year={}", 
                teamIds.size(), conference, year);
            
            logger.debug("QUERY_EXECUTION: About to execute findByTeamsAndYearOrderByConferenceRank");
            List<Standing> standings;
            try {
                standings = standingRepository.findByTeamsAndYearOrderByConferenceRank(teamIds, year);
                logger.info("QUERY_SUCCESS: Found {} standings for conference='{}', year={}", 
                    standings.size(), conference, year);
            } catch (Exception e) {
                logger.error("QUERY_ERROR: Failed to execute findByTeamsAndYearOrderByConferenceRank - {}", e.getMessage(), e);
                throw new RuntimeException("Failed to query standings: " + e.getMessage(), e);
            }
            
            // Log standings summary with safe access
            logger.debug("STANDINGS_SUMMARY: Processing {} standings for logging", standings.size());
            for (int i = 0; i < standings.size(); i++) {
                Standing standing = standings.get(i);
                try {
                    logger.debug("STANDING_{}: standingId={}, confRank={}, confRecord={}-{}, overallRecord={}-{}", 
                        i + 1, standing.getId(), standing.getConferenceRank(),
                        standing.getConferenceWins(), standing.getConferenceLosses(),
                        standing.getWins(), standing.getLosses());
                    
                    // Skip team name logging to avoid lazy loading issues
                    logger.debug("STANDING_{}_TEAM: teamId={}", i + 1, 
                        standing.getTeam() != null ? standing.getTeam().getId() : "null");
                } catch (Exception e) {
                    logger.error("STANDING_{}_ERROR: Failed to log standing details - {}", i + 1, e.getMessage(), e);
                }
            }
            
            logger.info("METHOD_EXIT: getConferenceStandings - conference='{}', year={}, returned {} standings", 
                conference, year, standings.size());
            return standings;
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: getConferenceStandings failed for conference='{}', year={} - {}", 
                conference, year, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all conference standings for a year, grouped by conference
     */
    public Map<String, List<Standing>> getAllConferenceStandings(Integer year) {
        logger.info("METHOD_ENTRY: getAllConferenceStandings - year={}", year);
        
        try {
            // Validate input
            if (year == null) {
                logger.error("VALIDATION_ERROR: year parameter is null");
                throw new IllegalArgumentException("Year parameter cannot be null");
            }
            
            logger.debug("DATA_ACCESS: Fetching all conferences");
            List<String> conferences = teamRepository.findAllConferences();
            logger.info("DATA_RESULT: Found {} conferences to process", conferences.size());
            
            if (conferences.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No conferences found - returning empty map");
                return new HashMap<>();
            }
            
            Map<String, List<Standing>> result = new HashMap<>();
            int processedConferences = 0;
            int standingsTotal = 0;
            
            for (String conference : conferences) {
                logger.debug("LOOP_ITERATION: Processing conference='{}' for year={}", conference, year);
                
                if (conference == null || conference.trim().isEmpty()) {
                    logger.warn("VALIDATION_WARNING: Skipping null or empty conference name");
                    continue;
                }
                
                try {
                    logger.info("DELEGATE_CALL: Getting standings for conference='{}', year={}", conference, year);
                    List<Standing> conferenceStandings = getConferenceStandings(conference, year);
                    result.put(conference, conferenceStandings);
                    processedConferences++;
                    standingsTotal += conferenceStandings.size();
                    
                    logger.info("CONFERENCE_SUCCESS: conference='{}', standings={}", 
                        conference, conferenceStandings.size());
                    
                } catch (Exception e) {
                    logger.error("CONFERENCE_ERROR: Failed to get standings for conference='{}', year={} - {}", 
                        conference, year, e.getMessage(), e);
                    logger.error("CONFERENCE_ERROR_STACK: Full stack trace:", e);
                    // Continue processing other conferences
                    result.put(conference, new ArrayList<>()); // Put empty list for failed conference
                }
            }
            
            logger.info("PROCESSING_SUMMARY: Processed {} conferences, total {} standings for year={}", 
                processedConferences, standingsTotal, year);
            
            logger.info("METHOD_EXIT: getAllConferenceStandings - year={}, returned {} conferences", 
                year, result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: getAllConferenceStandings failed for year={} - {}", year, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Internal class to hold standings data during calculation
     */
    private static class StandingData {
        Team team;
        int wins = 0;
        int losses = 0;
        int conferenceWins = 0;
        int conferenceLosses = 0;
        int conferenceRank = 0;
        
        StandingData(Team team) {
            this.team = team;
        }
        
        double getConferenceWinPercentage() {
            int totalConferenceGames = conferenceWins + conferenceLosses;
            return totalConferenceGames > 0 ? (double) conferenceWins / totalConferenceGames : 0.0;
        }
        
        double getOverallWinPercentage() {
            int totalGames = wins + losses;
            return totalGames > 0 ? (double) wins / totalGames : 0.0;
        }
    }
}