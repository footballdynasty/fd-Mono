import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  IconButton,
  alpha,
  Tooltip,
} from '@mui/material';
import {
  EmojiEvents,
  Lock,
  CheckCircle,
  Star,
  Diamond,
  LocalFireDepartment,
  Whatshot,
  MilitaryTech,
  Schedule,
  Pending,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Achievement, AchievementRarity, AchievementType } from '../../types';
import { useAchievementRewards } from '../../hooks/useAchievementRewards';
import { usePendingAchievements } from '../../contexts/PendingAchievementsContext';
import RewardDisplay from './RewardDisplay';

interface AchievementCardProps {
  achievement: Achievement;
  onComplete?: (id: string) => void;
  showActions?: boolean;
  compact?: boolean;
}

const AchievementCard: React.FC<AchievementCardProps> = ({
  achievement,
  onComplete,
  showActions = false,
  compact = false,
}) => {
  // Fetch rewards for this achievement
  const { data: rewardsData, isLoading: rewardsLoading } = useAchievementRewards(achievement.id);
  const { isPending: isLocalPending } = usePendingAchievements();
  const getRarityColor = (rarity: AchievementRarity): string => {
    switch (rarity) {
      case 'COMMON':
        return '#9e9e9e';
      case 'UNCOMMON':
        return '#4caf50';
      case 'RARE':
        return '#2196f3';
      case 'EPIC':
        return '#9c27b0';
      case 'LEGENDARY':
        return '#ff9800';
      default:
        return '#9e9e9e';
    }
  };

  const getRarityIcon = (rarity: AchievementRarity) => {
    switch (rarity) {
      case 'COMMON':
        return <Star sx={{ fontSize: '1rem' }} />;
      case 'UNCOMMON':
        return <LocalFireDepartment sx={{ fontSize: '1rem' }} />;
      case 'RARE':
        return <Whatshot sx={{ fontSize: '1rem' }} />;
      case 'EPIC':
        return <MilitaryTech sx={{ fontSize: '1rem' }} />;
      case 'LEGENDARY':
        return <Diamond sx={{ fontSize: '1rem' }} />;
      default:
        return <Star sx={{ fontSize: '1rem' }} />;
    }
  };

  const getTypeColor = (type: AchievementType): string => {
    switch (type) {
      case 'WINS':
        return '#4caf50';
      case 'SEASON':
        return '#2196f3';
      case 'CHAMPIONSHIP':
        return '#ffc107';
      case 'STATISTICS':
        return '#ff5722';
      case 'GENERAL':
        return '#9c27b0';
      default:
        return '#757575';
    }
  };

  const formatDate = (timestamp: number): string => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const cardVariants = {
    hover: {
      scale: 1.02,
      transition: { type: 'spring', stiffness: 300, damping: 20 },
    },
  };

  const rarityColor = getRarityColor(achievement.rarity);
  const typeColor = getTypeColor(achievement.type);
  const isCompleted = achievement.isCompleted;
  const isPending = achievement.isPending || isLocalPending(achievement.id);
  const pendingColor = '#ff9800'; // Orange color for pending status

  return (
    <motion.div
      variants={cardVariants}
      whileHover="hover"
      style={{ height: '100%' }}
    >
      <Card
        sx={{
          height: '100%',
          background: isCompleted
            ? `linear-gradient(135deg, ${alpha(rarityColor, 0.1)} 0%, ${alpha(rarityColor, 0.05)} 100%)`
            : isPending
            ? `linear-gradient(135deg, ${alpha(pendingColor, 0.1)} 0%, ${alpha(pendingColor, 0.05)} 100%)`
            : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
          backdropFilter: 'blur(20px)',
          border: `1px solid ${alpha(isCompleted ? rarityColor : isPending ? pendingColor : '#ffffff', 0.2)}`,
          borderRadius: 3,
          position: 'relative',
          overflow: 'visible',
          opacity: isCompleted ? 1 : isPending ? 0.9 : 0.8,
          filter: isCompleted ? 'none' : isPending ? 'grayscale(0.1)' : 'grayscale(0.3)',
          transition: 'all 0.3s ease',
          '&:hover': {
            border: `1px solid ${alpha(isCompleted ? rarityColor : isPending ? pendingColor : '#ffffff', 0.4)}`,
            background: isCompleted
              ? `linear-gradient(135deg, ${alpha(rarityColor, 0.15)} 0%, ${alpha(rarityColor, 0.1)} 100%)`
              : isPending
              ? `linear-gradient(135deg, ${alpha(pendingColor, 0.15)} 0%, ${alpha(pendingColor, 0.1)} 100%)`
              : 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
          },
        }}
      >
        {/* Status Border Glow */}
        {(isCompleted || isPending) && (
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: '50%',
              transform: 'translateX(-50%)',
              width: '80%',
              height: '3px',
              background: isCompleted 
                ? `linear-gradient(90deg, transparent 0%, ${rarityColor} 20%, ${rarityColor} 80%, transparent 100%)`
                : `linear-gradient(90deg, transparent 0%, ${pendingColor} 20%, ${pendingColor} 80%, transparent 100%)`,
            }}
          />
        )}

        {/* Completion Status Icon */}
        <Box
          sx={{
            position: 'absolute',
            top: 12,
            right: 12,
            zIndex: 2,
          }}
        >
          {isCompleted ? (
            <Tooltip title={`Completed on ${achievement.dateCompleted ? formatDate(achievement.dateCompleted) : 'Unknown'}`}>
              <CheckCircle
                sx={{
                  color: rarityColor,
                  fontSize: '1.5rem',
                  filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.3))',
                }}
              />
            </Tooltip>
          ) : isPending ? (
            <Tooltip title="Request pending admin review">
              <Schedule
                sx={{
                  color: pendingColor,
                  fontSize: '1.5rem',
                  filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.3))',
                  animation: 'pulse 2s infinite',
                }}
              />
            </Tooltip>
          ) : (
            <Lock
              sx={{
                color: 'text.secondary',
                fontSize: '1.5rem',
                opacity: 0.6,
              }}
            />
          )}
        </Box>

        <CardContent sx={{ p: compact ? 2 : 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
          {/* Header with Icon and Title */}
          <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
            <Box
              sx={{
                p: 1.5,
                borderRadius: 2,
                background: `linear-gradient(135deg, ${alpha(typeColor, 0.2)} 0%, ${alpha(typeColor, 0.1)} 100%)`,
                border: `1px solid ${alpha(typeColor, 0.3)}`,
                mr: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                minWidth: 48,
                height: 48,
              }}
            >
              <EmojiEvents
                sx={{
                  color: typeColor,
                  fontSize: '1.5rem',
                }}
              />
            </Box>

            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography
                variant={compact ? 'subtitle2' : 'h6'}
                sx={{
                  fontWeight: 600,
                  color: isCompleted ? 'text.primary' : 'text.secondary',
                  mb: 0.5,
                  lineHeight: 1.2,
                }}
              >
                {achievement.description}
              </Typography>

              {/* Rarity and Type Chips */}
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Chip
                  icon={getRarityIcon(achievement.rarity)}
                  label={achievement.rarity}
                  size="small"
                  sx={{
                    background: `linear-gradient(135deg, ${alpha(rarityColor, 0.2)} 0%, ${alpha(rarityColor, 0.1)} 100%)`,
                    border: `1px solid ${alpha(rarityColor, 0.3)}`,
                    color: rarityColor,
                    fontWeight: 600,
                    fontSize: '0.75rem',
                    '& .MuiChip-icon': {
                      color: rarityColor,
                    },
                  }}
                />
                <Chip
                  label={achievement.type}
                  size="small"
                  sx={{
                    background: `linear-gradient(135deg, ${alpha(typeColor, 0.2)} 0%, ${alpha(typeColor, 0.1)} 100%)`,
                    border: `1px solid ${alpha(typeColor, 0.3)}`,
                    color: typeColor,
                    fontWeight: 600,
                    fontSize: '0.75rem',
                  }}
                />
              </Box>
            </Box>
          </Box>

          {/* Reward Description */}
          <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <Typography
              variant="body2"
              sx={{
                color: 'text.secondary',
                mb: 1,
                fontStyle: 'italic',
                lineHeight: 1.4,
              }}
            >
              {achievement.reward}
            </Typography>

            {/* Configurable Rewards Display */}
            {!rewardsLoading && rewardsData?.rewards && rewardsData.rewards.length > 0 && (
              <RewardDisplay 
                rewards={rewardsData.rewards} 
                variant="compact"
                maxDisplay={compact ? 2 : 3}
              />
            )}

            {/* Action Buttons */}
            {showActions && !isCompleted && !isPending && onComplete && (
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 'auto' }}>
                <Tooltip title="Complete Achievement">
                  <IconButton
                    onClick={() => onComplete(achievement.id)}
                    sx={{
                      background: `linear-gradient(135deg, ${alpha(typeColor, 0.2)} 0%, ${alpha(typeColor, 0.1)} 100%)`,
                      border: `1px solid ${alpha(typeColor, 0.3)}`,
                      color: typeColor,
                      '&:hover': {
                        background: `linear-gradient(135deg, ${alpha(typeColor, 0.3)} 0%, ${alpha(typeColor, 0.2)} 100%)`,
                      },
                    }}
                  >
                    <CheckCircle />
                  </IconButton>
                </Tooltip>
              </Box>
            )}

            {/* Pending Status Text */}
            {isPending && (
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mt: 'auto', p: 1 }}>
                <Pending sx={{ color: pendingColor, mr: 1, fontSize: '1rem' }} />
                <Typography
                  variant="caption"
                  sx={{
                    color: pendingColor,
                    fontWeight: 600,
                    textAlign: 'center',
                    textTransform: 'uppercase',
                    letterSpacing: 1,
                  }}
                >
                  Pending Review
                </Typography>
              </Box>
            )}

            {/* Completion Date */}
            {isCompleted && achievement.dateCompleted && (
              <Typography
                variant="caption"
                sx={{
                  color: 'text.secondary',
                  textAlign: 'right',
                  mt: 'auto',
                  fontStyle: 'italic',
                }}
              >
                Completed {formatDate(achievement.dateCompleted)}
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>
    </motion.div>
  );
};

export default AchievementCard;