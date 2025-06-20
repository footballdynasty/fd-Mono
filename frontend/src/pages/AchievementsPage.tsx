import React, { useState, useMemo } from 'react';
import {
  Box,
  Container,
  Typography,
  Pagination,
  Tabs,
  Tab,
  IconButton,
  Tooltip,
  alpha,
} from '@mui/material';
import {
  EmojiEvents,
  Refresh,
  Analytics,
  ViewModule,
  ViewList,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useToast } from '../contexts/ToastContext';
import { usePendingAchievements } from '../contexts/PendingAchievementsContext';
import { useAchievements, useAchievementStats, useCompleteAchievement } from '../hooks/useAchievements';
import { AchievementType, AchievementRarity } from '../types';
import AchievementGrid from '../components/achievements/AchievementGrid';
import AchievementFilter from '../components/achievements/AchievementFilter';
import AchievementStats from '../components/achievements/AchievementStats';
import GlassCard from '../components/ui/GlassCard';

const AchievementsPage: React.FC = () => {
  const { showToast } = useToast();
  const { addPendingRequest, removePendingRequest } = usePendingAchievements();
  
  // Filtering state
  const [selectedType, setSelectedType] = useState<AchievementType | 'ALL'>('ALL');
  const [selectedRarity, setSelectedRarity] = useState<AchievementRarity | 'ALL'>('ALL');
  const [selectedCompletion, setSelectedCompletion] = useState<'ALL' | 'COMPLETED' | 'INCOMPLETE'>('ALL');
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 12;
  
  // View state
  const [currentTab, setCurrentTab] = useState(0);
  const [compactView, setCompactView] = useState(false);
  
  // Build query parameters for API
  const queryParams = useMemo(() => {
    const params: any = {
      page: currentPage - 1, // API is 0-based
      size: pageSize,
    };
    
    if (selectedType !== 'ALL') {
      params.type = selectedType;
    }
    
    if (selectedCompletion === 'COMPLETED') {
      params.completed = true;
    } else if (selectedCompletion === 'INCOMPLETE') {
      params.completed = false;
    }
    
    return params;
  }, [currentPage, selectedType, selectedCompletion]);
  
  // API hooks
  const {
    data: achievementsData,
    achievements,
    isLoading,
    isError,
    error,
    refetch,
  } = useAchievements(queryParams);
  
  const {
    data: statsData,
    isLoading: statsLoading,
    refetch: refetchStats,
  } = useAchievementStats();
  
  const completeAchievementMutation = useCompleteAchievement();
  
  // Filter achievements by rarity (client-side since API doesn't support rarity filter yet)
  const filteredAchievements = useMemo(() => {
    if (selectedRarity === 'ALL') {
      return achievements;
    }
    return achievements.filter(achievement => achievement.rarity === selectedRarity);
  }, [achievements, selectedRarity]);
  
  // Handle achievement completion
  const handleCompleteAchievement = async (achievementId: string) => {
    // Prevent duplicate requests
    if (completeAchievementMutation.isPending) {
      showToast('Request already in progress...', 'warning');
      return;
    }

    try {
      const response = await completeAchievementMutation.mutateAsync(achievementId);
      
      // Check the response to determine if it was completed or submitted for approval
      if (response.status === 'completed') {
        showToast('Achievement completed! 🎉', 'success');
        // Remove from pending if it was there (shouldn't happen for commissioners but just in case)
        removePendingRequest(achievementId);
      } else if (response.status === 'pending') {
        showToast('Achievement request submitted for admin review 📋', 'info');
        // Add to pending requests with the returned request ID
        addPendingRequest(achievementId, response.requestId || 'unknown');
      } else {
        showToast('Achievement request processed', 'success');
      }
      
      refetchStats(); // Refresh stats after completion
    } catch (error) {
      showToast('Failed to complete achievement', 'error');
    }
  };
  
  // Handle filter changes
  const handleTypeChange = (type: AchievementType | 'ALL') => {
    setSelectedType(type);
    setCurrentPage(1); // Reset to first page
  };
  
  const handleRarityChange = (rarity: AchievementRarity | 'ALL') => {
    setSelectedRarity(rarity);
    setCurrentPage(1); // Reset to first page
  };
  
  const handleCompletionChange = (completion: 'ALL' | 'COMPLETED' | 'INCOMPLETE') => {
    setSelectedCompletion(completion);
    setCurrentPage(1); // Reset to first page
  };
  
  // Handle pagination
  const handlePageChange = (event: React.ChangeEvent<unknown>, page: number) => {
    setCurrentPage(page);
  };
  
  // Handle refresh
  const handleRefresh = () => {
    refetch();
    refetchStats();
    showToast('Achievements refreshed', 'info');
  };
  
  // Calculate total pages (adjust for client-side rarity filtering)
  const totalFilteredCount = selectedRarity === 'ALL' 
    ? (achievementsData?.totalElements || 0)
    : filteredAchievements.length;
  
  const totalPages = Math.ceil(totalFilteredCount / pageSize);
  
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        delayChildren: 0.1,
        staggerChildren: 0.1,
      },
    },
  };
  
  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: { type: 'spring', stiffness: 300, damping: 20 },
    },
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* Header */}
        <motion.div variants={itemVariants}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <EmojiEvents
                sx={{
                  fontSize: '2.5rem',
                  mr: 2,
                  background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              />
              <Box>
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    mb: 0.5,
                  }}
                >
                  Achievements
                </Typography>
                <Typography variant="body1" color="text.secondary">
                  Track your football dynasty milestones and accomplishments
                </Typography>
              </Box>
            </Box>
            
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Tooltip title="Toggle Compact View">
                <IconButton
                  onClick={() => setCompactView(!compactView)}
                  sx={{
                    background: compactView
                      ? 'linear-gradient(135deg, rgba(33,150,243,0.2) 0%, rgba(33,150,243,0.1) 100%)'
                      : 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                    border: compactView
                      ? '1px solid rgba(33,150,243,0.3)'
                      : '1px solid rgba(255,255,255,0.2)',
                    color: compactView ? '#2196f3' : 'text.secondary',
                    '&:hover': {
                      background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                    },
                  }}
                >
                  {compactView ? <ViewList /> : <ViewModule />}
                </IconButton>
              </Tooltip>
              
              <Tooltip title="Refresh Achievements">
                <IconButton
                  onClick={handleRefresh}
                  disabled={isLoading || statsLoading}
                  sx={{
                    background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                    border: '1px solid rgba(255,255,255,0.2)',
                    color: 'text.secondary',
                    '&:hover': {
                      background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                    },
                  }}
                >
                  <Refresh />
                </IconButton>
              </Tooltip>
            </Box>
          </Box>
        </motion.div>

        {/* Tabs */}
        <motion.div variants={itemVariants}>
          <GlassCard sx={{ mb: 3 }}>
            <Tabs
              value={currentTab}
              onChange={(_, newValue) => setCurrentTab(newValue)}
              sx={{
                '& .MuiTab-root': {
                  textTransform: 'none',
                  fontWeight: 600,
                  color: 'text.secondary',
                  '&.Mui-selected': {
                    color: 'primary.main',
                  },
                },
                '& .MuiTabs-indicator': {
                  background: 'linear-gradient(90deg, #1e88e5, #42a5f5)',
                  height: 3,
                  borderRadius: 2,
                },
              }}
            >
              <Tab
                icon={<ViewModule />}
                label="All Achievements"
                iconPosition="start"
              />
              <Tab
                icon={<Analytics />}
                label="Statistics"
                iconPosition="start"
              />
            </Tabs>
          </GlassCard>
        </motion.div>

        {/* Tab Content */}
        {currentTab === 0 && (
          <motion.div
            key="achievements"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ type: 'spring', stiffness: 300, damping: 20 }}
          >
              {/* Filters */}
              <motion.div variants={itemVariants}>
                <AchievementFilter
                  selectedType={selectedType}
                  selectedRarity={selectedRarity}
                  selectedCompletion={selectedCompletion}
                  onTypeChange={handleTypeChange}
                  onRarityChange={handleRarityChange}
                  onCompletionChange={handleCompletionChange}
                  achievementCounts={statsData ? {
                    total: statsData.totalAchievements,
                    completed: statsData.completedAchievements,
                    byType: statsData.countByType,
                    byRarity: statsData.countByRarity,
                  } : undefined}
                />
              </motion.div>

              {/* Achievement Grid */}
              <motion.div variants={itemVariants}>
                <AchievementGrid
                  achievements={filteredAchievements}
                  isLoading={isLoading}
                  isError={isError}
                  error={error}
                  onCompleteAchievement={handleCompleteAchievement}
                  showActions={true}
                  compact={compactView}
                />
              </motion.div>

              {/* Pagination */}
              {totalPages > 1 && (
                <motion.div variants={itemVariants}>
                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <Pagination
                      count={totalPages}
                      page={currentPage}
                      onChange={handlePageChange}
                      color="primary"
                      size="large"
                      sx={{
                        '& .MuiPaginationItem-root': {
                          background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                          backdropFilter: 'blur(20px)',
                          border: '1px solid rgba(255,255,255,0.2)',
                          color: 'text.primary',
                          '&:hover': {
                            background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                          },
                          '&.Mui-selected': {
                            background: 'linear-gradient(135deg, rgba(33,150,243,0.2) 0%, rgba(33,150,243,0.1) 100%)',
                            border: '1px solid rgba(33,150,243,0.3)',
                            color: '#2196f3',
                          },
                        },
                      }}
                    />
                  </Box>
                </motion.div>
              )}
          </motion.div>
        )}

        {currentTab === 1 && (
          <motion.div
            key="stats"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ type: 'spring', stiffness: 300, damping: 20 }}
          >
            {statsData && (
              <AchievementStats stats={statsData} />
            )}
          </motion.div>
        )}
      </motion.div>
    </Container>
  );
};

export default AchievementsPage;