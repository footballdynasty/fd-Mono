import React from 'react';
import {
  Box,
  Chip,
  Typography,
  Tooltip,
  alpha,
} from '@mui/material';
import {
  TrendingUp,
  Refresh,
  Star,
  LocalFireDepartment,
  Whatshot,
  Diamond,
} from '@mui/icons-material';
import { AchievementReward, RewardType } from '../../types';

interface RewardDisplayProps {
  rewards: AchievementReward[];
  variant?: 'compact' | 'detailed';
  maxDisplay?: number;
}

const RewardDisplay: React.FC<RewardDisplayProps> = ({
  rewards,
  variant = 'compact',
  maxDisplay = 3,
}) => {
  const getRewardIcon = (reward: AchievementReward) => {
    if (reward.type === RewardType.GAME_RESTART) {
      return <Refresh sx={{ fontSize: '1rem' }} />;
    }
    
    // For trait boosts, use different icons based on category
    switch (reward.category) {
      case 'basic':
        return <Star sx={{ fontSize: '1rem' }} />;
      case 'intermediate':
        return <LocalFireDepartment sx={{ fontSize: '1rem' }} />;
      case 'advanced':
        return <Whatshot sx={{ fontSize: '1rem' }} />;
      case 'elite':
        return <Diamond sx={{ fontSize: '1rem' }} />;
      default:
        return <TrendingUp sx={{ fontSize: '1rem' }} />;
    }
  };

  const getRewardColor = (reward: AchievementReward): string => {
    if (reward.type === RewardType.GAME_RESTART) {
      return '#9c27b0'; // Purple for game restarts
    }
    
    // Color based on trait category
    switch (reward.category) {
      case 'basic':
        return '#9e9e9e'; // Gray
      case 'intermediate':
        return '#4caf50'; // Green
      case 'advanced':
        return '#2196f3'; // Blue
      case 'elite':
        return '#ff9800'; // Orange
      default:
        return '#757575';
    }
  };

  const getRewardTooltip = (reward: AchievementReward): string => {
    if (reward.type === RewardType.GAME_RESTART) {
      return `Commissioner Tool: Grant ${reward.boostAmount} game restart${reward.boostAmount > 1 ? 's' : ''} in CFB 25 dynasty mode`;
    }
    
    return `Commissioner Tool: Boost ${reward.traitName} by +${reward.boostAmount} rating points in CFB 25`;
  };

  if (!rewards || rewards.length === 0) {
    return null;
  }

  const activeRewards = rewards.filter(reward => reward.active);
  const displayRewards = activeRewards.slice(0, maxDisplay);
  const remainingCount = activeRewards.length - maxDisplay;

  if (variant === 'compact') {
    return (
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 1 }}>
        {displayRewards.map((reward) => (
          <Tooltip key={reward.id} title={getRewardTooltip(reward)} arrow>
            <Chip
              icon={getRewardIcon(reward)}
              label={reward.description}
              size="small"
              sx={{
                backgroundColor: alpha(getRewardColor(reward), 0.1),
                color: getRewardColor(reward),
                border: `1px solid ${alpha(getRewardColor(reward), 0.3)}`,
                fontSize: '0.75rem',
                height: '24px',
                '& .MuiChip-icon': {
                  color: getRewardColor(reward),
                },
              }}
            />
          </Tooltip>
        ))}
        
        {remainingCount > 0 && (
          <Chip
            label={`+${remainingCount} more`}
            size="small"
            sx={{
              backgroundColor: alpha('#757575', 0.1),
              color: '#757575',
              border: `1px solid ${alpha('#757575', 0.3)}`,
              fontSize: '0.75rem',
              height: '24px',
            }}
          />
        )}
      </Box>
    );
  }

  // Detailed variant
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" sx={{ mb: 1, color: 'text.secondary' }}>
        Rewards ({activeRewards.length})
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {activeRewards.map((reward) => (
          <Box
            key={reward.id}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              p: 1.5,
              backgroundColor: alpha(getRewardColor(reward), 0.05),
              border: `1px solid ${alpha(getRewardColor(reward), 0.2)}`,
              borderRadius: 2,
            }}
          >
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 32,
                height: 32,
                borderRadius: '50%',
                backgroundColor: alpha(getRewardColor(reward), 0.1),
                color: getRewardColor(reward),
              }}
            >
              {getRewardIcon(reward)}
            </Box>
            
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                {reward.displayName}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                {reward.description}
                {reward.category && ` • ${reward.category.charAt(0).toUpperCase() + reward.category.slice(1)} tier`}
              </Typography>
            </Box>
            
            <Chip
              label={`+${reward.boostAmount}`}
              size="small"
              sx={{
                backgroundColor: getRewardColor(reward),
                color: 'white',
                fontWeight: 'bold',
                minWidth: '40px',
              }}
            />
          </Box>
        ))}
      </Box>
    </Box>
  );
};

export default RewardDisplay;