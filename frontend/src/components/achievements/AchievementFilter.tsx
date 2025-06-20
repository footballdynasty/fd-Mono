import React from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
  alpha,
} from '@mui/material';
import {
  FilterList,
  CheckCircle,
  Lock,
  Star,
  LocalFireDepartment,
  Whatshot,
  MilitaryTech,
  Diamond,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { AchievementType, AchievementRarity } from '../../types';
import GlassCard from '../ui/GlassCard';

interface AchievementFilterProps {
  selectedType: AchievementType | 'ALL';
  selectedRarity: AchievementRarity | 'ALL';
  selectedCompletion: 'ALL' | 'COMPLETED' | 'INCOMPLETE';
  onTypeChange: (type: AchievementType | 'ALL') => void;
  onRarityChange: (rarity: AchievementRarity | 'ALL') => void;
  onCompletionChange: (completion: 'ALL' | 'COMPLETED' | 'INCOMPLETE') => void;
  achievementCounts?: {
    total: number;
    completed: number;
    byType: Record<string, number>;
    byRarity: Record<string, number>;
  };
}

const AchievementFilter: React.FC<AchievementFilterProps> = ({
  selectedType,
  selectedRarity,
  selectedCompletion,
  onTypeChange,
  onRarityChange,
  onCompletionChange,
  achievementCounts,
}) => {
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

  const getTypeCount = (type: AchievementType): number => {
    return achievementCounts?.byType[type] || 0;
  };

  const getRarityCount = (rarity: AchievementRarity): number => {
    return achievementCounts?.byRarity[rarity] || 0;
  };

  return (
    <GlassCard sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <FilterList sx={{ mr: 1, color: 'primary.main' }} />
        <Typography variant="h6" sx={{ fontWeight: 600 }}>
          Filter Achievements
        </Typography>
        {achievementCounts && (
          <Chip
            label={`${achievementCounts.completed}/${achievementCounts.total} Completed`}
            sx={{
              ml: 'auto',
              background: 'linear-gradient(135deg, rgba(76,175,80,0.2) 0%, rgba(76,175,80,0.1) 100%)',
              border: '1px solid rgba(76,175,80,0.3)',
              color: '#4caf50',
              fontWeight: 600,
            }}
          />
        )}
      </Box>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        {/* Completion Status Filter */}
        <Box>
          <Typography variant="subtitle2" sx={{ mb: 1.5, fontWeight: 600 }}>
            Completion Status
          </Typography>
          <ToggleButtonGroup
            value={selectedCompletion}
            exclusive
            onChange={(_, value) => value && onCompletionChange(value)}
            sx={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: 1,
              '& .MuiToggleButton-root': {
                border: '1px solid rgba(255,255,255,0.2)',
                borderRadius: 2,
                color: 'text.secondary',
                textTransform: 'none',
                fontWeight: 500,
                '&.Mui-selected': {
                  background: 'linear-gradient(135deg, rgba(33,150,243,0.2) 0%, rgba(33,150,243,0.1) 100%)',
                  color: '#2196f3',
                  border: '1px solid rgba(33,150,243,0.3)',
                },
                '&:hover': {
                  background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                },
              },
            }}
          >
            <ToggleButton value="ALL">
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <FilterList sx={{ fontSize: '1rem' }} />
                All
              </Box>
            </ToggleButton>
            <ToggleButton value="COMPLETED">
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CheckCircle sx={{ fontSize: '1rem' }} />
                Completed
              </Box>
            </ToggleButton>
            <ToggleButton value="INCOMPLETE">
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Lock sx={{ fontSize: '1rem' }} />
                Incomplete
              </Box>
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>

        {/* Type Filter */}
        <Box>
          <Typography variant="subtitle2" sx={{ mb: 1.5, fontWeight: 600 }}>
            Achievement Type
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
              <Chip
                label="All Types"
                clickable
                onClick={() => onTypeChange('ALL')}
                sx={{
                  background: selectedType === 'ALL'
                    ? 'linear-gradient(135deg, rgba(33,150,243,0.2) 0%, rgba(33,150,243,0.1) 100%)'
                    : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                  border: selectedType === 'ALL'
                    ? '1px solid rgba(33,150,243,0.3)'
                    : '1px solid rgba(255,255,255,0.2)',
                  color: selectedType === 'ALL' ? '#2196f3' : 'text.secondary',
                  fontWeight: 600,
                  '&:hover': {
                    background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                  },
                }}
              />
            </motion.div>
            {Object.values(AchievementType).map((type) => (
              <motion.div key={type} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Chip
                  label={`${type} ${achievementCounts ? `(${getTypeCount(type)})` : ''}`}
                  clickable
                  onClick={() => onTypeChange(type)}
                  sx={{
                    background: selectedType === type
                      ? `linear-gradient(135deg, ${alpha(typeColors[type], 0.2)} 0%, ${alpha(typeColors[type], 0.1)} 100%)`
                      : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                    border: selectedType === type
                      ? `1px solid ${alpha(typeColors[type], 0.3)}`
                      : '1px solid rgba(255,255,255,0.2)',
                    color: selectedType === type ? typeColors[type] : 'text.secondary',
                    fontWeight: 600,
                    '&:hover': {
                      background: selectedType === type
                        ? `linear-gradient(135deg, ${alpha(typeColors[type], 0.25)} 0%, ${alpha(typeColors[type], 0.15)} 100%)`
                        : 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                    },
                  }}
                />
              </motion.div>
            ))}
          </Box>
        </Box>

        {/* Rarity Filter */}
        <Box>
          <Typography variant="subtitle2" sx={{ mb: 1.5, fontWeight: 600 }}>
            Achievement Rarity
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
              <Chip
                label="All Rarities"
                clickable
                onClick={() => onRarityChange('ALL')}
                sx={{
                  background: selectedRarity === 'ALL'
                    ? 'linear-gradient(135deg, rgba(33,150,243,0.2) 0%, rgba(33,150,243,0.1) 100%)'
                    : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                  border: selectedRarity === 'ALL'
                    ? '1px solid rgba(33,150,243,0.3)'
                    : '1px solid rgba(255,255,255,0.2)',
                  color: selectedRarity === 'ALL' ? '#2196f3' : 'text.secondary',
                  fontWeight: 600,
                  '&:hover': {
                    background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                  },
                }}
              />
            </motion.div>
            {Object.entries(rarityData).map(([rarity, data]) => (
              <motion.div key={rarity} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Chip
                  icon={data.icon}
                  label={`${rarity} ${achievementCounts ? `(${getRarityCount(rarity as AchievementRarity)})` : ''}`}
                  clickable
                  onClick={() => onRarityChange(rarity as AchievementRarity)}
                  sx={{
                    background: selectedRarity === rarity
                      ? `linear-gradient(135deg, ${alpha(data.color, 0.2)} 0%, ${alpha(data.color, 0.1)} 100%)`
                      : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                    border: selectedRarity === rarity
                      ? `1px solid ${alpha(data.color, 0.3)}`
                      : '1px solid rgba(255,255,255,0.2)',
                    color: selectedRarity === rarity ? data.color : 'text.secondary',
                    fontWeight: 600,
                    '& .MuiChip-icon': {
                      color: selectedRarity === rarity ? data.color : 'text.secondary',
                    },
                    '&:hover': {
                      background: selectedRarity === rarity
                        ? `linear-gradient(135deg, ${alpha(data.color, 0.25)} 0%, ${alpha(data.color, 0.15)} 100%)`
                        : 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                    },
                  }}
                />
              </motion.div>
            ))}
          </Box>
        </Box>
      </Box>
    </GlassCard>
  );
};

export default AchievementFilter;