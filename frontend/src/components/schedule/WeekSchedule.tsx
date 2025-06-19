import React, { useMemo } from 'react';
import {
  Box,
  Typography,
  Grid,
  Chip,
  useTheme,
  useMediaQuery,
  Alert,
} from '@mui/material';
import { format, parseISO } from 'date-fns';
import { motion } from 'framer-motion';
import GameCard from './GameCard';
import GlassCard from '../ui/GlassCard';
import { Game, GameStatus } from '../../types';

interface WeekScheduleProps {
  games: Game[] | undefined;
  loading: boolean;
  filters?: {
    year: number;
    week: number | null;
    teamView: 'all' | 'selected';
    conference: string | null;
  };
  onGameClick?: (game: Game) => void;
  showCompact?: boolean;
}

const WeekSchedule: React.FC<WeekScheduleProps> = ({
  games = [],
  loading,
  filters,
  onGameClick,
  showCompact = false,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const isSmallMobile = useMediaQuery(theme.breakpoints.down('sm'));

  // Group games by date
  const gamesByDate = useMemo(() => {
    if (!games || games.length === 0) return {};

    const grouped: Record<string, Game[]> = {};
    
    games.forEach((game) => {
      try {
        const gameDate = parseISO(game.date);
        const dateKey = format(gameDate, 'yyyy-MM-dd');
        
        if (!grouped[dateKey]) {
          grouped[dateKey] = [];
        }
        grouped[dateKey].push(game);
      } catch (error) {
        console.warn('Invalid game date:', game.date, error);
      }
    });

    // Sort games within each date by time
    Object.keys(grouped).forEach((dateKey) => {
      grouped[dateKey].sort((a, b) => {
        try {
          return new Date(a.date).getTime() - new Date(b.date).getTime();
        } catch {
          return 0;
        }
      });
    });

    return grouped;
  }, [games]);

  // Calculate summary stats
  const stats = useMemo(() => {
    if (!games || games.length === 0) return null;

    const total = games.length;
    const completed = games.filter(g => g.status === GameStatus.COMPLETED).length;
    const inProgress = games.filter(g => g.status === GameStatus.IN_PROGRESS).length;
    const scheduled = games.filter(g => g.status === GameStatus.SCHEDULED).length;

    return { total, completed, inProgress, scheduled };
  }, [games]);

  // Animation variants
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
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { duration: 0.3 }
    },
  };

  // Date formatting helper
  const formatDateHeader = (dateKey: string): string => {
    try {
      const date = parseISO(dateKey);
      return format(date, 'EEEE, MMMM d');
    } catch {
      return dateKey;
    }
  };

  // Status chip component
  const StatusChip: React.FC<{ count: number; label: string; color: string; bgColor: string }> = ({
    count,
    label,
    color,
    bgColor,
  }) => (
    <Chip
      label={`${count} ${label}`}
      size="small"
      sx={{
        background: bgColor,
        color: color,
        fontWeight: 600,
        fontSize: '0.75rem',
      }}
    />
  );

  // Empty state
  if (!loading && (!games || games.length === 0)) {
    return (
      <motion.div variants={itemVariants}>
        <Alert 
          severity="info"
          sx={{ 
            background: 'linear-gradient(135deg, rgba(33,150,243,0.1) 0%, rgba(33,150,243,0.05) 100%)',
            border: `1px solid ${theme.palette.info.main}40`,
            backdropFilter: 'blur(20px)',
          }}
        >
          No games found for the selected filters. Try adjusting your search criteria.
        </Alert>
      </motion.div>
    );
  }

  const sortedDates = Object.keys(gamesByDate).sort();

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
    >
      {/* Summary Stats */}
      {stats && (
        <motion.div variants={itemVariants}>
          <GlassCard sx={{ mb: 3 }}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                Schedule Overview
              </Typography>
              <Box sx={{ 
                display: 'flex', 
                gap: 1.5, 
                flexWrap: 'wrap',
                alignItems: 'center',
              }}>
                <StatusChip 
                  count={stats.total} 
                  label="Total Games" 
                  color="#1e88e5" 
                  bgColor="rgba(30, 136, 229, 0.1)" 
                />
                {stats.completed > 0 && (
                  <StatusChip 
                    count={stats.completed} 
                    label="Completed" 
                    color="#4caf50" 
                    bgColor="rgba(76, 175, 80, 0.1)" 
                  />
                )}
                {stats.inProgress > 0 && (
                  <StatusChip 
                    count={stats.inProgress} 
                    label="Live" 
                    color="#ff9800" 
                    bgColor="rgba(255, 152, 0, 0.1)" 
                  />
                )}
                {stats.scheduled > 0 && (
                  <StatusChip 
                    count={stats.scheduled} 
                    label="Scheduled" 
                    color="#2196f3" 
                    bgColor="rgba(33, 150, 243, 0.1)" 
                  />
                )}
              </Box>
              
              {/* Additional context */}
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  {filters?.teamView === 'selected' 
                    ? 'Showing games for your selected team'
                    : filters?.conference 
                      ? `Showing ${filters.conference} conference games`
                      : 'Showing all games'
                  }
                  {filters?.week && ` for Week ${filters.week}`}
                  {filters?.year && ` in ${filters.year}`}
                </Typography>
              </Box>
            </Box>
          </GlassCard>
        </motion.div>
      )}

      {/* Games by Date */}
      {sortedDates.map((dateKey) => {
        const dayGames = gamesByDate[dateKey];
        
        return (
          <motion.div key={dateKey} variants={itemVariants}>
            <Box sx={{ mb: 4 }}>
              {/* Date Header */}
              <Typography 
                variant="h5" 
                sx={{ 
                  mb: 3,
                  fontWeight: 600,
                  background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              >
                {formatDateHeader(dateKey)}
              </Typography>

              {/* Games Grid */}
              <Grid container spacing={isSmallMobile ? 1.5 : isMobile ? 2 : 3}>
                {dayGames.map((game) => (
                  <Grid 
                    item 
                    xs={12} 
                    sm={6} 
                    md={showCompact ? 4 : 6} 
                    lg={showCompact ? 3 : 4}
                    xl={showCompact ? 2 : 3}
                    key={game.id}
                  >
                    <GameCard
                      game={game}
                      onClick={onGameClick ? () => onGameClick(game) : undefined}
                      showTeamLogos={!isSmallMobile}
                      compact={showCompact || isSmallMobile}
                    />
                  </Grid>
                ))}
              </Grid>
            </Box>
          </motion.div>
        );
      })}
    </motion.div>
  );
};

export default WeekSchedule;