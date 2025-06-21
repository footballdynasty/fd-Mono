import React, { useState } from 'react';
import {
  Grid,
  Typography,
  Box,
  Chip,
  Avatar,
  LinearProgress,
  CircularProgress,
} from '@mui/material';
import {
  SportsFootball,
  EmojiEvents,
  TrendingUp,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import GlassCard from '../components/ui/GlassCard';
import GradientButton from '../components/ui/GradientButton';
import GameDetailModal from '../components/ui/GameDetailModal';
import { gameApi, conferenceStandingsApi, conferenceChampionshipApi } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import { Game } from '../types';

const Dashboard: React.FC = () => {
  const { selectedTeam } = useAuth();
  
  // Modal state for game details
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [isGameModalOpen, setIsGameModalOpen] = useState(false);

  const handleGameDetailsClick = (game: Game) => {
    setSelectedGame(game);
    setIsGameModalOpen(true);
  };

  const handleGameModalClose = () => {
    setIsGameModalOpen(false);
    setSelectedGame(null);
  };

  // API Queries - now using the selected team
  const { data: recentGamesData, isLoading: recentGamesLoading } = useQuery({
    queryKey: ['games', 'recent', selectedTeam?.id],
    queryFn: async () => {
      if (!selectedTeam) return [];
      const response = await gameApi.getRecent(selectedTeam.id, 10);
      return response.data;
    },
    enabled: !!selectedTeam,
  });

  const { data: upcomingGamesData, isLoading: upcomingGamesLoading } = useQuery({
    queryKey: ['games', 'upcoming', selectedTeam?.id],
    queryFn: async () => {
      if (!selectedTeam) return [];
      const response = await gameApi.getUpcoming(selectedTeam.id);
      return response.data;
    },
    enabled: !!selectedTeam,
  });

  const { data: standings, isLoading: standingsLoading } = useQuery({
    queryKey: ['standings', new Date().getFullYear()],
    queryFn: async () => {
      const response = await conferenceStandingsApi.getAll(new Date().getFullYear());
      // Flatten the grouped conference standings into a single array
      const groupedStandings = response.data as Record<string, any[]>;
      return Object.values(groupedStandings).flat();
    },
  });

  const { data: championshipBid, isLoading: championshipLoading } = useQuery({
    queryKey: ['championship-bid', selectedTeam?.id, new Date().getFullYear()],
    queryFn: async () => {
      if (!selectedTeam) return null;
      const response = await conferenceChampionshipApi.getChampionshipBid(selectedTeam.id, new Date().getFullYear());
      return response.data;
    },
    enabled: !!selectedTeam,
  });

  const containerVariants = {
    initial: { opacity: 0 },
    animate: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    initial: { y: 20, opacity: 0 },
    animate: {
      y: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 300,
        damping: 24,
      },
    },
  };

  // Calculate stats from API data using selected team
  const userStanding = standings?.find(s => s.team.id === selectedTeam?.id);
  
  const statCards = [
    {
      title: 'Conference Rank',
      value: championshipBid?.currentRank ? `#${championshipBid.currentRank}` : '--',
      icon: TrendingUp,
      color: '#4caf50',
      gradient: 'success',
      subtitle: `${selectedTeam?.conference || ''} Standing`,
    },
    {
      title: 'Conference Record',
      value: championshipBid ? `${championshipBid.conferenceWins}-${championshipBid.conferenceLosses}` : '--',
      icon: SportsFootball,
      color: '#1e88e5',
      gradient: 'primary',
      subtitle: 'Conference Games',
    },
    {
      title: 'Overall Record',
      value: userStanding ? `${userStanding.wins}-${userStanding.losses}` : '--',
      icon: SportsFootball,
      color: '#ff9800',
      gradient: 'warning',
      subtitle: 'Season Record',
    },
    {
      title: 'Championship Bid',
      value: championshipBid?.canStillWinConference ? 'Alive' : 'Eliminated',
      icon: EmojiEvents,
      color: championshipBid?.canStillWinConference ? '#4caf50' : '#f44336',
      gradient: championshipBid?.canStillWinConference ? 'success' : 'error',
      subtitle: (() => {
        if (championshipBid?.canStillWinConference) {
          return 'Still in contention';
        }
        
        // Calculate total conference games played
        const totalConferenceGames = (championshipBid?.conferenceWins || 0) + (championshipBid?.conferenceLosses || 0);
        
        // Show odds instead of "Out of race" for teams with < 9 conference games
        if (totalConferenceGames < 9) {
          // Simple odds calculation based on remaining games and current position
          const remainingGames = championshipBid?.remainingGames || 0;
          const currentRank = championshipBid?.currentRank || 99;
          
          // Basic odds calculation: better rank and more remaining games = higher odds
          let oddsPercentage = 0;
          if (remainingGames > 0) {
            // Base odds decrease with worse ranking, increase with more remaining games
            const rankFactor = Math.max(0, (10 - currentRank) / 10); // Better rank = higher factor
            const gamesFactor = Math.min(1, remainingGames / 8); // More games = higher factor
            oddsPercentage = Math.round(rankFactor * gamesFactor * 25); // Max ~25% for early season
            oddsPercentage = Math.max(1, Math.min(25, oddsPercentage)); // Keep between 1-25%
          }
          
          return `${oddsPercentage}% championship odds`;
        }
        
        return 'Out of race';
      })(),
    },
  ];

  // Process recent games data
  const recentGames = (recentGamesData || []).slice(0, 4).map((game: any) => {
    const isHome = game.homeTeamId === selectedTeam?.id;
    const opponent = isHome ? game.awayTeamName : game.homeTeamName;
    const userScore = isHome ? game.homeScore : game.awayScore;
    const opponentScore = isHome ? game.awayScore : game.homeScore;
    
    let result = 'T';
    if (userScore > opponentScore) result = 'W';
    else if (userScore < opponentScore) result = 'L';
    
    return {
      opponent: opponent || 'Unknown',
      score: `${userScore}-${opponentScore}`,
      result,
      date: game.date ? format(new Date(game.date), 'MMM d') : 'TBD',
    };
  }).filter(Boolean);

  // Process upcoming games data
  const totalUpcomingGames = (upcomingGamesData || []).length;
  const displayedUpcomingGames = 2;
  const hiddenGamesCount = Math.max(0, totalUpcomingGames - displayedUpcomingGames);
  

  // Calculate season progress metrics
  const bowlEligible = (userStanding?.wins || 0) >= 6;
  const winPercentage = userStanding?.winPercentage || 0;
  
  // Conference championship progress based on championship bid
  const championshipProgress = championshipBid ? {
    gamesNeeded: championshipBid.gamesNeededToClinch,
    remainingGames: championshipBid.remainingGames,
    canWin: championshipBid.canStillWinConference,
    conferenceWinPct: championshipBid.conferenceWinPercentage,
    rank: championshipBid.currentRank,
    analysis: championshipBid.analysis,
  } : null;

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
    >
      <Box sx={{ mb: 4 }}>
        <motion.div variants={itemVariants}>
          <Typography
            variant="h3"
            sx={{
              fontWeight: 700,
              mb: 1,
              background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              pt: 7
            }}
          >
            Dashboard
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Welcome back, Coach! Here's your team overview.
          </Typography>
        </motion.div>
      </Box>

      <Grid container spacing={3}>
        {/* Stat Cards */}
        {standingsLoading || championshipLoading ? (
          Array.from({ length: 4 }).map((_, index) => (
            <Grid item xs={12} sm={6} lg={3} key={index}>
              <motion.div variants={itemVariants}>
                <GlassCard gradient>
                  <Box sx={{ p: 3, display: 'flex', justifyContent: 'center', alignItems: 'center', height: 120 }}>
                    <CircularProgress />
                  </Box>
                </GlassCard>
              </motion.div>
            </Grid>
          ))
        ) : statCards.map((stat, index) => (
          <Grid item xs={12} sm={6} lg={3} key={stat.title}>
            <motion.div variants={itemVariants}>
              <GlassCard gradient>
                <Box sx={{ p: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar
                      sx={{
                        background: `linear-gradient(135deg, ${stat.color}22, ${stat.color}44)`,
                        color: stat.color,
                        mr: 2,
                      }}
                    >
                      <stat.icon />
                    </Avatar>
                    <Box>
                      <Typography variant="h4" sx={{ fontWeight: 700 }}>
                        {stat.value}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {stat.subtitle}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="h6" color="text.primary">
                    {stat.title}
                  </Typography>
                </Box>
              </GlassCard>
            </motion.div>
          </Grid>
        ))}

        {/* Recent Games */}
        <Grid item xs={12} lg={6}>
          <motion.div variants={itemVariants}>
            <GlassCard>
              <Box sx={{ p: 3 }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                  Recent Games
                </Typography>
                {recentGamesLoading ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : recentGames.length === 0 ? (
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                    No recent games found
                  </Typography>
                ) : recentGames.map((game: any, index: number) => (
                  <Box
                    key={index}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      py: 2,
                      borderBottom: index < recentGames.length - 1 ? '1px solid rgba(255,255,255,0.1)' : 'none',
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Chip
                        label={game.result}
                        size="small"
                        sx={{
                          background: game.result === 'W' 
                            ? 'linear-gradient(135deg, #4caf50, #66bb6a)'
                            : 'linear-gradient(135deg, #f44336, #e57373)',
                          color: 'white',
                          fontWeight: 600,
                          mr: 2,
                        }}
                      />
                      <Typography variant="body1" sx={{ fontWeight: 600 }}>
                        vs {game.opponent}
                      </Typography>
                    </Box>
                    <Box sx={{ textAlign: 'right' }}>
                      <Typography variant="body1" sx={{ fontWeight: 600 }}>
                        {game.score}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {game.date}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Upcoming Games */}
        <Grid item xs={12} lg={6}>
          <motion.div variants={itemVariants}>
            <GlassCard>
              <Box sx={{ p: 3 }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                  Upcoming Games
                </Typography>
                {upcomingGamesLoading ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : (upcomingGamesData || []).length === 0 ? (
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                    No upcoming games scheduled
                  </Typography>
                ) : (
                  <>
                    {(upcomingGamesData || []).slice(0, displayedUpcomingGames).map((game: any, index: number) => {
                      const isHome = game.homeTeamId === selectedTeam?.id;
                      const opponent = isHome ? game.awayTeamName : game.homeTeamName;
                      const opponentRank = isHome ? game.awayTeamRank : game.homeTeamRank;
                      const displayGame = {
                        opponent: opponent || 'Unknown',
                        date: game.date ? format(new Date(game.date), 'MMM d') : 'TBD',
                        time: game.date ? format(new Date(game.date), 'h:mm a') : 'TBD',
                        rank: opponentRank ? `#${opponentRank}` : 'NR',
                      };
                      
                      return (
                      <Box
                        key={index}
                        sx={{
                          p: 2,
                          mb: 2,
                          background: 'linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05))',
                          borderRadius: '12px',
                          border: '1px solid rgba(255,255,255,0.1)',
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                          <Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                              <Typography variant="h6" sx={{ fontWeight: 600, mr: 1 }}>
                                vs {displayGame.opponent}
                              </Typography>
                              <Chip
                                label={displayGame.rank}
                                size="small"
                                sx={{
                                  background: 'linear-gradient(135deg, #ff9800, #ffb74d)',
                                  color: 'white',
                                  fontWeight: 600,
                                }}
                              />
                            </Box>
                            <Typography variant="body2" color="text.secondary">
                              {displayGame.date} • {displayGame.time}
                            </Typography>
                          </Box>
                          <GradientButton 
                            size="small"
                            onClick={() => handleGameDetailsClick(game)}
                          >
                            View Details
                          </GradientButton>
                        </Box>
                      </Box>
                      );
                    })}
                    {hiddenGamesCount > 0 && (
                      <Box sx={{ 
                        textAlign: 'center', 
                        py: 2,
                        borderTop: '1px solid rgba(255,255,255,0.1)',
                        mt: 1
                      }}>
                        <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                          ... {hiddenGamesCount} more game{hiddenGamesCount !== 1 ? 's' : ''} scheduled
                        </Typography>
                      </Box>
                    )}
                  </>
                )}
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Team Progress */}
        <Grid item xs={12}>
          <motion.div variants={itemVariants}>
            <GlassCard>
              <Box sx={{ p: 3 }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                  Season Progress
                </Typography>
                {standingsLoading || championshipLoading ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : (
                  <Grid container spacing={3}>
                  <Grid item xs={12} md={4}>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body1" sx={{ mb: 1 }}>
                        Conference Championship
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={championshipProgress?.canWin ? 
                          (championshipProgress.remainingGames > 0 && championshipProgress.gamesNeeded > 0 ? 
                            Math.max(10, 100 - ((championshipProgress.gamesNeeded / championshipProgress.remainingGames) * 100)) : 
                            championshipProgress.rank === 1 ? 100 : 50) : 0}
                        sx={{
                          height: 8,
                          borderRadius: 4,
                          background: 'rgba(255,255,255,0.1)',
                          '& .MuiLinearProgress-bar': {
                            background: championshipProgress?.canWin 
                              ? 'linear-gradient(90deg, #4caf50, #66bb6a)'
                              : 'linear-gradient(90deg, #f44336, #e57373)',
                            borderRadius: 4,
                          },
                        }}
                      />
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {championshipProgress?.canWin 
                          ? championshipProgress.rank === 1 
                            ? 'Leading conference'
                            : `Need ${championshipProgress.gamesNeeded} more wins`
                          : 'Eliminated from title race'}
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body1" sx={{ mb: 1 }}>
                        Bowl Eligibility
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={bowlEligible ? 100 : ((userStanding?.wins || 0) / 6) * 100}
                        sx={{
                          height: 8,
                          borderRadius: 4,
                          background: 'rgba(255,255,255,0.1)',
                          '& .MuiLinearProgress-bar': {
                            background: bowlEligible 
                              ? 'linear-gradient(90deg, #4caf50, #66bb6a)'
                              : 'linear-gradient(90deg, #ff9800, #ffb74d)',
                            borderRadius: 4,
                          },
                        }}
                      />
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {bowlEligible ? 'Qualified' : `${userStanding?.wins || 0}/6 wins`}
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body1" sx={{ mb: 1 }}>
                        Win Percentage
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={winPercentage * 100}
                        sx={{
                          height: 8,
                          borderRadius: 4,
                          background: 'rgba(255,255,255,0.1)',
                          '& .MuiLinearProgress-bar': {
                            background: winPercentage >= 0.75 
                              ? 'linear-gradient(90deg, #4caf50, #66bb6a)'
                              : winPercentage >= 0.5
                              ? 'linear-gradient(90deg, #ff9800, #ffb74d)'
                              : 'linear-gradient(90deg, #f44336, #e57373)',
                            borderRadius: 4,
                          },
                        }}
                      />
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {(winPercentage * 100).toFixed(1)}% this season
                      </Typography>
                    </Box>
                  </Grid>
                  </Grid>
                )}
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Conference Championship Analysis */}
        {championshipProgress && (
          <Grid item xs={12}>
            <motion.div variants={itemVariants}>
              <GlassCard>
                <Box sx={{ p: 3 }}>
                  <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                    Conference Championship Analysis
                  </Typography>
                  <Grid container spacing={3}>
                    <Grid item xs={12} md={8}>
                      <Box sx={{ 
                        p: 3, 
                        background: 'linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05))',
                        borderRadius: '12px',
                        border: '1px solid rgba(255,255,255,0.1)',
                        mb: 2
                      }}>
                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                          Current Situation
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 2 }}>
                          {championshipProgress.analysis}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                          <Chip 
                            label={`#${championshipProgress.rank} in ${selectedTeam?.conference}`}
                            sx={{ 
                              background: 'linear-gradient(135deg, #1e88e5, #42a5f5)',
                              color: 'white',
                              fontWeight: 600
                            }}
                          />
                          <Chip 
                            label={`${(championshipProgress.conferenceWinPct * 100).toFixed(1)}% conf. win rate`}
                            sx={{ 
                              background: 'linear-gradient(135deg, #4caf50, #66bb6a)',
                              color: 'white',
                              fontWeight: 600
                            }}
                          />
                          {championshipProgress.canWin && championshipProgress.gamesNeeded > 0 && (
                            <Chip 
                              label={`${championshipProgress.gamesNeeded} wins needed`}
                              sx={{ 
                                background: 'linear-gradient(135deg, #ff9800, #ffb74d)',
                                color: 'white',
                                fontWeight: 600
                              }}
                            />
                          )}
                        </Box>
                      </Box>
                    </Grid>
                    <Grid item xs={12} md={4}>
                      <Box sx={{ 
                        p: 3, 
                        background: championshipProgress.canWin 
                          ? 'linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.05))'
                          : 'linear-gradient(135deg, rgba(244, 67, 54, 0.1), rgba(244, 67, 54, 0.05))',
                        borderRadius: '12px',
                        border: `1px solid ${championshipProgress.canWin ? 'rgba(76, 175, 80, 0.3)' : 'rgba(244, 67, 54, 0.3)'}`,
                        textAlign: 'center',
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'center'
                      }}>
                        <Typography variant="h3" sx={{ 
                          fontWeight: 700, 
                          mb: 1,
                          color: championshipProgress.canWin ? '#4caf50' : '#f44336'
                        }}>
                          {championshipProgress.canWin ? '✓' : '✗'}
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                          Championship Bid
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {championshipProgress.canWin ? 'Still in contention' : 'Mathematically eliminated'}
                        </Typography>
                      </Box>
                    </Grid>
                  </Grid>
                </Box>
              </GlassCard>
            </motion.div>
          </Grid>
        )}
      </Grid>

      {/* Game Detail Modal */}
      <GameDetailModal
        open={isGameModalOpen}
        onClose={handleGameModalClose}
        game={selectedGame}
      />
    </motion.div>
  );
};

export default Dashboard;