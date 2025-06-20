package com.footballdynasty.service;

import com.footballdynasty.config.AppConfig;
import com.footballdynasty.entity.*;
import com.footballdynasty.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MockDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);
    
    private final AppConfig appConfig;
    private final Environment environment;
    private final TeamRepository teamRepository;
    private final WeekRepository weekRepository;
    private final GameRepository gameRepository;
    private final StandingRepository standingRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementRewardRepository achievementRewardRepository;
    private final AchievementRewardService achievementRewardService;
    private final ConferenceStandingsService conferenceStandingsService;
    
    private final Random random = new Random();
    private final int CURRENT_YEAR = LocalDate.now().getYear();
    
    @Autowired
    public MockDataService(AppConfig appConfig,
                          Environment environment,
                          TeamRepository teamRepository,
                          WeekRepository weekRepository,
                          GameRepository gameRepository,
                          StandingRepository standingRepository,
                          AchievementRepository achievementRepository,
                          AchievementRewardRepository achievementRewardRepository,
                          AchievementRewardService achievementRewardService,
                          ConferenceStandingsService conferenceStandingsService) {
        this.appConfig = appConfig;
        this.environment = environment;
        this.teamRepository = teamRepository;
        this.weekRepository = weekRepository;
        this.gameRepository = gameRepository;
        this.standingRepository = standingRepository;
        this.achievementRepository = achievementRepository;
        this.achievementRewardRepository = achievementRewardRepository;
        this.achievementRewardService = achievementRewardService;
        this.conferenceStandingsService = conferenceStandingsService;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeMockData() {
        logger.info("METHOD_ENTRY: initializeMockData - environment={}, mockDataEnabled={}", 
            appConfig.getEnvironment(), appConfig.isMockDataEnabled());
        
        if (!appConfig.isMockDataEnabled()) {
            logger.info("MOCK_DATA_SKIP: Mock data disabled for profile: {}", getActiveProfiles());
            
            // Clear any existing mock data when not in testing environment
            if (!appConfig.isTestingEnvironment()) {
                clearExistingMockData();
            }
            return;
        }
        
        // Only force recreation in testing environment for debugging
        if (appConfig.isTestingEnvironment()) {
            logger.info("FORCE_MOCK_DATA_RECREATE: Deleting existing data and recreating for debugging in testing environment");
            try {
                // Delete existing games, standings, and achievements to force recreation
                gameRepository.deleteAll();
                standingRepository.deleteAll();
                achievementRewardRepository.deleteAll(); // Delete rewards first to avoid FK constraint
                achievementRepository.deleteAll();
                logger.info("FORCE_DELETE_COMPLETE: Deleted existing games, standings, and achievements");
            } catch (Exception e) {
                logger.error("FORCE_DELETE_ERROR: Failed to delete existing data - {}", e.getMessage(), e);
            }
        }
        
        try {
            logger.info("MOCK_DATA_START: Initializing mock data for testing environment");
            
            // In testing environment, always recreate; otherwise check if mock data already exists
            if (!appConfig.isTestingEnvironment() && hasMockData()) {
                logger.info("MOCK_DATA_EXISTS: Mock data already present, skipping initialization");
                return;
            }
            
            createMockData();
            
            logger.info("MOCK_DATA_COMPLETE: Successfully initialized all mock data");
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to initialize mock data - {}", e.getMessage(), e);
        }
    }
    
    private boolean hasMockData() {
        try {
            // Check if we have games for current year
            List<Game> currentYearGames = gameRepository.findByYearOrderByWeekAndDate(CURRENT_YEAR);
            List<Team> allTeams = teamRepository.findAll();
            
            if (currentYearGames.isEmpty() || allTeams.isEmpty()) {
                logger.debug("MOCK_DATA_CHECK: No games or teams found");
                return false;
            }
            
            // Check if we have sufficient games (should be approximately 6+ games per team)
            double gamesPerTeam = (double) currentYearGames.size() / allTeams.size();
            boolean hasSufficientGames = gamesPerTeam >= 6.0;
            
            logger.info("MOCK_DATA_CHECK: Found {} games for {} teams (avg {:.1f} per team)", 
                currentYearGames.size(), allTeams.size(), gamesPerTeam);
            
            if (!hasSufficientGames) {
                logger.warn("MOCK_DATA_INSUFFICIENT: Average games per team ({:.1f}) is below threshold (6.0), recreating mock data", 
                    gamesPerTeam);
                return false;
            }
            
            logger.info("MOCK_DATA_SUFFICIENT: Mock data appears complete");
            return true;
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to check for existing mock data - {}", e.getMessage(), e);
            return false;
        }
    }
    
    private void createMockData() {
        logger.info("MOCK_DATA_CREATION: Starting comprehensive mock data creation");
        
        // 1. Ensure teams exist (create if needed)
        createMockTeamsIfNeeded();
        
        // 2. Create weeks for current season
        createMockWeeks();
        
        // 3. Create mock games and results
        createMockGames();
        
        // 4. Calculate standings from game results
        calculateMockStandings();
        
        // 5. Create mock achievements
        createMockAchievements();
        
        // 6. Initialize achievement rewards
        initializeAchievementRewards();
        
        logger.info("MOCK_DATA_SUMMARY: Created complete mock season data with achievements and rewards");
    }
    
    /**
     * Create comprehensive CFB teams if they don't exist in the database
     */
    private void createMockTeamsIfNeeded() {
        logger.info("MOCK_TEAMS_CHECK: Checking if teams exist in database");
        
        try {
            List<Team> existingTeams = teamRepository.findAll();
            
            if (!existingTeams.isEmpty()) {
                logger.info("MOCK_TEAMS_EXISTS: Found {} existing teams, skipping team creation", existingTeams.size());
                return;
            }
            
            logger.info("MOCK_TEAMS_CREATE: No teams found, creating comprehensive CFB team database");
            
            List<Team> teams = new ArrayList<>();
            
            // ACC Teams
            teams.addAll(createAccTeams());
            
            // Big 12 Teams  
            teams.addAll(createBig12Teams());
            
            // Big Ten Teams
            teams.addAll(createBigTenTeams());
            
            // SEC Teams
            teams.addAll(createSecTeams());
            
            // Group of 5 and Independent Teams
            teams.addAll(createGroupOf5Teams());
            
            // Save all teams to database
            List<Team> savedTeams = teamRepository.saveAll(teams);
            
            logger.info("MOCK_TEAMS_COMPLETE: Successfully created {} CFB teams across all conferences", savedTeams.size());
            
            // Log conference distribution
            Map<String, Long> conferenceDistribution = savedTeams.stream()
                .collect(Collectors.groupingBy(Team::getConference, Collectors.counting()));
            
            logger.info("MOCK_TEAMS_DISTRIBUTION: Teams by conference:");
            conferenceDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> logger.info("  {}: {} teams", entry.getKey(), entry.getValue()));
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to create mock teams - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Create ACC Conference teams
     */
    private List<Team> createAccTeams() {
        List<Team> teams = new ArrayList<>();
        
        teams.add(createTeam("Boston College Eagles", "Bill O'Brien", "ACC", "bc_eagles"));
        teams.add(createTeam("Clemson Tigers", "Dabo Swinney", "ACC", "clemson_tigers"));
        teams.add(createTeam("Duke Blue Devils", "Manny Diaz", "ACC", "duke_blue_devils"));
        teams.add(createTeam("Florida State Seminoles", "Mike Norvell", "ACC", "fsu_seminoles"));
        teams.add(createTeam("Georgia Tech Yellow Jackets", "Brent Key", "ACC", "gt_yellow_jackets"));
        teams.add(createTeam("Louisville Cardinals", "Jeff Brohm", "ACC", "louisville_cardinals"));
        teams.add(createTeam("Miami Hurricanes", "Mario Cristobal", "ACC", "miami_hurricanes"));
        teams.add(createTeam("NC State Wolfpack", "Dave Doeren", "ACC", "ncstate_wolfpack"));
        teams.add(createTeam("North Carolina Tar Heels", "Mack Brown", "ACC", "unc_tar_heels"));
        teams.add(createTeam("Pittsburgh Panthers", "Pat Narduzzi", "ACC", "pitt_panthers"));
        teams.add(createTeam("Syracuse Orange", "Fran Brown", "ACC", "syracuse_orange"));
        teams.add(createTeam("Virginia Cavaliers", "Tony Elliott", "ACC", "uva_cavaliers"));
        teams.add(createTeam("Virginia Tech Hokies", "Brent Pry", "ACC", "vt_hokies"));
        teams.add(createTeam("Wake Forest Demon Deacons", "Dave Clawson", "ACC", "wake_forest"));
        
        return teams;
    }
    
    /**
     * Create Big 12 Conference teams
     */
    private List<Team> createBig12Teams() {
        List<Team> teams = new ArrayList<>();
        
        teams.add(createTeam("Arizona Wildcats", "Brent Brennan", "Big 12", "arizona_wildcats"));
        teams.add(createTeam("Arizona State Sun Devils", "Kenny Dillingham", "Big 12", "asu_sun_devils"));
        teams.add(createTeam("Baylor Bears", "Dave Aranda", "Big 12", "baylor_bears"));
        teams.add(createTeam("BYU Cougars", "Kalani Sitake", "Big 12", "byu_cougars"));
        teams.add(createTeam("Cincinnati Bearcats", "Scott Satterfield", "Big 12", "cincinnati_bearcats"));
        teams.add(createTeam("Colorado Buffaloes", "Deion Sanders", "Big 12", "colorado_buffaloes"));
        teams.add(createTeam("Houston Cougars", "Willie Fritz", "Big 12", "houston_cougars"));
        teams.add(createTeam("Iowa State Cyclones", "Matt Campbell", "Big 12", "iowa_state_cyclones"));
        teams.add(createTeam("Kansas Jayhawks", "Lance Leipold", "Big 12", "kansas_jayhawks"));
        teams.add(createTeam("Kansas State Wildcats", "Chris Klieman", "Big 12", "ksu_wildcats"));
        teams.add(createTeam("Oklahoma State Cowboys", "Mike Gundy", "Big 12", "okstate_cowboys"));
        teams.add(createTeam("TCU Horned Frogs", "Sonny Dykes", "Big 12", "tcu_horned_frogs"));
        teams.add(createTeam("Texas Tech Red Raiders", "Joey McGuire", "Big 12", "texas_tech"));
        teams.add(createTeam("UCF Knights", "Gus Malzahn", "Big 12", "ucf_knights"));
        teams.add(createTeam("Utah Utes", "Kyle Whittingham", "Big 12", "utah_utes"));
        teams.add(createTeam("West Virginia Mountaineers", "Neal Brown", "Big 12", "wvu_mountaineers"));
        
        return teams;
    }
    
    /**
     * Create Big Ten Conference teams
     */
    private List<Team> createBigTenTeams() {
        List<Team> teams = new ArrayList<>();
        
        teams.add(createTeam("Illinois Fighting Illini", "Bret Bielema", "Big Ten", "illinois_illini"));
        teams.add(createTeam("Indiana Hoosiers", "Curt Cignetti", "Big Ten", "indiana_hoosiers"));
        teams.add(createTeam("Iowa Hawkeyes", "Kirk Ferentz", "Big Ten", "iowa_hawkeyes"));
        teams.add(createTeam("Maryland Terrapins", "Mike Locksley", "Big Ten", "maryland_terrapins"));
        teams.add(createTeam("Michigan Wolverines", "Sherrone Moore", "Big Ten", "michigan_wolverines"));
        teams.add(createTeam("Michigan State Spartans", "Jonathan Smith", "Big Ten", "msu_spartans"));
        teams.add(createTeam("Minnesota Golden Gophers", "P.J. Fleck", "Big Ten", "minnesota_gophers"));
        teams.add(createTeam("Nebraska Cornhuskers", "Matt Rhule", "Big Ten", "nebraska_cornhuskers"));
        teams.add(createTeam("Northwestern Wildcats", "David Braun", "Big Ten", "northwestern_wildcats"));
        teams.add(createTeam("Ohio State Buckeyes", "Ryan Day", "Big Ten", "osu_buckeyes"));
        teams.add(createTeam("Oregon Ducks", "Dan Lanning", "Big Ten", "oregon_ducks"));
        teams.add(createTeam("Penn State Nittany Lions", "James Franklin", "Big Ten", "psu_nittany_lions"));
        teams.add(createTeam("Purdue Boilermakers", "Ryan Walters", "Big Ten", "purdue_boilermakers"));
        teams.add(createTeam("Rutgers Scarlet Knights", "Greg Schiano", "Big Ten", "rutgers_knights"));
        teams.add(createTeam("UCLA Bruins", "DeShaun Foster", "Big Ten", "ucla_bruins"));
        teams.add(createTeam("USC Trojans", "Lincoln Riley", "Big Ten", "usc_trojans"));
        teams.add(createTeam("Washington Huskies", "Jedd Fisch", "Big Ten", "washington_huskies"));
        teams.add(createTeam("Wisconsin Badgers", "Luke Fickell", "Big Ten", "wisconsin_badgers"));
        
        return teams;
    }
    
    /**
     * Create SEC Conference teams
     */
    private List<Team> createSecTeams() {
        List<Team> teams = new ArrayList<>();
        
        teams.add(createTeam("Alabama Crimson Tide", "Kalen DeBoer", "SEC", "alabama_tide"));
        teams.add(createTeam("Arkansas Razorbacks", "Sam Pittman", "SEC", "arkansas_razorbacks"));
        teams.add(createTeam("Auburn Tigers", "Hugh Freeze", "SEC", "auburn_tigers"));
        teams.add(createTeam("Florida Gators", "Billy Napier", "SEC", "florida_gators"));
        teams.add(createTeam("Georgia Bulldogs", "Kirby Smart", "SEC", "georgia_bulldogs"));
        teams.add(createTeam("Kentucky Wildcats", "Mark Stoops", "SEC", "kentucky_wildcats"));
        teams.add(createTeam("LSU Tigers", "Brian Kelly", "SEC", "lsu_tigers"));
        teams.add(createTeam("Mississippi State Bulldogs", "Jeff Lebby", "SEC", "msstate_bulldogs"));
        teams.add(createTeam("Missouri Tigers", "Eli Drinkwitz", "SEC", "missouri_tigers"));
        teams.add(createTeam("Ole Miss Rebels", "Lane Kiffin", "SEC", "ole_miss_rebels"));
        teams.add(createTeam("Oklahoma Sooners", "Brent Venables", "SEC", "oklahoma_sooners"));
        teams.add(createTeam("South Carolina Gamecocks", "Shane Beamer", "SEC", "scar_gamecocks"));
        teams.add(createTeam("Tennessee Volunteers", "Josh Heupel", "SEC", "tennessee_vols"));
        teams.add(createTeam("Texas A&M Aggies", "Mike Elko", "SEC", "tamu_aggies"));
        teams.add(createTeam("Texas Longhorns", "Steve Sarkisian", "SEC", "texas_longhorns"));
        teams.add(createTeam("Vanderbilt Commodores", "Clark Lea", "SEC", "vanderbilt_commodores"));
        
        return teams;
    }
    
    /**
     * Create Group of 5 and Independent teams
     */
    private List<Team> createGroupOf5Teams() {
        List<Team> teams = new ArrayList<>();
        
        // American Athletic Conference
        teams.add(createTeam("Army Black Knights", "Jeff Monken", "American", "army_black_knights"));
        teams.add(createTeam("East Carolina Pirates", "Mike Houston", "American", "ecu_pirates"));
        teams.add(createTeam("Memphis Tigers", "Ryan Silverfield", "American", "memphis_tigers"));
        teams.add(createTeam("Navy Midshipmen", "Brian Newberry", "American", "navy_midshipmen"));
        teams.add(createTeam("SMU Mustangs", "Rhett Lashlee", "American", "smu_mustangs"));
        teams.add(createTeam("South Florida Bulls", "Alex Golesh", "American", "usf_bulls"));
        teams.add(createTeam("Temple Owls", "Stan Drayton", "American", "temple_owls"));
        teams.add(createTeam("Tulane Green Wave", "Jon Sumrall", "American", "tulane_green_wave"));
        teams.add(createTeam("Tulsa Golden Hurricane", "Kevin Wilson", "American", "tulsa_hurricane"));
        
        // Conference USA
        teams.add(createTeam("Florida International Panthers", "Mike MacIntyre", "C-USA", "fiu_panthers"));
        teams.add(createTeam("Liberty Flames", "Jamey Chadwell", "C-USA", "liberty_flames"));
        teams.add(createTeam("Middle Tennessee Blue Raiders", "Derek Mason", "C-USA", "mtsu_raiders"));
        teams.add(createTeam("New Mexico State Aggies", "Tony Sanchez", "C-USA", "nmsu_aggies"));
        teams.add(createTeam("UTEP Miners", "Scotty Walden", "C-USA", "utep_miners"));
        teams.add(createTeam("Western Kentucky Hilltoppers", "Tyson Helton", "C-USA", "wku_hilltoppers"));
        
        // MAC
        teams.add(createTeam("Akron Zips", "Joe Moorhead", "MAC", "akron_zips"));
        teams.add(createTeam("Ball State Cardinals", "Mike Neu", "MAC", "ball_state"));
        teams.add(createTeam("Bowling Green Falcons", "Scot Loeffler", "MAC", "bgsu_falcons"));
        teams.add(createTeam("Buffalo Bulls", "Pete Lembo", "MAC", "buffalo_bulls"));
        teams.add(createTeam("Central Michigan Chippewas", "Jim McElwain", "MAC", "cmu_chippewas"));
        teams.add(createTeam("Eastern Michigan Eagles", "Chris Creighton", "MAC", "emu_eagles"));
        teams.add(createTeam("Kent State Golden Flashes", "Kenni Burns", "MAC", "kent_state"));
        teams.add(createTeam("Miami RedHawks", "Chuck Martin", "MAC", "miami_oh"));
        teams.add(createTeam("Northern Illinois Huskies", "Thomas Hammock", "MAC", "niu_huskies"));
        teams.add(createTeam("Ohio Bobcats", "Tim Albin", "MAC", "ohio_bobcats"));
        teams.add(createTeam("Toledo Rockets", "Jason Candle", "MAC", "toledo_rockets"));
        teams.add(createTeam("Western Michigan Broncos", "Lance Taylor", "MAC", "wmu_broncos"));
        
        // Mountain West
        teams.add(createTeam("Air Force Falcons", "Troy Calhoun", "Mountain West", "air_force"));
        teams.add(createTeam("Boise State Broncos", "Spencer Danielson", "Mountain West", "boise_state"));
        teams.add(createTeam("Colorado State Rams", "Jay Norvell", "Mountain West", "colorado_state"));
        teams.add(createTeam("Fresno State Bulldogs", "Jeff Tedford", "Mountain West", "fresno_state"));
        teams.add(createTeam("Hawaii Rainbow Warriors", "Timmy Chang", "Mountain West", "hawaii_warriors"));
        teams.add(createTeam("Nevada Wolf Pack", "Jeff Choate", "Mountain West", "nevada_wolf_pack"));
        teams.add(createTeam("New Mexico Lobos", "Bronco Mendenhall", "Mountain West", "new_mexico"));
        teams.add(createTeam("San Diego State Aztecs", "Sean Lewis", "Mountain West", "sdsu_aztecs"));
        teams.add(createTeam("San Jose State Spartans", "Ken Niumatalolo", "Mountain West", "sjsu_spartans"));
        teams.add(createTeam("UNLV Rebels", "Barry Odom", "Mountain West", "unlv_rebels"));
        teams.add(createTeam("Utah State Aggies", "Nate Dreiling", "Mountain West", "utah_state"));
        teams.add(createTeam("Wyoming Cowboys", "Jay Sawvel", "Mountain West", "wyoming_cowboys"));
        
        // Sun Belt
        teams.add(createTeam("Appalachian State Mountaineers", "Shawn Clark", "Sun Belt", "app_state"));
        teams.add(createTeam("Arkansas State Red Wolves", "Butch Jones", "Sun Belt", "arkansas_state"));
        teams.add(createTeam("Coastal Carolina Chanticleers", "Tim Beck", "Sun Belt", "coastal_carolina"));
        teams.add(createTeam("Georgia Southern Eagles", "Clay Helton", "Sun Belt", "georgia_southern"));
        teams.add(createTeam("Georgia State Panthers", "Dell McGee", "Sun Belt", "georgia_state"));
        teams.add(createTeam("James Madison Dukes", "Bob Chesney", "Sun Belt", "jmu_dukes"));
        teams.add(createTeam("Louisiana Ragin Cajuns", "Michael Desormeaux", "Sun Belt", "louisiana_cajuns"));
        teams.add(createTeam("Louisiana Monroe Warhawks", "Bryant Vincent", "Sun Belt", "ulm_warhawks"));
        teams.add(createTeam("Marshall Thundering Herd", "Charles Huff", "Sun Belt", "marshall_herd"));
        teams.add(createTeam("Old Dominion Monarchs", "Ricky Rahne", "Sun Belt", "odu_monarchs"));
        teams.add(createTeam("South Alabama Jaguars", "Major Applewhite", "Sun Belt", "south_alabama"));
        teams.add(createTeam("Southern Miss Golden Eagles", "Will Hall", "Sun Belt", "southern_miss"));
        teams.add(createTeam("Texas State Bobcats", "G.J. Kinne", "Sun Belt", "texas_state"));
        teams.add(createTeam("Troy Trojans", "Jon Sumrall", "Sun Belt", "troy_trojans"));
        
        // Independents
        teams.add(createTeam("Notre Dame Fighting Irish", "Marcus Freeman", "Independent", "notre_dame"));
        teams.add(createTeam("UConn Huskies", "Jim Mora", "Independent", "uconn_huskies"));
        teams.add(createTeam("UMass Minutemen", "Don Brown", "Independent", "umass_minutemen"));
        
        return teams;
    }
    
    /**
     * Helper method to create a team entity
     */
    private Team createTeam(String name, String coach, String conference, String username) {
        Team team = new Team();
        team.setName(name);
        team.setCoach(coach);
        team.setConference(conference);
        team.setUsername(username);
        team.setIsHuman(false); // All teams start as AI-controlled
        team.setImageUrl("/images/teams/" + username + ".png");
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        return team;
    }
    
    private void createMockWeeks() {
        logger.info("MOCK_WEEK_CREATION: Creating weeks for year {}", CURRENT_YEAR);
        
        try {
            for (int weekNum = 1; weekNum <= 15; weekNum++) {
                Optional<Week> existingWeek = weekRepository.findByYearAndWeekNumber(CURRENT_YEAR, weekNum);
                
                if (!existingWeek.isPresent()) {
                    Week week = new Week(CURRENT_YEAR, weekNum);
                    weekRepository.save(week);
                    logger.debug("WEEK_CREATED: Week {} for year {}", weekNum, CURRENT_YEAR);
                }
            }
            
            logger.info("MOCK_WEEK_COMPLETE: Created 15 weeks for year {}", CURRENT_YEAR);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create mock weeks - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void createMockGames() {
        logger.info("MOCK_GAME_CREATION: Creating full CFB season schedule with comprehensive team schedules");
        
        try {
            List<Team> allTeams = teamRepository.findAll();
            List<Week> weeks = weekRepository.findByYearOrderByWeekNumber(CURRENT_YEAR);
            
            if (allTeams.isEmpty()) {
                logger.warn("MOCK_GAME_WARNING: No teams found, cannot create games");
                return;
            }
            
            logger.info("MOCK_GAME_TEAMS: Using {} teams to create full season schedule", allTeams.size());
            
            // Group teams by conference for realistic matchups
            Map<String, List<Team>> teamsByConference = allTeams.stream()
                .filter(team -> team.getConference() != null && !team.getConference().trim().isEmpty())
                .collect(Collectors.groupingBy(Team::getConference));
            
            // Generate full season schedule ensuring each team plays 12-15 games
            generateFullSeasonSchedule(weeks, allTeams, teamsByConference);
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to create full season schedule - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void generateFullSeasonSchedule(List<Week> weeks, List<Team> allTeams, Map<String, List<Team>> teamsByConference) {
        logger.info("STRUCTURED_SCHEDULE: Generating realistic schedule for {} teams across {} weeks", allTeams.size(), weeks.size());
        
        // Calculate realistic scheduling limits
        int maxGamesPerWeek = allTeams.size() / 2; // Each game uses 2 teams
        int maxTotalGames = maxGamesPerWeek * weeks.size();
        int maxGamesPerTeam = weeks.size(); // One game per team per week maximum
        int targetGamesPerTeam = Math.min(12, maxGamesPerTeam); // Target 12 games per team (realistic CFB)
        
        logger.info("SCHEDULE_LIMITS: Max {} games/week, {} total games, {} games/team target", 
            maxGamesPerWeek, maxTotalGames, targetGamesPerTeam);
        
        // Track games per team with specific conference/non-conference counts
        Map<Team, Integer> teamConferenceGames = new HashMap<>();
        Map<Team, Integer> teamNonConferenceGames = new HashMap<>();
        Map<Team, Set<Team>> teamOpponents = new HashMap<>();
        
        // Initialize tracking maps
        for (Team team : allTeams) {
            teamConferenceGames.put(team, 0);
            teamNonConferenceGames.put(team, 0);
            teamOpponents.put(team, new HashSet<>());
        }
        
        List<Game> allGames = new ArrayList<>();
        
        // Phase 1: Generate conference games (aim for 8 per team, but respect limits)
        int targetConfGamesPerTeam = Math.min(8, targetGamesPerTeam - 4); // Leave room for non-conf
        logger.info("PHASE_1: Generating conference games ({} per team target)", targetConfGamesPerTeam);
        generateConferenceGames(allTeams, teamsByConference, teamConferenceGames, teamOpponents, allGames, targetConfGamesPerTeam);
        
        // Phase 2: Generate non-conference games (fill remaining slots)
        int targetNonConfGamesPerTeam = Math.min(4, targetGamesPerTeam - targetConfGamesPerTeam);
        logger.info("PHASE_2: Generating non-conference games ({} per team target)", targetNonConfGamesPerTeam);
        generateNonConferenceGames(allTeams, teamsByConference, teamNonConferenceGames, teamOpponents, allGames, targetNonConfGamesPerTeam);
        
        // Phase 3: Verify game types and check scheduling feasibility
        logger.info("PHASE_3: Verifying and distributing {} games across {} weeks", allGames.size(), weeks.size());
        
        // Check if we have too many games for the available weeks
        int maxSchedulableGames = maxGamesPerWeek * weeks.size();
        if (allGames.size() > maxSchedulableGames) {
            logger.warn("SCHEDULE_OVERFLOW: Generated {} games but can only schedule {} max - will drop excess games", 
                allGames.size(), maxSchedulableGames);
            // Shuffle first to randomly select which games to keep
            Collections.shuffle(allGames, random);
            allGames = allGames.subList(0, maxSchedulableGames);
            logger.info("SCHEDULE_TRIMMED: Reduced to {} games for scheduling", allGames.size());
        }
        
        // Verify actual conference vs non-conference games before distribution
        long actualConfGames = allGames.stream()
            .filter(game -> {
                Team home = game.getHomeTeam();
                Team away = game.getAwayTeam();
                return home != null && away != null && 
                       Objects.equals(home.getConference(), away.getConference());
            })
            .count();
        long actualNonConfGames = allGames.size() - actualConfGames;
        
        logger.info("GAME_VERIFICATION: Actual conference games: {}, non-conference games: {}, total: {}", 
            actualConfGames, actualNonConfGames, allGames.size());
        
        Collections.shuffle(allGames, random); // Mix conference and non-conference games
        distributeGamesAcrossWeeks(allGames, weeks);
        
        // Log final summary with detailed team analysis
        int totalTeams = allTeams.size();
        int totalConfGames = teamConferenceGames.values().stream().mapToInt(Integer::intValue).sum();
        int totalNonConfGames = teamNonConferenceGames.values().stream().mapToInt(Integer::intValue).sum();
        double avgConferenceGames = (double) totalConfGames / totalTeams;
        double avgNonConferenceGames = (double) totalNonConfGames / totalTeams;
        
        // Find teams with insufficient games
        List<Team> teamsWithLowGames = allTeams.stream()
            .filter(team -> (teamConferenceGames.get(team) + teamNonConferenceGames.get(team)) < 10)
            .collect(Collectors.toList());
        
        logger.info("SCHEDULE_COMPLETE: Created {} total games for {} teams", allGames.size(), totalTeams);
        logger.info("SCHEDULE_STATS: Avg per team: {:.1f} conference, {:.1f} non-conference, {:.1f} total", 
            avgConferenceGames, avgNonConferenceGames, avgConferenceGames + avgNonConferenceGames);
        
        if (!teamsWithLowGames.isEmpty()) {
            logger.warn("SCHEDULE_WARNING: {} teams have fewer than 10 games", teamsWithLowGames.size());
            teamsWithLowGames.stream().limit(5).forEach(team -> 
                logger.warn("LOW_GAMES: {} has {} conference + {} non-conference = {} total games", 
                    team.getName(), teamConferenceGames.get(team), teamNonConferenceGames.get(team),
                    teamConferenceGames.get(team) + teamNonConferenceGames.get(team)));
        }
    }
    
    private void generateConferenceGames(List<Team> allTeams, Map<String, List<Team>> teamsByConference, 
                                       Map<Team, Integer> teamConferenceGames, Map<Team, Set<Team>> teamOpponents, 
                                       List<Game> allGames, int targetGamesPerTeam) {
        
        for (String conference : teamsByConference.keySet()) {
            List<Team> conferenceTeams = teamsByConference.get(conference);
            if (conferenceTeams.size() < 2) continue;
            
            logger.debug("CONFERENCE_GAMES: Generating games for {} ({} teams)", conference, conferenceTeams.size());
            
            // Use a more systematic approach to ensure all teams get games
            int maxIterations = conferenceTeams.size() * 10; // Prevent infinite loops
            int iterations = 0;
            
            while (iterations < maxIterations) {
                boolean gameCreated = false;
                iterations++;
                
                // Find teams that need more conference games
                List<Team> teamsNeedingGames = conferenceTeams.stream()
                    .filter(team -> teamConferenceGames.get(team) < targetGamesPerTeam)
                    .sorted((a, b) -> Integer.compare(teamConferenceGames.get(a), teamConferenceGames.get(b)))
                    .collect(Collectors.toList());
                
                if (teamsNeedingGames.isEmpty()) break;
                
                for (Team homeTeam : teamsNeedingGames) {
                    if (teamConferenceGames.get(homeTeam) >= targetGamesPerTeam) continue;
                    
                    // Find best available opponent
                    Team awayTeam = conferenceTeams.stream()
                        .filter(team -> !team.equals(homeTeam))
                        .filter(team -> teamConferenceGames.get(team) < targetGamesPerTeam)
                        .filter(team -> !teamOpponents.get(homeTeam).contains(team))
                        .min((a, b) -> Integer.compare(teamConferenceGames.get(a), teamConferenceGames.get(b)))
                        .orElse(null);
                    
                    // If no unplayed opponent available, allow rematches
                    if (awayTeam == null) {
                        awayTeam = conferenceTeams.stream()
                            .filter(team -> !team.equals(homeTeam))
                            .filter(team -> teamConferenceGames.get(team) < targetGamesPerTeam)
                            .min((a, b) -> Integer.compare(teamConferenceGames.get(a), teamConferenceGames.get(b)))
                            .orElse(null);
                    }
                    
                    if (awayTeam != null) {
                        // Create conference game
                        String gameId = String.format("CONF_%s_%s_%d", 
                            homeTeam.getName().replaceAll("\\s+", ""), 
                            awayTeam.getName().replaceAll("\\s+", ""), 
                            allGames.size());
                        
                        Game game = new Game(gameId, homeTeam, awayTeam, LocalDate.now(), null);
                        allGames.add(game);
                        
                        // Update tracking
                        teamConferenceGames.put(homeTeam, teamConferenceGames.get(homeTeam) + 1);
                        teamConferenceGames.put(awayTeam, teamConferenceGames.get(awayTeam) + 1);
                        teamOpponents.get(homeTeam).add(awayTeam);
                        teamOpponents.get(awayTeam).add(homeTeam);
                        
                        gameCreated = true;
                        logger.trace("CONF_GAME_CREATED: {} vs {} (home:{}/{}, away:{}/{})", 
                            homeTeam.getName(), awayTeam.getName(), 
                            teamConferenceGames.get(homeTeam), targetGamesPerTeam,
                            teamConferenceGames.get(awayTeam), targetGamesPerTeam);
                    }
                }
                
                if (!gameCreated) break; // No more games can be created
            }
            
            // Log final conference game counts
            int totalConfGames = conferenceTeams.stream()
                .mapToInt(team -> teamConferenceGames.get(team))
                .sum() / 2; // Divide by 2 since each game involves 2 teams
            logger.info("CONF_GAMES_COMPLETE: Conference {} created {} games for {} teams", 
                conference, totalConfGames, conferenceTeams.size());
        }
    }
    
    private void generateNonConferenceGames(List<Team> allTeams, Map<String, List<Team>> teamsByConference, 
                                          Map<Team, Integer> teamNonConferenceGames, Map<Team, Set<Team>> teamOpponents, 
                                          List<Game> allGames, int targetGamesPerTeam) {
        
        int maxIterations = allTeams.size() * 10; // Prevent infinite loops
        int iterations = 0;
        
        while (iterations < maxIterations) {
            boolean gameCreated = false;
            iterations++;
            
            // Find teams that need more non-conference games
            List<Team> teamsNeedingGames = allTeams.stream()
                .filter(team -> teamNonConferenceGames.get(team) < targetGamesPerTeam)
                .sorted((a, b) -> Integer.compare(teamNonConferenceGames.get(a), teamNonConferenceGames.get(b)))
                .collect(Collectors.toList());
            
            if (teamsNeedingGames.isEmpty()) break;
            
            for (Team homeTeam : teamsNeedingGames) {
                if (teamNonConferenceGames.get(homeTeam) >= targetGamesPerTeam) continue;
                
                // Find opponent from different conference first
                Team awayTeam = allTeams.stream()
                    .filter(team -> !team.equals(homeTeam))
                    .filter(team -> teamNonConferenceGames.get(team) < targetGamesPerTeam)
                    .filter(team -> !teamOpponents.get(homeTeam).contains(team))
                    .filter(team -> !Objects.equals(team.getConference(), homeTeam.getConference())) // Different conference
                    .min((a, b) -> Integer.compare(teamNonConferenceGames.get(a), teamNonConferenceGames.get(b)))
                    .orElse(null);
                
                // If no cross-conference opponent available with no previous matchup, try cross-conference with previous matchup
                if (awayTeam == null) {
                    awayTeam = allTeams.stream()
                        .filter(team -> !team.equals(homeTeam))
                        .filter(team -> teamNonConferenceGames.get(team) < targetGamesPerTeam)
                        .filter(team -> !Objects.equals(team.getConference(), homeTeam.getConference())) // Different conference
                        .min((a, b) -> Integer.compare(teamNonConferenceGames.get(a), teamNonConferenceGames.get(b)))
                        .orElse(null);
                }
                
                // If still no opponent, skip creating this non-conference game to ensure true non-conference games
                // Don't fall back to same-conference opponents for non-conference games
                
                if (awayTeam != null) {
                    // Double check that this is truly a non-conference game
                    if (!Objects.equals(homeTeam.getConference(), awayTeam.getConference())) {
                        // Create non-conference game
                        String gameId = String.format("NONCONF_%s_%s_%d", 
                            homeTeam.getName().replaceAll("\\s+", ""), 
                            awayTeam.getName().replaceAll("\\s+", ""), 
                            allGames.size());
                        
                        Game game = new Game(gameId, homeTeam, awayTeam, LocalDate.now(), null);
                        allGames.add(game);
                        
                        // Update tracking
                        teamNonConferenceGames.put(homeTeam, teamNonConferenceGames.get(homeTeam) + 1);
                        teamNonConferenceGames.put(awayTeam, teamNonConferenceGames.get(awayTeam) + 1);
                        teamOpponents.get(homeTeam).add(awayTeam);
                        teamOpponents.get(awayTeam).add(homeTeam);
                        
                        gameCreated = true;
                        logger.trace("NONCONF_GAME_CREATED: {} ({}) vs {} ({}) (home:{}/{}, away:{}/{})", 
                            homeTeam.getName(), homeTeam.getConference(), 
                            awayTeam.getName(), awayTeam.getConference(),
                            teamNonConferenceGames.get(homeTeam), targetGamesPerTeam,
                            teamNonConferenceGames.get(awayTeam), targetGamesPerTeam);
                    } else {
                        logger.warn("INVALID_NONCONF_GAME: Skipping same-conference matchup {} ({}) vs {} ({}) in non-conference generation", 
                            homeTeam.getName(), homeTeam.getConference(), 
                            awayTeam.getName(), awayTeam.getConference());
                    }
                }
            }
            
            if (!gameCreated) break; // No more games can be created
        }
        
        // Log final non-conference game summary with detailed stats
        int totalNonConfGames = allTeams.stream()
            .mapToInt(team -> teamNonConferenceGames.get(team))
            .sum() / 2; // Divide by 2 since each game involves 2 teams
        
        // Count teams by number of non-conference games
        Map<Integer, Long> nonConfDistribution = allTeams.stream()
            .collect(Collectors.groupingBy(
                team -> teamNonConferenceGames.get(team), 
                Collectors.counting()
            ));
        
        logger.info("NONCONF_GAMES_COMPLETE: Created {} non-conference games for {} teams", 
            totalNonConfGames, allTeams.size());
        logger.info("NONCONF_DISTRIBUTION: Games per team: {}", nonConfDistribution);
        
        // Show a few examples of teams with fewer than target non-conference games
        allTeams.stream()
            .filter(team -> teamNonConferenceGames.get(team) < targetGamesPerTeam)
            .limit(5)
            .forEach(team -> logger.info("LOW_NONCONF: {} ({}) has only {} non-conference games", 
                team.getName(), team.getConference(), teamNonConferenceGames.get(team)));
    }
    
    private void distributeGamesAcrossWeeks(List<Game> allGames, List<Week> weeks) {
        logger.info("TEAM_AWARE_DISTRIBUTION: Distributing {} games across {} weeks ensuring no team plays multiple games per week", allGames.size(), weeks.size());
        
        // Track which teams are already scheduled for each week
        Map<Integer, Set<Team>> teamsScheduledPerWeek = new HashMap<>();
        List<List<Game>> weekBuckets = new ArrayList<>();
        
        // Initialize tracking structures
        for (int i = 0; i < weeks.size(); i++) {
            teamsScheduledPerWeek.put(i, new HashSet<>());
            weekBuckets.add(new ArrayList<>());
        }
        
        // Distribute games ensuring no team conflict per week
        List<Game> unassignedGames = new ArrayList<>();
        
        for (Game game : allGames) {
            Team homeTeam = game.getHomeTeam();
            Team awayTeam = game.getAwayTeam();
            boolean gameAssigned = false;
            
            // Try to assign to a week where neither team is already scheduled
            for (int weekIndex = 0; weekIndex < weeks.size(); weekIndex++) {
                Set<Team> scheduledTeams = teamsScheduledPerWeek.get(weekIndex);
                
                // Check if neither team is already scheduled this week
                if (!scheduledTeams.contains(homeTeam) && !scheduledTeams.contains(awayTeam)) {
                    // Assign game to this week
                    weekBuckets.get(weekIndex).add(game);
                    scheduledTeams.add(homeTeam);
                    scheduledTeams.add(awayTeam);
                    gameAssigned = true;
                    
                    logger.trace("TEAM_AWARE_ASSIGN: Assigned {} vs {} to week {}", 
                        homeTeam.getName(), awayTeam.getName(), weekIndex + 1);
                    break;
                }
            }
            
            // If game couldn't be assigned to any week, add to unassigned list
            if (!gameAssigned) {
                unassignedGames.add(game);
                logger.warn("TEAM_CONFLICT: Could not assign {} vs {} to any week due to team conflicts", 
                    homeTeam.getName(), awayTeam.getName());
            }
        }
        
        // Handle unassigned games - ONLY assign to weeks with NO team conflicts
        for (Game game : unassignedGames) {
            Team homeTeam = game.getHomeTeam();
            Team awayTeam = game.getAwayTeam();
            boolean gameRescheduled = false;
            
            // Try to find a week where NEITHER team is already scheduled
            for (int weekIndex = 0; weekIndex < weeks.size(); weekIndex++) {
                Set<Team> scheduledTeams = teamsScheduledPerWeek.get(weekIndex);
                
                // Only assign if NO conflict exists
                if (!scheduledTeams.contains(homeTeam) && !scheduledTeams.contains(awayTeam)) {
                    weekBuckets.get(weekIndex).add(game);
                    scheduledTeams.add(homeTeam);
                    scheduledTeams.add(awayTeam);
                    gameRescheduled = true;
                    
                    logger.info("CONFLICT_RESOLUTION: Successfully rescheduled {} vs {} to week {} with no conflicts", 
                        homeTeam.getName(), awayTeam.getName(), weekIndex + 1);
                    break;
                }
            }
            
            // If still couldn't reschedule without conflicts, drop the game entirely
            if (!gameRescheduled) {
                logger.warn("GAME_DROPPED: Could not schedule {} vs {} to any week without team conflicts - dropping game", 
                    homeTeam.getName(), awayTeam.getName());
            }
        }
        
        int completedGames = 0;
        int totalAssigned = 0;
        
        // Now assign games from buckets to weeks
        for (int weekIndex = 0; weekIndex < weeks.size(); weekIndex++) {
            Week week = weeks.get(weekIndex);
            List<Game> gamesThisWeek = weekBuckets.get(weekIndex);
            boolean isCurrentWeek = weekIndex == 8; // Week 9 is "current"
            boolean isPastWeek = weekIndex < 8;
            
            logger.debug("TEAM_AWARE_WEEK_DISTRIBUTION: Week {} gets {} games", week.getWeekNumber(), gamesThisWeek.size());
            totalAssigned += gamesThisWeek.size();
            
            // Assign games to this week
            for (int i = 0; i < gamesThisWeek.size(); i++) {
                Game game = gamesThisWeek.get(i);
                game.setWeek(week);
                
                // Set realistic game date within the week
                LocalDate gameDate;
                if (isPastWeek) {
                    // Past games: Set dates in the past relative to now
                    gameDate = LocalDate.now()
                        .minusWeeks(8 - weekIndex)  // 8 weeks ago for week 1, 1 week ago for week 8
                        .minusDays(random.nextInt(3)) // Add some randomness within the week
                        .plusDays(random.nextInt(7));
                } else if (isCurrentWeek) {
                    // Current week: Set dates around now
                    gameDate = LocalDate.now().minusDays(random.nextInt(3)).plusDays(random.nextInt(7));
                } else {
                    // Future games: Set dates in the future
                    gameDate = LocalDate.now().plusWeeks(weekIndex - 8).plusDays(random.nextInt(7));
                }
                game.setDate(gameDate);
                
                // Set game status and scores based on week
                if (isPastWeek) {
                    game.setStatus(Game.GameStatus.COMPLETED);
                    generateShowcaseScore(game, weekIndex, i);
                    completedGames++;
                } else if (isCurrentWeek) {
                    // Current week - mix of statuses
                    Game.GameStatus[] statuses = {Game.GameStatus.IN_PROGRESS, Game.GameStatus.SCHEDULED, Game.GameStatus.COMPLETED};
                    game.setStatus(statuses[i % statuses.length]);
                    
                    if (game.getStatus() == Game.GameStatus.IN_PROGRESS) {
                        generatePartialScore(game);
                    } else if (game.getStatus() == Game.GameStatus.COMPLETED) {
                        generateShowcaseScore(game, weekIndex, i);
                        completedGames++;
                    }
                } else {
                    // Future games
                    game.setStatus(Game.GameStatus.SCHEDULED);
                    addGameRankings(game);
                }
                
                gameRepository.save(game);
            }
        }
        
        logger.info("TEAM_AWARE_DISTRIBUTION_COMPLETE: Distributed {} games across {} weeks, {} completed", 
            totalAssigned, weeks.size(), completedGames);
        
        // Log any remaining unassigned games
        if (totalAssigned < allGames.size()) {
            logger.warn("UNASSIGNED_GAMES: {} games could not be assigned due to scheduling conflicts", 
                allGames.size() - totalAssigned);
        }
        
        // Log team conflicts summary and verify no conflicts exist
        int totalConflicts = 0;
        for (int weekIndex = 0; weekIndex < weeks.size(); weekIndex++) {
            List<Game> weekGames = weekBuckets.get(weekIndex);
            
            // Check for any team conflicts in final schedule
            Set<Team> allTeamsInWeek = new HashSet<>();
            List<Team> conflictedTeams = new ArrayList<>();
            
            for (Game game : weekGames) {
                Team homeTeam = game.getHomeTeam();
                Team awayTeam = game.getAwayTeam();
                
                // Check if home team already played this week
                if (allTeamsInWeek.contains(homeTeam)) {
                    conflictedTeams.add(homeTeam);
                    totalConflicts++;
                    logger.error("TEAM_CONFLICT: {} has multiple games in week {} (gameId: {})", 
                        homeTeam.getName(), weekIndex + 1, game.getGameId());
                }
                
                // Check if away team already played this week  
                if (allTeamsInWeek.contains(awayTeam)) {
                    conflictedTeams.add(awayTeam);
                    totalConflicts++;
                    logger.error("TEAM_CONFLICT: {} has multiple games in week {} (gameId: {})", 
                        awayTeam.getName(), weekIndex + 1, game.getGameId());
                }
                
                allTeamsInWeek.add(homeTeam);
                allTeamsInWeek.add(awayTeam);
            }
            
            if (!conflictedTeams.isEmpty()) {
                logger.warn("WEEK_CONFLICTS: Week {} has {} team conflicts involving teams: {}", 
                    weekIndex + 1, conflictedTeams.size(), 
                    conflictedTeams.stream().map(Team::getName).collect(Collectors.joining(", ")));
            } else if (weekGames.size() > 0) {
                logger.debug("WEEK_CLEAN: Week {} has {} games with no team conflicts", 
                    weekIndex + 1, weekGames.size());
            }
        }
        
        if (totalConflicts == 0) {
            logger.info("SCHEDULE_VALIDATION: SUCCESS - No team conflicts detected across all weeks");
        } else {
            logger.error("SCHEDULE_VALIDATION: FAILED - {} total team conflicts detected", totalConflicts);
        }
    }
    
    /**
     * Create special showcase scenarios to highlight different dashboard features
     */
    private void createShowcaseScenarios(List<Week> weeks, List<Team> allTeams, Map<String, List<Team>> teamsByConference) {
        logger.info("SHOWCASE_SCENARIOS: Creating special scenarios to demonstrate UI features");
        
        try {
            // Scenario 1: Championship contenders - create teams with strong records
            createChampionshipContenderScenarios(weeks.subList(0, 8), teamsByConference);
            
            // Scenario 2: Upset victories - lower ranked teams beating higher ranked
            createUpsetVictoryScenarios(weeks.subList(3, 6), allTeams);
            
            // Scenario 3: Conference rivalry games with high stakes
            createRivalryGameScenarios(weeks.subList(6, 8), teamsByConference);
            
            // Scenario 4: Bowl eligibility scenarios
            createBowlEligibilityScenarios(weeks.subList(0, 7), allTeams);
            
            logger.info("SHOWCASE_SCENARIOS_COMPLETE: Created special scenarios for UI demonstration");
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create showcase scenarios - {}", e.getMessage(), e);
        }
    }
    
    private void createChampionshipContenderScenarios(List<Week> weeks, Map<String, List<Team>> teamsByConference) {
        logger.debug("CHAMPIONSHIP_SCENARIOS: Creating championship contender games");
        
        // Pick top conferences and create high-profile matchups
        String[] topConferences = {"SEC", "Big Ten", "Big 12", "ACC"};
        
        for (String conference : topConferences) {
            List<Team> confTeams = teamsByConference.get(conference);
            if (confTeams != null && confTeams.size() >= 4) {
                // Create matchups between top teams in each conference
                for (int i = 0; i < Math.min(weeks.size(), 4) && i < confTeams.size() - 1; i++) {
                    Week week = weeks.get(i);
                    Team team1 = confTeams.get(i);
                    Team team2 = confTeams.get(i + 1);
                    
                    Game championshipGame = createSpecialGame(week, team1, team2, "Championship Contender");
                    championshipGame.setHomeTeamRank(i + 1); // Give them rankings
                    championshipGame.setAwayTeamRank(i + 3);
                    
                    gameRepository.save(championshipGame);
                }
            }
        }
    }
    
    private void createUpsetVictoryScenarios(List<Week> weeks, List<Team> allTeams) {
        logger.debug("UPSET_SCENARIOS: Creating upset victory games");
        
        for (int i = 0; i < Math.min(weeks.size(), 3) && i < allTeams.size() - 10; i++) {
            Week week = weeks.get(i);
            Team lowerTeam = allTeams.get(50 + i); // Mid-tier team
            Team higherTeam = allTeams.get(i); // Top-tier team
            
            Game upsetGame = createSpecialGame(week, lowerTeam, higherTeam, "Upset Victory");
            upsetGame.setHomeTeamRank(0); // Unranked
            upsetGame.setAwayTeamRank(i + 5); // Ranked team
            
            // Make the unranked team win (upset!)
            upsetGame.setStatus(Game.GameStatus.COMPLETED);
            upsetGame.setHomeScore(28 + random.nextInt(15)); // 28-42 points
            upsetGame.setAwayScore(14 + random.nextInt(10)); // 14-23 points (loss)
            
            gameRepository.save(upsetGame);
        }
    }
    
    private void createRivalryGameScenarios(List<Week> weeks, Map<String, List<Team>> teamsByConference) {
        logger.debug("RIVALRY_SCENARIOS: Creating rivalry games");
        
        // Create intense rivalry matchups with close scores
        for (String conference : teamsByConference.keySet()) {
            List<Team> confTeams = teamsByConference.get(conference);
            if (confTeams.size() >= 2 && !weeks.isEmpty()) {
                Week week = weeks.get(random.nextInt(weeks.size()));
                Team team1 = confTeams.get(0);
                Team team2 = confTeams.get(1);
                
                Game rivalryGame = createSpecialGame(week, team1, team2, "Rivalry Game");
                rivalryGame.setHomeTeamRank(8 + random.nextInt(15)); // Ranked teams
                rivalryGame.setAwayTeamRank(12 + random.nextInt(15));
                
                // Make it a close, exciting game
                rivalryGame.setStatus(Game.GameStatus.COMPLETED);
                int baseScore = 21 + random.nextInt(14); // 21-34 base
                rivalryGame.setHomeScore(baseScore);
                rivalryGame.setAwayScore(baseScore + random.nextInt(7) - 3); // Within 3 points
                
                gameRepository.save(rivalryGame);
                break; // One rivalry game per conference
            }
        }
    }
    
    private void createBowlEligibilityScenarios(List<Week> weeks, List<Team> allTeams) {
        logger.debug("BOWL_SCENARIOS: Creating bowl eligibility scenarios");
        
        // Create scenarios where teams are fighting for bowl eligibility (6 wins)
        for (int i = 0; i < Math.min(weeks.size(), 5) && i < allTeams.size() - 20; i++) {
            Week week = weeks.get(i);
            Team team1 = allTeams.get(30 + i); // Mid-tier teams
            Team team2 = allTeams.get(40 + i);
            
            Game bowlGame = createSpecialGame(week, team1, team2, "Bowl Eligibility");
            bowlGame.setStatus(Game.GameStatus.COMPLETED);
            
            // Create a scenario where one team gets closer to bowl eligibility
            bowlGame.setHomeScore(24 + random.nextInt(14)); // 24-37 points
            bowlGame.setAwayScore(17 + random.nextInt(10)); // 17-26 points
            
            gameRepository.save(bowlGame);
        }
    }
    
    private Game createSpecialGame(Week week, Team homeTeam, Team awayTeam, String scenario) {
        logger.trace("SPECIAL_GAME: Creating {} game - {} vs {}", scenario, homeTeam.getName(), awayTeam.getName());
        
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setWeek(week);
        game.setStatus(Game.GameStatus.SCHEDULED);
        
        // Set realistic date
        LocalDate gameDate = LocalDate.now()
            .minusWeeks(8)
            .plusWeeks(week.getWeekNumber() - 1)
            .plusDays(random.nextInt(3));
        game.setDate(gameDate);
        
        return game;
    }
    
    /**
     * Create showcase game with enhanced features for UI demonstration
     */
    private Game createShowcaseGame(Week week, List<Team> allTeams, 
                                  Map<String, List<Team>> teamsByConference, 
                                  Set<Team> usedTeams, int weekIndex) {
        try {
            List<Team> availableTeams = allTeams.stream()
                .filter(team -> !usedTeams.contains(team))
                .collect(Collectors.toList());
            
            if (availableTeams.size() < 2) {
                return null;
            }
            
            Team homeTeam = availableTeams.get(random.nextInt(availableTeams.size()));
            usedTeams.add(homeTeam);
            
            // Create more interesting matchups for showcase
            Team awayTeam = null;
            
            // 80% conference games for better championship scenarios
            if (random.nextDouble() < 0.8 && homeTeam.getConference() != null) {
                List<Team> conferenceTeams = teamsByConference.get(homeTeam.getConference());
                if (conferenceTeams != null && conferenceTeams.size() > 1) {
                    List<Team> availableConferenceTeams = conferenceTeams.stream()
                        .filter(team -> !usedTeams.contains(team) && !team.getId().equals(homeTeam.getId()))
                        .collect(Collectors.toList());
                    
                    if (!availableConferenceTeams.isEmpty()) {
                        awayTeam = availableConferenceTeams.get(random.nextInt(availableConferenceTeams.size()));
                    }
                }
            }
            
            // If no conference opponent found, pick any available team
            if (awayTeam == null) {
                availableTeams.remove(homeTeam);
                if (availableTeams.isEmpty()) {
                    return null;
                }
                awayTeam = availableTeams.get(random.nextInt(availableTeams.size()));
            }
            
            usedTeams.add(awayTeam);
            
            // Create game with showcase features
            LocalDate gameDate = LocalDate.now()
                .minusWeeks(8)
                .plusWeeks(week.getWeekNumber() - 1)
                .plusDays(random.nextInt(3));
            
            Game game = new Game();
            game.setGameId("SHOWCASE_" + UUID.randomUUID().toString());
            game.setHomeTeam(homeTeam);
            game.setAwayTeam(awayTeam);
            game.setDate(gameDate);
            game.setWeek(week);
            game.setStatus(Game.GameStatus.SCHEDULED);
            
            // Add rankings for more interesting games
            addShowcaseRankings(game, weekIndex);
            
            return game;
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create showcase game - {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Add rankings to games to make them more interesting for showcase
     */
    private void addShowcaseRankings(Game game, int weekIndex) {
        try {
            // 60% chance of having at least one ranked team
            if (random.nextDouble() < 0.6) {
                // Home team ranking (1-25, or 0 for unranked)
                if (random.nextDouble() < 0.7) {
                    game.setHomeTeamRank(1 + random.nextInt(25));
                } else {
                    game.setHomeTeamRank(0); // Unranked
                }
                
                // Away team ranking
                if (random.nextDouble() < 0.7) {
                    game.setAwayTeamRank(1 + random.nextInt(25));
                } else {
                    game.setAwayTeamRank(0); // Unranked
                }
                
                // Ensure we don't have duplicate rankings
                if (game.getHomeTeamRank() > 0 && game.getAwayTeamRank() > 0 && 
                    game.getHomeTeamRank().equals(game.getAwayTeamRank())) {
                    game.setAwayTeamRank(game.getAwayTeamRank() + 1);
                    if (game.getAwayTeamRank() > 25) {
                        game.setAwayTeamRank(0); // Make unranked if over 25
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to add showcase rankings - {}", e.getMessage(), e);
            // Set default rankings
            game.setHomeTeamRank(0);
            game.setAwayTeamRank(0);
        }
    }
    
    /**
     * Add rankings to future games for better UI display
     */
    private void addGameRankings(Game game) {
        try {
            // 40% chance of having ranked teams in future games
            if (random.nextDouble() < 0.4) {
                if (random.nextBoolean()) {
                    game.setHomeTeamRank(1 + random.nextInt(25));
                }
                if (random.nextBoolean()) {
                    game.setAwayTeamRank(1 + random.nextInt(25));
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: Failed to add game rankings - {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate diverse showcase scores to demonstrate different UI states
     */
    private void generateShowcaseScore(Game game, int weekIndex, int gameIndex) {
        try {
            ScoreType scoreType = determineScoreType(weekIndex, gameIndex);
            
            switch (scoreType) {
                case BLOWOUT_HOME:
                    game.setHomeScore(42 + random.nextInt(20)); // 42-61 points
                    game.setAwayScore(7 + random.nextInt(14));  // 7-20 points
                    logger.trace("SHOWCASE_SCORE: Blowout victory for {}", game.getHomeTeam().getName());
                    break;
                    
                case BLOWOUT_AWAY:
                    game.setHomeScore(10 + random.nextInt(14)); // 10-23 points
                    game.setAwayScore(38 + random.nextInt(25)); // 38-62 points
                    logger.trace("SHOWCASE_SCORE: Blowout victory for {}", game.getAwayTeam().getName());
                    break;
                    
                case CLOSE_GAME:
                    int baseScore = 21 + random.nextInt(14); // 21-34 base
                    game.setHomeScore(baseScore);
                    game.setAwayScore(baseScore + random.nextInt(7) - 3); // Within 3 points
                    // Ensure positive score
                    if (game.getAwayScore() < 0) game.setAwayScore(3);
                    logger.trace("SHOWCASE_SCORE: Close game - {} vs {}", 
                        game.getHomeTeam().getName(), game.getAwayTeam().getName());
                    break;
                    
                case OVERTIME_THRILLER:
                    int otBase = 28 + random.nextInt(10); // 28-37 base
                    game.setHomeScore(otBase);
                    game.setAwayScore(otBase + (random.nextBoolean() ? 3 : -3)); // Exactly 3 point difference (OT)
                    logger.trace("SHOWCASE_SCORE: Overtime thriller between {} and {}", 
                        game.getHomeTeam().getName(), game.getAwayTeam().getName());
                    break;
                    
                case HIGH_SCORING:
                    game.setHomeScore(45 + random.nextInt(20)); // 45-64 points
                    game.setAwayScore(38 + random.nextInt(20)); // 38-57 points
                    logger.trace("SHOWCASE_SCORE: High-scoring affair - {} vs {}", 
                        game.getHomeTeam().getName(), game.getAwayTeam().getName());
                    break;
                    
                case DEFENSIVE_BATTLE:
                    game.setHomeScore(9 + random.nextInt(12));  // 9-20 points
                    game.setAwayScore(6 + random.nextInt(12));  // 6-17 points
                    logger.trace("SHOWCASE_SCORE: Defensive battle - {} vs {}", 
                        game.getHomeTeam().getName(), game.getAwayTeam().getName());
                    break;
                    
                default:
                    // Standard realistic score
                    generateRealisticScore(game);
                    break;
            }
            
            // Ensure scores are non-negative
            if (game.getHomeScore() < 0) game.setHomeScore(0);
            if (game.getAwayScore() < 0) game.setAwayScore(0);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to generate showcase score - {}", e.getMessage(), e);
            // Fallback to basic score
            game.setHomeScore(21);
            game.setAwayScore(17);
        }
    }
    
    /**
     * Determine what type of score to create for variety
     */
    private ScoreType determineScoreType(int weekIndex, int gameIndex) {
        // Create different types of games based on week and game index for variety
        int seed = weekIndex * 10 + gameIndex;
        int typeIndex = seed % 7;
        
        return ScoreType.values()[typeIndex];
    }
    
    /**
     * Enum for different types of scores to showcase various UI states
     */
    private enum ScoreType {
        BLOWOUT_HOME,      // Home team dominates
        BLOWOUT_AWAY,      // Away team dominates  
        CLOSE_GAME,        // Close, competitive game
        OVERTIME_THRILLER, // Game that likely went to overtime
        HIGH_SCORING,      // Both teams score a lot
        DEFENSIVE_BATTLE,  // Low-scoring defensive game
        STANDARD           // Normal CFB score
    }
    
    private Game createRealisticGame(Week week, List<Team> allTeams, 
                                   Map<String, List<Team>> teamsByConference, 
                                   Set<Team> usedTeams) {
        try {
            List<Team> availableTeams = allTeams.stream()
                .filter(team -> !usedTeams.contains(team))
                .collect(Collectors.toList());
            
            if (availableTeams.size() < 2) {
                return null;
            }
            
            Team homeTeam = availableTeams.get(random.nextInt(availableTeams.size()));
            usedTeams.add(homeTeam);
            
            // Try to find a conference opponent 70% of the time
            Team awayTeam = null;
            if (random.nextDouble() < 0.7 && homeTeam.getConference() != null) {
                List<Team> conferenceTeams = teamsByConference.get(homeTeam.getConference());
                if (conferenceTeams != null && conferenceTeams.size() > 1) {
                    List<Team> availableConferenceTeams = conferenceTeams.stream()
                        .filter(team -> !usedTeams.contains(team) && !team.getId().equals(homeTeam.getId()))
                        .collect(Collectors.toList());
                    
                    if (!availableConferenceTeams.isEmpty()) {
                        awayTeam = availableConferenceTeams.get(random.nextInt(availableConferenceTeams.size()));
                    }
                }
            }
            
            // If no conference opponent found, pick any available team
            if (awayTeam == null) {
                availableTeams.remove(homeTeam);
                if (availableTeams.isEmpty()) {
                    return null;
                }
                awayTeam = availableTeams.get(random.nextInt(availableTeams.size()));
            }
            
            usedTeams.add(awayTeam);
            
            // Create game with realistic date
            LocalDate gameDate = LocalDate.now()
                .minusWeeks(8)  // Start 8 weeks ago
                .plusWeeks(week.getWeekNumber() - 1)
                .plusDays(random.nextInt(3)); // Random day within the week
            
            Game game = new Game();
            game.setGameId(UUID.randomUUID().toString());
            game.setHomeTeam(homeTeam);
            game.setAwayTeam(awayTeam);
            game.setDate(gameDate);
            game.setWeek(week);
            game.setStatus(Game.GameStatus.SCHEDULED);
            
            return game;
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to create realistic game - {}", e.getMessage(), e);
            return null;
        }
    }
    
    private void generateRealisticScore(Game game) {
        try {
            // Generate realistic CFB scores (typically 14-45 points)
            int homeScore = 14 + random.nextInt(32); // 14-45
            int awayScore = 14 + random.nextInt(32); // 14-45
            
            // Add some randomness for blowouts or close games
            if (random.nextDouble() < 0.15) {
                // Blowout game
                if (random.nextBoolean()) {
                    homeScore += 20 + random.nextInt(21); // Add 20-40 more points
                } else {
                    awayScore += 20 + random.nextInt(21);
                }
            } else if (random.nextDouble() < 0.3) {
                // Close game (within 7 points)
                int leadingScore = Math.max(homeScore, awayScore);
                int deficit = random.nextInt(8); // 0-7 point difference
                
                if (homeScore > awayScore) {
                    awayScore = homeScore - deficit;
                } else {
                    homeScore = awayScore - deficit;
                }
            }
            
            game.setHomeScore(homeScore);
            game.setAwayScore(awayScore);
            
            logger.trace("SCORE_GENERATED: {} {} - {} {}", 
                game.getHomeTeam().getName(), homeScore, awayScore, game.getAwayTeam().getName());
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to generate realistic score - {}", e.getMessage(), e);
            // Set default scores
            game.setHomeScore(21);
            game.setAwayScore(17);
        }
    }
    
    private void generatePartialScore(Game game) {
        try {
            // Generate partial scores for in-progress games
            int homeScore = random.nextInt(25); // 0-24
            int awayScore = random.nextInt(25); // 0-24
            
            game.setHomeScore(homeScore);
            game.setAwayScore(awayScore);
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to generate partial score - {}", e.getMessage(), e);
            game.setHomeScore(7);
            game.setAwayScore(10);
        }
    }
    
    private void calculateMockStandings() {
        logger.info("MOCK_STANDINGS: Calculating standings from mock game results");
        
        try {
            // Calculate conference standings using our existing service
            conferenceStandingsService.calculateConferenceStandings(CURRENT_YEAR);
            
            // Calculate overall rankings across all teams
            calculateOverallRankings(CURRENT_YEAR);
            
            // Verify standings were created
            List<Standing> allStandings = standingRepository.findByYearOrderByWinsDescLossesAsc(CURRENT_YEAR);
            logger.info("MOCK_STANDINGS_COMPLETE: Created standings for {} teams", allStandings.size());
            
            // Log top 5 teams for verification
            allStandings.stream()
                .limit(5)
                .forEach(standing -> 
                    logger.debug("TOP_TEAM: {} - {}-{} ({:.3f}), Conf: {}-{} (Rank #{}), Overall Rank: {}", 
                        standing.getTeam().getName(),
                        standing.getWins(), standing.getLosses(), standing.getWinPercentage(),
                        standing.getConferenceWins(), standing.getConferenceLosses(), 
                        standing.getConferenceRank(), standing.getRank()));
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to calculate mock standings - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Calculate overall rankings across all teams based on wins/losses
     */
    private void calculateOverallRankings(Integer year) {
        logger.info("OVERALL_RANKINGS: Calculating overall rankings for year {}", year);
        
        try {
            // Get all standings for the year, sorted by wins desc, losses asc
            List<Standing> allStandings = standingRepository.findByYearOrderByWinsDescLossesAsc(year);
            logger.info("OVERALL_RANKINGS: Found {} teams to rank for year {}", allStandings.size(), year);
            
            if (allStandings.isEmpty()) {
                logger.warn("OVERALL_RANKINGS: No standings found for year {}", year);
                return;
            }
            
            // Sort by win percentage (primary) and then by total wins (secondary)
            allStandings.sort((a, b) -> {
                // Primary: Win percentage (higher is better)
                double aWinPct = a.getWinPercentage();
                double bWinPct = b.getWinPercentage();
                
                if (aWinPct != bWinPct) {
                    return Double.compare(bWinPct, aWinPct); // Higher percentage first
                }
                
                // Secondary: Total wins (more wins is better for same percentage)
                if (!a.getWins().equals(b.getWins())) {
                    return Integer.compare(b.getWins(), a.getWins()); // More wins first
                }
                
                // Tertiary: Fewer losses (better record)
                if (!a.getLosses().equals(b.getLosses())) {
                    return Integer.compare(a.getLosses(), b.getLosses()); // Fewer losses first
                }
                
                // Final: Team name for consistency
                return a.getTeam().getName().compareTo(b.getTeam().getName());
            });
            
            // Assign rankings - no ties allowed, only top 25 teams get ranks
            for (int i = 0; i < allStandings.size(); i++) {
                Standing currentStanding = allStandings.get(i);
                
                if (i < 25) {
                    // Top 25 teams get ranks 1-25
                    int rank = i + 1; 
                    currentStanding.setRank(rank);
                    logger.debug("RANKED: {} assigned rank #{}", 
                        currentStanding.getTeam().getName(), rank);
                    
                    // Log details for all ranked teams
                    logger.info("RANK_{}: {} - {}-{} ({:.3f})", 
                        rank,
                        currentStanding.getTeam().getName(),
                        currentStanding.getWins(), 
                        currentStanding.getLosses(),
                        currentStanding.getWinPercentage());
                } else {
                    // Teams ranked 26+ are unranked (no rank assigned)
                    currentStanding.setRank(null);
                    logger.debug("UNRANKED: {} - outside top 25", 
                        currentStanding.getTeam().getName());
                }
                
                // Save the updated standing
                standingRepository.save(currentStanding);
            }
            
            logger.info("OVERALL_RANKINGS_COMPLETE: Assigned top 25 rankings, {} teams unranked for year {}", 
                Math.max(0, allStandings.size() - 25), year);
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to calculate overall rankings for year {} - {}", year, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Create comprehensive mock achievements for CFB management game
     */
    private void createMockAchievements() {
        logger.info("MOCK_ACHIEVEMENTS: Creating comprehensive college football achievements");
        
        try {
            List<Achievement> achievements = new ArrayList<>();
            
            // WINS Achievements
            achievements.addAll(createWinAchievements());
            
            // SEASON Achievements  
            achievements.addAll(createSeasonAchievements());
            
            // CHAMPIONSHIP Achievements
            achievements.addAll(createChampionshipAchievements());
            
            // STATISTICS Achievements
            achievements.addAll(createStatisticsAchievements());
            
            // GENERAL Achievements
            achievements.addAll(createGeneralAchievements());
            
            // Save all achievements to database
            List<Achievement> savedAchievements = achievementRepository.saveAll(achievements);
            
            // Randomly complete some achievements for demonstration
            randomlyCompleteAchievements(savedAchievements);
            
            logger.info("MOCK_ACHIEVEMENTS_COMPLETE: Created {} achievements across all categories", savedAchievements.size());
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to create mock achievements - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private List<Achievement> createWinAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        // First Win
        achievements.add(new Achievement(
            "First Victory", 
            "Win your first game of the season", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.COMMON);
        achievements.get(achievements.size() - 1).setIcon("emoji_events");
        achievements.get(achievements.size() - 1).setColor("#4caf50");
        
        // Win Streak Achievements
        achievements.add(new Achievement(
            "Hot Streak", 
            "Win 3 games in a row", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.COMMON);
        achievements.get(achievements.size() - 1).setIcon("local_fire_department");
        achievements.get(achievements.size() - 1).setColor("#ff9800");
        
        achievements.add(new Achievement(
            "Unstoppable Force", 
            "Win 5 games in a row", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("whatshot");
        achievements.get(achievements.size() - 1).setColor("#ff5722");
        
        achievements.add(new Achievement(
            "Dynasty Building", 
            "Win 10 games in a row", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("military_tech");
        achievements.get(achievements.size() - 1).setColor("#9c27b0");
        
        // Season Win Milestones
        achievements.add(new Achievement(
            "Bowl Bound", 
            "Win 6 games in a season", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("sports_football");
        achievements.get(achievements.size() - 1).setColor("#2196f3");
        
        achievements.add(new Achievement(
            "Top Tier", 
            "Win 10 games in a season", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("star");
        achievements.get(achievements.size() - 1).setColor("#ffc107");
        
        achievements.add(new Achievement(
            "Perfect Season", 
            "Go undefeated for an entire season", 
            Achievement.AchievementType.WINS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.LEGENDARY);
        achievements.get(achievements.size() - 1).setIcon("diamond");
        achievements.get(achievements.size() - 1).setColor("#e91e63");
        
        return achievements;
    }
    
    private List<Achievement> createSeasonAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        achievements.add(new Achievement(
            "Rookie Coach", 
            "Complete your first season as head coach", 
            Achievement.AchievementType.SEASON
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.COMMON);
        achievements.get(achievements.size() - 1).setIcon("school");
        achievements.get(achievements.size() - 1).setColor("#4caf50");
        
        achievements.add(new Achievement(
            "Comeback Kid", 
            "Win a game when trailing by 14+ points", 
            Achievement.AchievementType.SEASON
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("trending_up");
        achievements.get(achievements.size() - 1).setColor("#ff9800");
        
        achievements.add(new Achievement(
            "Nail Biter", 
            "Win 3 games by 3 points or less", 
            Achievement.AchievementType.SEASON
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("favorite");
        achievements.get(achievements.size() - 1).setColor("#f44336");
        
        achievements.add(new Achievement(
            "Turnover Machine", 
            "Force 20 turnovers in a season", 
            Achievement.AchievementType.SEASON
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("security");
        achievements.get(achievements.size() - 1).setColor("#607d8b");
        
        achievements.add(new Achievement(
            "Scheduling Nightmare", 
            "Beat 3 ranked opponents in one season", 
            Achievement.AchievementType.SEASON
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("celebration");
        achievements.get(achievements.size() - 1).setColor("#3f51b5");
        
        return achievements;
    }
    
    private List<Achievement> createChampionshipAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        achievements.add(new Achievement(
            "Conference Champion", 
            "Win your conference championship", 
            Achievement.AchievementType.CHAMPIONSHIP
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("emoji_events");
        achievements.get(achievements.size() - 1).setColor("#ffc107");
        
        achievements.add(new Achievement(
            "Bowl Victory", 
            "Win a bowl game", 
            Achievement.AchievementType.CHAMPIONSHIP
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("sports");
        achievements.get(achievements.size() - 1).setColor("#4caf50");
        
        achievements.add(new Achievement(
            "National Champion", 
            "Win the College Football Playoff National Championship", 
            Achievement.AchievementType.CHAMPIONSHIP
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.LEGENDARY);
        achievements.get(achievements.size() - 1).setIcon("workspace_premium");
        achievements.get(achievements.size() - 1).setColor("#e91e63");
        
        achievements.add(new Achievement(
            "Playoff Participant", 
            "Reach the College Football Playoff", 
            Achievement.AchievementType.CHAMPIONSHIP
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("stars");
        achievements.get(achievements.size() - 1).setColor("#9c27b0");
        
        achievements.add(new Achievement(
            "Dynasty Established", 
            "Win 3 conference championships", 
            Achievement.AchievementType.CHAMPIONSHIP
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.LEGENDARY);
        achievements.get(achievements.size() - 1).setIcon("castle");
        achievements.get(achievements.size() - 1).setColor("#795548");
        
        return achievements;
    }
    
    private List<Achievement> createStatisticsAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        achievements.add(new Achievement(
            "Offensive Juggernaut", 
            "Score 50+ points in a single game", 
            Achievement.AchievementType.STATISTICS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("bolt");
        achievements.get(achievements.size() - 1).setColor("#ff9800");
        
        achievements.add(new Achievement(
            "Defensive Wall", 
            "Allow 7 points or fewer in a game", 
            Achievement.AchievementType.STATISTICS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("shield");
        achievements.get(achievements.size() - 1).setColor("#607d8b");
        
        achievements.add(new Achievement(
            "Shutout Artist", 
            "Win a game without allowing any points", 
            Achievement.AchievementType.STATISTICS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("block");
        achievements.get(achievements.size() - 1).setColor("#9e9e9e");
        
        achievements.add(new Achievement(
            "Balanced Attack", 
            "Have 200+ passing and 200+ rushing yards in one game", 
            Achievement.AchievementType.STATISTICS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("balance");
        achievements.get(achievements.size() - 1).setColor("#2196f3");
        
        achievements.add(new Achievement(
            "Big Play Specialist", 
            "Score 5+ touchdowns of 40+ yards in a season", 
            Achievement.AchievementType.STATISTICS
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("flash_on");
        achievements.get(achievements.size() - 1).setColor("#ffeb3b");
        
        return achievements;
    }
    
    private List<Achievement> createGeneralAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        achievements.add(new Achievement(
            "Welcome to College Football", 
            "Play your first game", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.COMMON);
        achievements.get(achievements.size() - 1).setIcon("sports_football");
        achievements.get(achievements.size() - 1).setColor("#4caf50");
        
        achievements.add(new Achievement(
            "Recruiting Ace", 
            "Successfully recruit 5 top-tier players", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.UNCOMMON);
        achievements.get(achievements.size() - 1).setIcon("group_add");
        achievements.get(achievements.size() - 1).setColor("#3f51b5");
        
        achievements.add(new Achievement(
            "Fan Favorite", 
            "Achieve 90%+ fan satisfaction", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.RARE);
        achievements.get(achievements.size() - 1).setIcon("favorite");
        achievements.get(achievements.size() - 1).setColor("#e91e63");
        
        achievements.add(new Achievement(
            "Rivalry Domination", 
            "Beat your biggest rival 3 years in a row", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("military_tech");
        achievements.get(achievements.size() - 1).setColor("#ff5722");
        
        achievements.add(new Achievement(
            "Program Builder", 
            "Transform a team from bottom-tier to top-10", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.LEGENDARY);
        achievements.get(achievements.size() - 1).setIcon("trending_up");
        achievements.get(achievements.size() - 1).setColor("#4caf50");
        
        achievements.add(new Achievement(
            "Upset Master", 
            "Beat a top-5 ranked team while unranked", 
            Achievement.AchievementType.GENERAL
        ));
        achievements.get(achievements.size() - 1).setRarity(Achievement.AchievementRarity.EPIC);
        achievements.get(achievements.size() - 1).setIcon("equalizer");
        achievements.get(achievements.size() - 1).setColor("#ff9800");
        
        return achievements;
    }
    
    private void randomlyCompleteAchievements(List<Achievement> achievements) {
        logger.info("MOCK_ACHIEVEMENTS_COMPLETION: Randomly completing achievements for demonstration");
        
        int completedCount = 0;
        for (Achievement achievement : achievements) {
            // Complete ~30% of achievements randomly for demonstration
            if (random.nextDouble() < 0.3) {
                achievement.setIsCompleted(true);
                achievement.setDateCompleted(Instant.now().toEpochMilli() - random.nextInt(1000000000)); // Random past completion time
                completedCount++;
            }
        }
        
        // Save the completion updates
        achievementRepository.saveAll(achievements);
        
        logger.info("MOCK_ACHIEVEMENTS_COMPLETION: Completed {} out of {} achievements for demonstration", 
            completedCount, achievements.size());
    }
    
    /**
     * Manually trigger mock data creation (for testing/admin purposes)
     */
    public void recreateMockData() {
        logger.info("MANUAL_RECREATION: Manually recreating mock data");
        
        try {
            // Clean existing mock data
            cleanMockData();
            
            // Create fresh mock data (includes overall rankings calculation)
            createMockData();
            
            logger.info("MANUAL_RECREATION_COMPLETE: Successfully recreated mock data with overall rankings");
            
        } catch (Exception e) {
            logger.error("FATAL_ERROR: Failed to recreate mock data - {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Public method to recalculate overall rankings for existing standings
     */
    public void recalculateOverallRankings() {
        logger.info("PUBLIC_RECALCULATE: Recalculating overall rankings for year {}", CURRENT_YEAR);
        calculateOverallRankings(CURRENT_YEAR);
    }
    
    /**
     * Debug method to analyze current game distribution per team
     */
    public void analyzeGameDistribution() {
        logger.info("GAME_ANALYSIS: Analyzing current game distribution");
        
        try {
            List<Team> allTeams = teamRepository.findAll();
            List<Game> allGames = gameRepository.findByYearOrderByWeekAndDate(CURRENT_YEAR);
            
            Map<Team, Integer> gameCount = new HashMap<>();
            for (Team team : allTeams) {
                gameCount.put(team, 0);
            }
            
            // Count games per team
            for (Game game : allGames) {
                if (game.getHomeTeam() != null) {
                    gameCount.put(game.getHomeTeam(), gameCount.getOrDefault(game.getHomeTeam(), 0) + 1);
                }
                if (game.getAwayTeam() != null) {
                    gameCount.put(game.getAwayTeam(), gameCount.getOrDefault(game.getAwayTeam(), 0) + 1);
                }
            }
            
            // Log summary
            logger.info("GAME_ANALYSIS: Total {} teams, {} games", allTeams.size(), allGames.size());
            
            // Show teams with different game counts
            Map<Integer, Long> gameCountDistribution = gameCount.values().stream()
                .collect(Collectors.groupingBy(count -> count, Collectors.counting()));
            
            logger.info("GAME_DISTRIBUTION: Game count per team distribution:");
            gameCountDistribution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> logger.info("  {} games: {} teams", entry.getKey(), entry.getValue()));
            
            // Show specific examples of teams with low game counts
            gameCount.entrySet().stream()
                .filter(entry -> entry.getValue() < 6)
                .limit(10)
                .forEach(entry -> logger.warn("LOW_GAME_TEAM: {} has {} games", 
                    entry.getKey().getName(), entry.getValue()));
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to analyze game distribution - {}", e.getMessage(), e);
        }
    }
    
    private void cleanMockData() {
        logger.info("MOCK_DATA_CLEANUP: Cleaning existing mock data");
        
        try {
            // Delete standings for current year
            standingRepository.findByYearOrderByWinsDescLossesAsc(CURRENT_YEAR)
                .forEach(standing -> standingRepository.delete(standing));
            
            // Delete games for current year
            gameRepository.findByYearOrderByWeekAndDate(CURRENT_YEAR)
                .forEach(game -> gameRepository.delete(game));
            
            // Delete weeks for current year
            weekRepository.findByYearOrderByWeekNumber(CURRENT_YEAR)
                .forEach(week -> weekRepository.delete(week));
            
            // Delete all achievements (and rewards first to avoid FK constraint)
            achievementRewardRepository.deleteAll();
            achievementRepository.deleteAll();
            
            logger.info("MOCK_DATA_CLEANUP_COMPLETE: Cleaned existing mock data");
            
        } catch (Exception e) {
            logger.error("ERROR: Failed to clean mock data - {}", e.getMessage(), e);
        }
    }

    private void clearExistingMockData() {
        logger.info("PROD_CLEAR_START: Clearing any existing mock data in production environment");
        
        try {
            // Count existing data before clearing
            long standingsCount = standingRepository.findByYearOrderByWinsDescLossesAsc(CURRENT_YEAR).size();
            long gamesCount = gameRepository.findByYearOrderByWeekAndDate(CURRENT_YEAR).size();
            long weeksCount = weekRepository.findByYearOrderByWeekNumber(CURRENT_YEAR).size();
            long achievementsCount = achievementRepository.count();
            
            if (standingsCount > 0 || gamesCount > 0 || weeksCount > 0 || achievementsCount > 0) {
                logger.info("PROD_CLEAR_FOUND: Found {} standings, {} games, {} weeks, {} achievements to clear", 
                    standingsCount, gamesCount, weeksCount, achievementsCount);
                
                // Use the existing clean method
                cleanMockData();
                
                logger.info("PROD_CLEAR_COMPLETE: Cleared mock data in production environment");
            } else {
                logger.info("PROD_CLEAR_SKIP: No mock data found to clear");
            }
            
        } catch (Exception e) {
            logger.error("PROD_CLEAR_ERROR: Failed to clear mock data in production - {}", e.getMessage(), e);
        }
    }
    
    private void initializeAchievementRewards() {
        logger.info("REWARD_INIT_START: Initializing achievement rewards system");
        
        try {
            // Check if rewards already exist to avoid duplicates
            long existingRewardCount = achievementRewardRepository.count();
            if (existingRewardCount > 0) {
                logger.info("REWARD_INIT_SKIP: Found {} existing rewards, skipping initialization", existingRewardCount);
                return;
            }
            
            // Initialize the reward system
            achievementRewardService.initializeDefaultRewards();
            
            // Get final count for logging
            long finalRewardCount = achievementRewardRepository.count();
            logger.info("REWARD_INIT_COMPLETE: Successfully initialized {} achievement rewards", finalRewardCount);
            
        } catch (Exception e) {
            logger.error("REWARD_INIT_ERROR: Failed to initialize achievement rewards - {}", e.getMessage(), e);
            // Don't throw exception as this shouldn't break mock data creation
        }
    }
    
    private String getActiveProfiles() {
        return String.join(", ", environment.getActiveProfiles());
    }
}