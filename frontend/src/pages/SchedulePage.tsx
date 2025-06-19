import React, { useState, useEffect, useMemo } from 'react';
import {
  Container,
  Typography,
  Box,
  Breadcrumbs,
  Link,
  Alert,
  CircularProgress,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Home as HomeIcon,
  Schedule as ScheduleIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Link as RouterLink, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useScheduleData, useWeekNavigation } from '../hooks/useSchedule';
import GlassCard from '../components/ui/GlassCard';
import WeekSchedule from '../components/schedule/WeekSchedule';
import ScheduleFilters from '../components/schedule/ScheduleFilters';
import PullToRefresh from '../components/schedule/PullToRefresh';
import SwipeNavigation from '../components/schedule/SwipeNavigation';



// Filter state interface
interface ScheduleFiltersState {
  year: number;
  week: number | null;
  teamView: 'all' | 'selected';
  conference: string | null;
}

const SchedulePage: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { selectedTeam } = useAuth();

  // Filter state
  const [filters, setFilters] = useState<ScheduleFiltersState>({
    year: new Date().getFullYear(),
    week: null, // null means current week
    teamView: 'all',
    conference: null,
  });

  // Track if we've initialized from current week data
  const [hasInitializedFromCurrentWeek, setHasInitializedFromCurrentWeek] = useState(false);

  // Fetch schedule data using the custom hook
  const {
    data: games,
    isLoading,
    error,
    currentWeek,
  } = useScheduleData({
    year: filters.year,
    weekNumber: filters.week, // Pass the week number, hook will resolve to weekId
    teamId: filters.teamView === 'selected' ? selectedTeam?.id : undefined,
    teamView: filters.teamView,
  });

  // Week navigation data
  const weekNavigation = useWeekNavigation(filters.year);

  // Mobile optimizations
  const handleRefresh = async () => {
    // Force refetch of current data
    window.location.reload();
  };

  const handleSwipeLeft = () => {
    // Next week
    if (weekNavigation.canGoNext) {
      const newWeek = (filters.week || weekNavigation.currentWeekNumber) + 1;
      handleFiltersChange({ ...filters, week: newWeek });
    }
  };

  const handleSwipeRight = () => {
    // Previous week
    if (weekNavigation.canGoPrevious) {
      const newWeek = (filters.week || weekNavigation.currentWeekNumber) - 1;
      handleFiltersChange({ ...filters, week: newWeek });
    }
  };

  // Initialize filters from URL parameters
  useEffect(() => {
    const yearParam = searchParams.get('year');
    const weekParam = searchParams.get('week');
    const teamViewParam = searchParams.get('teamView');
    const conferenceParam = searchParams.get('conference');

    setFilters({
      year: yearParam ? parseInt(yearParam, 10) : new Date().getFullYear(),
      week: weekParam ? parseInt(weekParam, 10) : null,
      teamView: (teamViewParam === 'selected' ? 'selected' : 'all') as 'all' | 'selected',
      conference: conferenceParam === 'All Conferences' ? null : conferenceParam,
    });
  }, [searchParams]);

  // Initialize to current week when current week data is available and no URL params specify a week
  useEffect(() => {
    if (currentWeek && !hasInitializedFromCurrentWeek && !searchParams.get('week')) {
      setFilters(prev => ({
        ...prev,
        week: currentWeek.currentWeek,
      }));
      setHasInitializedFromCurrentWeek(true);
    }
  }, [currentWeek, hasInitializedFromCurrentWeek, searchParams]);

  const handleFiltersChange = (newFilters: ScheduleFiltersState) => {
    setFilters(newFilters);
    
    // Update URL parameters
    const params = new URLSearchParams();
    if (newFilters.year !== new Date().getFullYear()) {
      params.set('year', newFilters.year.toString());
    }
    if (newFilters.week !== null) {
      params.set('week', newFilters.week.toString());
    }
    if (newFilters.teamView !== 'all') {
      params.set('teamView', newFilters.teamView);
    }
    if (newFilters.conference) {
      params.set('conference', newFilters.conference);
    }
    
    // Navigate to new URL without causing a page reload
    navigate(`/schedule?${params.toString()}`, { replace: true });
  };

  // Page animations
  const pageVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        duration: 0.6,
        staggerChildren: 0.1
      }
    },
  };

  const itemVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { opacity: 1, y: 0 },
  };

  // Generate page title
  const pageTitle = useMemo(() => {
    let title = 'Football Schedule';
    if (filters.year && filters.year !== new Date().getFullYear()) {
      title += ` - ${filters.year}`;
    }
    if (filters.week) {
      title += ` - Week ${filters.week}`;
    }
    if (filters.teamView === 'selected' && selectedTeam) {
      title += ` - ${selectedTeam.name}`;
    }
    if (filters.conference && filters.conference !== 'All Conferences') {
      title += ` - ${filters.conference}`;
    }
    return title;
  }, [filters.year, filters.week, filters.teamView, filters.conference, selectedTeam]);

  // Update document title
  useEffect(() => {
    document.title = `${pageTitle} | Football Dynasty`;
  }, [pageTitle]);

  // Error boundary fallback
  if (error) {
    return (
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Alert 
          severity="error" 
          sx={{ 
            background: 'linear-gradient(135deg, rgba(244,67,54,0.1) 0%, rgba(244,67,54,0.05) 100%)',
            border: `1px solid ${theme.palette.error.main}40`,
            backdropFilter: 'blur(20px)',
          }}
        >
          Failed to load schedule data. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
    <PullToRefresh 
      onRefresh={handleRefresh} 
      disabled={isLoading}
    >
      <Container 
        maxWidth="xl" 
        component={motion.div}
        variants={pageVariants}
        initial="initial"
        animate="animate"
        sx={{ py: { xs: 2, md: 4 } }}
      >
      {/* Breadcrumbs */}
      <motion.div variants={itemVariants}>
        <Breadcrumbs 
          aria-label="breadcrumb" 
          sx={{ 
            mb: 3,
            '& .MuiBreadcrumbs-ol': {
              flexWrap: 'wrap',
            },
            '& .MuiBreadcrumbs-li': {
              display: 'flex',
              alignItems: 'center',
            },
          }}
        >
          <Link
            component={RouterLink}
            to="/"
            sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              color: 'text.secondary',
              textDecoration: 'none',
              '&:hover': {
                color: 'primary.main',
                textDecoration: 'underline',
              },
            }}
          >
            <HomeIcon sx={{ mr: 0.5, fontSize: '1rem' }} />
            Dashboard
          </Link>
          <Typography 
            color="text.primary" 
            sx={{ 
              display: 'flex', 
              alignItems: 'center',
              fontWeight: 600,
            }}
          >
            <ScheduleIcon sx={{ mr: 0.5, fontSize: '1rem' }} />
            Schedule
          </Typography>
        </Breadcrumbs>
      </motion.div>

      {/* Page Header */}
      <motion.div variants={itemVariants}>
        <Box sx={{ mb: 4 }}>
          <Typography 
            variant={isMobile ? 'h4' : 'h3'} 
            component="h1"
            gutterBottom
            sx={{ 
              fontWeight: 700,
              background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              mb: 1,
            }}
          >
            Football Schedule
          </Typography>
          <Typography 
            variant="body1" 
            color="text.secondary"
            sx={{ 
              maxWidth: '600px',
              lineHeight: 1.6,
            }}
          >
            View and navigate through the complete football season schedule. Filter by week, team, 
            and conference to find the games you're looking for.
          </Typography>
          
          {/* Stats Summary */}
          <Box sx={{ 
            mt: 2, 
            display: 'flex', 
            gap: 2, 
            flexWrap: 'wrap',
            alignItems: 'center',
          }}>
            <Typography variant="body2" color="text.secondary">
              {filters.teamView === 'selected' && selectedTeam 
                ? `Showing schedule for ${selectedTeam.name}` 
                : 'Showing all games'}
              {filters.year && ` for ${filters.year}`}
              {filters.week && ` - Week ${filters.week}`}
              {filters.conference && filters.conference !== 'All Conferences' && ` in ${filters.conference}`}
            </Typography>
          </Box>
        </Box>
      </motion.div>

      {/* Filters */}
      <motion.div variants={itemVariants}>
        <ScheduleFilters
          filters={filters}
          onFiltersChange={handleFiltersChange}
          loading={isLoading}
          weekNavigation={weekNavigation}
          currentWeek={currentWeek}
        />
      </motion.div>

      {/* Loading State */}
      {isLoading && (
        <motion.div variants={itemVariants}>
          <Box 
            sx={{ 
              display: 'flex', 
              justifyContent: 'center', 
              alignItems: 'center',
              minHeight: 400,
              flexDirection: 'column',
              gap: 2,
            }}
          >
            <CircularProgress 
              size={48}
              sx={{ 
                color: 'primary.main',
                '& .MuiCircularProgress-circle': {
                  strokeLinecap: 'round',
                },
              }}
            />
            <Typography variant="body1" color="text.secondary">
              Loading schedule data...
            </Typography>
          </Box>
        </motion.div>
      )}

      {/* Schedule Display */}
      {!isLoading && (
        <motion.div variants={itemVariants}>
          <SwipeNavigation
            onSwipeLeft={handleSwipeLeft}
            onSwipeRight={handleSwipeRight}
            disabled={isLoading || !isMobile}
          >
            <WeekSchedule
              games={games}
              loading={isLoading}
              filters={filters}
            />
          </SwipeNavigation>
        </motion.div>
      )}

      {/* No Results */}
      {!isLoading && (!games || games.length === 0) && !error && (
        <motion.div variants={itemVariants}>
          <Alert 
            severity="info"
            sx={{ 
              mt: 3,
              background: 'linear-gradient(135deg, rgba(33,150,243,0.1) 0%, rgba(33,150,243,0.05) 100%)',
              border: `1px solid ${theme.palette.info.main}40`,
              backdropFilter: 'blur(20px)',
            }}
          >
            No games found for the selected filters. Try adjusting your search criteria or check back later.
          </Alert>
        </motion.div>
      )}
      </Container>
    </PullToRefresh>
  );
};

export default SchedulePage;