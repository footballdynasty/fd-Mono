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
  LeaderboardRounded as StandingsIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Link as RouterLink, useSearchParams } from 'react-router-dom';
// import { Helmet } from 'react-helmet-async'; // TODO: Add react-helmet-async dependency
import { useStandings } from '../hooks/useStandings';
import StandingsFilters, { StandingsFiltersState } from '../components/standings/StandingsFilters';
import StandingsTable from '../components/standings/StandingsTable';
import { Standing } from '../types';

const StandingsPage: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [searchParams] = useSearchParams();

  // Filter state
  const [filters, setFilters] = useState<StandingsFiltersState>({
    year: new Date().getFullYear(),
    conference: null,
    search: '',
  });

  // Initialize filters from URL parameters
  useEffect(() => {
    const yearParam = searchParams.get('year');
    const conferenceParam = searchParams.get('conference');
    const searchParam = searchParams.get('search');

    setFilters({
      year: yearParam ? parseInt(yearParam, 10) : new Date().getFullYear(),
      conference: conferenceParam === 'All Conferences' ? null : conferenceParam,
      search: searchParam || '',
    });
  }, [searchParams]);

  // Fetch standings data
  const { 
    data: standingsData, 
    isLoading, 
    error,
    // refetch // Future use for manual refresh functionality
  } = useStandings({
    year: filters.year || undefined,
    conference: filters.conference || undefined,
    page: 0,
    size: 100, // Get a large page to show all standings
  });

  // Filter standings by search term
  const filteredStandings = useMemo(() => {
    if (!standingsData?.content) return [];

    let standings = standingsData.content;

    // Apply search filter
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase();
      standings = standings.filter((standing: Standing) =>
        standing.team.name.toLowerCase().includes(searchTerm) ||
        standing.team.conference?.toLowerCase().includes(searchTerm)
      );
    }

    return standings;
  }, [standingsData, filters.search]);

  const handleFiltersChange = (newFilters: StandingsFiltersState) => {
    setFilters(newFilters);
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
    let title = 'Team Standings';
    if (filters.year && filters.year !== new Date().getFullYear()) {
      title += ` - ${filters.year}`;
    }
    if (filters.conference && filters.conference !== 'All Conferences') {
      title += ` - ${filters.conference}`;
    }
    return title;
  }, [filters.year, filters.conference]);

  // Generate meta description - future use for SEO
  // const metaDescription = useMemo(() => {
  //   let description = `View college football team standings`;
  //   if (filters.year) {
  //     description += ` for ${filters.year}`;
  //   }
  //   if (filters.conference && filters.conference !== 'All Conferences') {
  //     description += ` in the ${filters.conference} conference`;
  //   }
  //   description += '. Compare team records, win percentages, and conference standings.';
  //   return description;
  // }, [filters.year, filters.conference]);

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
          Failed to load standings data. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
    <>

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
              <StandingsIcon sx={{ mr: 0.5, fontSize: '1rem' }} />
              Standings
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
              Team Standings
            </Typography>
            <Typography 
              variant="body1" 
              color="text.secondary"
              sx={{ 
                maxWidth: '600px',
                lineHeight: 1.6,
              }}
            >
              Track team performance across conferences and seasons. Compare win records, 
              conference standings, and rankings to analyze team success.
            </Typography>
            
            {/* Stats Summary */}
            {filteredStandings.length > 0 && (
              <Box sx={{ 
                mt: 2, 
                display: 'flex', 
                gap: 2, 
                flexWrap: 'wrap',
                alignItems: 'center',
              }}>
                <Typography variant="body2" color="text.secondary">
                  Showing {filteredStandings.length} teams
                  {filters.year && ` for ${filters.year}`}
                  {filters.conference && filters.conference !== 'All Conferences' && ` in ${filters.conference}`}
                </Typography>
              </Box>
            )}
          </Box>
        </motion.div>

        {/* Filters */}
        <motion.div variants={itemVariants}>
          <StandingsFilters
            filters={filters}
            onFiltersChange={handleFiltersChange}
            loading={isLoading}
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
                Loading standings data...
              </Typography>
            </Box>
          </motion.div>
        )}

        {/* Standings Table */}
        {!isLoading && (
          <motion.div variants={itemVariants}>
            <StandingsTable
              standings={filteredStandings}
              loading={isLoading}
              error={error ? 'Failed to load standings data' : null}
              showConferenceStats={!filters.conference || filters.conference === 'All Conferences'}
            />
          </motion.div>
        )}

        {/* No Results */}
        {!isLoading && filteredStandings.length === 0 && !error && (
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
              No standings data found for the selected filters. Try adjusting your search criteria or check back later.
            </Alert>
          </motion.div>
        )}
      </Container>
    </>
  );
};

export default StandingsPage;