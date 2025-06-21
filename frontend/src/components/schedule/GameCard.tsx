import React from 'react';
import {
  Box,
  Typography,
  Chip,
  Avatar,
  useTheme,
} from '@mui/material';
import { format, isToday, isTomorrow, isYesterday } from 'date-fns';
import { motion } from 'framer-motion';
import GlassCard from '../ui/GlassCard';
import { Game, GameStatus } from '../../types';

interface GameCardProps {
  game: Game;
  isSelected?: boolean;
  showTeamLogos?: boolean;
  compact?: boolean;
  onClick?: () => void;
}

const GameCard: React.FC<GameCardProps> = ({
  game,
  isSelected = false,
  showTeamLogos = true,
  compact = false,
  onClick,
}) => {
  const theme = useTheme();

  // Format game date and time
  const gameDate = new Date(game.date);
  const formatGameTime = () => {
    if (isToday(gameDate)) {
      return `Today • ${format(gameDate, 'h:mm a')}`;
    } else if (isTomorrow(gameDate)) {
      return `Tomorrow • ${format(gameDate, 'h:mm a')}`;
    } else if (isYesterday(gameDate)) {
      return `Yesterday • ${format(gameDate, 'h:mm a')}`;
    } else {
      return format(gameDate, 'MMM d • h:mm a');
    }
  };

  // Game status styling
  const getStatusConfig = (status: GameStatus) => {
    switch (status) {
      case GameStatus.COMPLETED:
        return {
          color: '#4caf50',
          backgroundColor: 'rgba(76, 175, 80, 0.1)',
          label: 'Final',
        };
      case GameStatus.IN_PROGRESS:
        return {
          color: '#ff9800',
          backgroundColor: 'rgba(255, 152, 0, 0.1)',
          label: 'Live',
        };
      case GameStatus.CANCELLED:
        return {
          color: '#f44336',
          backgroundColor: 'rgba(244, 67, 54, 0.1)',
          label: 'Cancelled',
        };
      default: // SCHEDULED
        return {
          color: '#2196f3',
          backgroundColor: 'rgba(33, 150, 243, 0.1)',
          label: formatGameTime(),
        };
    }
  };

  const statusConfig = getStatusConfig(game.status);

  // Determine winner styling
  const getTeamStyle = (isHome: boolean) => {
    if (game.status !== GameStatus.COMPLETED) {
      return { color: 'text.primary', fontWeight: 600 };
    }

    const teamScore = isHome ? game.homeScore : game.awayScore;
    const opponentScore = isHome ? game.awayScore : game.homeScore;
    const isWinner = teamScore > opponentScore;

    return {
      color: isWinner ? 'text.primary' : 'text.secondary',
      fontWeight: isWinner ? 700 : 500,
    };
  };

  // Team logo component
  const TeamLogo: React.FC<{ imageUrl?: string; teamName: string; size?: number }> = ({
    imageUrl,
    teamName,
    size = 40,
  }) => (
    <Avatar
      src={imageUrl}
      alt={`${teamName} logo`}
      sx={{
        width: size,
        height: size,
        background: 'linear-gradient(135deg, #1e88e5, #42a5f5)',
        fontSize: size * 0.4,
        fontWeight: 700,
      }}
    >
      {teamName.substring(0, 2).toUpperCase()}
    </Avatar>
  );

  // Ranking chip
  const RankingChip: React.FC<{ rank?: number }> = ({ rank }) => {
    if (!rank) return null;
    
    return (
      <Chip
        label={`#${rank}`}
        size="small"
        sx={{
          background: 'linear-gradient(135deg, #ff9800, #ffb74d)',
          color: 'white',
          fontWeight: 600,
          fontSize: '0.75rem',
          height: 20,
        }}
      />
    );
  };

  const cardVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { duration: 0.3 }
    },
    hover: { 
      y: compact ? -2 : -4,
      transition: { duration: 0.2 }
    },
    tap: {
      scale: 0.98,
      transition: { duration: 0.1 }
    },
  };

  return (
    <motion.div
      variants={cardVariants}
      initial="initial"
      animate="animate"
      whileHover={onClick ? "hover" : undefined}
      whileTap={onClick ? "tap" : undefined}
    >
      <GlassCard
        sx={{
          position: 'relative',
          cursor: onClick ? 'pointer' : 'default',
          border: isSelected ? `2px solid ${theme.palette.primary.main}` : undefined,
          '&:hover': onClick ? {
            borderColor: theme.palette.primary.main,
          } : undefined,
        }}
        onClick={onClick}
      >
        <Box sx={{ 
          p: compact ? 1.5 : 3,
          minHeight: compact ? 140 : 180, // Normalize card height
          display: 'flex',
          flexDirection: 'column',
        }}>
          {/* Game Status/Time */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Chip
              label={statusConfig.label}
              size="small"
              sx={{
                background: statusConfig.backgroundColor,
                color: statusConfig.color,
                fontWeight: 600,
                fontSize: '0.75rem',
              }}
            />
            {game.weekNumber && (
              <Typography variant="caption" color="text.secondary">
                Week {game.weekNumber}
              </Typography>
            )}
          </Box>

          {/* Teams and Scores */}
          <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            gap: compact ? 1 : 2,
            flex: 1, // Take remaining space
          }}>
            {/* Away Team */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flex: 1 }}>
                {showTeamLogos && (
                  <TeamLogo
                    imageUrl={game.awayTeamImageUrl}
                    teamName={game.awayTeamName}
                    size={compact ? 32 : 40}
                  />
                )}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1 }}>
                  <Typography 
                    variant={compact ? 'body2' : 'body1'} 
                    sx={getTeamStyle(false)}
                  >
                    {game.awayTeamName}
                  </Typography>
                  <RankingChip rank={game.awayTeamRank} />
                </Box>
              </Box>
              {/* Always reserve space for score, show placeholder for scheduled games */}
              <Box sx={{ minWidth: 40, textAlign: 'right' }}>
                {game.status === GameStatus.COMPLETED ? (
                  <Typography 
                    variant={compact ? 'h6' : 'h5'} 
                    sx={getTeamStyle(false)}
                  >
                    {game.awayScore}
                  </Typography>
                ) : (
                  <Typography 
                    variant={compact ? 'h6' : 'h5'} 
                    sx={{ color: 'transparent' }}
                  >
                    --
                  </Typography>
                )}
              </Box>
            </Box>

            {/* VS Divider for scheduled games or @ for location */}
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 0.5 }}>
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ 
                  fontWeight: 600,
                  px: 2,
                  py: 0.5,
                  background: 'rgba(255,255,255,0.05)',
                  borderRadius: 1,
                }}
              >
                @
              </Typography>
            </Box>

            {/* Home Team */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flex: 1 }}>
                {showTeamLogos && (
                  <TeamLogo
                    imageUrl={game.homeTeamImageUrl}
                    teamName={game.homeTeamName}
                    size={compact ? 32 : 40}
                  />
                )}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1 }}>
                  <Typography 
                    variant={compact ? 'body2' : 'body1'} 
                    sx={getTeamStyle(true)}
                  >
                    {game.homeTeamName}
                  </Typography>
                  <RankingChip rank={game.homeTeamRank} />
                </Box>
              </Box>
              {/* Always reserve space for score, show placeholder for scheduled games */}
              <Box sx={{ minWidth: 40, textAlign: 'right' }}>
                {game.status === GameStatus.COMPLETED ? (
                  <Typography 
                    variant={compact ? 'h6' : 'h5'} 
                    sx={getTeamStyle(true)}
                  >
                    {game.homeScore}
                  </Typography>
                ) : (
                  <Typography 
                    variant={compact ? 'h6' : 'h5'} 
                    sx={{ color: 'transparent' }}
                  >
                    --
                  </Typography>
                )}
              </Box>
            </Box>
          </Box>

          {/* Additional Info for Completed Games - Fixed height container */}
          <Box sx={{ 
            mt: 'auto', // Push to bottom
            pt: game.status === GameStatus.COMPLETED && game.winnerName ? 2 : 0,
            borderTop: game.status === GameStatus.COMPLETED && game.winnerName ? '1px solid rgba(255,255,255,0.1)' : 'none',
            textAlign: 'center',
            minHeight: game.status === GameStatus.COMPLETED && game.winnerName ? 32 : 16, // Reserve space
          }}>
            {game.status === GameStatus.COMPLETED && game.winnerName && (
              <Typography variant="caption" color="text.secondary">
                Winner: <span style={{ color: theme.palette.primary.main, fontWeight: 600 }}>
                  {game.winnerName}
                </span>
              </Typography>
            )}
          </Box>
        </Box>
      </GlassCard>
    </motion.div>
  );
};

export default GameCard;