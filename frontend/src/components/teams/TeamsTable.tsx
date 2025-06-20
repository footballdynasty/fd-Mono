import React, { useState, useMemo, memo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Typography,
  Box,
  Skeleton,
  Alert,
  Chip,
  Avatar,
  useTheme,
  useMediaQuery,
  alpha,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Person as PersonIcon,
  Computer as ComputerIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Team } from '../../types';
import GlassCard from '../ui/GlassCard';

type SortOrder = 'asc' | 'desc';
type SortableField = 'name' | 'conference' | 'coach' | 'currentWins' | 'currentLosses' | 'winPercentage' | 'totalGames';

interface TeamsTableProps {
  teams: Team[];
  loading?: boolean;
  error?: string | null;
  compact?: boolean;
  showActions?: boolean;
  onTeamClick?: (team: Team) => void;
  onTeamEdit?: (team: Team) => void;
  onTeamDelete?: (team: Team) => void;
}

// Removed complex TableColumn interface - using simplified structure

const TeamsTable: React.FC<TeamsTableProps> = ({
  teams,
  loading = false,
  error = null,
  compact = false,
  showActions = false,
  onTeamClick,
  onTeamEdit,
  onTeamDelete,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const isTablet = useMediaQuery(theme.breakpoints.down('lg'));

  const [sortBy, setSortBy] = useState<SortableField>('name');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');

  // Removed complex column configuration - using simplified hardcoded structure for better performance

  // Sort teams
  const sortedTeams = useMemo(() => {
    if (!teams.length) return [];

    return [...teams].sort((a, b) => {
      let aValue = a[sortBy];
      let bValue = b[sortBy];

      // Handle undefined values
      if (aValue === undefined) aValue = '';
      if (bValue === undefined) bValue = '';

      // Convert to appropriate types for comparison
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }

      let comparison = 0;
      if (aValue < bValue) {
        comparison = -1;
      } else if (aValue > bValue) {
        comparison = 1;
      }

      return sortOrder === 'desc' ? -comparison : comparison;
    });
  }, [teams, sortBy, sortOrder]);

  const handleSort = (field: SortableField) => {
    const isAsc = sortBy === field && sortOrder === 'asc';
    setSortOrder(isAsc ? 'desc' : 'asc');
    setSortBy(field);
  };

  // Removed filteredColumns - using direct rendering for better performance

  const containerVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        duration: loading ? 0 : 0.6, // Skip animation during loading
        staggerChildren: loading ? 0 : 0.05
      }
    },
  };

  const rowVariants = {
    initial: { opacity: 0, x: -20 },
    animate: { opacity: 1, x: 0 },
  };

  // Simplified table row component for better performance
  const MemoizedTableRow = memo(({ team }: { team: Team }) => (
    <TableRow
      key={team.id}
      hover
      sx={{
        '&:hover': {
          backgroundColor: 'rgba(255,255,255,0.02)',
        },
        '& td': {
          borderBottom: '1px solid rgba(255,255,255,0.1)',
        },
      }}
    >
      {/* Team Name */}
      <TableCell align="left">
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Avatar
            src={team.imageUrl}
            alt={team.name}
            sx={{ 
              width: compact ? 24 : 32,
              height: compact ? 24 : 32,
              background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
            }}
          >
            {team.name.charAt(0)}
          </Avatar>
          <Box>
            <Typography 
              variant="body2" 
              fontWeight={600}
              sx={{ 
                cursor: onTeamClick ? 'pointer' : 'default',
                '&:hover': onTeamClick ? { color: 'primary.main' } : {},
              }}
              onClick={() => onTeamClick?.(team)}
            >
              {team.name}
            </Typography>
            {team.coach && !compact && (
              <Typography variant="caption" color="text.secondary">
                Coach: {team.coach}
              </Typography>
            )}
          </Box>
        </Box>
      </TableCell>

      {/* Conference */}
      {(!isMobile || !compact) && (
        <TableCell align="center">
          <Chip
            label={team.conference || 'Independent'}
            size="small"
            sx={{
              background: team.conference 
                ? 'rgba(66,165,245,0.2)'
                : 'rgba(158,158,158,0.2)',
              border: `1px solid ${team.conference ? 'rgba(66,165,245,0.3)' : 'rgba(158,158,158,0.3)'}`,
              color: 'text.primary',
              fontWeight: 500,
            }}
          />
        </TableCell>
      )}

      {/* Type */}
      {!isMobile && (
        <TableCell align="center">
          <Chip
            icon={team.isHuman ? <PersonIcon /> : <ComputerIcon />}
            label={team.isHuman ? 'Human' : 'AI'}
            size="small"
            sx={{
              background: team.isHuman 
                ? 'rgba(76,175,80,0.2)'
                : 'rgba(255,152,0,0.2)',
              border: `1px solid ${team.isHuman ? 'rgba(76,175,80,0.3)' : 'rgba(255,152,0,0.3)'}`,
              color: 'text.primary',
              fontWeight: 500,
            }}
          />
        </TableCell>
      )}

      {/* Record */}
      <TableCell align="center">
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="body2" fontWeight={600}>
            {(team.currentWins || 0)}-{(team.currentLosses || 0)}
          </Typography>
          {team.winPercentage !== undefined && (
            <Typography variant="caption" color="text.secondary">
              {(team.winPercentage * 100).toFixed(1)}%
            </Typography>
          )}
        </Box>
      </TableCell>

      {/* Wins */}
      {!isMobile && (
        <TableCell align="center">
          <Typography variant="body2" fontWeight={600} color="success.main">
            {team.currentWins || 0}
          </Typography>
        </TableCell>
      )}

      {/* Losses */}
      {!isMobile && (
        <TableCell align="center">
          <Typography variant="body2" fontWeight={600} color="error.main">
            {team.currentLosses || 0}
          </Typography>
        </TableCell>
      )}

      {/* Win Percentage */}
      {!isTablet && (
        <TableCell align="center">
          <Typography 
            variant="body2" 
            fontWeight={600}
            color={
              team.winPercentage !== undefined && team.winPercentage >= 0.7 ? 'success.main' : 
              team.winPercentage !== undefined && team.winPercentage >= 0.5 ? 'warning.main' : 
              team.winPercentage !== undefined ? 'error.main' : 'text.secondary'
            }
          >
            {team.winPercentage !== undefined ? `${(team.winPercentage * 100).toFixed(1)}%` : 'N/A'}
          </Typography>
        </TableCell>
      )}

      {/* Actions */}
      {showActions && (
        <TableCell align="center">
          <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
            <IconButton
              size="small"
              onClick={() => onTeamClick?.(team)}
              sx={{ color: 'primary.main' }}
            >
              <ViewIcon />
            </IconButton>
            <IconButton
              size="small"
              onClick={() => onTeamEdit?.(team)}
              sx={{ color: 'warning.main' }}
            >
              <EditIcon />
            </IconButton>
            <IconButton
              size="small"
              onClick={() => onTeamDelete?.(team)}
              sx={{ color: 'error.main' }}
            >
              <DeleteIcon />
            </IconButton>
          </Box>
        </TableCell>
      )}
    </TableRow>
  ));

  if (error) {
    return (
      <GlassCard>
        <Box sx={{ p: 3 }}>
          <Alert 
            severity="error"
            sx={{
              background: 'linear-gradient(135deg, rgba(244,67,54,0.1) 0%, rgba(244,67,54,0.05) 100%)',
              border: `1px solid ${theme.palette.error.main}40`,
              backdropFilter: 'blur(20px)',
            }}
          >
            {error}
          </Alert>
        </Box>
      </GlassCard>
    );
  }

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
    >
      <GlassCard>
        <TableContainer sx={{ maxHeight: 800 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell
                  align="left"
                  style={{ minWidth: 200 }}
                  sx={{
                    background: 'rgba(30,136,229,0.1)',
                    backdropFilter: 'blur(20px)',
                    borderBottom: 'rgba(255,255,255,0.1)',
                    color: 'text.primary',
                    fontWeight: 600,
                  }}
                >
                  <TableSortLabel
                    active={sortBy === 'name'}
                    direction={sortBy === 'name' ? sortOrder : 'asc'}
                    onClick={() => handleSort('name')}
                    sx={{
                      color: 'text.primary !important',
                      '&.Mui-active': {
                        color: 'primary.main !important',
                        '& .MuiTableSortLabel-icon': {
                          color: 'primary.main !important',
                        },
                      },
                    }}
                  >
                    Team
                  </TableSortLabel>
                </TableCell>

                {(!isMobile || !compact) && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    Conference
                  </TableCell>
                )}

                {!isMobile && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    Type
                  </TableCell>
                )}

                <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                  Record
                </TableCell>

                {!isMobile && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    <TableSortLabel
                      active={sortBy === 'currentWins'}
                      direction={sortBy === 'currentWins' ? sortOrder : 'asc'}
                      onClick={() => handleSort('currentWins')}
                      sx={{ color: 'text.primary !important', '&.Mui-active': { color: 'primary.main !important' } }}
                    >
                      Wins
                    </TableSortLabel>
                  </TableCell>
                )}

                {!isMobile && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    <TableSortLabel
                      active={sortBy === 'currentLosses'}
                      direction={sortBy === 'currentLosses' ? sortOrder : 'asc'}
                      onClick={() => handleSort('currentLosses')}
                      sx={{ color: 'text.primary !important', '&.Mui-active': { color: 'primary.main !important' } }}
                    >
                      Losses
                    </TableSortLabel>
                  </TableCell>
                )}

                {!isTablet && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    <TableSortLabel
                      active={sortBy === 'winPercentage'}
                      direction={sortBy === 'winPercentage' ? sortOrder : 'asc'}
                      onClick={() => handleSort('winPercentage')}
                      sx={{ color: 'text.primary !important', '&.Mui-active': { color: 'primary.main !important' } }}
                    >
                      Win %
                    </TableSortLabel>
                  </TableCell>
                )}

                {showActions && (
                  <TableCell align="center" sx={{ background: 'rgba(30,136,229,0.1)', color: 'text.primary', fontWeight: 600 }}>
                    Actions
                  </TableCell>
                )}
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                // Enhanced loading skeleton
                Array.from(new Array(10)).map((_, index) => (
                  <TableRow key={`skeleton-${index}`}>
                    {/* Team Name */}
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Skeleton variant="circular" width={32} height={32} />
                        <Box sx={{ flex: 1 }}>
                          <Skeleton variant="text" width="80%" height={20} />
                          <Skeleton variant="text" width="60%" height={16} />
                        </Box>
                      </Box>
                    </TableCell>
                    
                    {/* Conference */}
                    {(!isMobile || !compact) && (
                      <TableCell align="center">
                        <Skeleton variant="rounded" width={80} height={24} />
                      </TableCell>
                    )}
                    
                    {/* Type */}
                    {!isMobile && (
                      <TableCell align="center">
                        <Skeleton variant="rounded" width={60} height={24} />
                      </TableCell>
                    )}
                    
                    {/* Record */}
                    <TableCell align="center">
                      <Skeleton variant="text" width="60%" height={20} />
                    </TableCell>
                    
                    {/* Wins */}
                    {!isMobile && (
                      <TableCell align="center">
                        <Skeleton variant="text" width="40%" height={20} />
                      </TableCell>
                    )}
                    
                    {/* Losses */}
                    {!isMobile && (
                      <TableCell align="center">
                        <Skeleton variant="text" width="40%" height={20} />
                      </TableCell>
                    )}
                    
                    {/* Win Percentage */}
                    {!isTablet && (
                      <TableCell align="center">
                        <Skeleton variant="text" width="50%" height={20} />
                      </TableCell>
                    )}
                    
                    {/* Actions */}
                    {showActions && (
                      <TableCell align="center">
                        <Skeleton variant="rounded" width={120} height={32} />
                      </TableCell>
                    )}
                  </TableRow>
                ))
              ) : sortedTeams.length === 0 ? (
                <TableRow>
                  <TableCell 
                    colSpan={
                      4 + // Base columns: Team, Conference (conditional), Type (conditional), Record
                      (!isMobile ? 2 : 0) + // Wins, Losses
                      (!isTablet ? 1 : 0) + // Win %
                      (showActions ? 1 : 0) // Actions
                    }
                    sx={{ textAlign: 'center', py: 6 }}
                  >
                    <Typography variant="body1" color="text.secondary">
                      No teams found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                sortedTeams.map((team) => (
                  <MemoizedTableRow 
                    key={team.id}
                    team={team} 
                  />
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </GlassCard>
    </motion.div>
  );
};

export default memo(TeamsTable);