import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Box,
  Typography,
  Chip,
  Avatar,
  Divider,
  Grid,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Close,
  Schedule,
  Stadium,
  EmojiEvents,
  SportsFootball,
} from '@mui/icons-material';
import { format, isToday, isTomorrow, isYesterday } from 'date-fns';
import { motion } from 'framer-motion';
import { Game, GameStatus } from '../../types';
import GlassCard from './GlassCard';

interface GameDetailModalProps {
  open: boolean;
  onClose: () => void;
  game: Game | null;
}

const GameDetailModal: React.FC<GameDetailModalProps> = ({
  open,
  onClose,
  game,
}) => {
  const theme = useTheme();

  if (!game) return null;

  // Format game date and time with validation
  const gameDate = new Date(game.date);
  const isValidDate = !isNaN(gameDate.getTime());
  
  const formatGameTime = () => {
    if (!isValidDate) {
      return game.date || 'Date TBD';
    }
    
    try {
      if (isToday(gameDate)) {
        return `Today • ${format(gameDate, 'h:mm a')}`;
      } else if (isTomorrow(gameDate)) {
        return `Tomorrow • ${format(gameDate, 'h:mm a')}`;
      } else if (isYesterday(gameDate)) {
        return `Yesterday • ${format(gameDate, 'h:mm a')}`;
      } else {
        return format(gameDate, 'MMM d, yyyy • h:mm a');
      }
    } catch (error) {
      return game.date || 'Date TBD';
    }
  };

  // Game status styling
  const getStatusConfig = (status: GameStatus) => {
    switch (status) {
      case GameStatus.COMPLETED:
        return {
          color: '#4caf50',
          bgColor: alpha('#4caf50', 0.1),
          label: 'Final',
        };
      case GameStatus.IN_PROGRESS:
        return {
          color: '#ff9800',
          bgColor: alpha('#ff9800', 0.1),
          label: 'Live',
        };
      case GameStatus.SCHEDULED:
        return {
          color: '#2196f3',
          bgColor: alpha('#2196f3', 0.1),
          label: 'Scheduled',
        };
      case GameStatus.CANCELLED:
        return {
          color: '#f44336',
          bgColor: alpha('#f44336', 0.1),
          label: 'Cancelled',
        };
      default:
        return {
          color: '#757575',
          bgColor: alpha('#757575', 0.1),
          label: 'Unknown',
        };
    }
  };

  const statusConfig = getStatusConfig(game.status);

  // Create shortened game ID for display
  const createShortGameId = (gameId: string) => {
    if (!gameId) return 'N/A';
    // If it's a UUID, show first 8 characters
    if (gameId.length >= 8 && gameId.includes('-')) {
      return gameId.substring(0, 8).toUpperCase();
    }
    // If it's already short, return as is
    if (gameId.length <= 12) {
      return gameId;
    }
    // For longer IDs, truncate to 12 chars
    return gameId.substring(0, 12) + '...';
  };

  // Determine if game is completed and has a winner
  const isCompleted = game.status === GameStatus.COMPLETED;
  const homeWins = isCompleted && game.homeScore > game.awayScore;
  const awayWins = isCompleted && game.awayScore > game.homeScore;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          background: 'linear-gradient(145deg, rgba(26,29,53,0.95) 0%, rgba(36,39,68,0.95) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255,255,255,0.1)',
          borderRadius: '16px',
          boxShadow: '0 24px 48px rgba(0,0,0,0.3)',
        },
      }}
    >
      <DialogTitle sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        pb: 1,
        borderBottom: `1px solid ${alpha('#ffffff', 0.1)}`,
      }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          Game Details
        </Typography>
        <IconButton
          onClick={onClose}
          sx={{
            color: 'white',
            '&:hover': {
              background: alpha('#ffffff', 0.1),
            },
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ p: 3 }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
        >
          {/* Game Status */}
          <Box sx={{ mb: 3, textAlign: 'center' }}>
            <Chip
              icon={<Schedule />}
              label={statusConfig.label}
              sx={{
                color: statusConfig.color,
                backgroundColor: statusConfig.bgColor,
                border: `1px solid ${statusConfig.color}`,
                fontWeight: 600,
                mb: 2,
              }}
            />
            <Typography variant="h6" color="text.secondary">
              {formatGameTime()}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Week {game.weekNumber} • {game.year}
            </Typography>
          </Box>

          {/* Teams and Score */}
          <GlassCard sx={{ mb: 3, p: 3 }}>
            <Grid container spacing={3} alignItems="center">
              {/* Away Team */}
              <Grid item xs={5}>
                <Box sx={{ textAlign: 'center' }}>
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    transition={{ type: "spring", stiffness: 300 }}
                  >
                    <Avatar
                      src={game.awayTeamImageUrl}
                      sx={{
                        width: 80,
                        height: 80,
                        mx: 'auto',
                        mb: 2,
                        border: awayWins ? `3px solid #4caf50` : `2px solid ${alpha('#ffffff', 0.2)}`,
                        boxShadow: awayWins ? '0 0 20px rgba(76, 175, 80, 0.5)' : 'none',
                      }}
                    >
                      <SportsFootball />
                    </Avatar>
                  </motion.div>
                  <Typography 
                    variant="h6" 
                    sx={{ 
                      fontWeight: 600,
                      color: awayWins ? '#4caf50' : 'inherit',
                    }}
                  >
                    {game.awayTeamName}
                  </Typography>
                  {game.awayTeamRank && (
                    <Chip
                      label={`#${game.awayTeamRank}`}
                      size="small"
                      sx={{
                        mt: 1,
                        backgroundColor: alpha('#ffd700', 0.2),
                        color: '#ffd700',
                        fontWeight: 600,
                      }}
                    />
                  )}
                </Box>
              </Grid>

              {/* VS / Score */}
              <Grid item xs={2}>
                <Box sx={{ textAlign: 'center' }}>
                  {isCompleted ? (
                    <Box>
                      <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
                        {game.awayScore}
                      </Typography>
                      <Typography variant="h6" color="text.secondary" sx={{ mb: 1 }}>
                        -
                      </Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700 }}>
                        {game.homeScore}
                      </Typography>
                    </Box>
                  ) : (
                    <Typography variant="h5" color="text.secondary" sx={{ fontWeight: 600 }}>
                      vs
                    </Typography>
                  )}
                </Box>
              </Grid>

              {/* Home Team */}
              <Grid item xs={5}>
                <Box sx={{ textAlign: 'center' }}>
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    transition={{ type: "spring", stiffness: 300 }}
                  >
                    <Avatar
                      src={game.homeTeamImageUrl}
                      sx={{
                        width: 80,
                        height: 80,
                        mx: 'auto',
                        mb: 2,
                        border: homeWins ? `3px solid #4caf50` : `2px solid ${alpha('#ffffff', 0.2)}`,
                        boxShadow: homeWins ? '0 0 20px rgba(76, 175, 80, 0.5)' : 'none',
                      }}
                    >
                      <SportsFootball />
                    </Avatar>
                  </motion.div>
                  <Typography 
                    variant="h6" 
                    sx={{ 
                      fontWeight: 600,
                      color: homeWins ? '#4caf50' : 'inherit',
                    }}
                  >
                    {game.homeTeamName}
                  </Typography>
                  {game.homeTeamRank && (
                    <Chip
                      label={`#${game.homeTeamRank}`}
                      size="small"
                      sx={{
                        mt: 1,
                        backgroundColor: alpha('#ffd700', 0.2),
                        color: '#ffd700',
                        fontWeight: 600,
                      }}
                    />
                  )}
                </Box>
              </Grid>
            </Grid>

            {/* Winner Banner */}
            {isCompleted && game.winnerName && (
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <Chip
                  icon={<EmojiEvents />}
                  label={`${game.winnerName} Wins!`}
                  sx={{
                    backgroundColor: alpha('#4caf50', 0.2),
                    color: '#4caf50',
                    border: '1px solid #4caf50',
                    fontWeight: 600,
                    fontSize: '1rem',
                    p: 1,
                  }}
                />
              </Box>
            )}
          </GlassCard>

          {/* Game Information */}
          <GlassCard sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
              Game Information
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Schedule sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Date & Time
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {isValidDate ? format(gameDate, 'MMM d, yyyy') : (game.date || 'Date TBD')}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {isValidDate ? format(gameDate, 'h:mm a') : 'Time TBD'}
                    </Typography>
                  </Box>
                </Box>
              </Grid>

              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <SportsFootball sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Week & Season
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      Week {game.weekNumber}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {game.year} Season
                    </Typography>
                  </Box>
                </Box>
              </Grid>

              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Stadium sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Location
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {game.homeTeamName} (Home)
                    </Typography>
                  </Box>
                </Box>
              </Grid>

              <Grid item xs={6}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <EmojiEvents sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Game ID
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {createShortGameId(game.gameId || game.id)}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </GlassCard>
        </motion.div>
      </DialogContent>
    </Dialog>
  );
};

export default GameDetailModal;