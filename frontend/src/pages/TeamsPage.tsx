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
  Pagination,
  alpha,
} from '@mui/material';
import {
  Home as HomeIcon,
  SportsFootball as TeamsIcon,
  Add,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Link as RouterLink, useSearchParams } from 'react-router-dom';
import { useTeams, useConferences, useCreateTeam, useUpdateTeam, useDeleteTeam } from '../hooks/useTeams';
import { useAuth } from '../hooks/useAuth';
import TeamsFilters, { TeamsFiltersState } from '../components/teams/TeamsFilters';
import TeamsTable from '../components/teams/TeamsTable';
import TeamDetailModal from '../components/teams/TeamDetailModal';
import TeamManagementModal, { TeamManagementMode } from '../components/teams/TeamManagementModal';
import GradientButton from '../components/ui/GradientButton';
import { Team } from '../types';

const TeamsPage: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [searchParams] = useSearchParams();
  const { isCommissioner } = useAuth();

  // Modal state
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [isManagementModalOpen, setIsManagementModalOpen] = useState(false);
  const [managementMode, setManagementMode] = useState<TeamManagementMode>('create');

  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10); // Reduced to 10 teams per page for faster loading

  // Filter state
  const [filters, setFilters] = useState<TeamsFiltersState>({
    conference: null,
    search: '',
    humanOnly: false,
    sortBy: 'name',
    sortOrder: 'asc',
  });

  // Initialize filters from URL parameters
  useEffect(() => {
    const conferenceParam = searchParams.get('conference');
    const searchParam = searchParams.get('search');
    const humanOnlyParam = searchParams.get('humanOnly') === 'true';
    const sortByParam = searchParams.get('sortBy') as TeamsFiltersState['sortBy'];
    const sortOrderParam = searchParams.get('sortOrder') as TeamsFiltersState['sortOrder'];

    setFilters({
      conference: conferenceParam === 'All Conferences' ? null : conferenceParam,
      search: searchParam || '',
      humanOnly: humanOnlyParam,
      sortBy: sortByParam || 'name',
      sortOrder: sortOrderParam || 'asc',
    });
  }, [searchParams]);

  // Fetch teams data with pagination and filtering
  const { 
    data: teamsData, 
    isLoading, 
    error,
  } = useTeams({
    search: filters.search || undefined,
    page: page,
    size: pageSize,
    conference: filters.conference || undefined,
    humanOnly: filters.humanOnly,
  });

  // Additional API hooks
  const { data: conferencesData } = useConferences();
  const createTeamMutation = useCreateTeam();
  const updateTeamMutation = useUpdateTeam();
  const deleteTeamMutation = useDeleteTeam();

  // Get current page teams (server handles filtering and sorting)
  const currentTeams = useMemo(() => {
    return teamsData?.content || [];
  }, [teamsData]);

  const handleFiltersChange = (newFilters: TeamsFiltersState) => {
    setFilters(newFilters);
    setPage(0); // Reset to first page when filters change
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const handleTeamClick = (team: Team) => {
    setSelectedTeam(team);
    setIsDetailModalOpen(true);
  };

  const handleDetailModalClose = () => {
    setIsDetailModalOpen(false);
    setSelectedTeam(null);
  };

  const handleCreateTeam = () => {
    setManagementMode('create');
    setSelectedTeam(null);
    setIsManagementModalOpen(true);
  };

  const handleEditTeam = (team: Team) => {
    setManagementMode('edit');
    setSelectedTeam(team);
    setIsManagementModalOpen(true);
  };

  const handleDeleteTeam = (team: Team) => {
    setManagementMode('delete');
    setSelectedTeam(team);
    setIsManagementModalOpen(true);
  };

  const handleManagementModalClose = () => {
    setIsManagementModalOpen(false);
    setSelectedTeam(null);
  };

  const handleTeamSave = async (teamData: Partial<Team>) => {
    if (managementMode === 'create') {
      await createTeamMutation.mutateAsync(teamData as Omit<Team, 'id'>);
    } else if (managementMode === 'edit' && selectedTeam) {
      await updateTeamMutation.mutateAsync({
        id: selectedTeam.id,
        data: teamData,
      });
    }
  };

  const handleTeamDelete = async (teamId: string) => {
    await deleteTeamMutation.mutateAsync(teamId);
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
    let title = 'Teams Directory';
    if (filters.humanOnly) {
      title += ' - Human Teams';
    }
    if (filters.conference && filters.conference !== 'All Conferences') {
      title += ` - ${filters.conference}`;
    }
    return title;
  }, [filters.humanOnly, filters.conference]);

  // Generate stats summary
  const statsMessage = useMemo(() => {
    if (!teamsData) return '';
    
    const { totalElements, size, number } = teamsData;
    const start = number * size + 1;
    const end = Math.min((number + 1) * size, totalElements);
    
    let message = `Showing ${start}-${end} of ${totalElements} teams`;
    
    if (filters.humanOnly) {
      message += ' (human-controlled only)';
    }
    
    if (filters.conference && filters.conference !== 'All Conferences') {
      message += ` in ${filters.conference}`;
    }
    
    return message;
  }, [teamsData, filters]);

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
          Failed to load teams data. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
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
            <TeamsIcon sx={{ mr: 0.5, fontSize: '1rem' }} />
            Teams
          </Typography>
        </Breadcrumbs>
      </motion.div>

      {/* Page Header */}
      <motion.div variants={itemVariants}>
        <Box sx={{ mb: 4 }}>
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'flex-start',
            mb: 2,
          }}>
            <Typography 
              variant={isMobile ? 'h4' : 'h3'} 
              component="h1"
              sx={{ 
                fontWeight: 700,
                background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              Teams Directory
            </Typography>
            {isCommissioner && (
              <GradientButton
                onClick={handleCreateTeam}
                startIcon={<Add />}
                sx={{ ml: 2 }}
              >
                Create Team
              </GradientButton>
            )}
          </Box>
          <Typography 
            variant="body1" 
            color="text.secondary"
            sx={{ 
              maxWidth: '600px',
              lineHeight: 1.6,
            }}
          >
            Browse and manage all teams in the Football Dynasty league. View team records, 
            conference affiliations, and coaching assignments across human and AI-controlled teams.
          </Typography>
          
          {/* Stats Summary */}
          {statsMessage && (
            <Box sx={{ 
              mt: 2, 
              display: 'flex', 
              gap: 2, 
              flexWrap: 'wrap',
              alignItems: 'center',
            }}>
              <Typography variant="body2" color="text.secondary">
                {statsMessage}
              </Typography>
            </Box>
          )}
        </Box>
      </motion.div>

      {/* Filters */}
      <motion.div variants={itemVariants}>
        <TeamsFilters
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
              Loading teams data...
            </Typography>
          </Box>
        </motion.div>
      )}

      {/* Teams Table */}
      {!isLoading && (
        <motion.div variants={itemVariants}>
          <TeamsTable
            teams={currentTeams}
            loading={isLoading}
            error={error ? 'Failed to load teams data' : null}
            onTeamClick={handleTeamClick}
            showActions={isCommissioner}
            onTeamEdit={handleEditTeam}
            onTeamDelete={handleDeleteTeam}
          />
        </motion.div>
      )}

      {/* Pagination */}
      {!isLoading && teamsData && teamsData.totalPages > 1 && (
        <motion.div variants={itemVariants}>
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'center', 
            mt: 4,
            mb: 2,
          }}>
            <Pagination
              count={teamsData.totalPages}
              page={page + 1} // MUI Pagination is 1-indexed
              onChange={(_, value) => handlePageChange(value - 1)} // Convert back to 0-indexed
              color="primary"
              size={isMobile ? 'small' : 'medium'}
              sx={{
                '& .MuiPaginationItem-root': {
                  background: 'linear-gradient(135deg, rgba(30,136,229,0.1) 0%, rgba(66,165,245,0.05) 100%)',
                  backdropFilter: 'blur(20px)',
                  border: `1px solid ${alpha('#ffffff', 0.1)}`,
                  color: 'text.primary',
                  '&:hover': {
                    background: 'linear-gradient(135deg, rgba(30,136,229,0.2) 0%, rgba(66,165,245,0.1) 100%)',
                  },
                  '&.Mui-selected': {
                    background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                    color: 'white',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #1565c0 0%, #1e88e5 100%)',
                    },
                  },
                },
              }}
            />
          </Box>
        </motion.div>
      )}

      {/* No Results */}
      {!isLoading && currentTeams.length === 0 && !error && (
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
            No teams found matching the selected filters. Try adjusting your search criteria or check back later.
          </Alert>
        </motion.div>
      )}

      {/* Team Detail Modal */}
      <TeamDetailModal
        open={isDetailModalOpen}
        onClose={handleDetailModalClose}
        team={selectedTeam}
      />

      {/* Team Management Modal */}
      {isCommissioner && (
        <TeamManagementModal
          open={isManagementModalOpen}
          mode={managementMode}
          team={selectedTeam}
          conferences={conferencesData || []}
          onClose={handleManagementModalClose}
          onSave={handleTeamSave}
          onDelete={handleTeamDelete}
          loading={
            createTeamMutation.isPending || 
            updateTeamMutation.isPending || 
            deleteTeamMutation.isPending
          }
        />
      )}
    </Container>
  );
};

export default TeamsPage;