import React from 'react';
import {
  Box,
  Typography,
  LinearProgress,
  Grid,
  Chip,
  alpha,
} from '@mui/material';
import {
  EmojiEvents,
  Star,
  LocalFireDepartment,
  Whatshot,
  MilitaryTech,
  Diamond,
  TrendingUp,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import GlassCard from '../ui/GlassCard';
import { AchievementType, AchievementRarity } from '../../types';

interface AchievementStatsProps {
  stats: {
    totalAchievements: number;
    completedAchievements: number;
    completionPercentage: number;
    countByType: Record<string, number>;
    countByRarity: Record<string, number>;
    recentAchievements: any[];
  };
}

const AchievementStats: React.FC<AchievementStatsProps> = ({ stats }) => {
  const typeColors: Record<AchievementType, string> = {
    WINS: '#4caf50',
    SEASON: '#2196f3',
    CHAMPIONSHIP: '#ffc107',
    STATISTICS: '#ff5722',
    GENERAL: '#9c27b0',
  };

  const rarityData: Record<AchievementRarity, { color: string; icon: React.ReactElement }> = {
    COMMON: { color: '#9e9e9e', icon: <Star sx={{ fontSize: '1rem' }} /> },
    UNCOMMON: { color: '#4caf50', icon: <LocalFireDepartment sx={{ fontSize: '1rem' }} /> },
    RARE: { color: '#2196f3', icon: <Whatshot sx={{ fontSize: '1rem' }} /> },
    EPIC: { color: '#9c27b0', icon: <MilitaryTech sx={{ fontSize: '1rem' }} /> },
    LEGENDARY: { color: '#ff9800', icon: <Diamond sx={{ fontSize: '1rem' }} /> },
  };

  const containerVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        delayChildren: 0.1,
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { type: 'spring', stiffness: 300, damping: 20 },
    },
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      <Grid container spacing={3}>
        {/* Overall Progress */}
        <Grid item xs={12} md={6}>
          <motion.div variants={itemVariants}>
            <GlassCard sx={{ p: 3, height: '100%' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <EmojiEvents sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Overall Progress
                </Typography>
              </Box>

              <Box sx={{ mb: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    Achievements Completed
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {stats.completedAchievements} / {stats.totalAchievements}
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={stats.completionPercentage}
                  sx={{
                    height: 8,
                    borderRadius: 4,
                    backgroundColor: alpha('#ffffff', 0.1),
                    '& .MuiLinearProgress-bar': {
                      background: 'linear-gradient(90deg, #1e88e5, #42a5f5)',
                      borderRadius: 4,
                    },
                  }}
                />
                <Typography
                  variant="h4"
                  sx={{
                    mt: 2,
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                  }}
                >
                  {stats.completionPercentage.toFixed(1)}%
                </Typography>
              </Box>

              {stats.recentAchievements.length > 0 && (
                <Box>
                  <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                    Recently Completed
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {stats.recentAchievements.slice(0, 3).map((achievement) => (
                      <Chip
                        key={achievement.id}
                        label={achievement.description}
                        size="small"
                        sx={{
                          background: 'linear-gradient(135deg, rgba(76,175,80,0.2) 0%, rgba(76,175,80,0.1) 100%)',
                          border: '1px solid rgba(76,175,80,0.3)',
                          color: '#4caf50',
                          fontWeight: 500,
                        }}
                      />
                    ))}
                  </Box>
                </Box>
              )}
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Achievement Velocity */}
        <Grid item xs={12} md={6}>
          <motion.div variants={itemVariants}>
            <GlassCard sx={{ p: 3, height: '100%' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <TrendingUp sx={{ mr: 1, color: 'success.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Achievement Velocity
                </Typography>
              </Box>

              <Typography variant="h4" sx={{ mb: 1, fontWeight: 700, color: 'success.main' }}>
                {stats.recentAchievements.length}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Achievements completed in the last 30 days
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2" color="text.secondary">
                    Daily Average
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {(stats.recentAchievements.length / 30).toFixed(1)}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2" color="text.secondary">
                    Completion Rate
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {stats.totalAchievements > 0 
                      ? ((stats.recentAchievements.length / stats.totalAchievements) * 100).toFixed(1) + '%'
                      : '0%'
                    }
                  </Typography>
                </Box>
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Type Distribution */}
        <Grid item xs={12} md={6}>
          <motion.div variants={itemVariants}>
            <GlassCard sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                By Type
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {Object.entries(AchievementType).map(([key, type]) => {
                  const count = stats.countByType[type] || 0;
                  const percentage = stats.totalAchievements > 0 ? (count / stats.totalAchievements) * 100 : 0;
                  
                  return (
                    <Box key={type}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                          {type}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {count}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={percentage}
                        sx={{
                          height: 4,
                          borderRadius: 2,
                          backgroundColor: alpha('#ffffff', 0.1),
                          '& .MuiLinearProgress-bar': {
                            backgroundColor: typeColors[type],
                            borderRadius: 2,
                          },
                        }}
                      />
                    </Box>
                  );
                })}
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>

        {/* Rarity Distribution */}
        <Grid item xs={12} md={6}>
          <motion.div variants={itemVariants}>
            <GlassCard sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                By Rarity
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {Object.entries(rarityData).map(([rarity, data]) => {
                  const count = stats.countByRarity[rarity] || 0;
                  
                  return (
                    <Chip
                      key={rarity}
                      icon={data.icon}
                      label={`${rarity} (${count})`}
                      sx={{
                        background: `linear-gradient(135deg, ${alpha(data.color, 0.2)} 0%, ${alpha(data.color, 0.1)} 100%)`,
                        border: `1px solid ${alpha(data.color, 0.3)}`,
                        color: data.color,
                        fontWeight: 600,
                        '& .MuiChip-icon': {
                          color: data.color,
                        },
                      }}
                    />
                  );
                })}
              </Box>
            </GlassCard>
          </motion.div>
        </Grid>
      </Grid>
    </motion.div>
  );
};

export default AchievementStats;