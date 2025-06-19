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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConferenceChampionshipService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConferenceChampionshipService.class);
    
    private final TeamRepository teamRepository;
    private final StandingRepository standingRepository;
    private final GameRepository gameRepository;
    private final ConferenceStandingsService conferenceStandingsService;
    
    @Autowired
    public ConferenceChampionshipService(TeamRepository teamRepository,
                                       StandingRepository standingRepository,
                                       GameRepository gameRepository,
                                       ConferenceStandingsService conferenceStandingsService) {
        this.teamRepository = teamRepository;
        this.standingRepository = standingRepository;
        this.gameRepository = gameRepository;
        this.conferenceStandingsService = conferenceStandingsService;
    }
    
    /**
     * Get conference championship bid analysis for a specific team
     */
    public ConferenceChampionshipBid getChampionshipBid(UUID teamId, Integer year) {
        logger.info("METHOD_ENTRY: getChampionshipBid - teamId={}, year={}", teamId, year);
        
        try {
            // Validate inputs
            if (teamId == null) {
                logger.error("VALIDATION_ERROR: teamId parameter is null");
                throw new IllegalArgumentException("Team ID cannot be null");
            }
            
            if (year == null) {
                logger.error("VALIDATION_ERROR: year parameter is null");
                throw new IllegalArgumentException("Year cannot be null");
            }
            
            // Get team
            logger.debug("DATA_ACCESS: Fetching team with id={}", teamId);
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            
            if (!teamOpt.isPresent()) {
                logger.error("BUSINESS_ERROR: Team not found with id={}", teamId);
                throw new RuntimeException("Team not found with id: " + teamId);
            }
            
            Team team = teamOpt.get();
            String conference = team.getConference();
            
            logger.info("TEAM_INFO: Processing championship bid for team='{}', conference='{}'", team.getName(), conference);
            
            if (conference == null || conference.trim().isEmpty()) {
                logger.error("BUSINESS_ERROR: Team '{}' has no conference", team.getName());
                throw new RuntimeException("Team " + team.getName() + " is not in a conference");
            }
            
            // Get current standings for the conference
            logger.debug("SERVICE_CALL: Getting conference standings for conference='{}', year={}", conference, year);
            List<Standing> conferenceStandings = conferenceStandingsService.getConferenceStandings(conference, year);
            
            if (conferenceStandings.isEmpty()) {
                logger.warn("BUSINESS_WARNING: No standings found for conference='{}'", conference);
                return createEmptyChampionshipBid(team, conference, "No standings data available");
            }
            
            // Find team's current standing
            Standing teamStanding = conferenceStandings.stream()
                .filter(s -> s.getTeam().getId().equals(teamId))
                .findFirst()
                .orElse(null);
            
            if (teamStanding == null) {
                logger.warn("BUSINESS_WARNING: Team '{}' not found in conference standings", team.getName());
                return createEmptyChampionshipBid(team, conference, "Team not found in standings");
            }
            
            logger.debug("CURRENT_STANDING: team='{}', rank={}, confRecord={}-{}", 
                team.getName(), teamStanding.getConferenceRank(), 
                teamStanding.getConferenceWins(), teamStanding.getConferenceLosses());
            
            // Get remaining games for the team
            logger.debug("DATA_ACCESS: Fetching remaining games for team='{}'", team.getName());
            List<Game> remainingGames = getRemainingGames(team, year);
            
            logger.info("REMAINING_GAMES: Found {} remaining games for team='{}'", remainingGames.size(), team.getName());
            
            // Calculate scenarios
            ChampionshipScenarios scenarios = calculateChampionshipScenarios(
                team, teamStanding, conferenceStandings, remainingGames);
            
            ConferenceChampionshipBid bid = new ConferenceChampionshipBid(
                team.getName(),
                conference,
                teamStanding.getConferenceRank(),
                teamStanding.getConferenceWins(),
                teamStanding.getConferenceLosses(),
                teamStanding.getConferenceWinPercentage(),
                remainingGames.size(),
                scenarios.gamesNeededToClinch,
                scenarios.eliminationNumber,
                scenarios.canStillWin,
                scenarios.magicNumber,
                scenarios.analysis
            );
            
            logger.info("CHAMPIONSHIP_BID: team='{}', rank={}, canWin={}, gamesNeeded={}, magicNumber={}", 
                team.getName(), bid.getCurrentRank(), bid.isCanStillWinConference(), 
                bid.getGamesNeededToClinch(), bid.getMagicNumber());
            
            logger.info("METHOD_EXIT: getChampionshipBid - teamId={}, year={}", teamId, year);
            return bid;
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: getChampionshipBid failed for teamId={}, year={} - {}", 
                teamId, year, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get remaining games for a team in the current year
     */
    private List<Game> getRemainingGames(Team team, Integer year) {
        logger.debug("METHOD_ENTRY: getRemainingGames - team='{}', year={}", team.getName(), year);
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            
            logger.debug("DATE_FILTER: Looking for games from {} to {} for team='{}'", now, yearEnd, team.getName());
            
            List<Game> allGames = gameRepository.findByTeamAndYearOrderByWeekAndDate(team, year);
            logger.debug("ALL_GAMES: Found {} total games for team='{}' in year={}", allGames.size(), team.getName(), year);
            
            List<Game> remainingGames = allGames.stream()
                .filter(game -> game.getDate() != null && game.getDate().isAfter(now))
                .filter(game -> game.getStatus() == Game.GameStatus.SCHEDULED)
                .collect(Collectors.toList());
            
            logger.debug("FILTERED_GAMES: {} remaining scheduled games for team='{}'", remainingGames.size(), team.getName());
            
            // Log remaining games
            for (Game game : remainingGames) {
                String opponent = game.getHomeTeam().getId().equals(team.getId()) ? 
                    game.getAwayTeam().getName() : game.getHomeTeam().getName();
                logger.trace("REMAINING_GAME: team='{}' vs '{}' on {}", team.getName(), opponent, game.getDate());
            }
            
            return remainingGames;
            
        } catch (Exception e) {
            logger.error("ERROR: getRemainingGames failed for team='{}', year={} - {}", 
                team.getName(), year, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate championship scenarios for a team
     */
    private ChampionshipScenarios calculateChampionshipScenarios(Team team, Standing teamStanding, 
                                                                List<Standing> conferenceStandings, 
                                                                List<Game> remainingGames) {
        logger.debug("METHOD_ENTRY: calculateChampionshipScenarios - team='{}'", team.getName());
        
        try {
            int currentRank = teamStanding.getConferenceRank();
            int currentConfWins = teamStanding.getConferenceWins();
            int currentConfLosses = teamStanding.getConferenceLosses();
            
            // Count remaining conference games
            long remainingConfGames = remainingGames.stream()
                .filter(game -> isConferenceGame(game, team.getConference()))
                .count();
            
            logger.debug("SCENARIO_INPUT: currentRank={}, confRecord={}-{}, remainingConfGames={}", 
                currentRank, currentConfWins, currentConfLosses, remainingConfGames);
            
            // Maximum possible conference wins
            int maxPossibleConfWins = currentConfWins + (int) remainingConfGames;
            
            // Find current leader
            Standing leader = conferenceStandings.get(0);
            int leaderConfWins = leader.getConferenceWins();
            
            logger.debug("LEADER_INFO: leader='{}', confWins={}", leader.getTeam().getName(), leaderConfWins);
            
            // Calculate scenarios
            boolean canStillWin = maxPossibleConfWins >= leaderConfWins;
            int gamesNeededToClinch = Math.max(0, leaderConfWins + 1 - currentConfWins);
            int magicNumber = calculateMagicNumber(team, teamStanding, conferenceStandings, remainingGames);
            int eliminationNumber = calculateEliminationNumber(teamStanding, conferenceStandings);
            
            String analysis = generateAnalysis(team, currentRank, canStillWin, gamesNeededToClinch, 
                magicNumber, eliminationNumber, remainingConfGames);
            
            logger.debug("SCENARIO_RESULT: canWin={}, gamesNeeded={}, magic={}, elimination={}", 
                canStillWin, gamesNeededToClinch, magicNumber, eliminationNumber);
            
            return new ChampionshipScenarios(gamesNeededToClinch, eliminationNumber, canStillWin, magicNumber, analysis);
            
        } catch (Exception e) {
            logger.error("ERROR: calculateChampionshipScenarios failed for team='{}' - {}", 
                team.getName(), e.getMessage(), e);
            return new ChampionshipScenarios(0, 0, false, 0, "Error calculating scenarios");
        }
    }
    
    /**
     * Calculate magic number - games to clinch conference championship
     */
    private int calculateMagicNumber(Team team, Standing teamStanding, List<Standing> conferenceStandings, 
                                    List<Game> remainingGames) {
        logger.trace("METHOD_ENTRY: calculateMagicNumber - team='{}'", team.getName());
        
        try {
            // If already eliminated, return 0
            if (teamStanding.getConferenceRank() > 1) {
                // Simple calculation: games needed to surpass current leader
                Standing leader = conferenceStandings.get(0);
                int gamesNeeded = Math.max(0, leader.getConferenceWins() + 1 - teamStanding.getConferenceWins());
                
                // Can't need more games than remaining
                long remainingConfGames = remainingGames.stream()
                    .filter(game -> isConferenceGame(game, team.getConference()))
                    .count();
                
                return Math.min(gamesNeeded, (int) remainingConfGames);
            }
            
            // If currently leading, magic number is 1 (just need to maintain)
            return 1;
            
        } catch (Exception e) {
            logger.error("ERROR: calculateMagicNumber failed for team='{}' - {}", team.getName(), e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Calculate elimination number - games until mathematically eliminated
     */
    private int calculateEliminationNumber(Standing teamStanding, List<Standing> conferenceStandings) {
        try {
            // Simple elimination check: if behind by more than remaining games
            Standing leader = conferenceStandings.get(0);
            int deficit = leader.getConferenceWins() - teamStanding.getConferenceWins();
            
            // If already behind by more than possible to make up
            return Math.max(0, deficit);
            
        } catch (Exception e) {
            logger.error("ERROR: calculateEliminationNumber failed - {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Generate analysis text for the championship bid
     */
    private String generateAnalysis(Team team, int currentRank, boolean canStillWin, 
                                  int gamesNeededToClinch, int magicNumber, int eliminationNumber,
                                  long remainingConfGames) {
        try {
            StringBuilder analysis = new StringBuilder();
            
            if (currentRank == 1) {
                analysis.append("Currently leading the conference! ");
                if (magicNumber <= remainingConfGames) {
                    analysis.append(String.format("Win %d more conference game(s) to clinch.", magicNumber));
                } else {
                    analysis.append("Need to maintain lead through remaining games.");
                }
            } else if (canStillWin) {
                analysis.append(String.format("Currently ranked #%d in conference. ", currentRank));
                if (gamesNeededToClinch <= remainingConfGames) {
                    analysis.append(String.format("Need to win %d more conference game(s) for a shot at the title.", 
                        gamesNeededToClinch));
                } else {
                    analysis.append("Must win remaining games and hope for help from other results.");
                }
            } else {
                analysis.append(String.format("Currently ranked #%d. ", currentRank));
                analysis.append("Mathematically eliminated from conference championship.");
            }
            
            return analysis.toString();
            
        } catch (Exception e) {
            logger.error("ERROR: generateAnalysis failed for team='{}' - {}", team.getName(), e.getMessage(), e);
            return "Analysis unavailable";
        }
    }
    
    /**
     * Check if a game is a conference game involving the team
     */
    private boolean isConferenceGame(Game game, String teamConference) {
        try {
            return game.getHomeTeam().getConference() != null &&
                   game.getAwayTeam().getConference() != null &&
                   (game.getHomeTeam().getConference().equals(teamConference) ||
                    game.getAwayTeam().getConference().equals(teamConference)) &&
                   game.getHomeTeam().getConference().equals(game.getAwayTeam().getConference());
        } catch (Exception e) {
            logger.error("ERROR: isConferenceGame check failed - {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create empty championship bid for error cases
     */
    private ConferenceChampionshipBid createEmptyChampionshipBid(Team team, String conference, String reason) {
        logger.debug("EMPTY_BID: Creating empty bid for team='{}', reason='{}'", team.getName(), reason);
        
        return new ConferenceChampionshipBid(
            team.getName(),
            conference,
            999, // Rank unknown
            0, 0, 0.0, 0, 0, 0,
            false, 0, "Championship analysis unavailable: " + reason
        );
    }
    
    /**
     * Inner class to hold championship scenarios
     */
    private static class ChampionshipScenarios {
        final int gamesNeededToClinch;
        final int eliminationNumber;
        final boolean canStillWin;
        final int magicNumber;
        final String analysis;
        
        ChampionshipScenarios(int gamesNeededToClinch, int eliminationNumber, boolean canStillWin, 
                            int magicNumber, String analysis) {
            this.gamesNeededToClinch = gamesNeededToClinch;
            this.eliminationNumber = eliminationNumber;
            this.canStillWin = canStillWin;
            this.magicNumber = magicNumber;
            this.analysis = analysis;
        }
    }
    
    /**
     * DTO class for Conference Championship Bid
     */
    public static class ConferenceChampionshipBid {
        private final String teamName;
        private final String conference;
        private final int currentRank;
        private final int conferenceWins;
        private final int conferenceLosses;
        private final double conferenceWinPercentage;
        private final int remainingGames;
        private final int gamesNeededToClinch;
        private final int eliminationNumber;
        private final boolean canStillWinConference;
        private final int magicNumber;
        private final String analysis;
        
        public ConferenceChampionshipBid(String teamName, String conference, int currentRank, 
                                       int conferenceWins, int conferenceLosses, double conferenceWinPercentage,
                                       int remainingGames, int gamesNeededToClinch, int eliminationNumber,
                                       boolean canStillWinConference, int magicNumber, String analysis) {
            this.teamName = teamName;
            this.conference = conference;
            this.currentRank = currentRank;
            this.conferenceWins = conferenceWins;
            this.conferenceLosses = conferenceLosses;
            this.conferenceWinPercentage = conferenceWinPercentage;
            this.remainingGames = remainingGames;
            this.gamesNeededToClinch = gamesNeededToClinch;
            this.eliminationNumber = eliminationNumber;
            this.canStillWinConference = canStillWinConference;
            this.magicNumber = magicNumber;
            this.analysis = analysis;
        }
        
        // Getters
        public String getTeamName() { return teamName; }
        public String getConference() { return conference; }
        public int getCurrentRank() { return currentRank; }
        public int getConferenceWins() { return conferenceWins; }
        public int getConferenceLosses() { return conferenceLosses; }
        public double getConferenceWinPercentage() { return conferenceWinPercentage; }
        public int getRemainingGames() { return remainingGames; }
        public int getGamesNeededToClinch() { return gamesNeededToClinch; }
        public int getEliminationNumber() { return eliminationNumber; }
        public boolean isCanStillWinConference() { return canStillWinConference; }
        public int getMagicNumber() { return magicNumber; }
        public String getAnalysis() { return analysis; }
    }
}