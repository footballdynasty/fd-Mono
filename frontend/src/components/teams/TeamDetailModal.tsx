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
  Grid,
  alpha,
  LinearProgress,
} from '@mui/material';
import {
  Close,
  Person,
  Computer,
  SportsFootball,
  EmojiEvents,
  School,
  CalendarToday,
  TrendingUp,
  Star,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { motion } from 'framer-motion';
import { Team } from '../../types';
import GlassCard from '../ui/GlassCard';

interface TeamDetailModalProps {
  open: boolean;
  onClose: () => void;
  team: Team | null;
}

const TeamDetailModal: React.FC<TeamDetailModalProps> = ({
  open,
  onClose,
  team,
}) => {
  if (!team) return null;

  // Calculate team statistics
  const totalGames = team.totalGames || 0;
  const wins = team.currentWins || 0;
  const losses = team.currentLosses || 0;
  const winPercentage = team.winPercentage || 0;
  const rank = team.currentRank || 0;
  
  // Determine team performance level
  const getPerformanceLevel = (percentage: number) => {
    if (percentage >= 0.8) return { label: 'Excellent', color: '#4caf50' };
    if (percentage >= 0.6) return { label: 'Good', color: '#2196f3' };
    if (percentage >= 0.4) return { label: 'Average', color: '#ff9800' };
    return { label: 'Needs Improvement', color: '#f44336' };
  };

  const performance = getPerformanceLevel(winPercentage);
  const bowlEligible = wins >= 6;

  // Generate team initials for avatar fallback
  const getTeamInitials = (name: string) => {
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
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
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Avatar
            src={team.imageUrl}
            sx={{
              width: 48,
              height: 48,
              mr: 2,
              border: `2px solid ${alpha('#ffffff', 0.2)}`,
              background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
            }}
          >
            {getTeamInitials(team.name)}
          </Avatar>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 600 }}>
              {team.name}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {team.conference || 'Independent'}
            </Typography>
          </Box>
        </Box>
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
          {/* Team Overview */}
          <GlassCard sx={{ mb: 3, p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
              Team Overview
            </Typography>
            
            <Grid container spacing={3}>
              {/* Team Type */}
              <Grid item xs={12} sm={6} md={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Box sx={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center',
                    mb: 1,
                  }}>
                    {team.isHuman ? (
                      <Person sx={{ fontSize: 32, color: '#4caf50' }} />
                    ) : (
                      <Computer sx={{ fontSize: 32, color: '#2196f3' }} />
                    )}
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Team Type
                  </Typography>
                  <Chip
                    label={team.isHuman ? 'Human' : 'AI'}
                    size="small"
                    sx={{
                      mt: 0.5,
                      backgroundColor: team.isHuman 
                        ? alpha('#4caf50', 0.2) 
                        : alpha('#2196f3', 0.2),
                      color: team.isHuman ? '#4caf50' : '#2196f3',
                      fontWeight: 600,
                    }}
                  />
                </Box>
              </Grid>

              {/* Coach */}
              <Grid item xs={12} sm={6} md={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <School sx={{ fontSize: 32, color: '#ff9800', mb: 1 }} />
                  <Typography variant="body2" color="text.secondary">
                    Head Coach
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 500 }}>
                    {team.coach || team.username || 'Not Assigned'}
                  </Typography>
                </Box>
              </Grid>

              {/* Current Rank */}
              <Grid item xs={12} sm={6} md={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <EmojiEvents sx={{ 
                    fontSize: 32, 
                    color: rank <= 25 ? '#ffd700' : '#757575', 
                    mb: 1 
                  }} />
                  <Typography variant="body2" color="text.secondary">
                    Current Rank
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {rank > 0 ? `#${rank}` : 'Unranked'}
                  </Typography>
                </Box>
              </Grid>

              {/* Bowl Status */}
              <Grid item xs={12} sm={6} md={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Star sx={{ 
                    fontSize: 32, 
                    color: bowlEligible ? '#4caf50' : '#757575', 
                    mb: 1 
                  }} />
                  <Typography variant="body2" color="text.secondary">
                    Bowl Status
                  </Typography>
                  <Chip
                    label={bowlEligible ? 'Eligible' : 'Not Eligible'}
                    size="small"
                    sx={{
                      mt: 0.5,
                      backgroundColor: bowlEligible 
                        ? alpha('#4caf50', 0.2) 
                        : alpha('#757575', 0.2),
                      color: bowlEligible ? '#4caf50' : '#757575',
                      fontWeight: 600,
                    }}
                  />
                </Box>
              </Grid>
            </Grid>
          </GlassCard>

          {/* Season Statistics */}
          <GlassCard sx={{ mb: 3, p: 3 }}>
            <Typography variant="h6" sx={{ mb: 3, fontWeight: 600 }}>
              Season Statistics
            </Typography>
            
            <Grid container spacing={3}>
              {/* Record */}
              <Grid item xs={12} md={6}>
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    Win-Loss Record
                  </Typography>
                  <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
                    {wins} - {losses}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {totalGames > 0 ? `${totalGames} games played` : 'No games played'}
                  </Typography>
                </Box>
              </Grid>

              {/* Win Percentage */}
              <Grid item xs={12} md={6}>
                <Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="text.secondary">
                      Win Percentage
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {(winPercentage * 100).toFixed(1)}%
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={winPercentage * 100}
                    sx={{
                      height: 8,
                      borderRadius: 4,
                      backgroundColor: alpha('#ffffff', 0.1),
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: performance.color,
                        borderRadius: 4,
                      },
                    }}
                  />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
                    <Chip
                      label={performance.label}
                      size="small"
                      sx={{
                        backgroundColor: alpha(performance.color, 0.2),
                        color: performance.color,
                        fontWeight: 600,
                      }}
                    />
                    <Typography variant="body2" color="text.secondary">
                      Conference: {team.conference || 'Independent'}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </GlassCard>

          {/* Team Information */}
          <GlassCard sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
              Team Information
            </Typography>
            
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <SportsFootball sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Team Name
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {team.name}
                    </Typography>
                  </Box>
                </Box>
              </Grid>

              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <School sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Conference
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {team.conference || 'Independent'}
                    </Typography>
                  </Box>
                </Box>
              </Grid>

              {team.createdAt && (
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <CalendarToday sx={{ mr: 1, color: 'text.secondary' }} />
                    <Box>
                      <Typography variant="body2" color="text.secondary">
                        Team Created
                      </Typography>
                      <Typography variant="body1" sx={{ fontWeight: 500 }}>
                        {format(new Date(team.createdAt), 'MMM d, yyyy')}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
              )}

              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <TrendingUp sx={{ mr: 1, color: 'text.secondary' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Team ID
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      {team.id.substring(0, 8).toUpperCase()}
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

export default TeamDetailModal;