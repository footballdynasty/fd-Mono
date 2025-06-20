import React from 'react';
import {
  Grid,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Skeleton,
} from '@mui/material';
import { motion } from 'framer-motion';
import { Achievement } from '../../types';
import AchievementCard from './AchievementCard';

interface AchievementGridProps {
  achievements: Achievement[];
  isLoading?: boolean;
  isError?: boolean;
  error?: Error | null;
  onCompleteAchievement?: (id: string) => void;
  showActions?: boolean;
  compact?: boolean;
  emptyMessage?: string;
}

const AchievementGrid: React.FC<AchievementGridProps> = ({
  achievements,
  isLoading = false,
  isError = false,
  error = null,
  onCompleteAchievement,
  showActions = false,
  compact = false,
  emptyMessage = 'No achievements found',
}) => {
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        delayChildren: 0.1,
        staggerChildren: 0.05,
      },
    },
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 300,
        damping: 20,
      },
    },
  };

  if (isLoading) {
    return (
      <Grid container spacing={3}>
        {Array.from({ length: 8 }).map((_, index) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
            <Skeleton
              variant="rectangular"
              width="100%"
              height={compact ? 180 : 220}
              sx={{
                borderRadius: 3,
                background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
              }}
            />
          </Grid>
        ))}
      </Grid>
    );
  }

  if (isError) {
    return (
      <Alert
        severity="error"
        sx={{
          background: 'linear-gradient(135deg, rgba(244,67,54,0.1) 0%, rgba(244,67,54,0.05) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(244,67,54,0.2)',
          borderRadius: 3,
        }}
      >
        Failed to load achievements: {error?.message || 'Unknown error'}
      </Alert>
    );
  }

  if (achievements.length === 0) {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          py: 8,
          textAlign: 'center',
        }}
      >
        <Typography
          variant="h6"
          sx={{
            color: 'text.secondary',
            mb: 1,
          }}
        >
          {emptyMessage}
        </Typography>
        <Typography
          variant="body2"
          sx={{
            color: 'text.secondary',
            opacity: 0.7,
          }}
        >
          Start playing to unlock achievements!
        </Typography>
      </Box>
    );
  }

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      <Grid container spacing={3}>
        {achievements.map((achievement) => (
          <Grid
            item
            xs={12}
            sm={6}
            md={4}
            lg={compact ? 4 : 3}
            xl={compact ? 3 : 2.4}
            key={achievement.id}
            component={motion.div}
            variants={itemVariants}
            layout
          >
            <AchievementCard
              achievement={achievement}
              onComplete={onCompleteAchievement}
              showActions={showActions}
              compact={compact}
            />
          </Grid>
        ))}
      </Grid>
    </motion.div>
  );
};

export default AchievementGrid;